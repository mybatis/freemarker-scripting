package org.mybatis.scripting.freemarker;

import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Annotations-driven mapper for {@link org.mybatis.scripting.freemarker.FreeMarkerInAnnotationsTest}.
 *
 * @author elwood
 */
public interface NameMapper {
    /**
     * Simple query that is loaded from template.
     */
    @Lang(FreeMarkerLanguageDriver.class)
    @Select("getAllNames.ftl")
    List<Name> getAllNames();

    /**
     * Simple query with prepared statement parameter.
     */
    @Lang(FreeMarkerLanguageDriver.class)
    @Select("findName.ftl")
    Name findName(@Param("name") String name);

    /**
     * If any whitespace found inside @Select text, it is interpreted as inline script, not template name.
     * It is convenient to avoid creating templates when script is really small.
     */
    @Lang(FreeMarkerLanguageDriver.class)
    @Select("select * from names where id in (${ids?join(',')})")
    List<Name> findNamesByIds(@Param("ids") List<Integer> ids);

    /**
     * There are no @Param annotation on argument. This means NameParam instance
     * will be passed into driver as is, not as Map entry. So, we need to support this case.
     * Because in driver we need to add some another properties into template model,
     * and NameParam is not Map, we are need to wrap passed parameter object into
     * {@link org.mybatis.scripting.freemarker.ParamObjectAdapter} before processing template.
     */
    @Lang(FreeMarkerLanguageDriver.class)
    @Select("select * from names where id = <@p name='id'/> and id = ${id}")
    Name find(NameParam nameParam);

    /**
     * This query is to demonstrate MyBatis odd behaviour when using String as parameter
     * and can use properties that not exist. Both props will be use provided `name` parameter value.
     * Goal is to write FreeMarker lang plugin to support this behaviour too (although it is confusing one).
     */
    @Select("select * from names" +
            " where firstName = #{noSuchPropertyOnString}" +
            " or firstName = #{oneMoreUnexistingProperty}")
    List<Name> getNamesOddBehaviourStdLang(String name);

    /**
     * This query is to demonstrate that FreeMarker does not break the compatibility with this behaviour.
     */
    @Lang(FreeMarkerLanguageDriver.class)
    @Select("select * from names" +
            " where firstName = <@p name='noSuchPropertyOnString'/>" +
            " or firstName = <@p name='oneMoreUnexistingProperty'/>")
    List<Name> getNamesOddBehaviourFreeMarkerLang(String name);
}
