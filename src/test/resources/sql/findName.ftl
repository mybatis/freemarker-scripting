<#-- @ftlvariable name="p" type="org.mybatis.scripting.freemarker.MyBatisParamDirective" -->
SELECT *
FROM names
where firstName = <@p name="name"/>