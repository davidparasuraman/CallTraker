package com.david.calltraker;

import android.telephony.PhoneStateListener;

public interface MultiSimTelephonyManager {
    public void listen(PhoneStateListener listener, int events);
}
