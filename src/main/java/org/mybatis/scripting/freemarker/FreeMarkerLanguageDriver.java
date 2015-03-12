package org.mybatis.scripting.freemarker;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Template;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Properties;

/**
 * Adds FreeMarker templates support to scripting in MyBatis.
 *
 * @author elwood
 */
public class FreeMarkerLanguageDriver implements LanguageDriver {
    /**
     * Base package for all FreeMarker templates.
     */
    public static final String basePackage;

    public static final String DEFAULT_BASE_PACKAGE = "";

    static {
        Properties properties = new Properties();
        try {
            try (InputStream stream = FreeMarkerLanguageDriver.class.getClassLoader()
                    .getResourceAsStream("mybatis-freemarker.properties")) {
                if (stream != null) {
                    properties.load(stream);
                    basePackage = properties.getProperty("basePackage", DEFAULT_BASE_PACKAGE);
                } else {
                    basePackage = DEFAULT_BASE_PACKAGE;
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Creates a {@link ParameterHandler} that passes the actual parameters to the the JDBC statement.
     *
     * @see DefaultParameterHandler
     * @param mappedStatement The mapped statement that is being executed
     * @param parameterObject The input parameter object (can be null)
     * @param boundSql The resulting SQL once the dynamic language has been executed.
     */
    @Override
    public ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        // As default XMLLanguageDriver
        return new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
    }

    /**
     * Creates an {@link SqlSource} that will hold the statement read from a mapper xml file.
     * It is called during startup, when the mapped statement is read from a class or an xml file.
     *
     * @param configuration The MyBatis configuration
     * @param script XNode parsed from a XML file
     * @param parameterType input parameter type got from a mapper method or specified in the parameterType xml attribute. Can be null.
     */
    @Override
    public SqlSource createSqlSource(Configuration configuration, XNode script, Class<?> parameterType) {
        return createSqlSource(configuration, script.getNode().getTextContent());
    }

    /**
     * Creates an {@link SqlSource} that will hold the statement read from an annotation.
     * It is called during startup, when the mapped statement is read from a class or an xml file.
     *
     * @param configuration The MyBatis configuration
     * @param script The content of the annotation
     * @param parameterType input parameter type got from a mapper method or specified in the parameterType xml attribute. Can be null.
     */
    @Override
    public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType) {
        return createSqlSource(configuration, script);
    }

    private SqlSource createSqlSource(Configuration configuration, String scriptText) {
        Template template;
        freemarker.template.Configuration cfg = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_22);
        cfg.setNumberFormat("computer");
        if (scriptText.trim().contains(" ")) {
            // Consider that script is inline script
            try {
                template = new Template(null, new StringReader(scriptText), cfg);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            // Consider that script is template name, trying to find the template in classpath
            TemplateLoader templateLoader = new ClassTemplateLoader(this.getClass().getClassLoader(), basePackage);
            cfg.setTemplateLoader(templateLoader);
            try {
                template = cfg.getTemplate(scriptText.trim());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return new FreeMarkerSqlSource(template, configuration);
    }
}
