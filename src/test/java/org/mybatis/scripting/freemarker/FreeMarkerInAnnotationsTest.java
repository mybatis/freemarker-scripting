/*
 *    Copyright 2015-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.scripting.freemarker;

import java.io.Reader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test of using FreeMarker in annotations-driven mapper.
 *
 * @author elwood
 */
class FreeMarkerInAnnotationsTest {
  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
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
  void testNoParamsCall() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      NameMapper mapper = sqlSession.getMapper(NameMapper.class);
      List<Name> allNames = mapper.getAllNames();
      Assertions.assertEquals(5, allNames.size());
    }
  }

  @Test
  void testMyBatisParamCall() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      NameMapper mapper = sqlSession.getMapper(NameMapper.class);
      Name pebble = mapper.findName("Pebbles");
      Assertions.assertTrue(pebble != null && pebble.getFirstName().equals("Pebbles"));
    }
  }

  @Test
  void testInQuery() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      NameMapper mapper = sqlSession.getMapper(NameMapper.class);
      List<Name> namesByIds = mapper.findNamesByIds(new ArrayList<Integer>() {
        private static final long serialVersionUID = 1L;
        {
          add(1);
          add(3);
          add(4);
        }
      });
      Assertions.assertEquals(3, namesByIds.size());
    }
  }

  @Test
  void testParamObject() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      NameMapper mapper = sqlSession.getMapper(NameMapper.class);
      Name name = mapper.find(new NameParam(4));
      Assertions.assertTrue(name != null && name.getId() == 4);
    }
  }

  @Test
  void testStringParam() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      NameMapper mapper = sqlSession.getMapper(NameMapper.class);
      List<Name> stdLangResult = mapper.getNamesOddBehaviourStdLang("Pebbles");
      List<Name> freeMarkerLangResult = mapper.getNamesOddBehaviourFreeMarkerLang("Pebbles");
      Assertions.assertEquals(1, stdLangResult.size());
      Assertions.assertEquals("Pebbles", stdLangResult.get(0).getFirstName());
      Assertions.assertEquals(1, freeMarkerLangResult.size());
      Assertions.assertEquals("Pebbles", freeMarkerLangResult.get(0).getFirstName());
    }
  }
}
