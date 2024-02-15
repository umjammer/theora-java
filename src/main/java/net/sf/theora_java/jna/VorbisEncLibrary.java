/********************************************************************
 *                                                                  *
 * THIS FILE IS PART OF THE OggVorbis SOFTWARE CODEC SOURCE CODE.   *
 * USE, DISTRIBUTION AND REPRODUCTION OF THIS LIBRARY SOURCE IS     *
 * GOVERNED BY A BSD-STYLE SOURCE LICENSE INCLUDED WITH THIS SOURCE *
 * IN 'COPYING'. PLEASE READ THESE TERMS BEFORE DISTRIBUTING.       *
 *                                                                  *
 * THE OggVorbis SOURCE CODE IS (C) COPYRIGHT 1994-2001             *
 * by the XIPHOPHORUS Company http://www.xiph.org/                  *
 *                                                                  *
 ********************************************************************

 function: vorbis encode-engine setup
 last mod: $Id: VorbisLibrary.java,v 1.1 2007/08/28 15:48:21 kenlars99 Exp $
 ********************************************************************/

package net.sf.theora_java.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import net.sf.theora_java.jna.VorbisLibrary.vorbis_info;


/**
 * VorbisEncLibrary.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2024-02-15 nsano initial version <br>
 */
public interface VorbisEncLibrary extends Library {

    VorbisEncLibrary INSTANCE = Native.load("vorbisenc", VorbisEncLibrary.class);

    // vorbisenc.h:

    int vorbis_encode_init(vorbis_info vi,
                           NativeLong /* long */ channels,
                           NativeLong /* long */ rate,

                           NativeLong /* long */ max_bitrate,
                           NativeLong /* long */ nominal_bitrate,
                           NativeLong /* long */ min_bitrate);

    int vorbis_encode_setup_managed(vorbis_info vi,
                                    NativeLong /* long */ channels,
                                    NativeLong /* long */ rate,

                                    NativeLong /* long */ max_bitrate,
                                    NativeLong /* long */ nominal_bitrate,
                                    NativeLong /* long */ min_bitrate);

    int vorbis_encode_setup_vbr(vorbis_info vi,
                                NativeLong /* long */ channels,
                                NativeLong /* long */ rate,
                                float quality // quality level from 0. (lo) to 1. (hi)
    );

    int vorbis_encode_init_vbr(vorbis_info vi,
                               NativeLong /* long */ channels,
                               NativeLong /* long */ rate,
                               float base_quality // quality level from 0. (lo) to 1. (hi)
    );

    int vorbis_encode_setup_init(vorbis_info vi);

    int vorbis_encode_ctl(vorbis_info vi, int number, Pointer /* void* */ arg);
}
