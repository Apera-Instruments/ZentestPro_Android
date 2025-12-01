package com.zen.ui.utils;


public class AssectListview {
    private  String name;
    private  int ImageID;

    public  AssectListview(String name, int ImageID){
        this.name = name;
        this.ImageID = ImageID;
    }


    public String getName(){
        return name;
    }

    public int getImageID(){
        return ImageID;
    }
}
