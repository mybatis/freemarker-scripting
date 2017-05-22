/**
 *    Copyright 2015-2017 the original author or authors.
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

import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;

/**
 * Applies provided parameter(s) to FreeMarker template.
 * Then passes the result into default MyBatis engine
 * (and it finally replaces #{}-params to '?'-params).
 * So, FreeMarker is used as preprocessor for MyBatis engine.
 *
 * @author elwood
 */
public class FreeMarkerSqlSource implements SqlSource {
  private final Template template;
  private final Configuration configuration;

  public static final String GENERATED_PARAMS_KEY = "__GENERATED__";

  public FreeMarkerSqlSource(Template template, Configuration configuration) {
    this.template = template;
    this.configuration = configuration;
  }

  /**
   * Populates additional parameters to data context.
   * Data context can be {@link java.util.Map}
   * or {@link org.mybatis.scripting.freemarker.ParamObjectAdapter}
   * instance.
   */
  protected Object preProcessDataContext(Object dataContext, boolean isMap) {
    if (isMap) {
      ((Map<String, Object>) dataContext).put(MyBatisParamDirective.DEFAULT_KEY, new MyBatisParamDirective());
    } else {
      ((ParamObjectAdapter) dataContext).putAdditionalParam(MyBatisParamDirective.DEFAULT_KEY,
          new MyBatisParamDirective());
    }
    return dataContext;
  }

  @Override
  public BoundSql getBoundSql(Object parameterObject) {
    // Add to passed parameterObject our predefined directive - MyBatisParamDirective
    // It will be available as "p" inside templates
    Object dataContext;
    ArrayList generatedParams = new ArrayList<>();
    if (parameterObject != null) {
      if (parameterObject instanceof Map) {
        HashMap<String, Object> map = new HashMap<>((Map<String, Object>) parameterObject);
        map.put(GENERATED_PARAMS_KEY, generatedParams);
        dataContext = preProcessDataContext(map, true);
      } else {
        ParamObjectAdapter adapter = new ParamObjectAdapter(parameterObject, generatedParams);
        dataContext = preProcessDataContext(adapter, false);
      }
    } else {
      HashMap<Object, Object> map = new HashMap<>();
      map.put(GENERATED_PARAMS_KEY, generatedParams);
      dataContext = preProcessDataContext(map, true);
    }

    CharArrayWriter writer = new CharArrayWriter();
    try {
      template.process(dataContext, writer);
    } catch (TemplateException | IOException e) {
      throw new RuntimeException(e);
    }

    // We got SQL ready for MyBatis here. This SQL contains
    // params declarations like "#{param}",
    // they will be replaced to '?' by MyBatis engine further
    String sql = writer.toString();

    if (!generatedParams.isEmpty()) {
      if (!(parameterObject instanceof Map)) {
        throw new UnsupportedOperationException("Auto-generated prepared statements parameters"
            + " are not available if using parameters object. Use @Param-annotated parameters" + " instead.");
      }

      Map<String, Object> parametersMap = (Map<String, Object>) parameterObject;
      for (int i = 0; i < generatedParams.size(); i++) {
        parametersMap.put("_p" + i, generatedParams.get(i));
      }
    }

    // Pass retrieved SQL into MyBatis engine, it will substitute prepared-statements parameters
    SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
    Class<?> parameterType1 = parameterObject == null ? Object.class : parameterObject.getClass();
    SqlSource sqlSource = sqlSourceParser.parse(sql, parameterType1, new HashMap<String, Object>());
    return sqlSource.getBoundSql(parameterObject);
  }
}
