package com.developerphil.adbidea.adb.command;

import com.android.ddmlib.IDevice;
import com.developerphil.adbidea.adb.command.receiver.GenericReceiver;
import com.developerphil.adbidea.model.PermissionState;
import com.intellij.openapi.project.Project;
import org.jetbrains.android.facet.AndroidFacet;

import java.util.concurrent.TimeUnit;

import static com.developerphil.adbidea.adb.AdbUtil.isAppInstalled;
import static com.developerphil.adbidea.ui.NotificationHelper.error;
import static com.developerphil.adbidea.ui.NotificationHelper.info;

public class GrantRevokePermissionsCommand implements CommandWithParameter<PermissionState> {
    private static final String COMMAND = "pm %s %s %s ";
    private static final String GRANT = "grant";
    private static final String REVOKE = "revoke";

    @Override
    public boolean run(Project project, IDevice device, AndroidFacet facet, String packageName, PermissionState permission) {
        try {
            if (!isAppInstalled(device, packageName)) {
                error(String.format("<b>%s</b> is not installed on %s", packageName, device.getName()));
                return false;
            }
            device.executeShellCommand(
                    String.format(COMMAND, permission.isGranted() ? GRANT : REVOKE, packageName, permission.getPermissionName()),
                    new GenericReceiver(), 15L, TimeUnit.SECONDS);
            info(String.format("Permission <b>%s</b> "
                    + (permission.isGranted() ? "granted" : "revoked") + " for <b>%s</b> on %s",
                    permission.getPermissionName(),
                    packageName,
                    device.getName()));
            return true;
        } catch (Exception e1) {
            error("Kill fail... " + e1.getMessage());
        }
        return false;
    }
}
