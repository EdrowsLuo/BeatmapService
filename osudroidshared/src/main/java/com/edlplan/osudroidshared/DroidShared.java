package com.edlplan.osudroidshared;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import ru.nsu.ccfit.zuev.osu.BeatmapInfo;

public class DroidShared {

    public static List<File> findLibraryFiles(File songs) {
        if (songs == null || !songs.exists() || songs.isFile()) {
            return null;
        }
        String[] fns = songs.list((dir, name) -> name.startsWith("library.") && name.endsWith(".dat"));
        if (fns.length == 0) {
            return null;
        } else {
            List<File> fs = new ArrayList<>();
            for (String n : fns) {
                File f = new File(songs, n);
                if (f.exists() && f.isFile()) {
                    fs.add(f);
                }
            }
            return fs;
        }
    }

    public static File findTargetLibraryFile(File songs) {
        List<File> fs = findLibraryFiles(songs);
        if (fs == null || fs.size() == 0) {
            return null;
        }
        Collections.sort(fs, (a, b) -> (int) (b.lastModified() - a.lastModified()));
        return fs.get(0);
    }

    public static List<BeatmapInfo> loadDroidLibrary(File songs) {
        File lib = findTargetLibraryFile(songs);
        if (lib == null) {
            return null;
        }
        System.out.println(lib);
        String versionName = lib.getName().substring("library.".length(), lib.getName().length() - ".dat".length());
        List<BeatmapInfo> library = null;
        final ObjectInputStream istream;
        try {
            istream = new ObjectInputStream(
                    new FileInputStream(lib));
            Object obj = istream.readObject();
            if (obj instanceof String) {
                if (!versionName.equals(obj)) {
                    System.out.println("version not match : " + versionName + " != " + obj);
                    return null;
                }
            } else {
                System.out.println("format err : 1");
                istream.close();
                return null;
            }
            int fileCount = 0;
            obj = istream.readObject();
            if (obj instanceof Integer) {
                fileCount = (Integer) obj;
            } else {
                istream.close();
                System.out.println("format err : 2");
                return null;
            }
            //Log.i("ed-d", "load cache step 3");
            obj = istream.readObject();
            if (obj instanceof ArrayList<?>) {
                //Log.i("ed-d", "load cache step 4");
                library = (ArrayList<BeatmapInfo>) obj;
                istream.close();
                return library;
            } else {
                System.out.println("format err : 3");
            }
            istream.close();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }



}
