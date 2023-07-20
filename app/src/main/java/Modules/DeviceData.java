package Modules;

import java.util.ArrayList;
import java.util.List;

public class DeviceData {
    public Long id=0L;

    public String sn="";

    public Long orgId=0L;

    public String device_name = "";

    public String device_type = "";

    public String device_ip = "";

    public String api_ip="";

    public String api_port="";

    public String mac="";

    public List<OSTime> osTimes = new ArrayList<>();

    public int stream_type=-1;//叫号音频类型：-1媒体音频 0闹钟音频 1通知音频 2通话音频

    public int getStream_type() {
        return stream_type;
    }

    public void setStream_type(int stream_type) {
        this.stream_type = stream_type;
    }

    public List<OSTime> getOsTimes() {
        return osTimes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public void setOsTimes(List<OSTime> osTimes) {
        this.osTimes = osTimes;
    }

    public String getDevice_name() {
        return device_name;
    }

    public void setDevice_name(String device_name) {
        this.device_name = device_name;
    }

    public String getDevice_type() {
        return device_type;
    }

    public void setDevice_type(String device_type) {
        this.device_type = device_type;
    }

    public String getDevice_ip() {
        return device_ip;
    }

    public void setDevice_ip(String device_ip) {
        this.device_ip = device_ip;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getApi_ip() {
        return api_ip;
    }

    public void setApi_ip(String api_ip) {
        this.api_ip = api_ip;
    }

    public String getApi_port() {
        return api_port;
    }

    public void setApi_port(String api_port) {
        this.api_port = api_port;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    @Override
    public String toString() {
        return "{"
                + "\"id\":"
                + id
                + ",\"device_name\":\""
                + device_name + '\"'
                + ",\"device_type\":\""
                + device_type + '\"'
                + ",\"device_ip\":\""
                + device_ip + '\"'
                + ",\"device_port\":\""
                + ",\"mac\":\""
                + mac + '\"'
                + ",\"osTimes\":"
                + osTimes
                + "}";

    }
}
