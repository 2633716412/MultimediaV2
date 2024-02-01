package com.example.multimediav2.Socket;

import com.example.multimediav2.HttpUnit.HttpUnitFactory;

import Modules.DeviceData;
import Modules.LogHelper;
import Modules.Paras;
import Modules.SPUnit;

public class PollingService implements Runnable {
    @Override
    public void run() {
        final SPUnit spUnit = new SPUnit(Paras.appContext);
        final DeviceData deviceData = spUnit.Get("DeviceData", DeviceData.class);
        if(deviceData.getId()>0) {
            try {
                String jsonStr= HttpUnitFactory.Get().Get(Paras.mulAPIAddr + "/media/third/getCmd");

            } catch (Exception e) {
                LogHelper.Error("PollingService异常："+e.toString());
            }
        }

    }
}
