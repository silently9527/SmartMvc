package com.silently9527.springmvcdemo;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.LongStream;

public class SumRecursiveTask extends RecursiveTask<Long> {

    private final long[] numbers;
    private final int start;
    private final int end;

    public SumRecursiveTask(long[] numbers) {
        this.numbers = numbers;
        this.start = 0;
        this.end = numbers.length;
    }

    public SumRecursiveTask(long[] numbers, int start, int end) {
        this.numbers = numbers;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Long compute() {
        int length = end - start;
        if (length < 10000) { //小于1000个就不在进行拆分
            return sum();
        }
        SumRecursiveTask leftTask = new SumRecursiveTask(numbers, start, start + length / 2);
        SumRecursiveTask rightTask = new SumRecursiveTask(numbers, start + (length / 2), end);
        leftTask.fork();
        rightTask.fork();
        return leftTask.join() + rightTask.join();
    }


    private long sum() {
        long sum = 0;
        for (int i = start; i < end; i++) {
            sum += numbers[i];
        }
        return sum;
    }


    public static void main(String[] args) {
        long[] numbers = LongStream.rangeClosed(1, 100000000).toArray();

        long start = System.currentTimeMillis();
        Long result = new ForkJoinPool().invoke(new SumRecursiveTask(numbers));
        long end = System.currentTimeMillis();
        System.out.println("花费时间：" + (end - start));
        System.out.println("result：" +result);

    }

}
