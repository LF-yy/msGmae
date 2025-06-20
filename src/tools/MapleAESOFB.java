package tools;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;
import java.security.Key;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Cipher;

public class MapleAESOFB
{
    private byte[] iv;
    private Cipher cipher;
    private short mapleVersion;
    private static final SecretKeySpec skey;
    private static final byte[] funnyBytes;
    private static byte[] rammyByte;

    public MapleAESOFB(final byte[] iv, final short mapleVersion) {
        try {
            (this.cipher = Cipher.getInstance("AES")).init(1, (Key)MapleAESOFB.skey);
        }
        catch (NoSuchAlgorithmException e) {
            System.err.println("ERROR" + (Object)e);
        }
        catch (NoSuchPaddingException e2) {
            System.err.println("ERROR" + (Object)e2);
        }
        catch (InvalidKeyException e3) {
            System.err.println("Error initalizing the encryption cipher.  Make sure you're using the Unlimited Strength cryptography jar files.");
        }
        this.setIv(iv);
        this.mapleVersion = (short)((mapleVersion >> 8 & 0xFF) | (mapleVersion << 8 & 0xFF00));
    }

    private void setIv(final byte[] iv) {
        this.iv = iv;
    }

    public byte[] getIv() {
        return this.iv;
    }

    public byte[] crypt(final byte[] data) {
        int remaining = data.length;
        int llength = 1456;
        int start = 0;
        try {
            while (remaining > 0) {
                final byte[] myIv = BitTools.multiplyBytes(this.iv, 4, 4);
                if (remaining < llength) {
                    llength = remaining;
                }
                for (int x = start; x < start + llength; ++x) {
                    if ((x - start) % myIv.length == 0) {
                        final byte[] newIv = this.cipher.doFinal(myIv);
                        System.arraycopy((Object)newIv, 0, (Object)myIv, 0, myIv.length);
                    }
                    final int n = x;
                    data[n] ^= myIv[(x - start) % myIv.length];
                }
                start += llength;
                remaining -= llength;
                llength = 1460;
            }
            this.updateIv();
        }
        catch (IllegalBlockSizeException e) {
            //e.printStackTrace();
        }
        catch (BadPaddingException e2) {
            e2.printStackTrace();
        }
        return data;
    }

    private void updateIv() {
        this.iv = getNewIv(this.iv);
    }

    public byte[] getPacketHeader(final int length) {
        final int iiv = ((this.iv[3] & 0xFF) | (this.iv[2] << 8 & 0xFF00)) ^ this.mapleVersion;
        final int mlength = ((length << 8 & 0xFF00) | length >>> 8) ^ iiv;
        return new byte[] { (byte)(iiv >>> 8 & 0xFF), (byte)(iiv & 0xFF), (byte)(mlength >>> 8 & 0xFF), (byte)(mlength & 0xFF) };
    }

    public static int getPacketLength(final int packetHeader) {
        int packetLength = packetHeader >>> 16 ^ (packetHeader & 0xFFFF);
        packetLength = ((packetLength << 8 & 0xFF00) | (packetLength >>> 8 & 0xFF));
        return packetLength;
    }

    public boolean checkPacket(final byte[] packet) {
        return ((packet[0] ^ this.iv[2]) & 0xFF) == (this.mapleVersion >> 8 & 0xFF) && ((packet[1] ^ this.iv[3]) & 0xFF) == (this.mapleVersion & 0xFF);
    }

    public boolean checkPacket(final int packetHeader) {
        return this.checkPacket(new byte[] { (byte)(packetHeader >> 24 & 0xFF), (byte)(packetHeader >> 16 & 0xFF) });
    }

    public static byte[] getNewIv(final byte[] oldIv) {
        final byte[] in = { -14, 83, 80, -58 };
        for (int x = 0; x < 4; ++x) {
            funnyShit(oldIv[x], in);
        }
        return in;
    }

    @Override
    public String toString() {
        return "IV: " + HexTool.toString(this.iv);
    }

    public static void funnyShit(final byte inputByte, final byte[] in) {
        byte elina = in[1];
        final byte anna = inputByte;
        byte moritz = MapleAESOFB.funnyBytes[elina & 0xFF];
        moritz -= inputByte;
        final int n = 0;
        in[n] += moritz;
        moritz = in[2];
        moritz ^= MapleAESOFB.funnyBytes[anna & 0xFF];
        elina -= (byte)(moritz & 0xFF);
        in[1] = elina;
        elina = (moritz = in[3]);
        elina -= (byte)(in[0] & 0xFF);
        moritz = MapleAESOFB.funnyBytes[moritz & 0xFF];
        moritz += inputByte;
        moritz ^= in[2];
        in[2] = moritz;
        elina += (byte)(MapleAESOFB.funnyBytes[anna & 0xFF] & 0xFF);
        in[3] = elina;
        int merry = in[0] & 0xFF;
        merry |= (in[1] << 8 & 0xFF00);
        merry |= (in[2] << 16 & 0xFF0000);
        merry |= (in[3] << 24 & 0xFF000000);
        int ret_value = merry >>> 29;
        merry <<= 3;
        ret_value |= merry;
        in[0] = (byte)(ret_value & 0xFF);
        in[1] = (byte)(ret_value >> 8 & 0xFF);
        in[2] = (byte)(ret_value >> 16 & 0xFF);
        in[3] = (byte)(ret_value >> 24 & 0xFF);
    }

    static {
        skey = new SecretKeySpec(new byte[] {
                0x13, 0x00, 0x00, 0x00,
                0x08, 0x00, 0x00, 0x00,// 0x08, 0x00, 0x00, 0x00,
                0x06, 0x00, 0x00, 0x00,
          (byte)0xB4, 0x00, 0x00, 0x00,
                0x1B, 0x00, 0x00, 0x00,
                0x0F, 0x00, 0x00, 0x00,
                0x33, 0x00, 0x00, 0x00,
                0x52, 0x00, 0x00, 0x00 }, "AES");
        funnyBytes = new byte[] { -20, 63, 119, -92, 69, -48, 113, -65, -73, -104, 32, -4, 75, -23, -77, -31, 92, 34, -9, 12, 68, 27, -127, -67, 99, -115, -44, -61, -14, 16, 25, -32, -5, -95, 110, 102, -22, -82, -42, -50, 6, 24, 78, -21, 120, -107, -37, -70, -74, 66, 122, 42, -125, 11, 84, 103, 109, -24, 101, -25, 47, 7, -13, -86, 39, 123, -123, -80, 38, -3, -117, -87, -6, -66, -88, -41, -53, -52, -110, -38, -7, -109, 96, 45, -35, -46, -94, -101, 57, 95, -126, 33, 76, 105, -8, 49, -121, -18, -114, -83, -116, 106, -68, -75, 107, 89, 19, -15, 4, 0, -10, 90, 53, 121, 72, -113, 21, -51, -105, 87, 18, 62, 55, -1, -99, 79, 81, -11, -93, 112, -69, 20, 117, -62, -72, 114, -64, -19, 125, 104, -55, 46, 13, 98, 70, 23, 17, 77, 108, -60, 126, 83, -63, 37, -57, -102, 28, -120, 88, 44, -119, -36, 2, 100, 64, 1, 93, 56, -91, -30, -81, 85, -43, -17, 26, 124, -89, 91, -90, 111, -122, -97, 115, -26, 10, -34, 43, -103, 74, 71, -100, -33, 9, 118, -98, 48, 14, -28, -78, -108, -96, 59, 52, 29, 40, 15, 54, -29, 35, -76, 3, -40, -112, -56, 60, -2, 94, 50, 36, 80, 31, 58, 67, -118, -106, 65, 116, -84, 82, 51, -16, -39, 41, -128, -79, 22, -45, -85, -111, -71, -124, 127, 97, 30, -49, -59, -47, 86, 61, -54, -12, 5, -58, -27, 8, 73 };
        rammyByte = new byte[] { -20, 63, 119, -92, 69, -48, 113, -65, -73, -104, 32, -4, 75, -23, -77, -31, 92, 34, -9, 12, 68, 27, -127, -67, 99, -115, -44, -61, -14, 16, 25, -32, -5, -95, 110, 102, -22, -82, -42, -50, 6, 24, 78, -21, 120, -107, -37, -70, -74, 66, 122, 42, -125, 11, 84, 103, 109, -24, 101, -25, 47, 7, -13, -86, 39, 123, -123, -80, 38, -3, -117, -87, -6, -66, -88, -41, -53, -52, -110, -38, -7, -109, 96, 45, -35, -46, -94, -101, 57, 95, -126, 33, 76, 105, -8, 49, -121, -18, -114, -83, -116, 106, -68, -75, 107, 89, 19, -15, 4, 0, -10, 90, 53, 121, 72, -113, 21, -51, -105, 87, 18, 62, 55, -1, -99, 79, 81, -11, -93, 112, -69, 20, 117, -62, -72, 114, -64, -19, 125, 104, -55, 46, 13, 98, 70, 23, 17, 77, 108, -60, 126, 83, -63, 37, -57, -102, 28, -120, 88, 44, -119, -36, 2, 100, 64, 1, 93, 56, -91, -30, -81, 85, -43, -17, 26, 124, -89, 91, -90, 111, -122, -97, 115, -26, 10, -34, 43, -103, 74, 71, -100, -33, 9, 118, -98, 48, 14, -28, -78, -108, -96, 59, 52, 29, 40, 15, 54, -29, 35, -76, 3, -40, -112, -56, 60, -2, 94, 50, 36, 80, 31, 58, 67, -118, -106, 65, 116, -84, 82, 51, -16, -39, 41, -128, -79, 22, -45, -85, -111, -71, -124, 127, 97, 30, -49, -59, -47, 86, 61, -54, -12, 5, -58, -27, 8, 73 };
    }
}

