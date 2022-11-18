package Modules;

import android.content.Context;

public class IniHnalderDef implements IIniHanlder{
    @Override
    public void Ini(final Context context, final Action<String> OnIniEnd) {

        SPUnit spUnit = new SPUnit(context);
        final DeviceData deviceData = spUnit.Get("DeviceData", DeviceData.class);
    }
}
