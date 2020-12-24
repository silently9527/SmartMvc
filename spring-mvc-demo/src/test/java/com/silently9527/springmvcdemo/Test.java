package com.silently9527.springmvcdemo;

import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

public class Test {


    public static void main(String[] args) {


        Summer summer = new Summer();
        LongStream.rangeClosed(1, 100000000)
                .parallel()
                .forEach(summer::add);

//        long result = StreamSupport.longStream(new CustomizedSpliterator(1, 100000000, true), true)
//                .reduce(0, Long::sum);

//        long start = System.currentTimeMillis();

//        long result = 0;
//        for (int i = 1; i <= 100000000; i++) {
//            result += i;
//        }
//        long result = LongStream.rangeClosed(1, 100000000)
//                .parallel()
//                .reduce(0, Long::sum);

//        long result = StreamSupport.stream(new CustomizedSpliterator(1, 100000000, true), true)
//                .reduce(0L, Long::sum);

//        long end = System.currentTimeMillis();
//        System.out.println("花费时间：" + (end - start));
        System.out.println("result：" + summer.sum);


    }


    static class Summer {
        public long sum = 0;

        public void add(long value) {
            sum += value;
        }
    }

}
