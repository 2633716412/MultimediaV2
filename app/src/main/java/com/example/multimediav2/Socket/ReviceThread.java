package com.example.multimediav2.Socket;

import com.example.multimediav2.EnEvent.EventHandlers;
import com.example.multimediav2.EnEvent.EventInvoker;

import java.io.InputStream;
import java.net.Socket;

import Modules.LogHelper;

class ReviceThread extends Thread {

    private InputStream inputStream;
    private Socket socket;

    public ReviceThread(Socket socket) throws Exception {
        this.socket = socket;
        this.inputStream = socket.getInputStream();
    }

    public ReviceThread(Socket socket,IIsDisconnected isDisconnected) throws Exception {
        this(socket);
        this.isDisconnected=isDisconnected;
    }

    IIsDisconnected isDisconnected=new IIsDisconnected() {

        private static final long sleepTime = 60 * 1000;

        private long count=0;

        @Override
        public boolean IsDisconnected() {

            count++;

            if (count * 10 > sleepTime) {
                try {
                    socket.sendUrgentData(0xFF);
                    count = 0;
                } catch (Exception ex) {
                    LogHelper.Debug("ReviceThread socket被关闭");
                    return true;
                }
            }
            return false;
        }
    };

    private boolean _stop = false;

    public void Stop() {
        _stop = true;
    }

    public final EventHandlers<byte[]> Recived = new EventHandlers<>();

    public final EventHandlers Closed = new EventHandlers();

    @Override
    public void run() {

        while (!_stop) {
            try {

                if (isDisconnected.IsDisconnected()) {
                    EventInvoker.Invoke(Closed);
                    Recived.Clear();
                    Closed.Clear();
                    break;
                }

                if (inputStream.available() > 0) {
                    while (inputStream.available() > 0) {
                        int len;
                        byte[] bytes = new byte[4096];
                        len = inputStream.read(bytes);
                        if (len > 0) {
                            byte[] temp = new byte[len];
                            System.arraycopy(bytes, 0, temp, 0, len);
                            try {
                                EventInvoker.Invoke(Recived, temp);
                            } catch (Exception ex) {
                                LogHelper.Error(ex);
                            }
                        }
                    }
                } else {
                    Thread.sleep(10);
                }

            } catch (Exception ex) {
                LogHelper.Error(ex);
            }
        }
    }

}
