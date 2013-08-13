package net.kaoriya.qb.redis_lua;

import org.junit.Test;
import static org.junit.Assert.*;

public class LuaScriptTest
{
    @Test
    public void toHex1() {
        byte[] buf = {
            0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte)0x88,
            (byte)0x99, (byte)0xaa, (byte)0xbb, (byte)0xcc, (byte)0xdd,
            (byte)0xee, (byte)0xff,
        };
        assertEquals("00112233445566778899aabbccddeeff", LuaScript.toHex(buf));
    }

    @Test
    public void toHex2() {
        byte[] buf = {
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, (byte)0x08,
            (byte)0x09, (byte)0x0a, (byte)0x0b, (byte)0x0c, (byte)0x0d,
            (byte)0x0e, (byte)0x0f,
        };
        assertEquals("000102030405060708090a0b0c0d0e0f", LuaScript.toHex(buf));
    }

    @Test
    public void toHex3() {
        byte[] buf = {
            0x00, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, (byte)0x80,
            (byte)0x90, (byte)0xa0, (byte)0xb0, (byte)0xc0, (byte)0xd0,
            (byte)0xe0, (byte)0xf0,
        };
        assertEquals("00102030405060708090a0b0c0d0e0f0", LuaScript.toHex(buf));
    }

    @Test
    public void toHex4() {
        byte[] buf = {
            0x0f, 0x1e, 0x2d, 0x3c, 0x4b, 0x5a, 0x69, 0x78, (byte)0x87,
            (byte)0x96, (byte)0xa5, (byte)0xb4, (byte)0xc3, (byte)0xd2,
            (byte)0xe1, (byte)0xf0,
        };
        assertEquals("0f1e2d3c4b5a69788796a5b4c3d2e1f0", LuaScript.toHex(buf));
    }

    @Test
    public void getSHA1Hex() throws Exception {
        assertEquals("da39a3ee5e6b4b0d3255bfef95601890afd80709",
                LuaScript.getSHA1Hex(new byte[0]));
    }

    @Test
    public void newFromResource() throws Exception {
        LuaScript s = LuaScript.newFromResource(getClass(), "test0.lua");
        assertNotNull(s);
        assertEquals("return 0", s.getScript());
        assertEquals("06d3d9b2060dd51343d5f19f0e531f15c507e3d1",
                s.getSHA1Hex());
    }
}
