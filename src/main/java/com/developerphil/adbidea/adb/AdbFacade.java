package com.developerphil.adbidea.adb;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.tools.idea.model.DeclaredPermissionsLookup;
import com.android.tools.lint.checks.PermissionHolder;
import com.developerphil.adbidea.adb.command.*;
import com.developerphil.adbidea.compatibility.DeviceCompatibleCallable;
import com.developerphil.adbidea.model.PermissionState;
import com.developerphil.adbidea.ui.ModuleChooserDialogHelper;
import com.developerphil.adbidea.ui.device.DeviceChooserDialog;
import com.developerphil.adbidea.ui.permissions.PermissionChooserDialog;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.android.dom.manifest.UsesPermission;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.sdk.AndroidSdkUtils;
import org.jetbrains.android.util.AndroidUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static com.developerphil.adbidea.ui.NotificationHelper.error;

public class AdbFacade {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("AdbIdea-%d").build());

    public static void uninstall(Project project) {
        executeOnDevice(project, new UninstallCommand());
    }

    public static void kill(Project project) {
        executeOnDevice(project, new KillCommand());
    }

    public static void startDefaultActivity(Project project) {
        executeOnDevice(project, new StartDefaultActivityCommand());
    }

    public static void restartDefaultActivity(Project project) {
        executeOnDevice(project, new RestartPackageCommand());
    }

    public static void clearData(Project project) {
        executeOnDevice(project, new ClearDataCommand());
    }

    public static void clearDataAndRestart(Project project) {
        executeOnDevice(project, new ClearDataAndRestartCommand());
    }

    public static void grantRevokePermissions(Project project) {
        executeOnCompatibleDevice(project, new GrantRevokePermissionsCommand(), 23);
    }

    private static void executeOnDevice(Project project, Command runnable) {
        final DeviceResult result = getDevice(project, null);
        if (result == null) {
            error("No Device found");
            return;
        }
        for (IDevice device : result.devices) {
            runCommand(runnable, project, device, result, null);
        }
    }

    private static void executeOnCompatibleDevice(final Project project, final Command<Boolean, PermissionState> runnable, int apiVersion) {
        DeviceCompatibleCallable<Boolean, IDevice> isDeviceCompatible = device -> {
            if (AdbUtil.getApiVersion(device) >= apiVersion) {
                return true;
            } else {
                return false;
            }
        };
        final DeviceResult result = getDevice(project, isDeviceCompatible);
        if (result == null) {
            error("No Device found");
            return;
        }
        List<PermissionState> permissions = getApplicationPermissions(project, result);

        final PermissionChooserDialog chooser = new PermissionChooserDialog(project, permissions);
        chooser.show();

        if (chooser.getExitCode() != DialogWrapper.OK_EXIT_CODE) {
            return;
        }
        List<PermissionState> chosenPermissions = chooser.getPermissionStates();
        for (IDevice device : result.devices) {
            for (PermissionState permission : chosenPermissions) {
                runCommand(runnable, project, device, result, permission);
            }
        }
    }

    private static <T, V> T runCommand(final Command<T, V> runnable, final Project project,
                                                       final IDevice device, final DeviceResult result, V... parameter) {
        Future<T> future = EXECUTOR.submit(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return runnable.run(project, device, result.facet, result.packageName, parameter);
            }
        });
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static DeviceResult getDevice(Project project,
                                          DeviceCompatibleCallable<Boolean, IDevice> isDeviceCompatible) {
        AndroidFacet facet = getChosenFacet(project);
        if (facet == null) {
            return null;
        }
        String packageName = AdbUtil.computePackageName(facet);

        AndroidDebugBridge bridge = AndroidSdkUtils.getDebugBridge(project);
        if (bridge == null) {
            error("No platform configured");
            return null;
        }

        if (!bridge.isConnected() || !bridge.hasInitialDeviceList()) {
            return null;
        }
        IDevice[] devices = bridge.getDevices();

        if (isDeviceCompatible != null) {
            List<IDevice> deviceList = new ArrayList<>(devices.length);
            for (IDevice device : devices) {
                if (isDeviceCompatible.check(device)) {
                    deviceList.add(device);
                }
            }
            devices = new IDevice[deviceList.size()];
            deviceList.toArray(devices);
        }

        if (devices.length == 1) {
            return new DeviceResult(devices, facet, packageName);
        } else if (devices.length > 1) {
            return askUserForDevice(facet, packageName);
        } else {
            return null;
        }
    }

    private static List<PermissionState> getApplicationPermissions(Project project, DeviceResult deviceResult) {
        AndroidFacet facet = getChosenFacet(project);
        if (facet == null) {
            return null;
        }
        String packageName = AdbUtil.computePackageName(facet);

        AndroidDebugBridge bridge = AndroidSdkUtils.getDebugBridge(project);
        if (bridge == null) {
            error("No platform configured");
            return null;
        }
        if (!bridge.isConnected() || !bridge.hasInitialDeviceList()) {
            return null;
        }
        return new ListPermissionsCommand()
                .run(project, deviceResult.devices[0], facet, packageName);
        //return getApplicationPermissions(facet);
    }

    private static List<PermissionState> getApplicationPermissions(AndroidFacet facet) {
        List<PermissionState> permissionStateList = new ArrayList<>();
        for (UsesPermission usesPermission : facet.getManifest().getUsesPermissions()) {
            DeclaredPermissionsLookup lookup = DeclaredPermissionsLookup.getInstance(facet.getModule().getProject());
            lookup.reset();
            PermissionHolder holder = lookup.getPermissionHolder(facet.getModule());
            if (!holder.isRevocable(usesPermission.getName().toString())) {
                continue;
            }
            PermissionState permissionState = new PermissionState();
            permissionState.setPermissionName(usesPermission.getName().toString());
            permissionState.setGranted(true);
            permissionStateList.add(permissionState);
        }
        return permissionStateList;
    }

    private static AndroidFacet getChosenFacet(Project project) {
        List<AndroidFacet> facets = getApplicationFacets(project);
        if (facets.isEmpty()) {
            return null;
        }
        AndroidFacet facet;
        if (facets.size() > 1) {
            facet = ModuleChooserDialogHelper.showDialogForFacets(project, facets);
        } else {
            facet = facets.get(0);
        }
        return facet;
    }

    private static List<AndroidFacet> getApplicationFacets(Project project) {

        List<AndroidFacet> facets = Lists.newArrayList();
        for (AndroidFacet facet : AndroidUtils.getApplicationFacets(project)) {
            if (!isTestProject(facet)) {
                facets.add(facet);
            }
        }

        return facets;
    }

    private static boolean isTestProject(AndroidFacet facet) {
        return facet.getManifest() != null
                && facet.getManifest().getInstrumentations() != null
                && !facet.getManifest().getInstrumentations().isEmpty();
    }

    private static DeviceResult askUserForDevice(AndroidFacet facet, String packageName) {
        final DeviceChooserDialog chooser = new DeviceChooserDialog(facet);
        chooser.show();

        if (chooser.getExitCode() != DialogWrapper.OK_EXIT_CODE) {
            return null;
        }

        IDevice[] selectedDevices = chooser.getSelectedDevices();
        if (selectedDevices.length == 0) {
            return null;
        }

        return new DeviceResult(selectedDevices, facet, packageName);
    }

    private static final class DeviceResult {
        private final IDevice[] devices;
        private final AndroidFacet facet;
        private final String packageName;

        private DeviceResult(IDevice[] devices, AndroidFacet facet, String packageName) {
            this.devices = devices;
            this.facet = facet;
            this.packageName = packageName;
        }
    }

}
