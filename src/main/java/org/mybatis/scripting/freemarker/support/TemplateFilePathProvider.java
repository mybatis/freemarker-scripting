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
package org.mybatis.scripting.freemarker.support;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.io.Resources;
import org.mybatis.scripting.freemarker.FreeMarkerLanguageDriver;
import org.mybatis.scripting.freemarker.FreeMarkerLanguageDriverConfig;
import org.mybatis.scripting.freemarker.FreeMarkerLanguageDriverConfig.TemplateFileConfig.PathProviderConfig;

/**
 * The SQL provider class that return the SQL template file path. <br>
 * <b>IMPORTANT: This class required to use with mybatis 3.5.1+</b> and need to use with SQL provider annotation (such
 * as {@link org.apache.ibatis.annotations.SelectProvider} as follow: <br>
 * <br>
 * 
 * <pre>
 * package com.example.mapper;
 *
 * public interface BaseMapper&lt;T&gt; {
 *
 *   &#64;Options(useGeneratedKeys = true, keyProperty = "id")
 *   &#64;InsertProvider(type = TemplateFilePathProvider.class)
 *   void insert(T entity);
 *
 *   &#64;UpdateProvider(type = TemplateFilePathProvider.class)
 *   void update(T entity);
 *
 *   &#64;DeleteProvider(type = TemplateFilePathProvider.class)
 *   void delete(T entity);
 *
 *   &#64;SelectProvider(type = TemplateFilePathProvider.class)
 *   T findById(Integer id);
 *
 * }
 * </pre>
 *
 * <pre>
 * package com.example.mapper;
 *
 * public interface NameMapper extends BaseMapper {
 *
 *   &#64;SelectProvider(type = TemplateFilePathProvider.class)
 *   List&lt;Name&gt; findByConditions(NameConditions conditions);
 *
 * }
 * </pre>
 * 
 * @author Kazuki Shimizu
 * @version 1.2.0
 */
public class TemplateFilePathProvider {

  private static final PathGenerator DEFAULT_PATH_GENERATOR = TemplateFilePathProvider::generateTemplatePath;
  private static final FreeMarkerLanguageDriverConfig DEFAULT_LANGUAGE_DRIVER_CONFIG = FreeMarkerLanguageDriverConfig
      .newInstance();

  private static PathGenerator pathGenerator = DEFAULT_PATH_GENERATOR;
  private static FreeMarkerLanguageDriverConfig languageDriverConfig = DEFAULT_LANGUAGE_DRIVER_CONFIG;

  private static ConcurrentMap<ProviderContext, String> cache = new ConcurrentHashMap<>();

  private TemplateFilePathProvider() {
    // NOP
  }

  /**
   * Set custom implementation for {@link PathGenerator}.
   *
   * @param pathGenerator
   *          a instance for generating a template file path
   */
  public static void setCustomTemplateFilePathGenerator(PathGenerator pathGenerator) {
    TemplateFilePathProvider.pathGenerator = Optional.ofNullable(pathGenerator).orElse(DEFAULT_PATH_GENERATOR);
  }

  /**
   * Set a configuration instance for {@link FreeMarkerLanguageDriver}.
   * <p>
   * By default, {@link FreeMarkerLanguageDriverConfig#newInstance()} will used.
   * </p>
   * <p>
   * If you applied an user define {@link FreeMarkerLanguageDriverConfig} for {@link FreeMarkerLanguageDriver}, please
   * same instance to the this class.
   * </p>
   * 
   * @param languageDriverConfig
   *          A user defined {@link FreeMarkerLanguageDriverConfig}
   */
  public static void setLanguageDriverConfig(FreeMarkerLanguageDriverConfig languageDriverConfig) {
    TemplateFilePathProvider.languageDriverConfig = Optional.ofNullable(languageDriverConfig)
        .orElse(DEFAULT_LANGUAGE_DRIVER_CONFIG);
  }

  /**
   * Provide an SQL scripting string(template file path).
   *
   * <br>
   * By default implementation, a template file path resolve following format and priority order. If does not match all,
   * it throw an exception that indicate not found a template file.
   * <ul>
   * <li>com/example/mapper/NameMapper/NameMapper-{methodName}-{databaseId}.ftl</li>
   * <li>com/example/mapper/NameMapper/NameMapper-{methodName}.ftl (fallback using default database)</li>
   * <li>com/example/mapper/BaseMapper/BaseMapper-{methodName}-{databaseId}.ftl (fallback using declaring class of
   * method)</li>
   * <li>com/example/mapper/BaseMapper/BaseMapper-{methodName}.ftl (fallback using declaring class of method and default
   * database)</li>
   * </ul>
   * <br>
   *
   * @param context
   *          a context of SQL provider
   * @return an SQL scripting string(template file path)
   */
  @SuppressWarnings("unused")
  public static String provideSql(ProviderContext context) {
    return languageDriverConfig.getTemplateFile().getPathProvider().isCacheEnabled()
        ? cache.computeIfAbsent(context, c -> providePath(c.getMapperType(), c.getMapperMethod(), c.getDatabaseId()))
        : providePath(context.getMapperType(), context.getMapperMethod(), context.getDatabaseId());
  }

  /**
   * Clear cache.
   */
  public static void clearCache() {
    cache.clear();
  }

  static String providePath(Class<?> mapperType, Method mapperMethod, String databaseId) {
    boolean fallbackDeclaringClass = mapperType != mapperMethod.getDeclaringClass();
    boolean fallbackDatabase = databaseId != null;
    String path = pathGenerator.generatePath(mapperType, mapperMethod, databaseId);
    if (exists(path)) {
      return path;
    }
    if (fallbackDatabase) {
      path = pathGenerator.generatePath(mapperType, mapperMethod, null);
      if (exists(path)) {
        return path;
      }
    }
    if (fallbackDeclaringClass) {
      path = pathGenerator.generatePath(mapperMethod.getDeclaringClass(), mapperMethod, databaseId);
      if (exists(path)) {
        return path;
      }
      if (fallbackDatabase) {
        path = pathGenerator.generatePath(mapperMethod.getDeclaringClass(), mapperMethod, null);
        if (exists(path)) {
          return path;
        }
      }
    }
    throw new IllegalStateException("The SQL template file not found. mapperType:[" + mapperType + "] mapperMethod:["
        + mapperMethod + "] databaseId:[" + databaseId + "]");
  }

  private static String generateTemplatePath(Class<?> type, Method method, String databaseId) {
    Package pkg = type.getPackage();
    String packageName = pkg == null ? "" : pkg.getName();
    String className = type.getName().substring(packageName.length() + (packageName.isEmpty() ? 0 : 1));

    PathProviderConfig pathProviderConfig = languageDriverConfig.getTemplateFile().getPathProvider();
    StringBuilder path = new StringBuilder();
    if (!pathProviderConfig.getPrefix().isEmpty()) {
      path.append(pathProviderConfig.getPrefix());
    }
    if (pathProviderConfig.isIncludesPackagePath() && !packageName.isEmpty()) {
      path.append(packageName.replace('.', '/')).append('/');
    }
    path.append(className);
    if (pathProviderConfig.isSeparateDirectoryPerMapper()) {
      path.append('/');
      if (pathProviderConfig.isIncludesMapperNameWhenSeparateDirectory()) {
        path.append(className).append('-');
      }
    } else {
      path.append('-');
    }
    path.append(method.getName());
    if (databaseId != null) {
      path.append('-').append(databaseId);
    }
    path.append(".ftl");
    return path.toString();
  }

  private static boolean exists(String path) {
    String basePath = languageDriverConfig.getTemplateFile().getBaseDir();
    String actualPath = basePath.isEmpty() ? path : basePath + (basePath.endsWith("/") ? "" : "/") + path;
    try {
      Resources.getResourceURL(actualPath);
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  /**
   * The interface that implements a function for generating template file path.
   */
  @FunctionalInterface
  public interface PathGenerator {

    /**
     * Generate a template file path.
     *
     * @param type
     *          mapper interface type that specified provider (or declaring interface type of mapper method)
     * @param method
     *          a mapper method that specified provider
     * @param databaseId
     *          a database id that provided from {@link org.apache.ibatis.mapping.DatabaseIdProvider}
     * @return a template file path
     */
    String generatePath(Class<?> type, Method method, String databaseId);

  }

}
