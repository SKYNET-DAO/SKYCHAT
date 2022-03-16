package cn.wildfirechat.push.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * json utils class
 */
public class JsonUtil {

    private static Gson gson = null;

    static {
        if (gson == null) {
            gson = new Gson();
        }
    }

    public JsonUtil() {
    }

    /**
     * type exchange
     *
     * @param object
     * @return
     */
    public static String toJson(Object object) {
        String gsonString = null;
        if (gson != null) {
            gsonString = gson.toJson(object);
        }
        return gsonString;
    }

    /**
     * bean exchange
     *
     * @param gsonString
     * @param cls
     * @return
     */
    public static <T> T toBean(String gsonString, Type cls) {
        T t = null;
        if (gson != null) {
            t = gson.fromJson(gsonString, cls);
        }
        return t;
    }

    public static <T> T fromJson(String json, Type typeOfT) throws JsonSyntaxException {
        if (gson != null) {
            return gson.fromJson(json, typeOfT);
        }
        return null;
    }


    public static  <T> List<T> toList1(String json, Class<T> cls) {
        List<T> list = new ArrayList<T>();
        JsonArray array = new JsonParser().parse(json).getAsJsonArray();
        for (final JsonElement elem : array) {
            list.add(gson.fromJson(elem, cls));
        }
        return list;
    }


  
    public static <T> List<Map<String, T>> toListTMap(String gsonString) {
        List<Map<String, T>> list = null;
        if (gson != null) {
            list = gson.fromJson(gsonString,
                    new TypeToken<List<Map<String, T>>>() {
                    }.getType());
        }
        return list;
    }


    public static <T> Map<String, T> toMap(String gsonString) {
        Map<String, T> map = null;
        if (gson != null) {
            map = gson.fromJson(gsonString, new TypeToken<Map<String, T>>() {
            }.getType());
        }
        return map;
    }


    public static  Type getSuperclassTypeParameter(Class<?> subclass) {

        Type superclass = subclass.getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("Missing type parameter.");
        }

        ParameterizedType parameter = (ParameterizedType) superclass;
        return $Gson$Types.canonicalize(parameter.getActualTypeArguments()[0]);
    }
}