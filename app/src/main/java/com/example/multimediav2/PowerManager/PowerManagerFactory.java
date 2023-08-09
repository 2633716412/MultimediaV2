package com.example.multimediav2.PowerManager;

import Modules.Paras;

public class PowerManagerFactory {

    public static IPowerManager Get() {
        if (Paras.DEVA40_XiPin.equals(Paras.devType)) {
            return new PowerManagerA2040_XiPin(Paras.appContext);
        } else if (Paras.HAI_KANG.equals(Paras.devType)) {
            return new PowerManager_HaiKang(Paras.appContext);
        }else if (Paras.HAI_KANG_6055.equals(Paras.devType)) {
            return new PowerManager_HK6055(Paras.appContext);
        } else if(Paras.DEVA20_XiPinBox.equals(Paras.devType)) {
            return new PowerManagerA2040_XiPinBox(Paras.appContext);
        } else if(Paras.HAI_KANG_RK3128.equals(Paras.devType)) {
            return new PowerManager_HKRK3128(Paras.appContext);
        }
        return new PowerManagerDef();
    }
}
