package com.example.multimediav2.Socket;


import com.example.multimediav2.EnEvent.EventHandlers;

public interface ISocket {

     void Close() throws Exception;

     void Send(byte[] datas)  throws Exception;

     void Send(String str)  throws Exception;

     EventHandlers<byte[]> getRecived();

     EventHandlers getClosed();
}
