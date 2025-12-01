package com.zen.biz.utils;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;
import android.util.Log;

import com.orhanobut.logger.Logger;

public class AndroidDes3Util {
    private static final boolean DEBUG =false;
    // 密钥 长度不得小于24
    //private final static String secretKey = "123456789012345678901234" ;
    // 向量 可有可无 终端后台也要约定
    private final static String iv = "01234567" ;
    // 加解密统一使用的编码方式
    private final static String encoding = "utf-8" ;

    /**
     * 3DES加密
     *
     * @param plainText
     *            普通文本
     * @return
     * @throws Exception
     */
    public static String encode(String secretKey, String iv, String plainText) {
        if(plainText ==null || plainText.length()==0 || secretKey==null) return null;
        Key deskey = null;
        DESedeKeySpec spec = null;
        try {

            byte key[] = new byte[24];
            byte input[] = secretKey.getBytes();
            System.arraycopy(input,0,key,0,Math.min(key.length,input.length));
            spec = new DESedeKeySpec(key);

            SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("desede");
            deskey = keyfactory.generateSecret(spec);

            Cipher cipher = Cipher.getInstance("desede/CBC/PKCS5Padding");
            IvParameterSpec ips = new IvParameterSpec(iv.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, deskey, ips);
            byte[] encryptData = cipher.doFinal(plainText.getBytes(encoding));
            String out =okio.ByteString.of(encryptData).base64(); //Base64.encodeToString(encryptData, Base64.DEFAULT);
//            if(DEBUG){
//                Log.d("DEBUG","encode "+out);
////                Log.d("DEBUG","decode "+decode(out,secretKey));
//            }
            return  out;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

//    public static String encode(String plainText, String secretKey) {
//        if(plainText ==null || plainText.length()==0 || secretKey==null) return null;
//        Key deskey = null;
//        DESedeKeySpec spec = null;
//        try {
//            byte key[] = new byte[24];
//            byte input[] = secretKey.getBytes();
//            System.arraycopy(input,0,key,0,Math.min(key.length,input.length));
//            spec = new DESedeKeySpec(key);
//
//            SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("desede");
//            deskey = keyfactory.generateSecret(spec);
//
//            Cipher cipher = Cipher.getInstance("desede/CBC/PKCS5Padding");
//            IvParameterSpec ips = new IvParameterSpec(iv.getBytes());
//            cipher.init(Cipher.ENCRYPT_MODE, deskey, ips);
//            byte[] encryptData = cipher.doFinal(plainText.getBytes(encoding));
//            String out =okio.ByteString.of(encryptData).base64(); //Base64.encodeToString(encryptData, Base64.DEFAULT);
//            if(DEBUG){
//                Log.d("DEBUG","encode "+out);
//                Log.d("DEBUG","decode "+decode(out,secretKey));
//
//            }
//            return  out;
//        } catch (InvalidKeyException e) {
//            e.printStackTrace();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (InvalidAlgorithmParameterException e) {
//            e.printStackTrace();
//        } catch (NoSuchPaddingException e) {
//            e.printStackTrace();
//        } catch (BadPaddingException e) {
//            e.printStackTrace();
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        } catch (InvalidKeySpecException e) {
//            e.printStackTrace();
//        } catch (IllegalBlockSizeException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    /**
     * 3DES解密
     *
     *
     * @return
     * @throws Exception
     */

    public static String decode(String secretKey, String iv, String encryptText) {
        if(encryptText ==null || encryptText.length()==0 || secretKey==null) return null;
        byte key[] = new byte[24];
        byte input[] = secretKey.getBytes();
        System.arraycopy(input,0,key,0,Math.min(key.length,input.length));
        Key deskey = null ;
        DESedeKeySpec spec = null;
        try {
            spec = new DESedeKeySpec( key);
            SecretKeyFactory keyfactory = SecretKeyFactory.getInstance( "desede" );
            deskey = keyfactory. generateSecret(spec);
            Cipher cipher = Cipher.getInstance( "desede/CBC/PKCS5Padding" );
            IvParameterSpec ips = new IvParameterSpec( iv.getBytes());
            cipher. init(Cipher. DECRYPT_MODE, deskey, ips);

            byte [] decryptData = cipher.doFinal(Base64. decode(encryptText, Base64. DEFAULT));

            return new String (decryptData, encoding);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

//    public static String decode(String encryptText,String secretKey)  {
//        if(encryptText ==null || encryptText.length()==0 || secretKey==null) return null;
//        byte key[] = new byte[24];
//        byte input[] = secretKey.getBytes();
//        System.arraycopy(input,0,key,0,Math.min(key.length,input.length));
//        Key deskey = null ;
//        DESedeKeySpec spec = null;
//        try {
//            spec = new DESedeKeySpec( key);
//            SecretKeyFactory keyfactory = SecretKeyFactory.getInstance( "desede" );
//            deskey = keyfactory. generateSecret(spec);
//            Cipher cipher = Cipher.getInstance( "desede/CBC/PKCS5Padding" );
//            IvParameterSpec ips = new IvParameterSpec( iv.getBytes());
//            cipher. init(Cipher. DECRYPT_MODE, deskey, ips);
//
//            byte [] decryptData = cipher.doFinal(Base64. decode(encryptText, Base64. DEFAULT));
//
//            return new String (decryptData, encoding);
//        } catch (InvalidKeyException e) {
//            e.printStackTrace();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (InvalidAlgorithmParameterException e) {
//            e.printStackTrace();
//        } catch (NoSuchPaddingException e) {
//            e.printStackTrace();
//        } catch (BadPaddingException e) {
//            e.printStackTrace();
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        } catch (InvalidKeySpecException e) {
//            e.printStackTrace();
//        } catch (IllegalBlockSizeException e) {
//            e.printStackTrace();
//        }
//        return null;
//
//    }
}
