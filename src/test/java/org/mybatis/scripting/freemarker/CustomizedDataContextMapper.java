package org.mybatis.scripting.freemarker;

import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author elwood
 */
public interface CustomizedDataContextMapper {
    @Lang(CustomizedDataContextTest.CustomFreeMarkerLanguageDriver.class)
    @Select("findUsingCustomizedContext.ftl")
    List<Name> find();
}
