package com.zen.ui.utils;

public class AssectSaveData {
    private  String name;
    private  String Images;
    private  String Dataid;
    private  String nameid;

    public  AssectSaveData(String name, String Images,String dataid,String nameid){
        this.name = name;
        this.Images = Images;
        this.Dataid = dataid;
        this.nameid = nameid;
    }


    public String getName(){
        return name;
    }

    public String  getImageID(){
        return Images;
    }

    public  String getDataid(){
        return Dataid;
    }

    public String getNameid() {
        return nameid;
    }

}
