package com.example.multimediav2.SystemTimeSetter;

import Modules.Paras;

public class SystemTimeSetterFactory {

    static public ISystemTimeSetter Get() {
        if (Paras.DEVELOPMODE) {
            return new SystemTimeSetterFake();
        }

        return new SystemTimeSetterDef();
    }
}
