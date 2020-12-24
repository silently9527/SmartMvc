package com.silently9527.springmvcdemo;

import java.util.Spliterator;
import java.util.function.Consumer;

public class CustomizedSpliterator implements Spliterator<Long> {
    private long from;
    private final long upTo;
    private int last;

    CustomizedSpliterator(long from, long upTo, boolean closed) {
        this(from, upTo, closed ? 1 : 0);
    }

    private CustomizedSpliterator(long from, long upTo, int last) {
        assert upTo - from + last > 0;
        this.from = from;
        this.upTo = upTo;
        this.last = last;
    }

    @Override
    public boolean tryAdvance(Consumer<? super Long> consumer) {
        final long i = from;
        if (i < upTo) {
            from++;
            consumer.accept(i);
            return true;
        }
        else if (last > 0) {
            last = 0;
            consumer.accept(i);
            return true;
        }
        return false;
    }


    @Override
    public long estimateSize() {
        return upTo - from + last;
    }

    @Override
    public int characteristics() {
        return  Spliterator.SIZED ;
    }

    @Override
    public Spliterator<Long> trySplit() {
        long size = estimateSize();
        return size <= 20000 ? null : new CustomizedSpliterator(from, from = from +size / 2, 0);
    }


}
