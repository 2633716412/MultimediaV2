package com.example.multimediav2.PowerManager;

import Modules.Paras;

public class PowerManagerFactory {

    public static IPowerManager Get() {
        if (Paras.DEVA40.equals(Paras.devType)) {
            return new PowerManagerA2040(Paras.appContext);
        } else if (Paras.DEVA20.equals(Paras.devType)) {
            return new PowerManagerA2040(Paras.appContext);
        } else if (Paras.DEVA40_XiPin.equals(Paras.devType)) {
            return new PowerManagerA2040_XiPin(Paras.appContext);
        } else if (Paras.DEVA20_XiPin.equals(Paras.devType)) {
            return new PowerManagerA2040_XiPin(Paras.appContext);
        } else if ("hk".equals(Paras.devType)) {
            return new PowerManager_HaiKang(Paras.appContext);
        }
        return new PowerManagerDef();
    }
}
