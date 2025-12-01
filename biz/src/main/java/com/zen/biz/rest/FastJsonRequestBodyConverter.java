package com.zen.biz.rest;

import com.alibaba.fastjson.JSON;
import com.orhanobut.logger.Logger;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Converter;

import java.io.IOException;


/**

 *
 * @version 1.0.0 <br/>
 */
public class FastJsonRequestBodyConverter<T> implements Converter<T, RequestBody> {
    final boolean DEBUG=true;
    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");

    @Override
    public RequestBody convert(T value) throws IOException {
        byte b[] = JSON.toJSONBytes(value);
        if(DEBUG);
        return RequestBody.create(MEDIA_TYPE, b);
    }
}
