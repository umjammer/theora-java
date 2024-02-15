package net.sf.theora_java;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import net.sf.theora_java.jna.OggLibrary;
import net.sf.theora_java.jna.OggLibrary.ogg_page;
import net.sf.theora_java.jna.OggLibrary.ogg_stream_state;
import net.sf.theora_java.jna.OggLibrary.ogg_sync_state;
import net.sf.theora_java.jna.VorbisLibrary;
import net.sf.theora_java.jna.VorbisLibrary.vorbis_block;
import net.sf.theora_java.jna.VorbisLibrary.vorbis_comment;
import net.sf.theora_java.jna.VorbisLibrary.vorbis_dsp_state;
import net.sf.theora_java.jna.VorbisLibrary.vorbis_info;
import net.sf.theora_java.jna.XiphLibrary.ogg_packet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vavi.sound.SoundUtil;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;
import vavix.util.Checksum;

import static org.junit.jupiter.api.Assertions.assertEquals;


/*
 * VorbisDecoder.java
 */
@PropsEntity(url = "file:local.properties")
public class VorbisDecoder {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    static final double volume = Double.parseDouble(System.getProperty("vavi.test.volume",  "0.2"));

    @Property(name = "ogg")
    String ogg = "src/test/resources/test.ogg";

    @Property(name = "wav")
    String wav = "src/test/resources/test.wav";

    @Property(name = "play.ogg")
    String playOgg = "src/test/resources/test.ogg";

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
    }

    @Test
    void test1() throws Exception {
        Path out = Path.of("tmp", "out.wav");
        if (!Files.exists(out.getParent())) {
            Files.createDirectories(out.getParent());
        }

        decode(Path.of(ogg), out);

Debug.println(Checksum.getChecksum(out) + ", " + Checksum.getChecksum(Paths.get(wav)));
        assertEquals(Checksum.getChecksum(out), Checksum.getChecksum(Paths.get(wav)));
    }

    // TODO noise at first
    @Test
    void test2() throws Exception {
        Path out = Path.of("tmp", "out2.wav");
        if (!Files.exists(out.getParent())) {
            Files.createDirectories(out.getParent());
        }

        decode(Path.of(playOgg), out);

        AudioFormat audioFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                44100,
                16,
                2,
                4,
                44100,
                false);
        AudioInputStream ais = new AudioInputStream(Files.newInputStream(out), audioFormat, Files.size(out));

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat, AudioSystem.NOT_SPECIFIED);
        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
        line.addLineListener(event -> Debug.println(event.getType()));

        byte[] buf = new byte[8192];
        line.open(audioFormat, buf.length);
        SoundUtil.volume(line, volume);
        line.start();
        int r;
        while (true) {
            r = ais.read(buf, 0, buf.length);
            if (r < 0) {
                break;
            }
            line.write(buf, 0, r);
        }
        line.drain();
        line.close();
    }

    // ----

    private static int convsize = 4096;

    void decode(Path ogg, Path wav) throws IOException {
        // sync and verify incoming physical bitstream
        ogg_sync_state oy;
        // take physical pages, weld into a logical stream of packets
        ogg_stream_state os;
        // one Ogg bitstream page.  Vorbis packets are inside
        ogg_page og;
        // one raw packet of data for decode
        ogg_packet op;

        // struct that stores all the static vorbis bitstream settings
        vorbis_info vi;
        // struct that stores all the bitstream user comments
        vorbis_comment vc;
        // central working state for the packet->PCM decoder
        vorbis_dsp_state vd;
        // local working space for packet->PCM decode
        vorbis_block vb;

        oy = new ogg_sync_state();
        os = new ogg_stream_state();
        og = new ogg_page();
        op = new ogg_packet();

        vi = new vorbis_info();
        vc = new vorbis_comment();
        vd = new vorbis_dsp_state();
        vb = new vorbis_block();

        int[] convBuffer = new int[convsize];
        byte[] buffer;
        int bytes;

Debug.println("inputFile: " + ogg);
        InputStream inputStream = new BufferedInputStream(Files.newInputStream(ogg));
        OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(wav));

        buffer = new byte[4096];

        // Decode setup

        OggLibrary.INSTANCE.ogg_sync_init(oy); // Now we can read pages

        while (true) { // we repeat if the bitstream is chained
            boolean eos = false;

            // grab some data at the head of the stream.  We want the first page
            // (which is guaranteed to be small and only contain the Vorbis
            // stream initial header) We need the first page to get the stream
            // serialno.

            // submit a 4k block to libvorbis' Ogg layer
            bytes = inputStream.read(buffer);
            if (bytes > 0) {
                Pointer p = OggLibrary.INSTANCE.ogg_sync_buffer(oy, new NativeLong(bytes));
                p.write(0, buffer, 0, bytes);
                OggLibrary.INSTANCE.ogg_sync_wrote(oy, new NativeLong(bytes));
            } else if (bytes == -1) {
Debug.println("inputStream: EOF");
                break;
            }

            // Get the first page.
            int r = OggLibrary.INSTANCE.ogg_sync_pageout(oy, og);
Debug.println("pageOut: " + r);
            if (r != 1) {
                // have we simply run out of data?  If so, we're done.
                if (bytes < 4096) {
                    break;
                }

                // error case.  Must not be Vorbis data
                throw new IllegalStateException("Input does not appear to be an Ogg bitstream.");
            }

            // Get the serial number and set up the rest of decode.
            // serialno first; use it to set up a logical stream
            OggLibrary.INSTANCE.ogg_stream_init(os, OggLibrary.INSTANCE.ogg_page_serialno(og));

            // extract the initial header from the first page and verify that the
            // Ogg bitstream is in fact Vorbis data

            // I handle the initial header first instead of just having the code
            // read all three Vorbis headers at once because reading the initial
            // header is an easy way to identify a Vorbis bitstream and it's
            // useful to see that functionality separated out.

            VorbisLibrary.INSTANCE.vorbis_info_init(vi);
            VorbisLibrary.INSTANCE.vorbis_comment_init(vc);
            if (OggLibrary.INSTANCE.ogg_stream_pagein(os, og) < 0) {
                // error; stream version mismatch perhaps
                throw new IllegalStateException("Error reading first page of Ogg bitstream data.");
            }

            if (OggLibrary.INSTANCE.ogg_stream_packetout(os, op) != 1) {
                // no page? must not be vorbis
                throw new IllegalStateException("Error reading initial header packet.");
            }

            if (VorbisLibrary.INSTANCE.vorbis_synthesis_headerin(vi, vc, op) < 0) {
                // error case; not a vorbis header
                throw new IllegalStateException("This Ogg bitstream does not contain Vorbis audio data.");
            }

			// At this point, we're sure we're Vorbis.  We've set up the logical
			// (Ogg) bitstream decoder.  Get the comment and codebook headers and
			// set up the Vorbis decoder

            // The next two packets in order are the comment and codebook headers.
            // They're likely large and may span multiple pages.  Thus we read
            // and submit data until we get our two packets, watching that no
            // pages are missing.  If a page is missing, error out; losing a
            // header page is the only place where missing data is fatal.

            int i = 0;
            while (i < 2) {
                while (i < 2) {
                    int result = OggLibrary.INSTANCE.ogg_sync_pageout(oy, og);
                    if (result == 0) {
                        break; // Need more data
                    }
					// Don't complain about missing or corrupt data yet.  We'll
					// catch it at the packet output phase
                    if (result == 1) {
                        // we can ignore any errors here as they'll also become apparent
                        // at packetout
                        OggLibrary.INSTANCE.ogg_stream_pagein(os, og);
                        while (i < 2) {
                            result = OggLibrary.INSTANCE.ogg_stream_packetout(os, op);
                            if (result == 0) {
                                break;
                            }
                            if (result < 0) {
								// Uh oh; data at some point was corrupted or missing!
                                // We can't tolerate that in a header. Die.
                                throw new IllegalStateException("Corrupt secondary header. Exiting.");
                            }
                            VorbisLibrary.INSTANCE.vorbis_synthesis_headerin(vi, vc, op);
                            i++;
                        }
                    }
                }
                // no harm in not checking before adding more
                bytes = inputStream.read(buffer);
                if (bytes == 0 && i < 2) {
                    throw new IllegalStateException("End of file before finding all Vorbis headers!");
                }
                Pointer p = OggLibrary.INSTANCE.ogg_sync_buffer(oy, new NativeLong(bytes));
                p.write(0, buffer, 0, bytes);
                OggLibrary.INSTANCE.ogg_sync_wrote(oy, new NativeLong(bytes));
            }

			// Throw the comments plus a few lines about the bitstream we're decoding
            {
                String[] astrComments = new String[vc.comments];
Debug.print("comments: " + astrComments.length);
                for (i = 0; i < astrComments.length; i++) {
                    astrComments[i] = vc.user_comments.getValue().getString((long) i * Native.POINTER_SIZE);
                    Debug.println(astrComments[i]);
                }
                Debug.print("\nBitstream is " + vi.channels + " channel, " + vi.rate + " Hz\n" +
                        "Encoded by: " + vc.vendor.getString(0));
            }

            int nChannels = vi.channels;
            convsize = 4096 / nChannels;

			// OK, got and parsed all three headers. Initialize the Vorbis
            // packet->PCM decoder.
            VorbisLibrary.INSTANCE.vorbis_synthesis_init(vd, vi); // central decode state TODO
            // local state for most of the decode so multiple block decodes can
            // proceed in parallel. We could init multiple vorbis_block structures
            // for vd here
            VorbisLibrary.INSTANCE.vorbis_block_init(vd, vb);
            // The rest is just a straight decode loop until end of stream
            while (!eos) {
                while (!eos) {
                    int result = OggLibrary.INSTANCE.ogg_sync_pageout(oy, og);
                    if (result == 0) {
                        break; // need more data
                    }
                    if (result < 0) { // missing or corrupt data at this page position
                        Debug.print("Corrupt or missing data in bitstream; continuing...\n");
                    } else {
                        OggLibrary.INSTANCE.ogg_stream_pagein(os, og); // can safely ignore errors at this point
                        while (true) {
                            result = OggLibrary.INSTANCE.ogg_stream_packetout(os, op);

                            if (result == 0) {
                                break; // need more data
                            }
                            if (result < 0) {
                                // missing or corrupt data at this page position
                                // no reason to complain; already complained above
                            } else {
                                // we have a packet. Decode it
                                float[][] pcm = new float[nChannels][];
                                int samples;

                                if (VorbisLibrary.INSTANCE.vorbis_synthesis(vb, op) == 0) { // test for success!
                                    VorbisLibrary.INSTANCE.vorbis_synthesis_blockin(vd, vb);
                                }

                                // pcm[] is a multichannel float vector.  In stereo, for
                                // example, pcm[0] is left, and pcm[1] is right.  samples is
                                // the size of each channel. Convert the float values
                                // (-1.<=range<=1.) to whatever PCM format and write it out

                                PointerByReference pp = new PointerByReference();
                                while ((samples = VorbisLibrary.INSTANCE.vorbis_synthesis_pcmout(vd, pp)) > 0) {
                                    Pointer ppChannels = pp.getValue();
                                    Pointer[] pChannels = ppChannels.getPointerArray(0, vi.channels);

                                    for (int k = 0; k < pChannels.length; ++k) {
                                        pcm[k] = pChannels[k].getFloatArray(0, samples);
                                    }

                                    int j;
                                    boolean clipflag = false;
                                    int bout = Math.min(samples, convsize);

									// convert floats to 16 bit signed ints (host order) and interleave
                                    for (i = 0; i < nChannels; i++) {
                                        int ptr = i;
                                        //float *mono = pcm[i];
                                        for (j = 0; j < bout; j++) {
                                            int val = Math.round(pcm[i][j] * 32767.0F);
                                            // might as well guard against clipping
                                            if (val > 32767) {
                                                val = 32767;
                                                clipflag = true;
                                            }
                                            if (val < -32768) {
                                                val = -32768;
                                                clipflag = true;
                                            }
                                            convBuffer[ptr] = val;
                                            ptr += nChannels;
                                        }
                                    }

                                    if (clipflag) {
                                        Debug.print("Clipping in frame " + vd.sequence + "\n");
                                    }
                                    byte[] abBuffer = new byte[2 * nChannels * bout];
                                    int byteOffset = 0;
                                    boolean bigEndian = false;
                                    for (int nSample = 0; nSample < nChannels * bout; nSample++) {
                                        int sample = convBuffer[nSample];
                                        if (bigEndian) {
                                            abBuffer[byteOffset++] = (byte) (sample >> 8);
                                            abBuffer[byteOffset++] = (byte) (sample & 0xFF);
                                        } else {
                                            abBuffer[byteOffset++] = (byte) (sample & 0xFF);
                                            abBuffer[byteOffset++] = (byte) (sample >> 8);
                                        }
                                    }
                                    outputStream.write(abBuffer);

                                    // tell libvorbis how many samples we actually consumed
                                    VorbisLibrary.INSTANCE.vorbis_synthesis_read(vd, bout);
                                }
                            }
                        }
                        if (OggLibrary.INSTANCE.ogg_page_eos(og) != 0) {
                            eos = true;
                        }
                    }
                }
                if (!eos) {
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {
                        Pointer p = OggLibrary.INSTANCE.ogg_sync_buffer(oy, new NativeLong(bytes));
                        p.write(0, buffer, 0, bytes);
                        OggLibrary.INSTANCE.ogg_sync_wrote(oy, new NativeLong(bytes));
                    }
                    if (bytes == 0 || bytes == -1) {
                        eos = true;
                    }
                }
            }

			// clean up this logical bitstream; before exit we see if we're
            // followed by another [chained]

            os.clear();

			// ogg_page and ogg_packet structs always point to storage in
            // libvorbis. They're never freed or manipulated directly
            vb.clear();
            vd.clear();
            vc.clear();
            vi.clear(); // must be called last
        }

        // OK, clean up the framer
        oy.clear();

        outputStream.flush();
        outputStream.close();

        Debug.print("Done.\n");
    }

    public static void main(String[] args) throws IOException {
        Path wav = Path.of(args[0]);
        Path ogg = Path.of(args[1]);
        if (!Files.exists(ogg.getParent())) {
            Files.createDirectories(ogg.getParent());
        }
        VorbisDecoder app = new VorbisDecoder();
        app.decode(wav, ogg);
    }
}
