package com.example.multimediav2;

import com.example.multimediav2.Utils.NetWorkUtils;

import org.junit.Test;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.TimerTask;

import Modules.EDate;
import Modules.LogHelper;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {

        /*EDate now = EDate.Now();
        EDate secondDay=now.AddDays(1);
        String yearStr =now.ToString();
        int year = Integer.parseInt(yearStr.substring(0,4));
        System.out.println(secondDay.Year()+"-"+secondDay.Month()+"-"+secondDay.Day());
        System.out.println(year+"-"+secondDay.Month()+"-"+secondDay.Day());*/
        EDate now = EDate.Now();
        now=now.AddDays(1);
        EDate begin = new EDate(now.Year(), now.Month(), now.Day(), now.date.getHours(), now.date.getMinutes(), 0);
        System.out.println(begin.ToString());
        /*try {
            String gbIp= NetWorkUtils.GetGBIp();
            System.out.println("发送地址: " + gbIp);
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);
            String message = "你的广播消息";
            byte[] sendData = message.getBytes();
            DatagramPacket packet = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(gbIp), 8888);
            socket.send(packet);
            System.out.println("发送成功");

            socket.close();
        } catch (Exception e) {
            LogHelper.Error("组播发送异常："+e.toString());
        }*/
    }
    public static String filterSpecialChars(String str) {
        if (str == null || str.trim().isEmpty()) {
            return str;
        }
        String pattern = "[^a-zA-Z0-9\\u4E00-\\u9FA5\\s\\[\\]\\{\\}\\(\\),\\\"'?=_:./\\\\-]"; // 只允许字母、数字和中文
        return str.replaceAll(pattern, "");
    }

    static class SenderTask extends TimerTask {
            @Override
            public void run() {
                try {
                    String gbIp= NetWorkUtils.GetGBIp();
                    System.out.println("发送地址: " + gbIp);
                    DatagramSocket socket = new DatagramSocket();
                    socket.setBroadcast(true);
                    String message = "你的广播消息";
                    byte[] sendData = message.getBytes();
                    DatagramPacket packet = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(gbIp), 8888);
                    socket.send(packet);
                    System.out.println("发送成功");
                    socket.close();
                } catch (Exception e) {
                    LogHelper.Error("组播发送异常："+e.toString());
                }
            }
    }
}
