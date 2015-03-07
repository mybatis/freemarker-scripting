package org.mybatis.scripting.freemarker;

import freemarker.ext.beans.BeanModel;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Important: if you are using some object that already has property "p", then
 * MyBatisParamDirective will be unavailable from script.
 *
 * @author elwood
 */
public class ParamObjectAdapter implements TemplateHashModel {
    private final BeanModel beanModel;

    public ParamObjectAdapter(Object paramObject) {
        beanModel = new BeanModel(paramObject, new BeansWrapperBuilder(Configuration.VERSION_2_3_22).build());
    }

    @Override
    public TemplateModel get(String key) throws TemplateModelException {
        TemplateModel value = beanModel.get(key);
        if (value == null && MyBatisParamDirective.DEFAULT_KEY.equals(key)) {
            return new MyBatisParamDirective();
        }
        return value;
    }

    @Override
    public boolean isEmpty() throws TemplateModelException {
        return beanModel.isEmpty();
    }
}
