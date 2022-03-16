package com.android.mine_android.api


import cn.wildfire.imshat.net.AppConst
import cn.wildfire.imshat.net.api.ImRetrofitService
import cn.wildfire.imshat.net.bean.RandomIp
import cn.wildfire.imshat.net.helper.JsonConvert
import cn.wildfire.imshat.net.helper.LzyResponse
import com.google.gson.Gson
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import com.lzy.okrx2.adapter.ObservableBody
import com.orhanobut.logger.Logger
import com.vondear.rxtool.RxAppTool
import io.reactivex.Observable
import java.io.File

class Api : ImRetrofitService() {
    companion object {

        val randomAction="/api/getGatewayIP"

        fun getRandomIp(url:String): Observable<LzyResponse<RandomIp>>? {
            return OkGo.get<LzyResponse<RandomIp>>(url +randomAction)
                    .converter(object : JsonConvert<LzyResponse<RandomIp>>() {

                    }).adapt(ObservableBody())

        }

    }


}
