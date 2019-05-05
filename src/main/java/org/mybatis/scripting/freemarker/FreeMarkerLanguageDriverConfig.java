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
package org.mybatis.scripting.freemarker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;

import freemarker.template.Configuration;
import freemarker.template.Version;
import org.apache.commons.text.WordUtils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;

/**
 * Configuration class for {@link FreeMarkerLanguageDriver}.
 *
 * @author Kazuki Shimizu
 * @since 1.2.0
 */
public class FreeMarkerLanguageDriverConfig {

  private static final String PROPERTY_KEY_CONFIG_FILE = "mybatis-freemarker.config.file";
  private static final String PROPERTY_KEY_CONFIG_ENCODING = "mybatis-freemarker.config.encoding";
  private static final String DEFAULT_PROPERTIES_FILE = "mybatis-freemarker.properties";
  private static Map<Class<?>, Function<String, Object>> TYPE_CONVERTERS;

  static {
    Map<Class<?>, Function<String, Object>> converters = new HashMap<>();
    converters.put(String.class, String::trim);
    converters.put(Object.class, v -> v);
    TYPE_CONVERTERS = Collections.unmodifiableMap(converters);
  }

  /**
   * The configuration properties.
   */
  private final Map<String, String> freemarkerSettings = new HashMap<>();

  /**
   * The base directory for reading template resources.
   */
  private String basePackage = "";

  /**
   * Get FreeMarker settings.
   *
   * @return FreeMarker settings
   */
  public Map<String, String> getFreemarkerSettings() {
    return freemarkerSettings;
  }

  /**
   * Get a base directory for reading template resources.
   * <p>
   * Default is none (just under classpath).
   * </p>
   *
   * @return a base directory for reading template resources
   */
  public String getBasePackage() {
    return basePackage;
  }

  /**
   * Set a base directory for reading template resources.
   *
   * @param basePackage
   *          a base directory for reading template resources
   */
  public void setBasePackage(String basePackage) {
    this.basePackage = basePackage;
  }

  /**
   * Create an instance from default properties file. <br>
   * If you want to customize a default {@code TemplateEngine}, you can configure some property using
   * mybatis-freemarker.properties that encoded by UTF-8. Also, you can change the properties file that will read using
   * system property (-Dmybatis-freemarker.config.file=... -Dmybatis-freemarker.config.encoding=...). <br>
   * Supported properties are as follows:
   * <table border="1">
   * <caption>Supported properties</caption>
   * <tr>
   * <th>Property Key</th>
   * <th>Description</th>
   * <th>Default</th>
   * </tr>
   * <tr>
   * <th colspan="3">General configuration</th>
   * </tr>
   * <tr>
   * <td>base-package</td>
   * <td>The base directory for reading template resources</td>
   * <td>None(just under classpath)</td>
   * </tr>
   * <tr>
   * <td>freemarker-settings.*</td>
   * <td>The settings of freemarker {@link freemarker.core.Configurable#setSetting(String, String)}).</td>
   * <td>-</td>
   * </tr>
   * </table>
   *
   * @return a configuration instance
   */
  public static FreeMarkerLanguageDriverConfig newInstance() {
    return newInstance(loadDefaultProperties());
  }

  /**
   * Create an instance from specified properties.
   *
   * @param customProperties
   *          custom configuration properties
   * @return a configuration instance
   * @see #newInstance()
   */
  public static FreeMarkerLanguageDriverConfig newInstance(Properties customProperties) {
    FreeMarkerLanguageDriverConfig config = new FreeMarkerLanguageDriverConfig();
    Properties properties = loadDefaultProperties();
    Optional.ofNullable(customProperties).ifPresent(properties::putAll);
    override(config, properties);
    return config;
  }

  /**
   * Create an instance using specified customizer and override using a default properties file.
   *
   * @param customizer
   *          baseline customizer
   * @return a configuration instance
   * @see #newInstance()
   */
  public static FreeMarkerLanguageDriverConfig newInstance(Consumer<FreeMarkerLanguageDriverConfig> customizer) {
    FreeMarkerLanguageDriverConfig config = new FreeMarkerLanguageDriverConfig();
    customizer.accept(config);
    override(config, loadDefaultProperties());
    return config;
  }

  private static void override(FreeMarkerLanguageDriverConfig config, Properties properties) {
    MetaObject metaObject = MetaObject.forObject(config, new DefaultObjectFactory(), new DefaultObjectWrapperFactory(),
        new DefaultReflectorFactory());
    properties.forEach((key, value) -> {
      String propertyPath = WordUtils
          .uncapitalize(WordUtils.capitalize(Objects.toString(key), '-').replaceAll("-", ""));
      Optional.ofNullable(value).ifPresent(v -> {
        Object convertedValue = TYPE_CONVERTERS.get(metaObject.getSetterType(propertyPath)).apply(value.toString());
        metaObject.setValue(propertyPath, convertedValue);
      });
    });
  }

  private static Properties loadDefaultProperties() {
    return loadProperties(System.getProperty(PROPERTY_KEY_CONFIG_FILE, DEFAULT_PROPERTIES_FILE));
  }

  private static Properties loadProperties(String resourcePath) {
    Properties properties = new Properties();
    InputStream in;
    try {
      in = Resources.getResourceAsStream(resourcePath);
    } catch (IOException e) {
      in = null;
    }
    if (in != null) {
      Charset encoding = Optional.ofNullable(System.getProperty(PROPERTY_KEY_CONFIG_ENCODING)).map(Charset::forName)
          .orElse(StandardCharsets.UTF_8);
      try (InputStreamReader inReader = new InputStreamReader(in, encoding);
          BufferedReader bufReader = new BufferedReader(inReader)) {
        properties.load(bufReader);
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
    return properties;
  }

}
