package org.mybatis.scripting.freemarker;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.Environment;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Test of using FreeMarker in annotations-driven mapper.
 *
 * @author elwood
 */
public class FreeMarkerInAnnotationsTest {
    protected static SqlSessionFactory sqlSessionFactory;

    @BeforeClass
    public static void setUp() throws Exception {
        Class.forName("org.hsqldb.jdbcDriver");

        JDBCDataSource dataSource = new JDBCDataSource();
        dataSource.setUrl("jdbc:hsqldb:mem:db1");
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

        configuration.addMapper(NameMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    }

    @Test
    public void testNoParamsCall() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            NameMapper mapper = sqlSession.getMapper(NameMapper.class);
            List<Name> allNames = mapper.getAllNames();
            Assert.assertTrue(allNames.size() == 5);
        }
    }

    @Test
    public void testMyBatisParamCall() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            NameMapper mapper = sqlSession.getMapper(NameMapper.class);
            Name pebble = mapper.findName("Pebbles");
            Assert.assertTrue(pebble != null && pebble.getFirstName().equals("Pebbles"));
        }
    }

    @Test
    public void testInQuery() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            NameMapper mapper = sqlSession.getMapper(NameMapper.class);
            List<Name> namesByIds = mapper.findNamesByIds(new ArrayList<Integer>() {{
                                                              add(1);
                                                              add(3);
                                                              add(4);
                                                          }}
            );
            Assert.assertTrue(namesByIds.size() == 3);
        }
    }

    @Test
    public void testParamObject() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            NameMapper mapper = sqlSession.getMapper(NameMapper.class);
            Name name = mapper.find(new NameParam(4));
            Assert.assertTrue(name != null && name.getId() == 4);
        }
    }

    @Test
    public void testStringParam() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            NameMapper mapper = sqlSession.getMapper(NameMapper.class);
            List<Name> stdLangResult = mapper.getNamesOddBehaviourStdLang("Pebbles");
            List<Name> freeMarkerLangResult = mapper.getNamesOddBehaviourFreeMarkerLang("Pebbles");
            Assert.assertTrue(stdLangResult.size() == 1);
            Assert.assertTrue(stdLangResult.get(0).getFirstName().equals("Pebbles"));
            Assert.assertTrue(freeMarkerLangResult.size() == 1);
            Assert.assertTrue(freeMarkerLangResult.get(0).getFirstName().equals("Pebbles"));
        }
    }
}
