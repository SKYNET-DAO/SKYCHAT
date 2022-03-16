package cn.wildfire.imshat.kit.third.utils;

import java.io.Closeable;
import java.io.IOException;


public class IOUtils {
   
    public static boolean close(Closeable io) {
        if (io != null) {
            try {
                io.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}