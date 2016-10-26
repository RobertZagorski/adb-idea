package com.developerphil.adbidea.adb.command;

import com.android.ddmlib.IDevice;
import com.intellij.openapi.project.Project;
import org.jetbrains.android.facet.AndroidFacet;

public interface Command<T, V> {
    /**
     *
     * @return The result of the command (usually if is executed properly)
     */
    T run(Project project, IDevice device, AndroidFacet facet, String packageName, V... parameters);
}
