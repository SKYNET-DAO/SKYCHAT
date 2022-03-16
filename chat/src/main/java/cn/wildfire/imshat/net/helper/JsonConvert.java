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
package cn.wildfire.imshat.net.helper;

import com.google.gson.stream.JsonReader;
import com.lzy.okgo.convert.Converter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import cn.wildfire.imshat.kit.third.utils.UIUtils;
import cn.wildfirechat.imshat.R;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class JsonConvert<T> implements Converter<T> {

    private Type type;
    private Class<T> clazz;

    public JsonConvert() {
    }

    public JsonConvert(Type type) {
        this.type = type;
    }

    public JsonConvert(Class<T> clazz) {
        this.clazz = clazz;
    }

    
    @Override
    public T convertResponse(Response response) throws Throwable {

        
        if (type == null) {
            if (clazz == null) {
                
                Type genType = getClass().getGenericSuperclass();
                type = ((ParameterizedType) genType).getActualTypeArguments()[0];
            } else {
                return parseClass(response, clazz);
            }
        }

        if (type instanceof ParameterizedType) {
            return parseParameterizedType(response, (ParameterizedType) type);
        } else if (type instanceof Class) {
            return parseClass(response, (Class<?>) type);
        } else {
            return parseType(response, type);
        }
    }

    private T parseClass(Response response, Class<?> rawType) throws Exception {
        if (rawType == null) return null;
        ResponseBody body = response.body();
        if (body == null) return null;
        JsonReader jsonReader = new JsonReader(body.charStream());

        if (rawType == String.class) {
            //noinspection unchecked
            return (T) body.string();
        } else if (rawType == JSONObject.class) {
            //noinspection unchecked
            return (T) new JSONObject(body.string());
        } else if (rawType == JSONArray.class) {
            //noinspection unchecked
            return (T) new JSONArray(body.string());
        } else {
            T t = Convert.fromJson(jsonReader, rawType);
            response.close();
            return t;
        }
    }

    private T parseType(Response response, Type type) throws Exception {
        if (type == null) return null;
        ResponseBody body = response.body();
        if (body == null) return null;
        JsonReader jsonReader = new JsonReader(body.charStream());

        
        T t = Convert.fromJson(jsonReader, type);
        response.close();
        return t;
    }

    private T parseParameterizedType(Response response, ParameterizedType type) throws Exception {
        if (type == null) return null;
        ResponseBody body = response.body();
        if (body == null) return null;
        JsonReader jsonReader = new JsonReader(body.charStream());

        Type rawType = type.getRawType();                     
        Type typeArgument = type.getActualTypeArguments()[0]; 
        if (rawType != LzyResponse.class) {
            
            T t = Convert.fromJson(jsonReader, type);
            response.close();
            return t;
        } else {
            if (typeArgument == Void.class) {
                
                SimpleResponse simpleResponse = Convert.fromJson(jsonReader, SimpleResponse.class);
                response.close();
                
                return (T) simpleResponse.toLzyResponse();
            } else {
                
                LzyResponse lzyResponse = Convert.fromJson(jsonReader, type);
                response.close();
                int code = lzyResponse.code;
                
                if (code == 0) {
                    //noinspection unchecked
                    return (T) lzyResponse;
                } else if (code == 104) {
                    throw new IllegalStateException(UIUtils.getString(R.string.str_auto_invanid));
                } else if (code == 105) {
                    throw new IllegalStateException(UIUtils.getString(R.string.str_userinfo_data_out));
                } else {
                    
                    throw new IllegalStateException(UIUtils.getString(R.string.str_error_code) + code + UIUtils.getString(R.string.str_error_info) + lzyResponse.msg);
                }
            }
        }
    }
}
