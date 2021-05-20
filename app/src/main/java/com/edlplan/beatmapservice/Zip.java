package com.edlplan.beatmapservice;

import android.content.Context;

import androidx.documentfile.provider.DocumentFile;

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

    public static void unzipDocumentFile(DocumentFile songsDir, File oszFile, Context context) throws IOException {
        String fileName = oszFile.getName();
        File tempDir = new File(oszFile.getParentFile(), fileName.substring(0, fileName.lastIndexOf('.')));
        tempDir.mkdirs();
        try {
            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(oszFile));
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                File f = new File(tempDir, entry.getName());
                String[] videoExtension = {"avi", "flv", "mp4"};//跳过视频文件
                boolean flag = false;
                for (String ex : videoExtension) {
                    if (ex.equals(f.getName().substring(f.getName().lastIndexOf('.') + 1))) {
                        flag = true;
                        break;
                    }
                }
                if (flag) {
                    continue;
                }
                Util.checkFile(f);
                FileOutputStream outputStream = new FileOutputStream(f);
                Util.flow(zipInputStream, outputStream);
                outputStream.close();
            }
            Util.moveDocument(context, DocumentFile.fromFile(tempDir), songsDir);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            oszFile.delete();
            tempDir.delete();
        }
    }
}
