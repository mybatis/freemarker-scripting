package org.mybatis.scripting.freemarker;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

import java.io.IOException;
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
 * @author elwood
 */
public class MyBatisParamDirective implements TemplateDirectiveModel {
    public static String DEFAULT_KEY = "p";

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        env.getOut().write(String.format("#{%s}", params.get("name")));
    }
}
