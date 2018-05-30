package com.example.gxw.indoorlocation;

/**
 * Created by GXW on 2018/4/16.
 */

public class PathPoint {           //使用此类完成点到坐标的对应

    static public int Xcoord;
    static public int Ycoord;


//            0    2 1           点对应相关坐标
//            1    6 1
//            2    9 1
//            3    2 5
//            4    6 5
//            5    9 5
//            6    2 9
//            7    6 9
//            8    9 9

    static public void getCoord(int i){
        if(i==0)
        {
            Xcoord=2;
            Ycoord=1;
        }
        if(i==1)
        {
            Xcoord=6;
            Ycoord=1;
        }
        if(i==2)
        {
            Xcoord=9;
            Ycoord=1;
        }
        if(i==3)
        {
            Xcoord=2;
            Ycoord=5;
        }
        if(i==4)
        {
            Xcoord=6;
            Ycoord=5;
        }
        if(i==5)
        {
            Xcoord=9;
            Ycoord=5;
        }
        if(i==6)
        {
            Xcoord=2;
            Ycoord=9;
        }
        if(i==7)
        {
            Xcoord=6;
            Ycoord=9;
        }
        if(i==8)
        {
            Xcoord=9;
            Ycoord=9;
        }
    }
}
