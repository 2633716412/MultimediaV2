package com.example.multimediav2.EnEvent;

public class EventInvoker {

    static public <T> void Invoke(EventHandlers<T> handlers, T data) {
        for (IEventHandler<T> eh : handlers.handlers) {
            Event<T> event = new Event<>(data);
            eh.Handle(event);
            handlers.Invoked=true;
        }
    }

    static public <T> void Invoke(EventHandlers<T> handlers) {
        for (IEventHandler<T> eh : handlers.handlers) {
            Event event = new Event();
            eh.Handle(event);
            handlers.Invoked=true;
        }
    }

}