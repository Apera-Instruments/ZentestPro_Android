package com.zen.api.protocol;

public interface Convent <T>{
    String TAG =Convent.class.getSimpleName();
    T unpack(byte[] data);
    byte[] pack();
    byte getCode();
    String getData();
}
