package com.zen.biz.rest;

import com.alibaba.fastjson.JSON;
import com.orhanobut.logger.Logger;

import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.Okio;
import retrofit2.Converter;

import java.io.IOException;
import java.lang.reflect.Type;

/**

 *
 * @version 1.0.0 <br/>
 */
public class FastJsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private final Type type;
    final boolean DEBUG=true;
    public FastJsonResponseBodyConverter(Type type) {
        this.type = type;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        BufferedSource bufferedSource = Okio.buffer(value.source());
        String tempStr = bufferedSource.readUtf8();
        bufferedSource.close();
        if (DEBUG);
        if (tempStr.startsWith("{"))
            return JSON.parseObject(tempStr, type);
        else {

            return null;
        }
    }
}
