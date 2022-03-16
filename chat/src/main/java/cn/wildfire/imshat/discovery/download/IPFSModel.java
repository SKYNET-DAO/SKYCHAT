/*
 * Copyright 2016 jeasonlzy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.wildfire.imshat.discovery.download;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Random;

import cn.wildfire.imshat.net.AppConst;


public class IPFSModel implements Serializable {
    private String id;
    private String type;
    @SerializedName(value = "appname")
    private String title;
    private String author;
    @SerializedName("apphash")
    private String url;
    @SerializedName("appicon")
    private String iconUrl;
    private int priority;
    private String account;
    private String sign;
    private String version;
    private String hash;



    public void setIconhash(String iconhash) {
        this.iconhash = iconhash;
    }

    private String iconhash;


    public IPFSModel(String name, String url, String iconUrl) {
        this.title = name;
        this.url = url;
        this.iconUrl = iconUrl;
    }

    public IPFSModel(String name, String url) {
        this.title = name;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        this.title = name;
    }


    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIconUrl() {
        return AppConst.BASE_URL+iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public IPFSModel() {
        Random random = new Random();
        priority = random.nextInt(100);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }


    public String getIconhash() {
        return iconhash;
    }

}
