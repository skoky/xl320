package com.skoky.dynamixel.raw;

import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by skoky on 9.5.15.
 */
public class PacketV2 extends PacketCommon implements  Packet {


    @Override
    public byte[] buildPing() {
        return buildPing(0xFE);
    }

    @Override
    public byte[] buildPing(int servoId) {
        int[] buffer = new int[10];
        buffer[0] = 0xFF;
        buffer[1] = 0xFF;
        buffer[2] = 0xFD;
        buffer[3] = 0;
        buffer[4] = servoId;   // 0xFE = broadcast all
        buffer[5] = 3;    // length L
        buffer[6] = 0;    // length H
        buffer[7] = 0x01;    // PING
        int crc = crc16(buffer,buffer.length-2);
        buffer[8]=(byte) crc;           // CRC L
        buffer[9]=(byte)(crc>>8);       // CRC H
        return toByteArray(buffer);
    }

    @Override
    public byte[] buildWriteData(int servoId, int... params) {
        int[] buffer = new int[10+params.length];
        buffer[0] = 0xFF;
        buffer[1] = 0xFF;
        buffer[2] = 0xFD;
        buffer[3] = 0;
        buffer[4] = servoId;
        buffer[5] = 3+params.length;    // length L
        buffer[6] = 0;    // length H
        buffer[7] = 0x03;    // WRITE
        for(int i=0;i<params.length;i++) {
            buffer[8+i] = params[i];
        }
        int crc = crc16(buffer,buffer.length-2);
        buffer[buffer.length-2]=(byte) crc;           // CRC L
        buffer[buffer.length-1]=(byte)(crc>>8);       // CRC H
        return toByteArray(buffer);

    }


    @Override
    public byte[] buildReadData(int servoId, int... params) {
        int[] buffer = new int[10+params.length];
        buffer[0] = 0xFF;
        buffer[1] = 0xFF;
        buffer[2] = 0xFD;
        buffer[3] = 0;
        buffer[4] = servoId;
        buffer[5] = 3+params.length;    // length L
        buffer[6] = 0;    // length H
        buffer[7] = 0x02;    // READ
        for(int i=0;i<params.length;i++) {
            buffer[8+i] = params[i];
        }
        int crc = crc16(buffer,buffer.length-2);
        buffer[buffer.length-2]=(byte) crc;           // CRC L
        buffer[buffer.length-1]=(byte)(crc>>8);       // CRC H
        return toByteArray(buffer);
    }


    @Override
    public List<Data> parse(byte[] data) {

        List<Data> results = new ArrayList<Data>();
        int offset=0;
        System.out.println("To parse:"+ Hex.encodeHexString(data));
        if (data ==null || data.length==0 ) throw new IllegalStateException("Unable to parse empty array");
        while(true) {
            if (data[0+offset]!=(byte)0xFF || data[1+offset] != (byte)0xFF ||
                    data[2+offset] != (byte)0xFD || data[3+offset] != 0) break; //throw new IllegalArgumentException("Invalid packet header");
            TYPES type = TYPES.getByNumber(data[7]);
            Data result = new Data(type);
            result.servoId = data[4+offset];
            int length = data[5+offset];
            length += data[6+offset] * 256;
            ByteBuffer bb = ByteBuffer.allocate(2);
            bb.put(data[6 + length + offset]);
            bb.put(data[5 + length + offset]);
            int crc3 = Short.toUnsignedInt(bb.getShort(0));
            int[] forCRC = toIntArray(Arrays.copyOfRange(data, offset, offset + 5 + length));
            int calculatedCRC = crc16(forCRC, 5 + length);
            if (crc3 != calculatedCRC) {
                // System.out.println("CRC does not match. Calculated:"+Integer.toHexString(calculatedCRC) + " from data:"+crc2 + "CRC3:"+crc3);
                throw new IllegalStateException("CRC does not match");
            }

            result.params = new int[length - 4];
            for (int i = 0; i < length - 3; i++) {
                if (i==0) // error
                    result.error= Byte.toUnsignedInt(data[8 + offset]);
                else
                    result.params[i-1] = Byte.toUnsignedInt(data[8 + i + offset]);
            }
            results.add(result);
            if (data.length<=6+length+offset+1)
                break;
            offset += 5 + length + offset+2;
        }
        return results;
    }

    private int[] toIntArray(byte[] data) {
        int[] x = new int[data.length];
        for(int i=0;i<data.length;i++)
            x[i]=data[i];
        return x;
    }

    private int[] crc_table = new int[]{
            0x0000, 0x8005, 0x800F, 0x000A, 0x801B, 0x001E, 0x0014, 0x8011,
            0x8033, 0x0036, 0x003C, 0x8039, 0x0028, 0x802D, 0x8027, 0x0022,
            0x8063, 0x0066, 0x006C, 0x8069, 0x0078, 0x807D, 0x8077, 0x0072,
            0x0050, 0x8055, 0x805F, 0x005A, 0x804B, 0x004E, 0x0044, 0x8041,
            0x80C3, 0x00C6, 0x00CC, 0x80C9, 0x00D8, 0x80DD, 0x80D7, 0x00D2,
            0x00F0, 0x80F5, 0x80FF, 0x00FA, 0x80EB, 0x00EE, 0x00E4, 0x80E1,
            0x00A0, 0x80A5, 0x80AF, 0x00AA, 0x80BB, 0x00BE, 0x00B4, 0x80B1,
            0x8093, 0x0096, 0x009C, 0x8099, 0x0088, 0x808D, 0x8087, 0x0082,
            0x8183, 0x0186, 0x018C, 0x8189, 0x0198, 0x819D, 0x8197, 0x0192,
            0x01B0, 0x81B5, 0x81BF, 0x01BA, 0x81AB, 0x01AE, 0x01A4, 0x81A1,
            0x01E0, 0x81E5, 0x81EF, 0x01EA, 0x81FB, 0x01FE, 0x01F4, 0x81F1,
            0x81D3, 0x01D6, 0x01DC, 0x81D9, 0x01C8, 0x81CD, 0x81C7, 0x01C2,
            0x0140, 0x8145, 0x814F, 0x014A, 0x815B, 0x015E, 0x0154, 0x8151,
            0x8173, 0x0176, 0x017C, 0x8179, 0x0168, 0x816D, 0x8167, 0x0162,
            0x8123, 0x0126, 0x012C, 0x8129, 0x0138, 0x813D, 0x8137, 0x0132,
            0x0110, 0x8115, 0x811F, 0x011A, 0x810B, 0x010E, 0x0104, 0x8101,
            0x8303, 0x0306, 0x030C, 0x8309, 0x0318, 0x831D, 0x8317, 0x0312,
            0x0330, 0x8335, 0x833F, 0x033A, 0x832B, 0x032E, 0x0324, 0x8321,
            0x0360, 0x8365, 0x836F, 0x036A, 0x837B, 0x037E, 0x0374, 0x8371,
            0x8353, 0x0356, 0x035C, 0x8359, 0x0348, 0x834D, 0x8347, 0x0342,
            0x03C0, 0x83C5, 0x83CF, 0x03CA, 0x83DB, 0x03DE, 0x03D4, 0x83D1,
            0x83F3, 0x03F6, 0x03FC, 0x83F9, 0x03E8, 0x83ED, 0x83E7, 0x03E2,
            0x83A3, 0x03A6, 0x03AC, 0x83A9, 0x03B8, 0x83BD, 0x83B7, 0x03B2,
            0x0390, 0x8395, 0x839F, 0x039A, 0x838B, 0x038E, 0x0384, 0x8381,
            0x0280, 0x8285, 0x828F, 0x028A, 0x829B, 0x029E, 0x0294, 0x8291,
            0x82B3, 0x02B6, 0x02BC, 0x82B9, 0x02A8, 0x82AD, 0x82A7, 0x02A2,
            0x82E3, 0x02E6, 0x02EC, 0x82E9, 0x02F8, 0x82FD, 0x82F7, 0x02F2,
            0x02D0, 0x82D5, 0x82DF, 0x02DA, 0x82CB, 0x02CE, 0x02C4, 0x82C1,
            0x8243, 0x0246, 0x024C, 0x8249, 0x0258, 0x825D, 0x8257, 0x0252,
            0x0270, 0x8275, 0x827F, 0x027A, 0x826B, 0x026E, 0x0264, 0x8261,
            0x0220, 0x8225, 0x822F, 0x022A, 0x823B, 0x023E, 0x0234, 0x8231,
            0x8213, 0x0216, 0x021C, 0x8219, 0x0208, 0x820D, 0x8207, 0x0202
    };

    private int crc16(int[] data, int length) {

        short crc_accum = 0;
        for (int j = 0; j < length; j++) {
            int i = ((crc_accum >> 8) ^ data[j]) & 0xFF;
            crc_accum = (short) (((crc_accum << 8) ^ crc_table[i]));
        }
        int crc = Short.toUnsignedInt(crc_accum);
        System.out.println("Calculated CRC is " + crc + " / Hex " + Integer.toHexString(crc));
        return crc;
    }

}

