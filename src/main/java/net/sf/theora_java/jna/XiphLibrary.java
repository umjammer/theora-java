package net.sf.theora_java.jna;

import java.util.List;

import com.sun.jna.Library;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;


/**
 * Contains structures from ogg.h referenced in theora.h
 *
 * @author Ken Larson
 */
public interface XiphLibrary extends Library {

    /**
     * ogg_packet is used to encapsulate the data and metadata belonging
     * to a single raw Ogg/Vorbis packet
     */
    class ogg_packet extends Structure {

        public Pointer /* unsigned char* */ packet;
        public NativeLong /* long */ bytes;
        public NativeLong /* long */ b_o_s;
        public NativeLong /* long */ e_o_s;

        public long /* ogg_int64_t */ granulepos;
        /**
         * sequence number for decode; the framing
         * knows where there's a hole in the data,
         * but we need coupling so that the codec
         * (which is in a seperate abstraction
         * layer) also knows about the gap
         */
        public long /* ogg_int64_t */ packetno;

        public ogg_packet() {}

        @Override
        protected List<String> getFieldOrder() {
            return List.of("packet", "bytes", "b_o_s", "e_o_s", "granulepos", "packetno");
        }
    }
}
