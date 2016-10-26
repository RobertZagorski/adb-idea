package com.developerphil.adbidea.adb.command;

import com.android.ddmlib.IDevice;
import com.developerphil.adbidea.adb.command.receiver.GenericReceiver;
import com.developerphil.adbidea.model.PermissionState;
import com.intellij.openapi.project.Project;
import org.jetbrains.android.facet.AndroidFacet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.developerphil.adbidea.adb.AdbUtil.isAppInstalled;
import static com.developerphil.adbidea.ui.NotificationHelper.error;

public class ListPermissionsCommand implements Command<List<PermissionState>, Void> {
    private static final String COMMAND = "dumpsys package %s " +
            "| sed -n '/requested permissions/,$p' " +
            "| sed 's/^ *//g'";

    @Override
    public List<PermissionState> run(Project project, IDevice device, AndroidFacet facet, String packageName, Void... parameters) {
        try {
            if (isAppInstalled(device, packageName)) {
                GenericReceiver outputReceiver = new GenericReceiver();
                device.executeShellCommand(String.format(COMMAND, packageName), outputReceiver, 15L, TimeUnit.SECONDS);
                List<String> outputLines = outputReceiver.getAdbOutputLines();
                List<PermissionState> outputPermissionList = new ArrayList<PermissionState>(outputLines.size());
                if (outputLines.get(0).startsWith("requested permissions")) {
                    outputLines.remove(0);
                }
                for (Iterator<String> iterator = outputLines.iterator() ; iterator.hasNext() ; ) {
                    String line = iterator.next();
                    if (!line.contains("install permissions")) {
                        PermissionState state = new PermissionState();
                        state.setPermissionName(line);
                        outputPermissionList.add(state);
                        iterator.remove();
                    } else {
                        iterator.remove();
                        break;
                    }
                }
                for (Iterator<String> iterator = outputLines.iterator() ; iterator.hasNext() ; ) {
                    String line = iterator.next();
                    if (!line.startsWith("User")) {
                        for (Iterator<PermissionState> permissionStateIterator
                             = outputPermissionList.iterator() ;
                             permissionStateIterator.hasNext() ;) {
                            PermissionState permissionState = permissionStateIterator.next();
                            if (permissionState.getPermissionName().equals(line.substring(0, line.indexOf(':')))) {
                                permissionStateIterator.remove();
                                iterator.remove();
                                break;
                            }
                        }
                    } else {
                        iterator.remove();
                        break;
                    }
                }
                if (outputLines.get(0).startsWith("gids")) {
                    outputLines.remove(0);
                }
                if (outputLines.get(0).startsWith("runtime permissions")) {
                    outputLines.remove(0);
                }
                for (String line : outputLines) {
                    if (line.equals("")) {
                        continue;
                    }
                    for (Iterator<PermissionState> permissionStateIterator
                         = outputPermissionList.iterator() ;
                         permissionStateIterator.hasNext() ;) {
                        PermissionState permissionState = permissionStateIterator.next();
                        if (permissionState.getPermissionName().equals(line.substring(0, line.indexOf(':')))) {
                            permissionState.setGranted(Boolean.valueOf(line.substring(line.indexOf("=") + 1)));
                            break;
                        }
                    }
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
