package com.example.demoprojectapp;

import org.json.JSONException;
import org.json.JSONObject;

public class helperclass {

    public static int add_million(){
        Long start = System.currentTimeMillis();


        int i =0;


        for(i=1;i<=1000000;i++){
            System.out.println(i);
        }

        return  i;
    }
    static int fib(int n)
    {
        if (n <= 1)
            return n;
        return fib(n-1) + fib(n-2);
    }
    //function2

    static JSONObject function_metric_add_million() throws JSONException{
        System.out.println("started");
        Long start = System.currentTimeMillis();

        //-----------wrapped function---------//

        int x = add_million();

        //-------------------------------------//

        System.out.println("Ended");
        long end = System.currentTimeMillis();
        long diff = end-start;

        JSONObject json = new JSONObject();
        json.put("output",x+"");
        json.put("computation_time",diff+"");
        json.put("function",2);
        json.put("device","local");
        return json;
    }

    //function value 1
    static JSONObject function_metric(int n) throws JSONException {
        System.out.println("started");
        Long start = System.currentTimeMillis();

        //-----------wrapped function---------//

        int x = fib(n);

        //-------------------------------------//

        System.out.println("Ended");
        long end = System.currentTimeMillis();
        long diff = end-start;

        JSONObject json = new JSONObject();
        json.put("output",x+"");
        json.put("computation_time",diff+"");
        json.put("function",1);
        json.put("device","local");
        return json;
    }
}

