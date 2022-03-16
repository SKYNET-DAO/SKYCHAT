package com.cjt2325.cameralibrary.util;

import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;


public class CheckPermission {
    public static final int STATE_RECORDING = -1;
    public static final int STATE_NO_PERMISSION = -2;
    public static final int STATE_SUCCESS = 1;


    public static int getRecordState() {
        int minBuffer = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat
                .ENCODING_PCM_16BIT);
        AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, 44100, AudioFormat
                .CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, (minBuffer * 100));
        short[] point = new short[minBuffer];
        int readSize = 0;
        try {

            audioRecord.startRecording();
        } catch (Exception e) {
            if (audioRecord != null) {
                audioRecord.release();
                audioRecord = null;
            }
            return STATE_NO_PERMISSION;
        }
        if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {

            if (audioRecord != null) {
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
                Log.d("CheckAudioPermission", "Recorder in used.");
            }
            return STATE_RECORDING;
        } else {
            

            readSize = audioRecord.read(point, 0, point.length);


            if (readSize <= 0) {
                if (audioRecord != null) {
                    audioRecord.stop();
                    audioRecord.release();
                    audioRecord = null;

                }
                Log.d("CheckAudioPermission", "Blank records.");
                return STATE_NO_PERMISSION;

            } else {
                if (audioRecord != null) {
                    audioRecord.stop();
                    audioRecord.release();
                    audioRecord = null;

                }

                return STATE_SUCCESS;
            }
        }
    }

    public synchronized static boolean isCameraUseable(int cameraID) {
        boolean canUse = true;
        Camera mCamera = null;
        try {
            mCamera = Camera.open(cameraID);
            
            Camera.Parameters mParameters = mCamera.getParameters();
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
            e.printStackTrace();
            canUse = false;
        } finally {
            if (mCamera != null) {
                mCamera.release();
            } else {
                canUse = false;
            }
            mCamera = null;
        }
        return canUse;
    }
}