package com.example.multimediav2.EnEvent;

import java.util.ArrayList;

public class EventHandlers<T> {

    public boolean Invoked=false;

    public void Add(IEventHandler<T> eventHandler)
    {
        handlers.add(eventHandler);
    }

    public void Remove(IEventHandler<T> eventHandler)
    {
        handlers.remove(eventHandler);
    }

    final ArrayList<IEventHandler<T>> handlers = new ArrayList<>();

    public void Clear() { handlers.clear(); }

}
