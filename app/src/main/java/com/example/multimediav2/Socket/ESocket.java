package com.example.multimediav2.Socket;

import com.example.multimediav2.EnEvent.Event;
import com.example.multimediav2.EnEvent.EventHandlers;
import com.example.multimediav2.EnEvent.EventInvoker;
import com.example.multimediav2.EnEvent.IEventHandler;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import Modules.LogHelper;

public class ESocket implements ISocket {

    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private ReviceThread reviceThread;

    protected final EventHandlers Closed = new EventHandlers();

    @Override
    public EventHandlers getClosed() {
        return Closed;
    }

    protected final EventHandlers<byte[]> Recived = new EventHandlers<>();

    @Override
    public EventHandlers<byte[]> getRecived() {
        return Recived;
    }

    public ESocket(Socket socket) throws Exception {
        this.socket = socket;

        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
        reviceThread = new ReviceThread(socket);
        reviceThread.Closed.Add(new IEventHandler() {
            @Override
            public void Handle(Event event) {
                reviceThread.Stop();
                Close();
                EventInvoker.Invoke(Closed);
                Recived.Clear();
                Closed.Clear();
            }
        });
        reviceThread.Recived.Add(new IEventHandler<byte[]>() {
            @Override
            public void Handle(Event<byte[]> event) {
                EventInvoker.Invoke(Recived, event.getData());
            }
        });
        reviceThread.start();
    }

    public ESocket(Socket socket,IIsDisconnected isDisconnected) throws Exception {
        this.socket = socket;

        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
        reviceThread = new ReviceThread(socket,isDisconnected);
        reviceThread.Closed.Add(new IEventHandler() {
            @Override
            public void Handle(Event event) {
                reviceThread.Stop();
                Close();
                EventInvoker.Invoke(Closed);
                Recived.Clear();
                Closed.Clear();
            }
        });
        reviceThread.Recived.Add(new IEventHandler<byte[]>() {
            @Override
            public void Handle(Event<byte[]> event) {
                EventInvoker.Invoke(Recived, event.getData());
            }
        });
        reviceThread.start();
    }

    public void Send(byte[] datas) throws Exception {
        outputStream.write(datas);
        outputStream.flush();
    }

    public void Send(String str) throws Exception {
        LogHelper.Debug(str, "发送");
        Send(str.getBytes());
    }

    public void Close() {

        reviceThread.Stop();

        try {
            outputStream.close();
        } catch (Exception ex) {
            LogHelper.Error(ex);
        }

        try {
            inputStream.close();
        } catch (Exception ex) {
            LogHelper.Error(ex);
        }

        if (!socket.isClosed()) {
            try {
                socket.close();
            } catch (Exception ex) {
                LogHelper.Error(ex);
            }
        }
    }

}
