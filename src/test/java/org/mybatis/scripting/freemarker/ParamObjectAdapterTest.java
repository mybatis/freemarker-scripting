/*
 *    Copyright 2015-2026 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import freemarker.template.Configuration;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Direct unit tests for {@link ParamObjectAdapter}. Note that {@link ParamObjectAdapter#getGeneratedParams()} has no
 * caller in the FreeMarker template-processing pipeline (only {@link GeneratedParamsTemplateModel#getGeneratedParams()}
 * is used by {@link MyBatisParamDirective}), so it cannot be exercised via a mapper/template integration test and is
 * covered here directly.
 */
class ParamObjectAdapterTest {

  @Test
  void getGeneratedParamsReturnsSameListInstance() {
    List<Object> generatedParams = new ArrayList<>();
    ParamObjectAdapter adapter = new ParamObjectAdapter(new PreparedParam(), generatedParams,
        Configuration.VERSION_2_3_22);

    Assertions.assertSame(generatedParams, adapter.getGeneratedParams());

    generatedParams.add("foo");
    Assertions.assertEquals(1, adapter.getGeneratedParams().size());
    Assertions.assertEquals("foo", adapter.getGeneratedParams().get(0));
  }

  @Test
  void isEmptyDelegatesToBeanModel() throws TemplateModelException {
    ParamObjectAdapter adapter = new ParamObjectAdapter(new PreparedParam(), new ArrayList<>(),
        Configuration.VERSION_2_3_22);

    // PreparedParam has readable bean properties, so the wrapped BeanModel is not empty.
    Assertions.assertFalse(adapter.isEmpty());
  }

  @Test
  void getReturnsBeanProperty() throws TemplateModelException {
    PreparedParam param = new PreparedParam();
    ParamObjectAdapter adapter = new ParamObjectAdapter(param, new ArrayList<>(), Configuration.VERSION_2_3_22);

    Assertions.assertNotNull(adapter.get("innerObject"));
  }

  @Test
  void getFallsBackToAdditionalParams() throws TemplateModelException {
    ParamObjectAdapter adapter = new ParamObjectAdapter(new PreparedParam(), new ArrayList<>(),
        Configuration.VERSION_2_3_22);
    TemplateModel additional = new MyBatisParamDirective();
    adapter.putAdditionalParam("p", additional);

    Assertions.assertSame(additional, adapter.get("p"));
  }

  @Test
  void getReturnsGeneratedParamsWrapperForGeneratedParamsKey() throws TemplateModelException {
    List<Object> generatedParams = new ArrayList<>();
    ParamObjectAdapter adapter = new ParamObjectAdapter(new PreparedParam(), generatedParams,
        Configuration.VERSION_2_3_22);

    TemplateModel model = adapter.get(FreeMarkerSqlSource.GENERATED_PARAMS_KEY);
    Assertions.assertInstanceOf(GeneratedParamsTemplateModel.class, model);
    Assertions.assertSame(generatedParams, ((GeneratedParamsTemplateModel) model).getGeneratedParams());
  }

  @Test
  void getReturnsNullForUnknownKey() throws TemplateModelException {
    ParamObjectAdapter adapter = new ParamObjectAdapter(new PreparedParam(), new ArrayList<>(),
        Configuration.VERSION_2_3_22);

    Assertions.assertNull(adapter.get("doesNotExist"));
  }
}
