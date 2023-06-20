/*
 *    Copyright 2015-2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
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
import java.util.Optional;

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
 * Test of using FreeMarker to generate prepared statements parameters.
 *
 * @author s-nakao
 */
class PreparedDatabaseIdParamsTest {
  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    Class.forName("org.hsqldb.jdbcDriver");

    JDBCDataSource dataSource = new JDBCDataSource();
    dataSource.setUrl("jdbc:hsqldb:mem:db5");
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

    // set databaseId. default null
    // If it is a property, please refer to the following
    // https://mybatis.org/mybatis-3/ja/configuration.html#databaseIdProvider.
    configuration.setDatabaseId("hsqldb");

    configuration.addMapper(PreparedDatabaseIdParamsMapper.class);
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
  }

  @Test
  void testReferDatabaseIdInTemplate() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      PreparedDatabaseIdParamsMapper mapper = sqlSession.getMapper(PreparedDatabaseIdParamsMapper.class);
      Optional<Name> nameList = mapper.getDatabaseIdTest();
      Assertions.assertTrue(nameList.isPresent());
      Assertions.assertEquals("Fred", nameList.get().getFirstName());
      Assertions.assertEquals("Flintstone", nameList.get().getLastName());
    }
  }

  @Test
  void testReferDatabaseIdInTemplateWithParam() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      PreparedDatabaseIdParamsMapper mapper = sqlSession.getMapper(PreparedDatabaseIdParamsMapper.class);
      Optional<Name> nameList = mapper.getDatabaseIdTestWithParam(new PreparedParam());
      Assertions.assertTrue(nameList.isPresent());
      Assertions.assertEquals("Fred", nameList.get().getFirstName());
      Assertions.assertEquals("Flintstone", nameList.get().getLastName());
    }
  }

}
