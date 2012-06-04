/*
 * Copyright 2010-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.jet.plugin.project;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Pavel Talanov
 *         <p/>
 *         This class has utility functions to determine whether the project (or module) is js project.
 */
public final class JsModuleDetector {
    private JsModuleDetector() {
    }

    public static boolean isJsProject(@NotNull Project project) {
        return getJSModule(project) != null;
    }

    public static boolean isJsModule(@NotNull Module module) {
        return K2JSModuleComponent.getInstance(module).isJavaScriptModule();
    }

    @NotNull
    public static Pair<String, String> getLibLocationAndTargetForProject(@NotNull Project project) {
        Module module = getJSModule(project);
        if (module == null) {
            return Pair.empty();
        }
        K2JSModuleComponent jsModuleComponent = K2JSModuleComponent.getInstance(module);
        String pathToJavaScriptLibrary = jsModuleComponent.getPathToJavaScriptLibrary();
        String basePath = ModuleRootManager.getInstance(module).getContentRoots()[0].getPath();
        return Pair.create(basePath + pathToJavaScriptLibrary, jsModuleComponent.getEcmaVersion().toString());
    }

    @Nullable
    private static Module getJSModule(@NotNull Project project) {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            if (isJsModule(module)) {
                return module;
            }
        }
        return null;
    }
}
