package com.zen.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface RestApi {

    int USER_LOGIN = 1;
    int USER_LOGOUT = 2;
    int SUCCESS = 0;
    int FAIL = 1;
    int ACCOUNT_NOT_FOUND_FAIL = 10;

    int register(
            String userName,
            String userPassword,
            String firstName,
            String lastName
    );


    int acquireCode(
            String userName
    );


    void update(
            String userName,
            int type,
            String oldPassword,
            String newPassword,
            String authCode
    );

    int login(
            String userName,

            String userPassword

    );

    int updateLoginTime(

    );

    boolean isLogin();
    boolean isDemoLogin();
    void setDemoLogin(boolean b);
    String getUserId();

    void logout();

    int changePassword(String userName, String oldPassword, String newPassword, String code, int type);

    int verificationCode(String userName, String code);


    int phdataList();
    int phdataDelete();

/*





    int orpdataList();





    int orpdataDelete();


    int conducitivitydataList();




    int conducitivitydataDelete();


    int resistivitydataList();




    int resistivitydataDelete();


    int salinitydataList();





    int salinitydataDelete();


    int tdsdataList();





    int tdsdataDelete();


    int checkdataList();




    int checkdataDelete();*/

    int syncUpload();

    int phdataDelete(String id);

    int downloadList();

    int dataDelete(@Nullable String id,int type);

    void setLocalType(String type);

    String getUserName();

    String getLastErrMessage();
}
