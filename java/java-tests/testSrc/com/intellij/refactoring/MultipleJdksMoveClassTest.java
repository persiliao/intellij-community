/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

package com.intellij.refactoring;

import com.intellij.JavaTestUtil;
import com.intellij.openapi.application.ex.PathManagerEx;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PostprocessReformattingAspect;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.move.moveClassesOrPackages.MoveClassesOrPackagesProcessor;
import com.intellij.refactoring.move.moveClassesOrPackages.SingleSourceRootMoveDestination;
import com.intellij.refactoring.util.RefactoringConflictsUtil;
import com.intellij.testFramework.*;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.*;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.Consumer;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.util.Collections;

public class MultipleJdksMoveClassTest extends RefactoringTestCase {

  private CodeInsightTestFixture myFixture;
  private Module myJava7Module;
  private Module myJava8Module;

  @Override
  protected void tearDown() throws Exception {
    try {
      myFixture.tearDown();
    }
    finally {
      myFixture = null;
      myJava7Module = null;
      myJava8Module = null;

      super.tearDown();
    }
  }
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    TestFixtureBuilder<IdeaProjectTestFixture> projectBuilder = IdeaTestFixtureFactory.getFixtureFactory().createFixtureBuilder(getName());
    myFixture = JavaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(projectBuilder.getFixture());
    myFixture.setTestDataPath(PathManagerEx.getTestDataPath() + "/refactoring/multipleJdks");
    final JavaModuleFixtureBuilder[] builders = new JavaModuleFixtureBuilder[2];
    builders[0] = projectBuilder.addModule(JavaModuleFixtureBuilder.class);
    builders[1] = projectBuilder.addModule(JavaModuleFixtureBuilder.class);
    myFixture.setUp();
    myJava7Module = builders[0].getFixture().getModule();
    myJava8Module = builders[1].getFixture().getModule();

    ModuleRootModificationUtil.updateModel(myJava7Module, model -> {
      model.setSdk(IdeaTestUtil.getMockJdk17());
      String contentUrl = VfsUtilCore.pathToUrl(myFixture.getTempDirPath()) + "/java7";
      model.addContentEntry(contentUrl).addSourceFolder(contentUrl, false);
    });

    ModuleRootModificationUtil.updateModel(myJava8Module, model -> {
      model.setSdk(IdeaTestUtil.getMockJdk18());
      String contentUrl = VfsUtilCore.pathToUrl(myFixture.getTempDirPath()) + "/java8";
      model.addContentEntry(contentUrl).addSourceFolder(contentUrl, false);
    });
  }


  public void testConflictStringUsage() throws Exception {
    final PsiFile[] files = myFixture.configureByFiles("java7/p/Main.java", "java8/p/Foo.java");
    final MultiMap<PsiElement, String> conflicts = new MultiMap<>();
    RefactoringConflictsUtil.analyzeModuleConflicts(files[0].getProject(), Collections.singletonList(files[0]), 
                                                    UsageInfo.EMPTY_ARRAY, files[1].getVirtualFile(), new MultiMap<>());
    
    assertEmpty(conflicts.keySet());
  }
}
