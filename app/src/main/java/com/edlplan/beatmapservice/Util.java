package com.edlplan.beatmapservice;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Util {

    public static byte[] readFullByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        byte[] buf = new byte[256];
        int l;
        while ((l = in.read(buf)) != -1) {
            o.write(buf, 0, l);
        }
        return o.toByteArray();
    }

    public static String readFullString(InputStream in) throws IOException {
        return new String(readFullByteArray(in), "UTF-8");
    }

}
