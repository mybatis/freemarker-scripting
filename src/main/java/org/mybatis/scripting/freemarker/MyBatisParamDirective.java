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

import freemarker.core.Environment;
import freemarker.ext.util.WrapperTemplateModel;
import freemarker.template.DefaultListAdapter;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateNumberModel;
import freemarker.template.TemplateScalarModel;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Custom FreeMarker directive for generating "#{paramName}" declarations in convenient way.
 * Problem is FreeMarker supports this syntax natively and there are no chance to disable this
 * (although it is deprecated). And to get "#{paramName}" we should write ${r"#{paramName}"}.
 * With this directive you can write more simple:
 *
 * <blockquote><pre>
 *     &lt;@p name="paramName"/&gt;
 * </pre></blockquote>
 *
 * <p>
 * Also directive supports `value` attribute. If it is specified, param will take passed value
 * and create the corresponding #{}-parameter. This is useful in loops:
 * </p>
 *
 * <blockquote><pre>
 *     &lt;#list ids as id&gt;
 *       &lt;@p value=id/&gt;
 *       &lt;#if id_has_next&gt;,&lt;/#if&gt;
 *     &lt;/#list&gt;
 * </pre></blockquote>
 *
 * <p>
 * will be translated into
 * </p>
 *
 * <blockquote><pre>
 *     #{_p0},#{_p1},#{_p2}
 * </pre></blockquote>
 *
 * <p>
 * And MyBatis engine will convert it to `?`-params finally.
 * </p>
 *
 * @author elwood
 */
public class MyBatisParamDirective implements TemplateDirectiveModel {
  public static String DEFAULT_KEY = "p";

  @Override
  public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
      throws TemplateException, IOException {
    SimpleScalar name = (SimpleScalar) params.get("name");
    if (params.containsKey("value")) {
      Object valueObject = params.get("value");
      Object value;
      if (valueObject == null) {
        value = null;
      } else if (valueObject instanceof WrapperTemplateModel) {
        value = ((WrapperTemplateModel) valueObject).getWrappedObject();
      } else if (valueObject instanceof TemplateScalarModel) {
        value = ((TemplateScalarModel) valueObject).getAsString();
      } else if (valueObject instanceof TemplateNumberModel) {
        value = ((TemplateNumberModel) valueObject).getAsNumber();
      } else if (valueObject instanceof TemplateDateModel) {
        value = ((TemplateDateModel) valueObject).getAsDate();
      } else if (valueObject instanceof TemplateBooleanModel) {
        value = ((TemplateBooleanModel) valueObject).getAsBoolean();
      } else {
        throw new UnsupportedOperationException(
            String.format("Type %s is not supported yet in this context.", valueObject.getClass().getSimpleName()));
      }

      TemplateModel generatedParamsObject = env.getGlobalVariables().get(FreeMarkerSqlSource.GENERATED_PARAMS_KEY);
      List generatedParams;
      if (generatedParamsObject instanceof DefaultListAdapter) {
        generatedParams = (List) ((DefaultListAdapter) generatedParamsObject).getWrappedObject();
      } else {
        generatedParams = ((GeneratedParamsTemplateModel) generatedParamsObject).getGeneratedParams();
      }
      String generatedParamName = "_p" + generatedParams.size();
      env.getOut().write(String.format("#{%s}", generatedParamName));
      generatedParams.add(value);
    } else {
      env.getOut().write(String.format("#{%s}", name));
    }
  }
}
