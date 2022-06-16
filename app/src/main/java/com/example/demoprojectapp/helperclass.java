package com.example.demoprojectapp;

public class helperclass {

    public static String add_million(){
        Long start = System.currentTimeMillis();


        int i =0;


        for(i=1;i<=1000000;i++){
            System.out.println(i);
        }


        long end = System.currentTimeMillis();
        Long diff = end-start;
        return  diff+"";
    }
}

