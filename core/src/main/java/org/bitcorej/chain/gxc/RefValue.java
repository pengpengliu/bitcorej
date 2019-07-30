package org.bitcorej.chain.gxc;

public class RefValue<T> {
    public T data;

    public  RefValue(){
        data = null;
    }

    public  RefValue(T initialVal ){
        data = initialVal;
    }
}