package com.example.multimediav2.EnEvent;

public interface IEventHandler<T> {
    void Handle(Event<T> event);
}
