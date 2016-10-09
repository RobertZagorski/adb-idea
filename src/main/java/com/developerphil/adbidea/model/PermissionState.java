package com.developerphil.adbidea.model;

/**
 * Created by Robert on 06.10.2016.
 */
public class PermissionState {
    private String permissionName;
    private boolean granted;

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public boolean isGranted() {
        return granted;
    }

    public void setGranted(boolean granted) {
        this.granted = granted;
    }
}
