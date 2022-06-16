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
    static int fib(int n)
    {
        if (n <= 1)
            return n;
        return fib(n-1) + fib(n-2);
    }

    static String format(int n)
    {
        System.out.println("started");
        Long start = System.currentTimeMillis();

        int x = fib(n);

        System.out.println("Ended");
        long end = System.currentTimeMillis();
        long diff = end-start;

        return diff+"";


    }
}

