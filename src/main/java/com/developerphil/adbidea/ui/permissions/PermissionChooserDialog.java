package com.developerphil.adbidea.ui.permissions;

import com.developerphil.adbidea.model.PermissionState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.util.List;
import java.util.Map;

public class PermissionChooserDialog extends DialogWrapper {

    PermissionChooser permissionChooser;

    private JPanel panel;
    private JPanel permissionChooserWrapper;

    @Nonnull
    public PermissionChooserDialog(Project project, List<PermissionState> permissions) {
        super(project, true);
        setTitle("Change Permissions");

        permissionChooser = new PermissionChooser(project, getOKAction(), permissions);
        permissionChooserWrapper.add(permissionChooser.getPanel());

        init();
    }

    @Override
    protected JComponent createCenterPanel() {
        return panel;
    }

    public List<PermissionState> getPermissionStates() {
        return permissionChooser.getPermissions();
    }
}
