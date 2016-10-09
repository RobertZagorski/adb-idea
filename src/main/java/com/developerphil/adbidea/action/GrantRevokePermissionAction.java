package com.developerphil.adbidea.action;

import com.developerphil.adbidea.adb.AdbFacade;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

/**
 * Created by Robert on 22.08.2016.
 */
public class GrantRevokePermissionAction extends AdbAction {

    @Override
    public void actionPerformed(AnActionEvent e, Project project) {
        AdbFacade.grantRevokePermissions(project);
    }
}
