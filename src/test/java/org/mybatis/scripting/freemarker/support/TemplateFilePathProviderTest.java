/**
 *    Copyright 2015-2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.scripting.freemarker.support;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.scripting.freemarker.FreeMarkerLanguageDriverConfig;

class TemplateFilePathProviderTest {

  @BeforeAll
  static void setup() {
    System.setProperty("mybatis-freemarker.config.file", "mybatis-freemarker-empty.properties");
  }

  @AfterAll
  static void restore() {
    System.clearProperty("mybatis-freemarker.config.file");
  }

  @BeforeEach
  @AfterEach
  void clean() {
    TemplateFilePathProvider.setCustomTemplateFilePathGenerator(null);
    TemplateFilePathProvider.setLanguageDriverConfig(FreeMarkerLanguageDriverConfig.newInstance());
  }

  @Test
  void withoutDatabaseId() {
    String path = TemplateFilePathProvider.providePath(TestMapper.class, extractMethod(TestMapper.class, "update"),
        null);
    Assertions.assertEquals("org/mybatis/scripting/freemarker/support/TestMapper/TestMapper-update.ftl", path);
  }

  @Test
  void withDatabaseId() {
    String path = TemplateFilePathProvider.providePath(TestMapper.class, extractMethod(TestMapper.class, "update"),
        "h2");
    Assertions.assertEquals("org/mybatis/scripting/freemarker/support/TestMapper/TestMapper-update-h2.ftl", path);
  }

  @Test
  void fallbackWithDefaultDatabase() {
    String path = TemplateFilePathProvider.providePath(TestMapper.class, extractMethod(TestMapper.class, "delete"),
        "h2");
    Assertions.assertEquals("org/mybatis/scripting/freemarker/support/TestMapper/TestMapper-delete.ftl", path);
  }

  @Test
  void fallbackDeclaringClassWithoutDatabaseId() {
    String path = TemplateFilePathProvider.providePath(TestMapper.class, extractMethod(TestMapper.class, "insert"),
        null);
    Assertions.assertEquals("org/mybatis/scripting/freemarker/support/BaseMapper/BaseMapper-insert.ftl", path);
  }

  @Test
  void fallbackDeclaringClassWithDatabaseId() {
    String path = TemplateFilePathProvider.providePath(TestMapper.class, extractMethod(TestMapper.class, "insert"),
        "h2");
    Assertions.assertEquals("org/mybatis/scripting/freemarker/support/BaseMapper/BaseMapper-insert-h2.ftl", path);
  }

  @Test
  void fallbackDeclaringClassAndDefaultDatabase() {
    String path = TemplateFilePathProvider.providePath(TestMapper.class, extractMethod(TestMapper.class, "count"),
        "h2");
    Assertions.assertEquals("org/mybatis/scripting/freemarker/support/BaseMapper/BaseMapper-count.ftl", path);
  }

  @Test
  void notFoundSqlFile() {
    IllegalStateException e = Assertions.assertThrows(IllegalStateException.class, () -> TemplateFilePathProvider
        .providePath(TestMapper.class, extractMethod(TestMapper.class, "selectOne"), "h2"));
    Assertions.assertEquals(
        "The SQL template file not found. mapperType:[interface org.mybatis.scripting.freemarker.support.TestMapper] mapperMethod:[public abstract java.lang.Object org.mybatis.scripting.freemarker.support.BaseMapper.selectOne(int)] databaseId:[h2]",
        e.getMessage());
  }

  @Test
  void notFoundSqlFileWithoutDatabaseId() {
    IllegalStateException e = Assertions.assertThrows(IllegalStateException.class, () -> TemplateFilePathProvider
        .providePath(TestMapper.class, extractMethod(TestMapper.class, "selectOne"), null));
    Assertions.assertEquals(
        "The SQL template file not found. mapperType:[interface org.mybatis.scripting.freemarker.support.TestMapper] mapperMethod:[public abstract java.lang.Object org.mybatis.scripting.freemarker.support.BaseMapper.selectOne(int)] databaseId:[null]",
        e.getMessage());
  }

  @Test
  void notFoundSqlFileWithoutFallbackDeclaringClass() {
    IllegalStateException e = Assertions.assertThrows(IllegalStateException.class, () -> TemplateFilePathProvider
        .providePath(TestMapper.class, extractMethod(TestMapper.class, "selectAllByFirstName"), null));
    Assertions.assertEquals(
        "The SQL template file not found. mapperType:[interface org.mybatis.scripting.freemarker.support.TestMapper] mapperMethod:[public abstract java.util.List org.mybatis.scripting.freemarker.support.TestMapper.selectAllByFirstName(java.lang.String)] databaseId:[null]",
        e.getMessage());
  }

  @Test
  void includesPackagePathAndSeparatesDirectoryPerMapperIsFalse() {
    TemplateFilePathProvider.setLanguageDriverConfig(FreeMarkerLanguageDriverConfig.newInstance(c -> {
      c.getTemplateFile().setBaseDir("org/mybatis/scripting/freemarker/support/sql");
      c.getTemplateFile().getPathProvider().setIncludesPackagePath(false);
      c.getTemplateFile().getPathProvider().setSeparateDirectoryPerMapper(false);
    }));
    String path = TemplateFilePathProvider.providePath(TestMapper.class,
        extractMethod(TestMapper.class, "selectAllDesc"), null);
    Assertions.assertEquals("TestMapper-selectAllDesc.ftl", path);
  }

  @Test
  void baseDirEndWithSlash() {
    TemplateFilePathProvider.setLanguageDriverConfig(FreeMarkerLanguageDriverConfig.newInstance(c -> {
      c.getTemplateFile().setBaseDir("org/mybatis/scripting/freemarker/support/sql/");
      c.getTemplateFile().getPathProvider().setIncludesPackagePath(false);
      c.getTemplateFile().getPathProvider().setSeparateDirectoryPerMapper(false);
    }));
    String path = TemplateFilePathProvider.providePath(TestMapper.class,
        extractMethod(TestMapper.class, "selectAllDesc"), null);
    Assertions.assertEquals("TestMapper-selectAllDesc.ftl", path);
  }

  @Test
  void includesMapperNameWhenSeparateDirectoryIsFalse() {
    TemplateFilePathProvider.setLanguageDriverConfig(FreeMarkerLanguageDriverConfig
        .newInstance(c -> c.getTemplateFile().getPathProvider().setIncludesMapperNameWhenSeparateDirectory(false)));
    String path = TemplateFilePathProvider.providePath(TestMapper.class,
        extractMethod(TestMapper.class, "selectAllAsc"), null);
    Assertions.assertEquals("org/mybatis/scripting/freemarker/support/TestMapper/selectAllAsc.ftl", path);
  }

  @Test
  void prefix() {
    TemplateFilePathProvider.setLanguageDriverConfig(FreeMarkerLanguageDriverConfig.newInstance(c -> {
      c.getTemplateFile().getPathProvider().setPrefix("org/mybatis/scripting/freemarker/support/sql/");
      c.getTemplateFile().getPathProvider().setIncludesPackagePath(false);
      c.getTemplateFile().getPathProvider().setSeparateDirectoryPerMapper(false);
    }));
    String path = TemplateFilePathProvider.providePath(TestMapper.class,
        extractMethod(TestMapper.class, "selectAllDesc"), null);
    Assertions.assertEquals("org/mybatis/scripting/freemarker/support/sql/TestMapper-selectAllDesc.ftl", path);
  }

  @Test
  void defaultPackageMapper() throws ClassNotFoundException {
    TemplateFilePathProvider.setLanguageDriverConfig(FreeMarkerLanguageDriverConfig
        .newInstance(c -> c.getTemplateFile().setBaseDir("org/mybatis/scripting/freemarker/support/")));
    Class<?> mapperType = Class.forName("DefaultPackageNameMapper");
    String path = TemplateFilePathProvider.providePath(mapperType, extractMethod(mapperType, "selectAllDesc"), null);
    Assertions.assertEquals("DefaultPackageNameMapper/DefaultPackageNameMapper-selectAllDesc.ftl", path);
  }

  @Test
  void defaultPackageMapperWithIncludesPackagePathIsFalse() throws ClassNotFoundException {
    TemplateFilePathProvider.setLanguageDriverConfig(FreeMarkerLanguageDriverConfig.newInstance(c -> {
      c.getTemplateFile().setBaseDir("org/mybatis/scripting/freemarker/support/");
      c.getTemplateFile().getPathProvider().setIncludesPackagePath(false);
    }));
    Class<?> mapperType = Class.forName("DefaultPackageNameMapper");
    String path = TemplateFilePathProvider.providePath(mapperType, extractMethod(mapperType, "selectAllDesc"), null);
    Assertions.assertEquals("DefaultPackageNameMapper/DefaultPackageNameMapper-selectAllDesc.ftl", path);
  }

  @Test
  void customTemplateFileGenerator() {
    TemplateFilePathProvider.setCustomTemplateFilePathGenerator(
        (type, method, databaseId) -> type.getName().replace('.', '/') + "_" + method.getName() + ".ftl");
    String path = TemplateFilePathProvider.providePath(TestMapper.class, extractMethod(TestMapper.class, "selectOne"),
        null);
    Assertions.assertEquals("org/mybatis/scripting/freemarker/support/BaseMapper_selectOne.ftl", path);

  }

  private Method extractMethod(Class<?> type, String methodName) {
    return Arrays.stream(type.getMethods()).filter(m -> m.getName().equals(methodName)).findFirst().orElseThrow(
        () -> new IllegalArgumentException("The method not found. type:" + type + " methodName:" + methodName));
  }

}
