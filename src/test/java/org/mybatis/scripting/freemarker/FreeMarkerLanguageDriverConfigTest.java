/*
 *    Copyright 2015-2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.scripting.freemarker;

import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FreeMarkerLanguageDriverConfigTest {

  private String currentConfigFile;
  private String currentConfigEncoding;

  @BeforeEach
  void saveCurrentConfig() {
    currentConfigFile = System.getProperty("mybatis-freemarker.config");
    currentConfigEncoding = System.getProperty("mybatis-freemarker.config.encoding");
  }

  @AfterEach
  void restoreConfig() {
    if (currentConfigFile == null) {
      System.clearProperty("mybatis-freemarker.config.file");
    } else {
      System.setProperty("mybatis-freemarker.config.file", currentConfigFile);
    }
    if (currentConfigEncoding == null) {
      System.clearProperty("mybatis-freemarker.config.encoding");
    } else {
      System.setProperty("mybatis-freemarker.config.encoding", currentConfigEncoding);
    }
  }

  @Test
  void newInstanceWithEmptyPropertiesFile() {
    System.setProperty("mybatis-freemarker.config.file", "mybatis-freemarker-empty.properties");
    FreeMarkerLanguageDriverConfig config = FreeMarkerLanguageDriverConfig.newInstance();
    @SuppressWarnings("deprecation")
    String basePackage = config.getBasePackage();
    Assertions.assertEquals("", basePackage);
    Assertions.assertEquals("", config.getTemplateFile().getBaseDir());
    Assertions.assertEquals("", config.getTemplateFile().getPathProvider().getPrefix());
    Assertions.assertTrue(config.getTemplateFile().getPathProvider().isIncludesPackagePath());
    Assertions.assertTrue(config.getTemplateFile().getPathProvider().isSeparateDirectoryPerMapper());
    Assertions.assertTrue(config.getTemplateFile().getPathProvider().isIncludesMapperNameWhenSeparateDirectory());
    Assertions.assertTrue(config.getTemplateFile().getPathProvider().isCacheEnabled());
  }

  @Test
  void newInstanceWithPropertiesFileNotFound() {
    System.setProperty("mybatis-freemarker.config.file", "mybatis-freemarker-notfound.properties");
    FreeMarkerLanguageDriverConfig config = FreeMarkerLanguageDriverConfig.newInstance();
    @SuppressWarnings("deprecation")
    String basePackage = config.getBasePackage();
    Assertions.assertEquals("", basePackage);
    Assertions.assertEquals("", config.getTemplateFile().getBaseDir());
    Assertions.assertEquals("", config.getTemplateFile().getPathProvider().getPrefix());
    Assertions.assertTrue(config.getTemplateFile().getPathProvider().isIncludesPackagePath());
    Assertions.assertTrue(config.getTemplateFile().getPathProvider().isSeparateDirectoryPerMapper());
    Assertions.assertTrue(config.getTemplateFile().getPathProvider().isIncludesMapperNameWhenSeparateDirectory());
    Assertions.assertTrue(config.getTemplateFile().getPathProvider().isCacheEnabled());
  }

  @Test
  void newInstanceWithCustomPropertiesFile() {
    System.setProperty("mybatis-freemarker.config.file", "mybatis-freemarker-custom.properties");
    FreeMarkerLanguageDriverConfig config = FreeMarkerLanguageDriverConfig.newInstance();
    @SuppressWarnings("deprecation")
    String basePackage = config.getBasePackage();
    Assertions.assertEquals("sqls", basePackage);
    Assertions.assertEquals("sqls", config.getTemplateFile().getBaseDir());
    Assertions.assertEquals("mappers", config.getTemplateFile().getPathProvider().getPrefix());
    Assertions.assertFalse(config.getTemplateFile().getPathProvider().isIncludesPackagePath());
    Assertions.assertFalse(config.getTemplateFile().getPathProvider().isSeparateDirectoryPerMapper());
    Assertions.assertFalse(config.getTemplateFile().getPathProvider().isIncludesMapperNameWhenSeparateDirectory());
    Assertions.assertFalse(config.getTemplateFile().getPathProvider().isCacheEnabled());
    Assertions.assertEquals(2, config.getFreemarkerSettings().size());
    Assertions.assertEquals("dollar", config.getFreemarkerSettings().get("interpolation_syntax"));
    Assertions.assertEquals("yes", config.getFreemarkerSettings().get("whitespace_stripping"));
  }

  @Test
  void newInstanceWithCustomProperties() {
    Properties properties = new Properties();
    properties.setProperty("templateFile.pathProvider.prefix", "mapper");
    properties.setProperty("templateFile.pathProvider.includesPackagePath", "false");
    properties.setProperty("templateFile.pathProvider.separateDirectoryPerMapper", "false");
    properties.setProperty("templateFile.pathProvider.includesMapperNameWhenSeparateDirectory", "false");
    properties.setProperty("templateFile.pathProvider.cacheEnabled", "false");
    properties.setProperty("freemarkerSettings.interpolation_syntax", "dollar");
    properties.setProperty("freemarkerSettings.whitespace_stripping", "yes");

    FreeMarkerLanguageDriverConfig config = FreeMarkerLanguageDriverConfig.newInstance(properties);
    @SuppressWarnings("deprecation")
    String basePackage = config.getBasePackage();
    Assertions.assertEquals("sql", basePackage);
    Assertions.assertEquals("sql", config.getTemplateFile().getBaseDir());
    Assertions.assertEquals("mapper", config.getTemplateFile().getPathProvider().getPrefix());
    Assertions.assertFalse(config.getTemplateFile().getPathProvider().isIncludesPackagePath());
    Assertions.assertFalse(config.getTemplateFile().getPathProvider().isSeparateDirectoryPerMapper());
    Assertions.assertFalse(config.getTemplateFile().getPathProvider().isIncludesMapperNameWhenSeparateDirectory());
    Assertions.assertFalse(config.getTemplateFile().getPathProvider().isCacheEnabled());
    Assertions.assertEquals("dollar", config.getFreemarkerSettings().get("interpolation_syntax"));
    Assertions.assertEquals("yes", config.getFreemarkerSettings().get("whitespace_stripping"));
  }

  @Test
  @SuppressWarnings("deprecation")
  void newInstanceWithConsumer() {
    FreeMarkerLanguageDriverConfig config = FreeMarkerLanguageDriverConfig.newInstance(c -> {
      c.setBasePackage("sqls");
    });
    Assertions.assertEquals("sql", config.getBasePackage());
    Assertions.assertEquals("sql", config.getTemplateFile().getBaseDir());
  }

}
