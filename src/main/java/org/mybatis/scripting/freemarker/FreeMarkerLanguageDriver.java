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

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.Configuration;
import org.mybatis.scripting.freemarker.support.TemplateFilePathProvider;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Adds FreeMarker templates support to scripting in MyBatis. If you want to change or extend template loader
 * configuration, use can inherit from this class and override {@link #createFreeMarkerConfiguration()} method.
 *
 * @author elwood
 * @author Kazuki Shimizu
 */
public class FreeMarkerLanguageDriver implements LanguageDriver {

  protected final FreeMarkerLanguageDriverConfig driverConfig;
  protected final freemarker.template.Configuration freemarkerCfg;

  /**
   * Constructor.
   *
   * @see FreeMarkerLanguageDriverConfig#newInstance()
   */
  public FreeMarkerLanguageDriver() {
    this(FreeMarkerLanguageDriverConfig.newInstance());
  }

  /**
   * Constructor.
   *
   * @param driverConfig
   *          a language driver configuration
   *
   * @since 1.2.0
   */
  public FreeMarkerLanguageDriver(FreeMarkerLanguageDriverConfig driverConfig) {
    this.driverConfig = driverConfig;
    this.freemarkerCfg = createFreeMarkerConfiguration();
    TemplateFilePathProvider.setLanguageDriverConfig(driverConfig);
  }

  /**
   * Creates the {@link freemarker.template.Configuration} instance and sets it up. If you want to change it (set
   * another props, for example), you can override it in inherited class and use your own class in @Lang directive.
   */
  protected freemarker.template.Configuration createFreeMarkerConfiguration() {
    freemarker.template.Configuration cfg = new freemarker.template.Configuration(
        freemarker.template.Configuration.VERSION_2_3_22);

    TemplateLoader templateLoader = new ClassTemplateLoader(this.getClass().getClassLoader(),
        driverConfig.getTemplateFile().getBaseDir());
    cfg.setTemplateLoader(templateLoader);

    // To avoid formatting numbers using spaces and commas in SQL
    cfg.setNumberFormat("computer");

    // Because it defaults to default system encoding, we should set it always explicitly
    cfg.setDefaultEncoding(StandardCharsets.UTF_8.name());

    driverConfig.getFreemarkerSettings().forEach((name, value) -> {
      try {
        cfg.setSetting(name, value);
      } catch (TemplateException e) {
        throw new IllegalStateException(
            String.format("Fail to configure FreeMarker template setting. name[%s] value[%s]", name, value), e);
      }
    });

    return cfg;
  }

  /**
   * Creates a {@link ParameterHandler} that passes the actual parameters to the the JDBC statement.
   *
   * @see DefaultParameterHandler
   *
   * @param mappedStatement
   *          The mapped statement that is being executed
   * @param parameterObject
   *          The input parameter object (can be null)
   * @param boundSql
   *          The resulting SQL once the dynamic language has been executed.
   */
  @Override
  public ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject,
      BoundSql boundSql) {
    // As default XMLLanguageDriver
    return new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
  }

  /**
   * Creates an {@link SqlSource} that will hold the statement read from a mapper xml file. It is called during startup,
   * when the mapped statement is read from a class or an xml file.
   *
   * @param configuration
   *          The MyBatis configuration
   * @param script
   *          XNode parsed from a XML file
   * @param parameterType
   *          input parameter type got from a mapper method or specified in the parameterType xml attribute. Can be
   *          null.
   */
  @Override
  public SqlSource createSqlSource(Configuration configuration, XNode script, Class<?> parameterType) {
    return createSqlSource(configuration, script.getNode().getTextContent());
  }

  /**
   * Creates an {@link SqlSource} that will hold the statement read from an annotation. It is called during startup,
   * when the mapped statement is read from a class or an xml file.
   *
   * @param configuration
   *          The MyBatis configuration
   * @param script
   *          The content of the annotation
   * @param parameterType
   *          input parameter type got from a mapper method or specified in the parameterType xml attribute. Can be
   *          null.
   */
  @Override
  public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType) {
    return createSqlSource(configuration, script);
  }

  protected SqlSource createSqlSource(Template template, Configuration configuration) {
    return new FreeMarkerSqlSource(template, configuration, freemarkerCfg.getIncompatibleImprovements());
  }

  private SqlSource createSqlSource(Configuration configuration, String scriptText) {
    Template template;
    if (scriptText.trim().contains(" ")) {
      // Consider that script is inline script
      try {
        template = new Template(null, new StringReader(scriptText), freemarkerCfg);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      // Consider that script is template name, trying to find the template in classpath
      try {
        template = freemarkerCfg.getTemplate(scriptText.trim());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    return createSqlSource(template, configuration);
  }

}
