package com.example.multimediav2.EnEvent;

public class Event<T>  {

    public T getData() {
        return data;
    }

    private T data;

    public Event() {
    }

    public Event(T data) {
        this.data=data;
    }


}
