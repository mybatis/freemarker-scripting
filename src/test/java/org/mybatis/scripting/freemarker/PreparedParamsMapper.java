package org.mybatis.scripting.freemarker;

import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * This mapper demonstrates the usage of auto-generating prepared statement
 * parameters instead of usual inline strategy.
 *
 * @author elwood
 */
public interface PreparedParamsMapper {
    @Lang(FreeMarkerLanguageDriver.class)
    @Select("preparedIn.ftl")
    List<Name> findByNames(@Param("ids") List<String> ids);

    /**
     * This is doesn't work - because params objects are unsupported when using
     * auto-generated prepared parameters (it is impossible to add parameters
     * to MyBatis engine). This call will throw exception.
     */
    @Lang(FreeMarkerLanguageDriver.class)
    @Select("prepared.ftl")
    Name findUsingParamsObject(PreparedParam param);

    @Lang(FreeMarkerLanguageDriver.class)
    @Select("prepared.ftl")
    Name findUsingParams(@Param("innerObject") PreparedParam.InnerClass innerClass);
}
