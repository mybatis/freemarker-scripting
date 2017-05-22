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

import freemarker.ext.beans.BeanModel;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Important: if you are using some object that already has property "p", then
 * MyBatisParamDirective will be unavailable from script.
 *
 * @author elwood
 */
public class ParamObjectAdapter implements TemplateHashModel {
  private final BeanModel beanModel;
  private final ArrayList generatedParams;
  private HashMap<String, TemplateModel> additionalParams;

  public ParamObjectAdapter(Object paramObject, ArrayList generatedParams) {
    beanModel = new BeanModel(paramObject, new BeansWrapperBuilder(Configuration.VERSION_2_3_22).build());
    this.generatedParams = generatedParams;
  }

  /**
   * Puts the additional parameter into adapter, it will be available if no
   * existing property with same key exists. For example, it is suitable to add
   * custom objects and directives into dataContext.
   */
  public void putAdditionalParam(String key, TemplateModel value) {
    if (additionalParams == null) {
      additionalParams = new HashMap<>();
    }
    additionalParams.put(key, value);
  }

  public ArrayList getGeneratedParams() {
    return generatedParams;
  }

  @Override
  public TemplateModel get(String key) throws TemplateModelException {
    // Trying to get bean property
    TemplateModel value = beanModel.get(key);

    // If no value retrieved, trying to find the key in additional params
    if (value == null && additionalParams != null && additionalParams.containsKey(key)) {
      return additionalParams.get(key);
    }

    // If it is GENERATED_PARAMS_KEY, returning wrapper of generated params list
    if (value == null && FreeMarkerSqlSource.GENERATED_PARAMS_KEY.equals(key)) {
      return new GeneratedParamsTemplateModel(generatedParams);
    }

    return value;
  }

  @Override
  public boolean isEmpty() throws TemplateModelException {
    return beanModel.isEmpty();
  }
}
