/*
 * THIS FILE IS PART OF THE OggVorbis SOFTWARE CODEC SOURCE CODE.
 * USE, DISTRIBUTION AND REPRODUCTION OF THIS LIBRARY SOURCE IS
 * GOVERNED BY A BSD-STYLE SOURCE LICENSE INCLUDED WITH THIS SOURCE
 * IN 'COPYING'. PLEASE READ THESE TERMS BEFORE DISTRIBUTING.
 *
 * THE OggVorbis SOURCE CODE IS (C) COPYRIGHT 1994-2001
 * by the XIPHOPHORUS Company http://www.xiph.org/
 *
 * function: libvorbis codec headers
 * last mod: $Id: VorbisLibrary.java,v 1.1 2007/08/28 15:48:21 kenlars99 Exp $
 */

package net.sf.theora_java.jna;

import java.util.List;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import net.sf.theora_java.jna.OggLibrary.ogg_stream_state;
import net.sf.theora_java.jna.OggLibrary.ogg_sync_state;
import net.sf.theora_java.jna.OggLibrary.oggpack_buffer;


/**
 * JNA wrapper for libvorbis.  Includes codec.h, vorbisenc.h, vorbisfile.h
 *
 * @author Ken Larson
 */
public interface VorbisLibrary extends XiphLibrary {

    VorbisLibrary INSTANCE = Native.load("vorbis", VorbisLibrary.class);

    // codec.h:

    class vorbis_info extends Structure {

        public int version;
        public int channels;
        public NativeLong /* long */ rate;

      /* The below bitrate declarations are *hints*.
         Combinations of the three values carry the following implications:

         all three set to the same value:
           implies a fixed rate bitstream
         only nominal set:
           implies a VBR stream that averages the nominal bitrate.  No hard
           upper/lower limit
         upper and or lower set:
           implies a VBR bitstream that obeys the bitrate limits. nominal
           may also be set to give a nominal rate.
         none set:
           the coder does not care to speculate.
      */

        public NativeLong /* long */ bitrate_upper;
        public NativeLong /* long */ bitrate_nominal;
        public NativeLong /* long */ bitrate_lower;
        public NativeLong /* long */ bitrate_window;

        public Pointer /*void**/ codec_setup;

        public vorbis_info() {}

        @Override
        protected List<String> getFieldOrder() {
            return List.of("version", "channels", "rate", "bitrate_upper", "bitrate_nominal", "bitrate_lower",
                    "bitrate_window", "codec_setup");
        }
    }

    /**
     * vorbis_dsp_state buffers the current vorbis audio
     * analysis/synthesis state.  The DSP state belongs to a specific
     * logical bitstream
     */
    class vorbis_dsp_state extends Structure {

        public int analysisp;
        public Pointer /* vorbis_info* */ vi;

        public Pointer /* float** */ pcm;
        public Pointer /* float** */ pcmret;
        public int pcm_storage;
        public int pcm_current;
        public int pcm_returned;

        public int preextrapolate;
        public int eofflag;

        public NativeLong /* long */ lW;
        public NativeLong /* long */ W;
        public NativeLong /* long */ nW;
        public NativeLong /* long */ centerW;

        public long /* ogg_int64_t */ granulepos;
        public long /* ogg_int64_t */ sequence;

        public long /* ogg_int64_t */ glue_bits;
        public long /* ogg_int64_t */ time_bits;
        public long /* ogg_int64_t */ floor_bits;
        public long /* ogg_int64_t */ res_bits;

        public Pointer /* void* */ backend_state;

        public vorbis_dsp_state() {}

        @Override
        protected List<String> getFieldOrder() {
            return List.of("analysisp", "vi", "pcm", "pcmret", "pcm_storage", "pcm_current", "pcm_returned",
                    "preextrapolate", "eofflag", "lW", "W", "nW", "centerW", "granulepos", "sequence", "glue_bits",
                    "time_bits", "floor_bits", "res_bits", "backend_state");
        }
    }

    /** necessary stream state for linking to the framing abstraction */
    class vorbis_block extends Structure {

        /** this is a pointer into local storage */
        public Pointer /* float** */ pcm;
        public oggpack_buffer opb;

        public NativeLong /* long */ lW;
        public NativeLong /* long */ W;
        public NativeLong /* long */ nW;
        public int pcmend;
        public int mode;

        public int eofflag;
        public long /* ogg_int64_t */ granulepos;
        public long /* ogg_int64_t */ sequence;
        /** For read-only access of configuration */
        public Pointer /* vorbis_dsp_state* */ vd;

        /**
         * local storage to avoid remallocing; it's up to the mapping to
         * structure it
         */
        public Pointer /* void* */ localstore;
        public NativeLong /* long */ localtop;
        public NativeLong /* long */ localalloc;
        public NativeLong /* long */ totaluse;
        public Pointer /*struct alloc_chain* */ reap;

        // bitmetrics for the frame
        public NativeLong /* long */ glue_bits;
        public NativeLong /* long */ time_bits;
        public NativeLong /* long */ floor_bits;
        public NativeLong /* long */ res_bits;

        public Pointer /*void**/ internal;

        public vorbis_block() {}

        @Override
        protected List<String> getFieldOrder() {
            return List.of("pcm", "opb", "lW", "W", "nW", "pcmend", "mode", "eofflag", "granulepos",
                    "sequence", "vd", "localstore", "localtop", "localalloc", "totaluse", "reap", "glue_bits",
                    "time_bits", "floor_bits", "res_bits", "internal");
        }
    }

    /**
     * vorbis_block is a single block of data to be processed as part of
     * the analysis/synthesis stream; it belongs to a specific logical
     * bitstream, but is independant from other vorbis_blocks belonging to
     * that logical bitstream.
     */
    class alloc_chain extends Structure {

        public Pointer /*void**/ ptr;
        public Pointer /*struct alloc_chain **/ next;

        public alloc_chain() {}

        @Override
        protected List<String> getFieldOrder() {
            return List.of("ptr", "next");
        }
    }

    /**
     * vorbis_info contains all the setup information specific to the
     * specific compression/decompression mode in progress (eg,
     * psychoacoustic settings, channel setup, options, codebook
     * etc). vorbis_info and substructures are in backends.h.
     * <p>
     * the comments are not part of vorbis_info so that vorbis_info can be
     * static storage
     */
    class vorbis_comment extends Structure {

        /**
         * unlimited user comment fields.  libvorbis writes 'libvorbis'
         * whatever vendor is set to in encode
         */
        public PointerByReference /* char** */ user_comments;
        public IntByReference /* int* */ comment_lengths;
        public int comments;
        public Pointer /* char* */ vendor;

        public vorbis_comment() {}

        @Override
        protected List<String> getFieldOrder() {
            return List.of("user_comments", "comment_lengths", "comments", "vendor");
        }
    }

    // libvorbis encodes in two abstraction layers; first we perform DSP
    // and produce a packet (see docs/analysis.txt).  The packet is then
    // coded into a framed OggSquish bitstream by the second layer (see
    // docs/framing.txt).  Decode is the reverse process; we sync/frame
    // the bitstream and extract individual packets, then decode the
    // packet back into PCM audio.
    //
    // The extra framing/packetizing is used in streaming formats, such as
    // files.  Over the net (such as with UDP), the framing and
    // packetization aren't necessary as they're provided by the transport
    // and the streaming layer is not used
    //
    // Vorbis PRIMITIVES: general

    void vorbis_info_init(vorbis_info vi);

    void vorbis_info_clear(vorbis_info vi);

    int vorbis_info_blocksize(vorbis_info vi, int zo);

    void vorbis_comment_init(vorbis_comment vc);

    void vorbis_comment_add(vorbis_comment vc, Pointer /* char* */ comment);

    void vorbis_comment_add_tag(vorbis_comment vc, String /* char* */ tag, String /* char* */ contents);

    Pointer /* char* */ vorbis_comment_query(vorbis_comment vc, Pointer /* char* */ tag, int count);

    int vorbis_comment_query_count(vorbis_comment vc, Pointer /* char* */ tag);

    void vorbis_comment_clear(vorbis_comment vc);

    int vorbis_block_init(vorbis_dsp_state v, vorbis_block vb);

    int vorbis_block_clear(vorbis_block vb);

    void vorbis_dsp_clear(vorbis_dsp_state v);

    double vorbis_granule_time(vorbis_dsp_state v, long /* ogg_int64_t */ granulepos);

    // Vorbis PRIMITIVES: analysis/DSP layer

    int vorbis_analysis_init(vorbis_dsp_state v, vorbis_info vi);

    int vorbis_commentheader_out(vorbis_comment vc, ogg_packet op);

    int vorbis_analysis_headerout(vorbis_dsp_state v,
                                  vorbis_comment vc,
                                  ogg_packet op,
                                  ogg_packet op_comm,
                                  ogg_packet op_code);

    Pointer /* float** */ vorbis_analysis_buffer(vorbis_dsp_state v, int vals);

    int vorbis_analysis_wrote(vorbis_dsp_state v, int vals);

    int vorbis_analysis_blockout(vorbis_dsp_state v, vorbis_block vb);

    int vorbis_analysis(vorbis_block vb, ogg_packet op);

    int vorbis_bitrate_addblock(vorbis_block vb);

    int vorbis_bitrate_flushpacket(vorbis_dsp_state vd, ogg_packet op);

    // Vorbis PRIMITIVES: synthesis layer

    int vorbis_synthesis_headerin(vorbis_info vi, vorbis_comment vc, ogg_packet op);

    int vorbis_synthesis_init(vorbis_dsp_state v, vorbis_info vi);

    int vorbis_synthesis_restart(vorbis_dsp_state v);

    int vorbis_synthesis(vorbis_block vb, ogg_packet op);

    int vorbis_synthesis_trackonly(vorbis_block vb, ogg_packet op);

    int vorbis_synthesis_blockin(vorbis_dsp_state v, vorbis_block vb);

    int vorbis_synthesis_pcmout(vorbis_dsp_state v, PointerByReference /* float*** */ pcm);

    int vorbis_synthesis_lapout(vorbis_dsp_state v, PointerByReference /* float*** */ pcm);

    int vorbis_synthesis_read(vorbis_dsp_state v, int samples);

    NativeLong /* long */ vorbis_packet_blocksize(vorbis_info vi, ogg_packet p);

    int vorbis_synthesis_halfrate(vorbis_info v, int flag);

    int vorbis_synthesis_halfrate_p(vorbis_info v);

    // Vorbis ERRORS and return codes

    int OV_FALSE = -1;
    int OV_EOF = -2;
    int OV_HOLE = -3;

    int OV_EREAD = -128;
    int OV_EFAULT = -129;
    int OV_EIMPL = -130;
    int OV_EINVAL = -131;
    int OV_ENOTVORBIS = -132;
    int OV_EBADHEADER = -133;
    int OV_EVERSION = -134;
    int OV_ENOTAUDIO = -135;
    int OV_EBADPACKET = -136;
    int OV_EBADLINK = -137;
    int OV_ENOSEEK = -138;

    // deprecated rate management supported only for compatability
    int OV_ECTL_RATEMANAGE_GET = 0x10;
    int OV_ECTL_RATEMANAGE_SET = 0x11;
    int OV_ECTL_RATEMANAGE_AVG = 0x12;
    int OV_ECTL_RATEMANAGE_HARD = 0x13;

    class ovectl_ratemanage_arg extends Structure {

        public int management_active;

        public NativeLong /* long */ bitrate_hard_min;
        public NativeLong /* long */ bitrate_hard_max;
        public double bitrate_hard_window;

        public NativeLong /* long */ bitrate_av_lo;
        public NativeLong /* long */ bitrate_av_hi;
        public double bitrate_av_window;
        public double bitrate_av_window_center;

        public ovectl_ratemanage_arg() {}

        @Override
        protected List<String> getFieldOrder() {
            return List.of("management_active", "bitrate_hard_min", "bitrate_hard_max", "bitrate_hard_window",
                    "bitrate_av_lo", "bitrate_av_hi", "bitrate_av_window", "bitrate_av_window_center");
        }
    }

    // new rate setup

    int OV_ECTL_RATEMANAGE2_GET = 0x14;
    int OV_ECTL_RATEMANAGE2_SET = 0x15;

    class ovectl_ratemanage2_arg extends Structure {

        public int management_active;

        public NativeLong /* long */ bitrate_limit_min_kbps;
        public NativeLong /* long */ bitrate_limit_max_kbps;
        public NativeLong /* long */ bitrate_limit_reservoir_bits;
        public double bitrate_limit_reservoir_bias;

        public NativeLong /* long */   bitrate_average_kbps;
        public double bitrate_average_damping;

        public ovectl_ratemanage2_arg() {}

        @Override
        protected List<String> getFieldOrder() {
            return List.of("management_active", "bitrate_limit_min_kbps", "bitrate_limit_max_kbps",
                    "bitrate_limit_reservoir_bits", "bitrate_limit_reservoir_bias", "bitrate_average_kbps",
                    "bitrate_average_damping");
        }
    }

    int OV_ECTL_LOWPASS_GET = 0x20;
    int OV_ECTL_LOWPASS_SET = 0x21;

    int OV_ECTL_IBLOCK_GET = 0x30;
    int OV_ECTL_IBLOCK_SET = 0x31;

    // vorbisfile.h:

    /*
     *                                                                  *
     * THIS FILE IS PART OF THE OggVorbis SOFTWARE CODEC SOURCE CODE.   *
     * USE, DISTRIBUTION AND REPRODUCTION OF THIS LIBRARY SOURCE IS     *
     * GOVERNED BY A BSD-STYLE SOURCE LICENSE INCLUDED WITH THIS SOURCE *
     * IN 'COPYING'. PLEASE READ THESE TERMS BEFORE DISTRIBUTING.       *
     *                                                                  *
     * THE OggVorbis SOURCE CODE IS (C) COPYRIGHT 1994-2001             *
     * by the XIPHOPHORUS Company http://www.xiph.org/                  *
     *                                                                  *
     *

     function: stdio-based convenience library for opening/seeking/decoding
     last mod: $Id: VorbisLibrary.java,v 1.1 2007/08/28 15:48:21 kenlars99 Exp $
     */

    /**
     * The function prototypes for the callbacks are basically the same as for
     * the stdio functions fread, fseek, fclose, ftell.
     * The one difference is that the FILE * arguments have been replaced with
     * a void * - this is to be used as a pointer to whatever internal data these
     * functions might need. In the stdio case, it's just a FILE * cast to a void *
     *
     * If you use other functions, check the docs for these functions and return
     * the right values. For seek_func(), you *MUST* return -1 if the stream is
     * unseekable
     */
    class ov_callbacks extends Structure {

        // TODO: use JNA callbacks:
        /** size_t (*read_func)  (void *ptr, size_t size, size_t nmemb, void *datasource); */
        public Pointer read_func;
        /** int (*seek_func)  (void *datasource, long ogg_int64_t offset, int whence); */
        public Pointer seek_func;
        /** int (*close_func) (void *datasource); */
        public Pointer close_func;
        //long   (*tell_func)  (void *datasource); */
        public Pointer tell_func;

        public ov_callbacks() {}

        @Override
        protected List<String> getFieldOrder() {
            return List.of("read_func", "seek_func", "close_func", "tell_func");
        }
    }

    int NOTOPEN = 0;
    int PARTOPEN = 1;
    int OPENED = 2;
    int STREAMSET = 3;
    int INITSET = 4;

    class OggVorbis_File extends Structure {

        // Pointer to a FILE *, etc.
        public Pointer /* void* */ datasource;
        public int seekable;
        public long /* ogg_int64_t */ offset;
        public long /* ogg_int64_t */ end;
        public ogg_sync_state oy;

        // If the FILE handle isn't seekable (eg, a pipe), only the current stream appears
        public int links;
        public Pointer /* ogg_int64_t* */ offsets;
        public Pointer /* ogg_int64_t* */ dataoffsets;
        public Pointer /* long* */ serialnos;
        /**
         * overloaded to maintain binary
         * compatability; x2 size, stores both
         * beginning and end values
         */
        public Pointer /* ogg_int64_t* */ pcmlengths;
        public Pointer /* vorbis_info* */ vi;
        public Pointer /* vorbis_comment* */ vc;

        /* Decoding working state local storage */
        public long /* ogg_int64_t */ pcm_offset;
        public int ready_state;
        public NativeLong /* long */ current_serialno;
        public int current_link;

        public double bittrack;
        public double samptrack;

        /**
         * take physical pages, weld into a logical
         * stream of packets
         */
        public ogg_stream_state os;
        /** central working state for the packet->PCM decoder */
        public vorbis_dsp_state vd;
        /** local working space for packet->PCM decode */
        public vorbis_block vb;

        public ov_callbacks callbacks;

        public OggVorbis_File() {}

        @Override
        protected List<String> getFieldOrder() {
            return List.of("datasource", "seekable", "offset", "end", "oy", "links", "offsets", "dataoffsets",
                    "serialnos", "pcmlengths", "vi", "vc", "pcm_offset", "ready_state", "current_serialno",
                    "current_link", "bittrack", "samptrack", "os", "vd", "vb", "callbacks");
        }
    }

    int ov_clear(OggVorbis_File vf);

    int ov_open(Pointer /* FILE* */ f, OggVorbis_File vf, Pointer /* char* */ initial, NativeLong /* long */ ibytes);

    int ov_open_callbacks(Pointer /* void* */ datasource, OggVorbis_File vf,
                          Pointer /* char* */ initial, NativeLong /* long */ ibytes, ov_callbacks callbacks);

    int ov_test(Pointer /* FILE* */ f, OggVorbis_File vf, Pointer /* char* */ initial, NativeLong /* long */ ibytes);

    int ov_test_callbacks(Pointer /* void* */ datasource, OggVorbis_File vf,
                          Pointer /* char* */ initial, NativeLong /* long */ ibytes, ov_callbacks callbacks);

    int ov_test_open(OggVorbis_File vf);

    NativeLong /* long */ ov_bitrate(OggVorbis_File vf, int i);

    NativeLong /* long */ ov_bitrate_instant(OggVorbis_File vf);

    NativeLong /* long */ ov_streams(OggVorbis_File vf);

    NativeLong /* long */ ov_seekable(OggVorbis_File vf);

    NativeLong /* long */ ov_serialnumber(OggVorbis_File vf, int i);

    long /* ogg_int64_t */ ov_raw_total(OggVorbis_File vf, int i);

    long /* ogg_int64_t */ ov_pcm_total(OggVorbis_File vf, int i);

    double ov_time_total(OggVorbis_File vf, int i);

    int ov_raw_seek(OggVorbis_File vf, long /* ogg_int64_t */ pos);

    int ov_pcm_seek(OggVorbis_File vf, long /* ogg_int64_t */ pos);

    int ov_pcm_seek_page(OggVorbis_File vf, long /* ogg_int64_t */ pos);

    int ov_time_seek(OggVorbis_File vf, double pos);

    int ov_time_seek_page(OggVorbis_File vf, double pos);

    int ov_raw_seek_lap(OggVorbis_File vf, long /* ogg_int64_t */ pos);

    int ov_pcm_seek_lap(OggVorbis_File vf, long /* ogg_int64_t */ pos);

    int ov_pcm_seek_page_lap(OggVorbis_File vf, long /* ogg_int64_t */ pos);

    int ov_time_seek_lap(OggVorbis_File vf, double pos);

    int ov_time_seek_page_lap(OggVorbis_File vf, double pos);

    long /* ogg_int64_t */ ov_raw_tell(OggVorbis_File vf);

    long /* ogg_int64_t */ ov_pcm_tell(OggVorbis_File vf);

    double ov_time_tell(OggVorbis_File vf);

    vorbis_info ov_info(OggVorbis_File vf, int link);

    vorbis_comment ov_comment(OggVorbis_File vf, int link);

    NativeLong /* long */ ov_read_float(OggVorbis_File vf, Pointer /* float*** */ pcm_channels, int samples,
                                      Pointer /* int* */ bitstream);

    NativeLong /* long */ ov_read(OggVorbis_File vf, Pointer /* char* */ buffer, int length,
                                int bigendianp, int word, int sgned, Pointer /* int* */ bitstream);

    int ov_crosslap(OggVorbis_File vf1, OggVorbis_File vf2);

    int ov_halfrate(OggVorbis_File vf, int flag);

    int ov_halfrate_p(OggVorbis_File vf);
}
