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

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.mybatis.scripting.freemarker.support.TemplateFilePathProvider;

public interface TemplateFilePathProviderMapper {

  @Options(useGeneratedKeys = true, keyProperty = "id")
  @InsertProvider(type = TemplateFilePathProvider.class)
  void insert(Name name);

  @UpdateProvider(type = TemplateFilePathProvider.class)
  void update(Name name);

  @DeleteProvider(type = TemplateFilePathProvider.class)
  void delete(Name name);

  @SelectProvider(type = TemplateFilePathProvider.class)
  Name findById(Integer id);

}
