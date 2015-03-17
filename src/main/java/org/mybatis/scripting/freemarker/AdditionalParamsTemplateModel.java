package org.mybatis.scripting.freemarker;

import freemarker.template.TemplateModel;

import java.util.List;

/**
 * @author elwood
 */
public class AdditionalParamsTemplateModel implements TemplateModel {
    private final List additionalParams;

    public AdditionalParamsTemplateModel(List additionalParams) {
        this.additionalParams = additionalParams;
    }

    public List getAdditionalParams() {
        return additionalParams;
    }
}
