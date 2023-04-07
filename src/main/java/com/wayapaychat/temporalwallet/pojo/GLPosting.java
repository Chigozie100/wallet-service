package com.wayapaychat.temporalwallet.pojo;

public class GLPosting <T, U>  {

    private final T first;
    private final U second;

    public GLPosting(T first, U second) {
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return first;
    }

    public U getSecond() {
        return second;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
    
}
