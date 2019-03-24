package com.edlplan.beatmapservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Zip {

    public static void unzip(File file) throws IOException {
        File dir = file.getParentFile();
        dir = new File(dir, file.getName().substring(0, file.getName().lastIndexOf('.')));
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file));
        ZipEntry entry;
        while ((entry = zipInputStream.getNextEntry()) != null) {
            File f = new File(dir, entry.getName());
            Util.checkFile(f);
            FileOutputStream outputStream = new FileOutputStream(f);
            Util.flow(zipInputStream, outputStream);
            outputStream.close();
        }
    }

}
