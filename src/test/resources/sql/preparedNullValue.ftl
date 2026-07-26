<#--

       Copyright 2015-2026 the original author or authors.

       Licensed under the Apache License, Version 2.0 (the "License");
       you may not use this file except in compliance with the License.
       You may obtain a copy of the License at

          https://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing, software
       distributed under the License is distributed on an "AS IS" BASIS,
       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       See the License for the specific language governing permissions and
       limitations under the License.

-->
<#-- nullParam is a top-level @Param bound to a real null value (not a bean property), so FreeMarker's -->
<#-- SimpleHash#get() -> ObjectWrapper#wrap(null) yields an actual TemplateModel null, guaranteeing -->
<#-- valueObject == null in MyBatisParamDirective.execute. -->
select * from names where firstName = 'Wilma'
and <@p value=nullParam/> is null
