package com.developerphil.adbidea.adb.command;

import com.android.ddmlib.IDevice;
import com.developerphil.adbidea.adb.command.receiver.GenericReceiver;
import com.developerphil.adbidea.model.PermissionState;
import com.intellij.openapi.project.Project;
import org.jetbrains.android.facet.AndroidFacet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.developerphil.adbidea.adb.AdbUtil.isAppInstalled;
import static com.developerphil.adbidea.ui.NotificationHelper.error;

public class ListPermissionsCommand implements CommandWithOutput {
    private static final String COMMAND = "dumpsys package %s " +
            "| sed '1,/runtime permissions/d;/User/,$d' " +
            "| sed 's/^ *//g'";

    @Override
    public List<PermissionState> run(Project project, IDevice device, AndroidFacet facet, String packageName) {
        try {
            if (isAppInstalled(device, packageName)) {
                GenericReceiver outputReceiver = new GenericReceiver();
                device.executeShellCommand(String.format(COMMAND, packageName), outputReceiver, 15L, TimeUnit.SECONDS);
                List<String> outputpermissions = outputReceiver.getAdbOutputLines();
                List<PermissionState> outputPermissionList = new ArrayList<PermissionState>(outputpermissions.size());
                for (String line : outputpermissions) {
                    if (line.isEmpty()) {
                        continue;
                    }
                    PermissionState permissionState = new PermissionState();
                    permissionState.setPermissionName(line.substring(0, line.indexOf(':')));
                    permissionState.setGranted(Boolean.valueOf(line.substring(line.indexOf("=")+1)));
                    outputPermissionList.add(permissionState);
                }
                return outputPermissionList;
            } else {
                error(String.format("<b>%s</b> is not installed on %s", packageName, device.getName()));
            }
        } catch (Exception e1) {
            error("Kill fail... " + e1.getMessage());
        }
        return Collections.emptyList();
    }
}
