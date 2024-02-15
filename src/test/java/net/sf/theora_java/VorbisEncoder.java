package net.sf.theora_java;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import net.sf.theora_java.jna.OggLibrary;
import net.sf.theora_java.jna.OggLibrary.ogg_page;
import net.sf.theora_java.jna.OggLibrary.ogg_stream_state;
import net.sf.theora_java.jna.VorbisEncLibrary;
import net.sf.theora_java.jna.VorbisLibrary;
import net.sf.theora_java.jna.VorbisLibrary.vorbis_block;
import net.sf.theora_java.jna.VorbisLibrary.vorbis_comment;
import net.sf.theora_java.jna.VorbisLibrary.vorbis_dsp_state;
import net.sf.theora_java.jna.VorbisLibrary.vorbis_info;
import net.sf.theora_java.jna.XiphLibrary.ogg_packet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;
import vavix.util.Checksum;

import static org.junit.jupiter.api.Assertions.assertEquals;


/*
 * VorbisEncoder.java
 */
@PropsEntity(url = "file:local.properties")
public class VorbisEncoder {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property(name = "wav")
    String wav = "src/test/resources/test.wav";

    @Property(name = "ogg")
    String ogg = "src/test/resources/test.ogg";

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
    }

    @Test
    void test1() throws Exception {
        Path out = Path.of("tmp", "out.ogg");
        if (!Files.exists(out.getParent())) {
            Files.createDirectories(out.getParent());
        }

        encode(Path.of(wav), out);

        assertEquals(Checksum.getChecksum(out), Checksum.getChecksum(Paths.get(ogg)));
    }

    // ----

    private static final int READ = 1024;

    /** out of the data segment, not the stack */
    private static byte[] readbuffer = new byte[READ * 4 + 44];

    void encode(Path wav, Path ogg) throws Exception {
        // take physical pages, weld into a logical stream of packets
        ogg_stream_state os = new ogg_stream_state();
        // one Ogg bitstream page. Vorbis packets are inside
        ogg_page og = new ogg_page();
        // one raw packet of data for decode
        ogg_packet op = new ogg_packet();
        // struct that stores all the static vorbis bitstream settings
        vorbis_info vi = new vorbis_info();
        // struct that stores all the user comments
        vorbis_comment vc = new vorbis_comment();

        // central working state for the packet->PCM decoder
        vorbis_dsp_state vd = new vorbis_dsp_state();
        // local working space for packet->PCM decode
        vorbis_block vb = new vorbis_block();

        boolean eos = false;

        // we cheat on the WAV header; we just bypass 44 bytes and never
        // verify that it matches 16bit/stereo/44.1kHz.  This is just an
        // example, after all.

        AudioInputStream ais = null;
        try {
            ais = AudioSystem.getAudioInputStream(new BufferedInputStream(Files.newInputStream(wav)));
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
        AudioFormat format = ais.getFormat();
        if (format.getChannels() != 2 || format.getSampleSizeInBits() != 16) {
            throw new IllegalArgumentException("need 16 bit stereo!");
        }
        OutputStream output = new BufferedOutputStream(Files.newOutputStream(ogg));

        // Encode setup

        // choose an encoding mode
        // (quality mode .4: 44kHz stereo coupled, roughly 128kbps VBR)
        VorbisLibrary.INSTANCE.vorbis_info_init(vi);

        VorbisEncLibrary.INSTANCE.vorbis_encode_init_vbr(vi,
                new NativeLong(format.getChannels()),
                new NativeLong((int) format.getSampleRate()),
                0.1F); // max compression

        // add a comment
        VorbisLibrary.INSTANCE.vorbis_comment_init(vc);
        VorbisLibrary.INSTANCE.vorbis_comment_add_tag(vc, "ENCODER", "theora-java");

        // set up the analysis state and auxiliary encoding storage
        VorbisLibrary.INSTANCE.vorbis_analysis_init(vd, vi);
        VorbisLibrary.INSTANCE.vorbis_block_init(vd, vb);

        // set up our packet->stream encoder
		// pick a random serial number; that way we can more likely build
		// chained streams just by concatenation
        Random random = new Random(314159265358979L); // fixed seed for test
        OggLibrary.INSTANCE.ogg_stream_init(os, random.nextInt());

        // Vorbis streams begin with three headers; the initial header (with
        // most of the codec setup parameters) which is mandated by the Ogg
        // bitstream spec.  The second header holds any comment fields.  The
        // third header holds the bitstream codebook.  We merely need to
        // make the headers, then pass them to libvorbis one at a time;
        // libvorbis handles the additional Ogg bitstream constraints

        ogg_packet header = new ogg_packet();
        ogg_packet header_comm = new ogg_packet();
        ogg_packet header_code = new ogg_packet();

        VorbisLibrary.INSTANCE.vorbis_analysis_headerout(vd, vc, header, header_comm, header_code);
        OggLibrary.INSTANCE.ogg_stream_packetin(os, header); // automatically placed in its own page
        OggLibrary.INSTANCE.ogg_stream_packetin(os, header_comm);
        OggLibrary.INSTANCE.ogg_stream_packetin(os, header_code);

        // We don't have to write out here, but doing so makes streaming
        // much easier, so we do, flushing ALL pages. This ensures the actual
        // audio data will start on a new page
        while (!eos) {
            int result = OggLibrary.INSTANCE.ogg_stream_flush(os, og);
            if (result == 0)
                break;
            output.write(og.header.getByteArray(0, og.header_len.intValue()));
            output.write(og.body.getByteArray(0, og.body_len.intValue()));
        }


        while (!eos) {
            int bytes = ais.read(readbuffer, 0, READ * 4); // stereo hardwired here

            if (bytes == 0 || bytes == -1) {
                // end of file.  this can be done implicitly in the mainline,
                // but it's easier to see here in non-clever fashion.
                // Tell the library we're at end of stream so that it can handle
                // the last frame and mark end of stream in the output properly
                VorbisLibrary.INSTANCE.vorbis_analysis_buffer(vd, 0);
                VorbisLibrary.INSTANCE.vorbis_analysis_wrote(vd, 0);

            } else {
                // data to encode

                // expose the buffer to submit data
                float[][] buffer = new float[format.getChannels()][READ];
                //float[][] buffer = vd.buffer(READ);

                // uninterleave samples
                for (int i = 0; i < bytes / 4; i++) {
                    int nSample = (readbuffer[i * 4 + 1] << 8) | (0x00ff & readbuffer[i * 4 + 0]);
                    float fSample = nSample / 32768.0F;
                    buffer[0][i] = fSample;
                    nSample = (readbuffer[i * 4 + 3] << 8) | (0x00ff & readbuffer[i * 4 + 2]);
                    fSample = nSample / 32768.f;
                    buffer[1][i] = fSample;
                }

                // tell the library how much we actually submitted
                Pointer bufferPointer = VorbisLibrary.INSTANCE.vorbis_analysis_buffer(vd, bytes / 4);
                long bufferPointerP = 0;
                for (float[] floatArray : buffer) {
                    bufferPointer.write(bufferPointerP, floatArray, 0, bytes / 4);
                    bufferPointerP += bytes / 4;
                }
                VorbisLibrary.INSTANCE.vorbis_analysis_wrote(vd, bytes / 4);
            }

			// vorbis does some data preanalysis, then divvies up blocks for
            // more involved (potentially parallel) processing.  Get a single
            // block for encoding now
            while (VorbisLibrary.INSTANCE.vorbis_analysis_blockout(vd, vb) == 1) {

                // analysis, assume we want to use bitrate management
                VorbisLibrary.INSTANCE.vorbis_analysis(vb, null);
                VorbisLibrary.INSTANCE.vorbis_bitrate_addblock(vb);

                while (VorbisLibrary.INSTANCE.vorbis_bitrate_flushpacket(vd, op) != 0) {
                    // weld the packet into the bitstream
                    OggLibrary.INSTANCE.ogg_stream_packetin(os, op);

                    // write out pages (if any)
                    while (!eos) {
                        int result = OggLibrary.INSTANCE.ogg_stream_pageout(os, og);
                        if (result == 0)
                            break;
                        output.write(og.header.getByteArray(0, og.header_len.intValue()));
                        output.write(og.body.getByteArray(0, og.body_len.intValue()));

						// this could be set above, but for illustrative purposes, I do
						// it here (to show that vorbis does know where the stream ends)

                        if (OggLibrary.INSTANCE.ogg_page_eos(og) != 0) {
                            eos = true;
                        }
                    }
                }
            }
        }

        // clean up and exit.  vorbis_info_clear() must be called last
        os.clear();
        vb.clear();
        vd.clear();
        vc.clear();
        vi.clear();

        output.flush();
        output.close();

		// ogg_page and ogg_packet structs always point to storage in
		// libvorbis. They're never freed or manipulated directly
        Debug.println("Done.");
    }

    /**
     * @param args 0: , 1:
     */
    public static void main(String[] args) throws Exception {
        Path in = Path.of(args[0]);
        Path out = Path.of(args[1]);
        if (!Files.exists(out.getParent())) {
            Files.createDirectories(out.getParent());
        }
        VorbisEncoder app = new VorbisEncoder();
        app.encode(in, out);
    }
}
