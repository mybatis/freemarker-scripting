package org.mybatis.scripting.freemarker;

import freemarker.template.TemplateModel;

import java.util.List;

/**
 * Just a wrapper for list of generated params. Only to be able to return this object from
 * {@link freemarker.template.TemplateHashModel#get(java.lang.String)} method.
 *
 * @author elwood
 */
public class GeneratedParamsTemplateModel implements TemplateModel {
    private final List generatedParams;

    public GeneratedParamsTemplateModel(List generatedParams) {
        this.generatedParams = generatedParams;
    }

    public List getGeneratedParams() {
        return generatedParams;
    }
}
