/**
 *    Copyright 2015-2020 the original author or authors.
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

import org.apache.commons.text.WordUtils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;

import freemarker.template.Configuration;
import freemarker.template.Version;

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
  private static final Map<Class<?>, Function<String, Object>> TYPE_CONVERTERS;

  static {
    Map<Class<?>, Function<String, Object>> converters = new HashMap<>();
    converters.put(String.class, String::trim);
    converters.put(boolean.class, v -> Boolean.valueOf(v.trim()));
    converters.put(Object.class, v -> v);
    TYPE_CONVERTERS = Collections.unmodifiableMap(converters);
  }

  private static final Log log = LogFactory.getLog(FreeMarkerLanguageDriverConfig.class);

  /**
   * The configuration properties.
   */
  private final Map<String, String> freemarkerSettings = new HashMap<>();

  /**
   * Template file configuration.
   */
  private final TemplateFileConfig templateFile = new TemplateFileConfig();

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
   * @deprecated Recommend to use the {@link TemplateFileConfig#getBaseDir()}} because this method defined for keeping
   *             backward compatibility (There is possibility that this method removed at a future version)
   */
  @Deprecated
  public String getBasePackage() {
    return templateFile.getBaseDir();
  }

  /**
   * Set a base directory for reading template resources.
   *
   * @param basePackage
   *          a base directory for reading template resources
   * @deprecated Recommend to use the {@link TemplateFileConfig#setBaseDir(String)} because this method defined for
   *             keeping backward compatibility (There is possibility that this method removed at a future version)
   */
  @Deprecated
  public void setBasePackage(String basePackage) {
    log.warn("The 'basePackage' has been deprecated since 1.2.0. Please use the 'templateFile.baseDir'.");
    templateFile.setBaseDir(basePackage);
  }

  /**
   * Get a template file configuration.
   *
   * @return a template file configuration
   */
  public TemplateFileConfig getTemplateFile() {
    return templateFile;
  }

  /**
   * Template file configuration.
   */
  public static class TemplateFileConfig {

    /**
     * The base directory for reading template resources.
     */
    private String baseDir = "";

    /**
     * The template file path provider configuration.
     */
    private final PathProviderConfig pathProvider = new PathProviderConfig();

    /**
     * Get the base directory for reading template resource file.
     * <p>
     * Default is {@code ""}(none).
     * </p>
     *
     * @return the base directory for reading template resource file
     */
    public String getBaseDir() {
      return baseDir;
    }

    /**
     * Set the base directory for reading template resource file.
     *
     * @param baseDir
     *          the base directory for reading template resource file
     */
    public void setBaseDir(String baseDir) {
      this.baseDir = baseDir;
    }

    /**
     * Get the template file path provider configuration.
     *
     * @return the template file path provider configuration
     */
    public PathProviderConfig getPathProvider() {
      return pathProvider;
    }

    /**
     * The template file path provider configuration.
     */
    public static class PathProviderConfig {

      /**
       * The prefix for adding to template file path.
       */
      private String prefix = "";

      /**
       * Whether includes package path part.
       */
      private boolean includesPackagePath = true;

      /**
       * Whether separate directory per mapper.
       */
      private boolean separateDirectoryPerMapper = true;

      /**
       * Whether includes mapper name into file name when separate directory per mapper.
       */
      private boolean includesMapperNameWhenSeparateDirectory = true;

      /**
       * Whether cache a resolved template file path.
       */
      private boolean cacheEnabled = true;

      /**
       * Get a prefix for adding to template file path.
       * <p>
       * Default is {@code ""}.
       * </p>
       *
       * @return a prefix for adding to template file path
       */
      public String getPrefix() {
        return prefix;
      }

      /**
       * Set the prefix for adding to template file path.
       *
       * @param prefix
       *          The prefix for adding to template file path
       */
      public void setPrefix(String prefix) {
        this.prefix = prefix;
      }

      /**
       * Get whether includes package path part.
       * <p>
       * Default is {@code true}.
       * </p>
       *
       * @return If includes package path, return {@code true}
       */
      public boolean isIncludesPackagePath() {
        return includesPackagePath;
      }

      /**
       * Set whether includes package path part.
       *
       * @param includesPackagePath
       *          If want to includes, set {@code true}
       */
      public void setIncludesPackagePath(boolean includesPackagePath) {
        this.includesPackagePath = includesPackagePath;
      }

      /**
       * Get whether separate directory per mapper.
       *
       * @return If separate directory per mapper, return {@code true}
       */
      public boolean isSeparateDirectoryPerMapper() {
        return separateDirectoryPerMapper;
      }

      /**
       * Set whether separate directory per mapper.
       * <p>
       * Default is {@code true}.
       * </p>
       *
       * @param separateDirectoryPerMapper
       *          If want to separate directory, set {@code true}
       */
      public void setSeparateDirectoryPerMapper(boolean separateDirectoryPerMapper) {
        this.separateDirectoryPerMapper = separateDirectoryPerMapper;
      }

      /**
       * Get whether includes mapper name into file name when separate directory per mapper.
       * <p>
       * Default is {@code true}.
       * </p>
       *
       * @return If includes mapper name, return {@code true}
       */
      public boolean isIncludesMapperNameWhenSeparateDirectory() {
        return includesMapperNameWhenSeparateDirectory;
      }

      /**
       * Set whether includes mapper name into file name when separate directory per mapper.
       * <p>
       * Default is {@code true}.
       * </p>
       *
       * @param includesMapperNameWhenSeparateDirectory
       *          If want to includes, set {@code true}
       */
      public void setIncludesMapperNameWhenSeparateDirectory(boolean includesMapperNameWhenSeparateDirectory) {
        this.includesMapperNameWhenSeparateDirectory = includesMapperNameWhenSeparateDirectory;
      }

      /**
       * Get whether cache a resolved template file path.
       * <p>
       * Default is {@code true}.
       * </p>
       *
       * @return If cache a resolved template file path, return {@code true}
       */
      public boolean isCacheEnabled() {
        return cacheEnabled;
      }

      /**
       * Set whether cache a resolved template file path.
       *
       * @param cacheEnabled
       *          If want to cache, set {@code true}
       */
      public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
      }

    }

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
