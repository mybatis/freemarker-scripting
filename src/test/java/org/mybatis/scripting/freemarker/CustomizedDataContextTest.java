package org.mybatis.scripting.freemarker;

import freemarker.template.SimpleScalar;
import freemarker.template.Template;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.Reader;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * @author elwood
 */
public class CustomizedDataContextTest {
    protected static SqlSessionFactory sqlSessionFactory;

    @BeforeClass
    public static void setUp() throws Exception {
        Class.forName("org.hsqldb.jdbcDriver");

        JDBCDataSource dataSource = new JDBCDataSource();
        dataSource.setUrl("jdbc:hsqldb:mem:db4");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        try (Connection conn = dataSource.getConnection()) {
            try (Reader reader = Resources.getResourceAsReader("org/mybatis/scripting/freemarker/create-db.sql")) {
                ScriptRunner runner = new ScriptRunner(conn);
                runner.setLogWriter(null);
                runner.setErrorLogWriter(null);
                runner.runScript(reader);
                conn.commit();
            }
        }

        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("development", transactionFactory, dataSource);

        // You can call configuration.setDefaultScriptingLanguage(FreeMarkerLanguageDriver.class)
        // after this to use FreeMarker driver by default.
        Configuration configuration = new Configuration(environment);

        configuration.addMapper(CustomizedDataContextMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    }

    public static class CustomSqlSource extends FreeMarkerSqlSource {
        public CustomSqlSource(Template template, Configuration configuration) {
            super(template, configuration);
        }

        @Override
        protected Object preProcessDataContext(Object dataContext, boolean isMap) {
            dataContext = super.preProcessDataContext(dataContext, isMap);
            if (isMap) {
                ((Map<String, Object>) dataContext).put("MY_NAME", new SimpleScalar("Barney"));
            } else {
                ((ParamObjectAdapter) dataContext).putAdditionalParam("MY_NAME", new SimpleScalar("Barney"));
            }
            return dataContext;
        }
    }

    public static class CustomFreeMarkerLanguageDriver extends FreeMarkerLanguageDriver {
        @Override
        protected SqlSource createSqlSource(Template template, Configuration configuration) {
            return new CustomSqlSource(template, configuration);
        }
    }

    @Test
    public void test() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CustomizedDataContextMapper mapper = sqlSession.getMapper(CustomizedDataContextMapper.class);
            List<Name> names = mapper.find();
            Assert.assertTrue(names.size() == 1);
        }
    }
}
