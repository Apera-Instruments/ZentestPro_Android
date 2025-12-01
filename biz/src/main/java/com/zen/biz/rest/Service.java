package com.zen.biz.rest;


import com.zen.biz.json.ServiceRespond;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

/**
 * Created by xinhua.lin on 2016/11/1.
 */
public interface Service {

   /* @POST("/gateway.do")
    @Headers({"Accept:application/json"})
    Call<ServiceRespond> service(@Body ServiceBody body);
*/

    @POST("interfaces/user/register")
    @Headers({"Accept:application/json"})
    @FormUrlEncoded
    Call<ServiceRespond> register(
            @Field("userName") String userName,
            @Field("userPassword") String userPassword,
            @Field("firstName") String firstName,
            @Field("lastName") String lastName
    );


    @POST("interfaces/acquire/code")
    @Headers({"Accept:application/json"})
    @FormUrlEncoded
    Call<ServiceRespond> acquireCode(
            @Field("userName") String userName,@Field("type") String type
           );



    @POST("interfaces/user/update")
    @Headers({"Accept:application/json"})
    @FormUrlEncoded
    Call<ServiceRespond> update(
            @Field("userName") String userName,
            @Field("type") int type,
            @Field("oldPassword") String oldPassword,
            @Field("newPassword") String newPassword,
            @Field("authCode") String authCode
    );

    @POST("interfaces/user/login")
    @Headers({"Accept:application/json"})
    @FormUrlEncoded
    Call<ServiceRespond> login(
            @Field("userName") String userName,

            @Field("userPassword") String userPassword

    );

    @POST("interfaces/user/updateLoginTime")
    @Headers({"Accept:application/json"})
    @FormUrlEncoded
    Call<ServiceRespond> updateLoginTime(
            @Field("userId") String userId,
            @Field("sign") String sign,
            @Field("traceNo") String traceNo,
            @Field("timestamp") String timestamp



    );

    @POST("interfaces/verification/code")
    @Headers({"Accept:application/json"})
    @FormUrlEncoded
    Call<ServiceRespond>  verificationCode(@Field("userName") String userName, @Field("authCode") String code);

    @POST("interfaces/phdata/list_data")
    @Headers({"Accept:application/json"})
    @FormUrlEncoded
    Call<ServiceRespond>  phdataList(@FieldMap Map<String,Object> map);
    @POST("interfaces/phdata/add")
    @Headers({"Accept:application/json"})
    @FormUrlEncoded
    Call<ServiceRespond>  phdataAdd(@FieldMap Map<String,Object> map);
    @POST("interfaces/phdata/delete")
    @Headers({"Accept:application/json"})
    @FormUrlEncoded
    Call<ServiceRespond>  phdataDelete(@FieldMap Map<String,Object> map);
   /* @POST("interfaces/orpdata/list_data")
    @Headers({"Accept:application/json"})
    @FormUrlEncoded
    Call<ServiceRespond>  orpdataList(@FieldMap Map<String,Object> map);
    @POST("interfaces/orpdata/add")
    @Headers({"Accept:application/json"})
    @FormUrlEncoded
    Call<ServiceRespond>  orpdataAdd(@FieldMap Map<String,Object> map);
    @POST("interfaces/orpdata/delete")
    @Headers({"Accept:application/json"})
    @FormUrlEncoded
    Call<ServiceRespond>  orpdataDelete(@FieldMap Map<String,Object> map);
    @POST("interfaces/conducitivitydata/list_data")
    @Headers({"Accept:application/json"})
    @FormUrlEncoded
    Call<ServiceRespond>  conducitivitydataList(@FieldMap Map<String,Object> map);
    @POST("interfaces/conducitivitydata/add")
    @Headers({"Accept:application/json"})
    @FormUrlEncoded
    Call<ServiceRespond>  conducitivitydataAdd(@FieldMap Map<String,Object> map);
    @POST("interfaces/conducitivitydata/delete")
    @Headers({"Accept:application/json"})
    @FormUrlEncoded
    Call<ServiceRespond>  conducitivitydataDelete(@FieldMap Map<String,Object> map);
    @POST("interfaces/resistivitydata/list_data")
    @Headers({"Accept:application/json"})
    @FormUrlEncoded
    Call<ServiceRespond>  resistivitydataList(@FieldMap Map<String,Object> map);
    @POST("interfaces/resistivitydata/add")
    @Headers({"Accept:application/json"})
    @FormUrlEncoded
    Call<ServiceRespond>  resistivitydataAdd(@FieldMap Map<String,Object> map);
    @POST("interfaces/resistivitydata/delete")
    @Headers({"Accept:application/json"})
    @FormUrlEncoded
    Call<ServiceRespond>  resistivitydataDelete(@FieldMap Map<String,Object> map);
    @POST("interfaces/salinitydata/list_data")
    @Headers({"Accept:application/json"})
    @FormUrlEncoded
    Call<ServiceRespond>  salinitydataList(@FieldMap Map<String,Object> map);
    @POST("interfaces/salinitydata/add")
    @Headers({"Accept:application/json"})
    @FormUrlEncoded
    Call<ServiceRespond>  salinitydataAdd(@FieldMap Map<String,Object> map);
    @POST("interfaces/salinitydata/delete")
    @Headers({"Accept:application/json"})
    @FormUrlEncoded
    Call<ServiceRespond>  salinitydataDelete(@FieldMap Map<String,Object> map);
    @POST("interfaces/tdsdata/list_data")
    @Headers({"Accept:application/json"})
    @FormUrlEncoded
    Call<ServiceRespond>  tdsdataList(@FieldMap Map<String,Object> map);
    @POST("interfaces/tdsdata/add")
    @Headers({"Accept:application/json"})
    @FormUrlEncoded
    Call<ServiceRespond>  tdsdataAdd(@FieldMap Map<String,Object> map);
    @POST("interfaces/tdsdata/delete")
    @Headers({"Accept:application/json"})
    @FormUrlEncoded
    Call<ServiceRespond>  tdsdataDelete(@FieldMap Map<String,Object> map);
    @POST("interfaces/checkdata/list_data")
    @Headers({"Accept:application/json"})
    @FormUrlEncoded
    Call<ServiceRespond>  checkdataList(@FieldMap Map<String,Object> map);
    @POST("interfaces/checkdata/add")
    @Headers({"Accept:application/json"})
    @FormUrlEncoded
    Call<ServiceRespond>  checkdataAdd(@FieldMap Map<String,Object> map);
    @POST("interfaces/checkdata/delete")
    @Headers({"Accept:application/json"})
    @FormUrlEncoded
    Call<ServiceRespond>  checkdataDelete(@FieldMap Map<String,Object> map);*/


}
