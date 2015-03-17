package org.mybatis.scripting.freemarker;

import freemarker.core.Environment;
import freemarker.ext.util.WrapperTemplateModel;
import freemarker.template.*;

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
 * Also directive supports `value` attribute. If it is specified, param will take passed value
 * and create the corresponding #{}-parameter. This is useful in loops:
 *
 * <blockquote><pre>
 *     &lt;#list ids as id&gt;
 *       &lt;@p value=id/&gt;
 *       &lt;#if id_has_next&gt;,&lt;/#if&gt;
 *     &lt;/#list&gt;
 * </pre></blockquote>
 *
 * will be translated into
 *
 * <blockquote><pre>
 *     #{_p0},#{_p1},#{_p2}
 * </pre></blockquote>
 *
 * And MyBatis engine will convert it to `?`-params finally.
 *
 * @author elwood
 */
public class MyBatisParamDirective implements TemplateDirectiveModel {
    public static String DEFAULT_KEY = "p";

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        SimpleScalar name = (SimpleScalar) params.get("name");
        if (params.containsKey("value")) {
            Object valueObject = params.get("value");
            Object value;
            if (valueObject == null) {
                value = null;
            } else if (valueObject instanceof WrapperTemplateModel) {
                value = ((WrapperTemplateModel) valueObject).getWrappedObject();
            } else if (valueObject instanceof SimpleScalar) {
                value = ((SimpleScalar) valueObject).getAsString();
            } else if (valueObject instanceof SimpleNumber) {
                value = ((SimpleNumber) valueObject).getAsNumber();
            } else if (valueObject instanceof SimpleDate) {
                value = ((SimpleDate) valueObject).getAsDate();
            } else {
                throw new UnsupportedOperationException(
                        String.format("Type %s is not supported yet in this context.",
                                valueObject.getClass().getSimpleName()));
            }

            TemplateModel additionalParamsObject = env.getGlobalVariables().get("__additional_params__");
            List additionalParams;
            if (additionalParamsObject instanceof DefaultListAdapter) {
                additionalParams = (List) ((DefaultListAdapter) additionalParamsObject).getWrappedObject();
            } else {
                additionalParams = ((AdditionalParamsTemplateModel) additionalParamsObject)
                        .getAdditionalParams();
            }
            String generatedParamName = "_p" + additionalParams.size();
            env.getOut().write(String.format("#{%s}", generatedParamName));
            additionalParams.add(value);
        } else {
            env.getOut().write(String.format("#{%s}", name));
        }
    }
}
