/*
 * Copyright (c) 2024 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.util.Arrays;

import com.sun.jna.Structure;
import net.sf.theora_java.jna.OggLibrary.ogg_page;
import net.sf.theora_java.jna.OggLibrary.ogg_stream_state;
import net.sf.theora_java.jna.OggLibrary.ogg_sync_state;
import net.sf.theora_java.jna.OggLibrary.oggpack_buffer;
import net.sf.theora_java.jna.TheoraLibrary.theora_comment;
import net.sf.theora_java.jna.TheoraLibrary.theora_info;
import net.sf.theora_java.jna.TheoraLibrary.theora_state;
import net.sf.theora_java.jna.TheoraLibrary.yuv_buffer;
import net.sf.theora_java.jna.VorbisLibrary.OggVorbis_File;
import net.sf.theora_java.jna.VorbisLibrary.alloc_chain;
import net.sf.theora_java.jna.VorbisLibrary.ov_callbacks;
import net.sf.theora_java.jna.VorbisLibrary.ovectl_ratemanage2_arg;
import net.sf.theora_java.jna.VorbisLibrary.ovectl_ratemanage_arg;
import net.sf.theora_java.jna.VorbisLibrary.vorbis_block;
import net.sf.theora_java.jna.VorbisLibrary.vorbis_comment;
import net.sf.theora_java.jna.VorbisLibrary.vorbis_dsp_state;
import net.sf.theora_java.jna.VorbisLibrary.vorbis_info;
import net.sf.theora_java.jna.XiphLibrary.ogg_packet;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


/**
 * Test1.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2024-02-14 nsano initial version <br>
 */
public class Test1 {

    @Test
    @Disabled("jna method maker")
    void fields() throws Exception {
        Class<?>[] classes = new Class[] {
                oggpack_buffer.class,
                ogg_page.class,
                ogg_stream_state.class,
                ogg_sync_state.class,
                ogg_packet.class,
                yuv_buffer.class,
                theora_info.class,
                theora_state.class,
                theora_comment.class,
                vorbis_info.class,
                vorbis_dsp_state.class,
                vorbis_block.class,
                alloc_chain.class,
                vorbis_comment.class,
                ovectl_ratemanage_arg.class,
                ovectl_ratemanage2_arg.class,
                ov_callbacks.class,
                OggVorbis_File.class,
        };
        for (Class<?> c : classes) {
            System.out.println(c.getName());
            Arrays.stream(c.getDeclaredFields()).forEach(f -> {
                System.out.print("\"" + f.getName() + "\", ");
            });
            System.out.println();
        }
    }
}
