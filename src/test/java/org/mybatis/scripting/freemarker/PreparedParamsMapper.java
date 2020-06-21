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

import java.util.List;

import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * This mapper demonstrates the usage of auto-generating prepared statement parameters instead of usual inline strategy.
 *
 * @author elwood
 */
public interface PreparedParamsMapper {
  @Lang(FreeMarkerLanguageDriver.class)
  @Select("preparedIn.ftl")
  List<Name> findByNames(@Param("ids") List<String> ids);

  /**
   * This is doesn't work - because params objects are unsupported when using auto-generated prepared parameters (it is
   * impossible to add parameters to MyBatis engine). This call will throw exception.
   */
  @Lang(FreeMarkerLanguageDriver.class)
  @Select("prepared.ftl")
  Name findUsingParamsObject(PreparedParam param);

  @Lang(FreeMarkerLanguageDriver.class)
  @Select("prepared.ftl")
  Name findUsingParams(@Param("innerObject") PreparedParam.InnerClass innerClass);
}
