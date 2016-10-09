package com.developerphil.adbidea.adb.command;

import com.android.ddmlib.IDevice;
import com.intellij.openapi.project.Project;
import org.jetbrains.android.facet.AndroidFacet;

public interface CommandWithOutput<T> {
    /**
     *
     * @return true if the command executed properly
     */
    T run(Project project, IDevice device, AndroidFacet facet, String packageName);
}
