package com.cjt2325.cameralibrary.util;


public class AngleUtil {
    public static int getSensorAngle(float x, float y) {
        if (Math.abs(x) > Math.abs(y)) {
            
            if (x > 4) {
                
                return 270;
            } else if (x < -4) {
               
                return 90;
            } else {
                
                return 0;
            }
        } else {
            if (y > 7) {
               
                return 0;
            } else if (y < -7) {
                
                return 180;
            } else {
                
                return 0;
            }
        }
    }
}
