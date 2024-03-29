/********************************************************************
 *                                                                  *
 * THIS FILE IS PART OF THE OggTheora SOFTWARE CODEC SOURCE CODE.   *
 * USE, DISTRIBUTION AND REPRODUCTION OF THIS LIBRARY SOURCE IS     *
 * GOVERNED BY A BSD-STYLE SOURCE LICENSE INCLUDED WITH THIS SOURCE *
 * IN 'COPYING'. PLEASE READ THESE TERMS BEFORE DISTRIBUTING.       *
 *                                                                  *
 * THE Theora SOURCE CODE IS COPYRIGHT (C) 2002-2003                *
 * by the Xiph.Org Foundation http://www.xiph.org/                  *
 *                                                                  *
 ********************************************************************

 function:
 last mod: $Id: TheoraLibrary.java,v 1.1 2007/08/28 15:48:21 kenlars99 Exp $
 ********************************************************************/

package net.sf.theora_java.jna;

import java.util.List;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;


/**
 * Based on libtheora-1.0alpha7 theora.h
 *
 * @author Ken Larson
 */
public interface TheoraLibrary extends XiphLibrary {

    TheoraLibrary INSTANCE = Native.loadLibrary("theora", TheoraLibrary.class);

//	#ifndef _O_THEORA_H_
//	#define _O_THEORA_H_
//
//	#ifdef __cplusplus
//	extern "C"
//	{
//	#endif /* __cplusplus */
//
//	#ifndef LIBOGG2
//	#include <ogg/ogg.h>
//	#else
//	#include <ogg2/ogg.h>
//	/* This is temporary until libogg2 is more complete */
//	ogg_buffer_state *ogg_buffer_create(void);	// TODO: JNA
//	#endif

    /** \mainpage
     *
     * \section intro Introduction
     *
     * This is the documentation for the libtheora C API.
     * libtheora is the reference implementation for
     * <a href="http://www.theora.org/">Theora</a>, a free video codec.
     * Theora is derived from On2's VP3 codec with improved integration for
     * Ogg multimedia formats by <a href="http://www.xiph.org/">Xiph.Org</a>.
     */

    /** \file
     * The libtheora C API.
     */

    /**
     * A YUV buffer for passing uncompressed frames to and from the codec.
     * This holds a Y'CbCr frame in planar format. The CbCr planes can be
     * subsampled and have their own separate dimensions and row stride
     * offsets. Note that the strides may be negative in some
     * configurations. For theora the width and height of the largest plane
     * must be a multiple of 16. The actual meaningful picture size and
     * offset are stored in the theora_info structure; frames returned by
     * the decoder may need to be cropped for display.
     * <p>
     * All samples are 8 bits. Within each plane samples are ordered by
     * row from the top of the frame to the bottom. Within each row samples
     * are ordered from left to right.
     */
    class yuv_buffer extends Structure {

        /** Width of the Y' luminance plane */
        public int y_width;
        /** Height of the luminance plane */
        public int y_height;
        /** Offset in bytes between successive rows */
        public int y_stride;

        /** Height of the Cb and Cr chroma planes */
        public int uv_width;
        /** Width of the chroma planes */
        public int uv_height;
        /** Offset between successive chroma rows */
        public int uv_stride;
        /** Pointer to start of luminance data */
        public Pointer /* unsigned char* */ y;
        /** Pointer to start of Cb data */
        public Pointer /* unsigned char* */ u;
        /** Pointer to start of Cr data */
        public Pointer /* unsigned char* */ v;

        public yuv_buffer() {}

        @Override
        protected List<String> getFieldOrder() {
            return List.of("y_width", "y_height", "y_stride", "uv_width", "uv_height", "uv_stride", "y", "u", "v");
        }
    }

    /**
     * A Colorspace.
     */
//	typedef enum {
    int OC_CS_UNSPECIFIED = 0;
    /** The colorspace is unknown or unspecified */
    int OC_CS_ITU_REC_470M = 1;
    /** This is the best option for 'NTSC' content */
    int OC_CS_ITU_REC_470BG = 2;
    /** This is the best option for 'PAL' content */
    int OC_CS_NSPACES = 3;         /**< This marks the end of the defined colorspaces */
//	} theora_colorspace;

    /**
     * A Chroma subsampling
     * <p>
     * These enumerate the available chroma subsampling options supported
     * by the theora format. See Section 4.4 of the specification for
     * exact definitions.
     */
//	typedef enum {
    int OC_PF_420 = 0;
    /** Chroma subsampling by 2 in each direction (4:2:0) */
    int OC_PF_RSVD = 1;
    /** Reserved value */
    int OC_PF_422 = 2;
    /** Horizonatal chroma subsampling by 2 (4:2:2) */
    int OC_PF_444 = 3;    /**< No chroma subsampling at all (4:4:4) */
//	} theora_pixelformat;

    /**
     * Theora bitstream info.
     * Contains the basic playback parameters for a stream,
     * corresponds to the initial 'info' header packet.
     * <p>
     * Encoded theora frames must be a multiple of 16 is size;
     * this is what the width and height members represent. To
     * handle other sizes, a crop rectangle is specified in
     * frame_height and frame_width, offset_x and offset_y. The
     * offset and size should still be a multiple of 2 to avoid
     * chroma sampling shifts. Offset values in this structure
     * are measured from the  upper left of the image.
     * <p>
     * Frame rate, in frames per second, is stored as a rational
     * fraction. So is the aspect ratio. Note that this refers
     * to the aspect ratio of the frame pixels, not of the
     * overall frame itself.
     * <p>
     * see the example code for use of the other parameters and
     * good default settings for the encoder parameters.
     */
    class theora_info extends Structure {

        /** encoded frame width */
        public int /* ogg_uint32_t */ width;
        /** encoded frame height */
        public int /* ogg_uint32_t */ height;
        /** display frame width */
        public int /* ogg_uint32_t */ frame_width;
        /** display frame height */
        public int /* ogg_uint32_t */ frame_height;
        /** horizontal offset of the displayed frame */
        public int /* ogg_uint32_t */ offset_x;
        /** vertical offset of the displayed frame */
        public int /* ogg_uint32_t */ offset_y;
        /** frame rate numerator **/
        public int /* ogg_uint32_t */ fps_numerator;
        /** frame rate denominator **/
        public int /* ogg_uint32_t */ fps_denominator;
        /** pixel aspect ratio numerator */
        public int /* ogg_uint32_t */ aspect_numerator;
        /** pixel aspect ratio denominator */
        public int /* ogg_uint32_t */ aspect_denominator;
        /** colorspace */
        public int /*theora_colorspace*/ colorspace;
        /** nominal bitrate in bits per second */
        public int target_bitrate;
        /** Nominal quality setting, 0-63 */
        public int quality;
        /** Quick encode/decode */
        public int quick_p;

        // decode only

        public byte /* unsigned char */ version_major;
        public byte /* unsigned char */ version_minor;
        public byte /* unsigned char */ version_subminor;

        public Pointer /* void* */ codec_setup;

        // encode only

        public int dropframes_p;
        public int keyframe_auto_p;
        public int /* ogg_uint32_t */ keyframe_frequency;
        /**
         * also used for decode init to
         * get granpos shift correct
         */
        public int /* ogg_uint32_t */ keyframe_frequency_force;
        public int /* ogg_uint32_t */ keyframe_data_target_bitrate;
        public int /*ogg_int32_t*/ keyframe_auto_threshold;
        public int /* ogg_uint32_t */ keyframe_mindistance;
        public int /*ogg_int32_t*/ noise_sensitivity;
        public int /*ogg_int32_t*/ sharpness;

        /** chroma subsampling mode to expect */
        public int /*theora_pixelformat*/ pixelformat;

        public theora_info() {}

        @Override
        protected List<String> getFieldOrder() {
            return List.of("width", "height", "frame_width", "frame_height", "offset_x", "offset_y",
                    "fps_numerator", "fps_denominator", "aspect_numerator", "aspect_denominator", "colorspace",
                    "target_bitrate", "quality", "quick_p", "version_major", "version_minor", "version_subminor",
                    "codec_setup", "dropframes_p", "keyframe_auto_p", "keyframe_frequency", "keyframe_frequency_force",
                    "keyframe_data_target_bitrate", "keyframe_auto_threshold", "keyframe_mindistance",
                    "noise_sensitivity", "sharpness", "pixelformat");
        }
    }

    /**
     * Codec internal state and context.
     */
    class theora_state extends Structure {

        public theora_info i;
        public long /* ogg_int64_t */ granulepos;

        public Pointer /* void* */ internal_encode;
        public Pointer /* void* */ internal_decode;

        public theora_state() {}

        @Override
        protected List<String> getFieldOrder() {
            return List.of("i", "granulepos", "internal_encode", "internal_decode");
        }
    }

    /**
     * Comment header metadata.
     * <p>
     * This structure holds the in-stream metadata corresponding to
     * the 'comment' header packet.
     * <p>
     * Meta data is stored as a series of (tag, value) pairs, in
     * length-encoded string vectors. The first occurence of the
     * '=' character delimits the tag and value. A particular tag
     * may occur more than once. The character set encoding for
     * the strings is always utf-8, but the tag names are limited
     * to case-insensitive ascii. See the spec for details.
     * <p>
     * In filling in this structure, theora_decode_header() will
     * null-terminate the user_comment strings for safety. However,
     * the bitstream format itself treats them as 8-bit clean,
     * and so the length array should be treated as authoritative
     * for their length.
     */
    class theora_comment extends Structure {

        /** An array of comment string vectors */
        public Pointer /* char** */ user_comments;
        /** An array of corresponding string vector lengths in bytes */
        public Pointer /* int* */ comment_lengths;
        /** The total number of comment string vectors */
        public int comments;
        /** The vendor string identifying the encoder, null terminated */
        public Pointer /* char* */ vendor;

        public theora_comment() {}

        @Override
        protected List<String> getFieldOrder() {
            return List.of("user_comments", "comment_lengths", "comments", "vendor");
        }
    }

    /** General failure */
    int OC_FAULT = -1;
    /** Library encountered invalid internal data */
    int OC_EINVAL = -10;
    /** Requested action is disabled */
    int OC_DISABLED = -11;
    /** Header packet was corrupt/invalid */
    int OC_BADHEADER = -20;
    /** Packet is not a theora packet */
    int OC_NOTFORMAT = -21;
    /** Bitstream version is not handled */
    int OC_VERSION = -22;
    /** Feature or action not implemented */
    int OC_IMPL = -23;
    /** Packet is corrupt */
    int OC_BADPACKET = -24;
    /** Packet is an (ignorable) unhandled extension */
    int OC_NEWPACKET = -25;
    /** Packet is a dropped frame */
    int OC_DUPFRAME = 1;

    /**
     * Retrieve a human-readable string to identify the encoder vendor and version.
     * @return A version string.
     */
    String theora_version_string();

    /**
     * Retrieve a 32-bit version number.
     * This number is composed of a 16-bit major version, 8-bit minor version
     * and 8 bit sub-version, composed as follows:
     * <pre>
     * (VERSION_MAJOR<<16) + (VERSION_MINOR<<8) + (VERSION_SUB)
     * </pre>
     * @return The version number.
     */
    int /* ogg_uint32_t */ theora_version_number();

    /**
     * Initialize the theora encoder.
     * @param th The theora_state handle to initialize for encoding.
     * @param ti A theora_info struct filled with the desired encoding parameters.
     * @return 0 Success
     */
    int theora_encode_init(theora_state th, theora_info ti);

    /**
     * Submit a YUV buffer to the theora encoder.
     * @param t A theora_state handle previously initialized for encoding.
     * @param yuv A buffer of YUV data to encode.
     * @return OC_EINVAL Encoder is not ready, or is finished.
     *  -1 The size of the given frame differs from those previously input
     *   0 Success
     */
    int theora_encode_YUVin(theora_state t, yuv_buffer yuv);

    /**
     * Request the next packet of encoded video.
     * The encoded data is placed in a user-provided ogg_packet structure.
     * @param t A theora_state handle previously initialized for encoding.
     * @param last_p whether this is the last packet the encoder should produce.
     * @param op An ogg_packet structure to fill. libtheora will set all
     * elements of this structure, including a pointer to encoded
     * data. The memory for the encoded data is owned by libtheora.
     * @return 0 No internal storage exists OR no packet is ready
     *  -1 The encoding process has completed
     *   1 Success
     */
    int theora_encode_packetout(theora_state t, int last_p, ogg_packet op);

    /**
     * Request a packet containing the initial header.
     * A pointer to the header data is placed in a user-provided ogg_packet
     * structure.
     * @param t A theora_state handle previously initialized for encoding.
     * @param op An ogg_packet structure to fill. libtheora will set all
     * elements of this structure, including a pointer to the header
     * data. The memory for the header data is owned by libtheora.
     * @return 0 Success
     */
    int theora_encode_header(theora_state t, ogg_packet op);

    /**
     * Request a comment header packet from provided metadata.
     * A pointer to the comment data is placed in a user-provided ogg_packet
     * structure.
     * @param tc A theora_comment structure filled with the desired metadata
     * @param op An ogg_packet structure to fill. libtheora will set all
     * elements of this structure, including a pointer to the encoded
     * comment data. The memory for the comment data is owned by
     * libtheora.
     * @return 0 Success
     */
    int theora_encode_comment(theora_comment tc, ogg_packet op);

    /**
     * Request a packet containing the codebook tables for the stream.
     * A pointer to the codebook data is placed in a user-provided ogg_packet
     * structure.
     * @param t A theora_state handle previously initialized for encoding.
     * @param op An ogg_packet structure to fill. libtheora will set all
     * elements of this structure, including a pointer to the codebook
     * data. The memory for the header data is owned by libtheora.
     * @return 0 Success
     */
    int theora_encode_tables(theora_state t, ogg_packet op);

    /**
     * Decode an Ogg packet, with the expectation that the packet contains
     * an initial header, comment data or codebook tables.
     * <p>
     * @param ci A theora_info structure to fill. This must have been previously
     * initialized with theora_info_init(). If \a op contains an initial
     * header, theora_decode_header() will fill \a ci with the
     * parsed header values. If \a op contains codebook tables,
     * theora_decode_header() will parse these and attach an internal
     * representation to \a ci->codec_setup.
     * @param cc A theora_comment structure to fill. If \a op contains comment
     * data, theora_decode_header() will fill \a cc with the parsed
     * comments.
     * @param op An ogg_packet structure which you expect contains an initial
     * header, comment data or codebook tables.
     * <p>
     * @return OC_BADHEADER \a op is NULL; OR the first byte of \a op->packet
     * has the signature of an initial packet, but op is
     * not a b_o_s packet; OR this packet has the signature
     * of an initial header packet, but an initial header
     * packet has already been seen; OR this packet has the
     * signature of a comment packet, but the initial header
     * has not yet been seen; OR this packet has the signature
     * of a comment packet, but contains invalid data; OR
     * this packet has the signature of codebook tables,
     * but the initial header or comments have not yet
     * been seen; OR this packet has the signature of codebook
     * tables, but contains invalid data;
     * OR the stream being decoded has a compatible version
     * but this packet does not have the signature of a
     * theora initial header, comments, or codebook packet
     * @return OC_VERSION   The packet data of \a op is an initial header with
     *   a version which is incompatible with this version of
     *   libtheora.
     *  OC_NEWPACKET the stream being decoded has an incompatible (future)
     * version and contains an unknown signature.
     *  0            Success
     * <p>
     * \note The normal usage is that theora_decode_header() be called on the
     * first three packets of a theora logical bitstream in succession.
     */
    int theora_decode_header(theora_info ci, theora_comment cc,
                             ogg_packet op);

    /**
     * Initialize a theora_state handle for decoding.
     * @param th The theora_state handle to initialize.
     * @param c  A theora_info struct filled with the desired decoding parameters.
     * This is of course usually obtained from a previous call to
     * theora_decode_header().
     * @return 0 Success
     */
    int theora_decode_init(theora_state th, theora_info c);

    /**
     * Input a packet containing encoded data into the theora decoder.
     * @param th A theora_state handle previously initialized for decoding.
     * @param op An ogg_packet containing encoded theora data.
     * @return 0 Success
     * @return OC_BADPACKET \a op does not contain encoded video data
     */
    int theora_decode_packetin(theora_state th, ogg_packet op);

    /**
     * Output the next available frame of decoded YUV data.
     * @param th A theora_state handle previously initialized for decoding.
     * @param yuv A yuv_buffer in which libtheora should place the decoded data.
     * @return 0 Success
     */
    int theora_decode_YUVout(theora_state th, yuv_buffer yuv);

    /**
     * Report whether a theora packet is a header or not
     * This function does no verification beyond checking the header
     * flag bit so it should not be used for bitstream identification;
     * use theora_decode_header() for that.
     * <p>
     * @param op An ogg_packet containing encoded theora data.
     * @return 1 The packet is a header packet
     *  0 The packet is not a header packet (and so contains frame data)
     * <p>
     * Thus function was added in the 1.0alpha4 release.
     */
    int theora_packet_isheader(ogg_packet op);

    /**
     * Report whether a theora packet is a keyframe or not
     * <p>
     * @param op An ogg_packet containing encoded theora data.
     * @return 1 The packet contains a keyframe image
     *  0 The packet is contains an interframe delta
     *  -1 The packet is not an image data packet at all
     * <p>
     * Thus function was added in the 1.0alpha4 release.
     */
    int theora_packet_iskeyframe(ogg_packet op);

    /**
     * Report the granulepos shift radix
     * <p>
     * When embedded in Ogg, Theora uses a two-part granulepos,
     * splitting the 64-bit field into two pieces. The more-significant
     * section represents the frame count at the last keyframe,
     * and the less-significant section represents the count of
     * frames since the last keyframe. In this way the overall
     * field is still non-decreasing with time, but usefully encodes
     * a pointer to the last keyframe, which is necessary for
     * correctly restarting decode after a seek.
     * <p>
     * This function reports the number of bits used to represent
     * the distance to the last keyframe, and thus how the granulepos
     * field must be shifted or masked to obtain the two parts.
     * <p>
     * Since libtheora returns compressed data in an ogg_packet
     * structure, this may be generally useful even if the Theora
     * packets are not being used in an Ogg container.
     * <p>
     * @param ti A previously initialized theora_info struct
     * @return The bit shift dividing the two granulepos fields
     * <p>
     * This function was added in the 1.0alpha5 release.
     */
    int theora_granule_shift(theora_info ti);

    /**
     * Convert a granulepos to an absolute frame number. The granulepos is
     * interpreted in the context of a given theora_state handle.
     * <p>
     * @param th A previously initialized theora_state handle (encode or decode)
     * @param granulepos The granulepos to convert.
     * @return The frame number corresponding to \a granulepos.
     *  -1 The given granulepos is undefined (i.e. negative)
     * <p>
     * Thus function was added in the 1.0alpha4 release.
     */
    long /* ogg_int64_t */ theora_granule_frame(theora_state th, long /* ogg_int64_t */ granulepos);

    /**
     * Convert a granulepos to absolute time in seconds. The granulepos is
     * interpreted in the context of a given theora_state handle.
     * @param th A previously initialized theora_state handle (encode or decode)
     * @param granulepos The granulepos to convert.
     * @return The absolute time in seconds corresponding to \a granulepos.
     *  -1. The given granulepos is undefined (i.e. negative), or
     *  -1. The function has been disabled because floating
     * point support is not available.
     */
    double theora_granule_time(theora_state th, long /* ogg_int64_t */ granulepos);

    /**
     * Initialize a theora_info structure. All values within the given theora_info
     * structure are initialized, and space is allocated within libtheora for
     * internal codec setup data.
     * @param c A theora_info struct to initialize.
     */
    void theora_info_init(theora_info c);

    /**
     * Clear a theora_info structure. All values within the given theora_info
     * structure are cleared, and associated internal codec setup data is freed.
     * @param c A theora_info struct to initialize.
     */
    void theora_info_clear(theora_info c);

    /**
     * Free all internal data associated with a theora_state handle.
     * @param t A theora_state handle.
     */
    void theora_clear(theora_state t);

    /**
     * Initialize an allocated theora_comment structure
     * @param tc An allocated theora_comment structure
     **/
    void theora_comment_init(theora_comment tc);

    /**
     * Add a comment to an initialized theora_comment structure
     * @param tc A previously initialized theora comment structure
     * @param comment A null-terminated string encoding the comment in the form
     * "TAG=the value"
     * <p>
     * Neither theora_comment_add() nor theora_comment_add_tag() support
     * comments containing null values, although the bitstream format
     * supports this. To add such comments you will need to manipulate
     * the theora_comment structure directly.
     */
    void theora_comment_add(theora_comment tc, Pointer /* char* */ comment);

    /**
     * Add a comment to an initialized theora_comment structure.
     * @param tc A previously initialized theora comment structure
     * @param tag A null-terminated string containing the tag
     * associated with the comment.
     * @param value The corresponding value as a null-terminated string
     * <p>
     * Neither theora_comment_add() nor theora_comment_add_tag() support
     * comments containing null values, although the bitstream format
     * supports this. To add such comments you will need to manipulate
     * the theora_comment structure directly.
     **/
    void theora_comment_add_tag(theora_comment tc, Pointer /* char* */ tag, Pointer /* char* */ value);

    /**
     * Look up a comment value by tag.
     * @param tc Tn initialized theora_comment structure
     * @param tag The tag to look up
     * @param count The instance of the tag. The same tag can appear multiple
     * times, each with a distinct and ordered value, so an index
     * is required to retrieve them all.
     * @return A pointer to the queried tag's value
     *  NULL No matching tag is found
     * <p>
     * \note Use theora_comment_query_count() to get the legal range for the
     * count parameter.
     */
    Pointer /* char* */theora_comment_query(theora_comment tc, Pointer /* char* */ tag, int count);

    /**
     * Look up the number of instances of a tag.
     * @param tc An initialized theora_comment structure
     * @param tag The tag to look up
     * @return The number on instances of a particular tag.
     * <p>
     * Call this first when querying for a specific tag and then interate
     * over the number of instances with separate calls to
     * theora_comment_query() to retrieve all instances in order.
     */
    int theora_comment_query_count(theora_comment tc, Pointer /* char* */ tag);

    /**
     * Clear an allocated theora_comment struct so that it can be freed.
     * @param tc An allocated theora_comment structure.
     */
    void theora_comment_clear(theora_comment tc);
}
