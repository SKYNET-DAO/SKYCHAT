package cn.wildfire.imshat.app

import android.os.Environment
import com.orhanobut.logger.Logger
import java.util.*

/**
 * Created by heavyrain lee on 2017/11/24.
 */

interface Config {
    companion object {

         val IM_SERVER_HOST = "43.249.31.56"
        val IM_SERVER_PORT = 80



        val APP_SERVER_HOST = "43.249.31.56"
        val APP_SERVER_PORT = 8888
        val ICE_ADDRESS = "turn:turn.liyufan.win:3478"
        val ICE_USERNAME = "wfchat"
        val ICE_PASSWORD = "wfchat"

        val DEFAULT_MAX_AUDIO_RECORD_TIME_SECOND = 120

        val SP_NAME = "config"
        val SP_KEY_SHOW_GROUP_MEMBER_ALIAS = "show_group_member_alias:%s"

        val VIDEO_SAVE_DIR = Environment.getExternalStorageDirectory().path + "/wfc/video"
        val AUDIO_SAVE_DIR = Environment.getExternalStorageDirectory().path + "/wfc/audio"
        val PHOTO_SAVE_DIR = Environment.getExternalStorageDirectory().path + "/wfc/photo"
        val FILE_SAVE_DIR = Environment.getExternalStorageDirectory().path + "/wfc/file"

        var peers = mutableListOf(IM_SERVER_HOST)


    }

}
