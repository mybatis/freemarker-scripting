package org.mybatis.scripting.freemarker;

import freemarker.ext.beans.BeanModel;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.ext.util.WrapperTemplateModel;
import freemarker.template.*;

import java.util.ArrayList;

/**
 * Important: if you are using some object that already has property "p", then
 * MyBatisParamDirective will be unavailable from script.
 *
 * @author elwood
 */
public class ParamObjectAdapter implements TemplateHashModel {
    private final BeanModel beanModel;
    private final ArrayList additionalParams;

    public ParamObjectAdapter(Object paramObject, ArrayList additionalParams) {
        beanModel = new BeanModel(paramObject, new BeansWrapperBuilder(Configuration.VERSION_2_3_22).build());
        this.additionalParams = additionalParams;
    }

    public ArrayList getAdditionalParams() {
        return additionalParams;
    }

    @Override
    public TemplateModel get(String key) throws TemplateModelException {
        TemplateModel value = beanModel.get(key);
        if (value == null && MyBatisParamDirective.DEFAULT_KEY.equals(key)) {
            return new MyBatisParamDirective();
        }
        if (value == null && "__additional_params__".equals(key)) {
            return new AdditionalParamsTemplateModel(additionalParams);
        }
        return value;
    }

    @Override
    public boolean isEmpty() throws TemplateModelException {
        return beanModel.isEmpty();
    }
}
