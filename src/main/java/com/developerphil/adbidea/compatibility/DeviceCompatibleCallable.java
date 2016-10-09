package com.developerphil.adbidea.compatibility;

import com.android.ddmlib.IDevice;

/**
 * Created by Robert on 31.08.2016.
 */
@FunctionalInterface
public interface DeviceCompatibleCallable<T, V extends IDevice> {

    T check(V device);
}
