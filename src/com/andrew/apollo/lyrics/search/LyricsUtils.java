package com.andrew.apollo.lyrics.search;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import android.os.Environment;

public class LyricsUtils {

    public static File getLyricsDirectory() {
        String location = "/Apollo/lyrics";
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        File file = new File(externalStorageDirectory.getPath() + location);
        boolean exists = file.exists();
        if (!exists) {
            exists = file.mkdirs();
        }
        return exists ? file : null;
    }

    public static File getLyricsFile(String songFilePath) {
        int hashCode = songFilePath.hashCode();
        String lyricsFileName = new File(songFilePath).getName() + "." + hashCode + ".txt";
        File lyricsDir = getLyricsDirectory();
        if (getLyricsDirectory() != null) {
            return new File(lyricsDir.getAbsoluteFile() + "/" + lyricsFileName);
        }
        return null;
    }

    public static void saveLyricsToFile(String lyrics, String filePath) {
        File lyricsFlie = getLyricsFile(filePath);
        if (lyricsFlie != null) {
            PrintWriter br = null;
            try {
                br = new PrintWriter(new BufferedWriter(new FileWriter(lyricsFlie)));
                br.print(lyrics);
            } catch (Exception e) {
                // TODO log error
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (Exception e) {
                        // TODO log error
                    }
                }
            }
        }
    }

    public static String readLyricsFromFile(String songFilePath) {
        File lyricsFlie = getLyricsFile(songFilePath);
        if (lyricsFlie != null && lyricsFlie.exists()) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(lyricsFlie));
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();
                while (line != null) {
                    sb.append(line).append("\n");
                    line = br.readLine();
                }
                return sb.toString();
            } catch (Exception e) {
                // TODO log error
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        // TODO log error
                    }
                }
            }
        }
        return null;
    }
}
