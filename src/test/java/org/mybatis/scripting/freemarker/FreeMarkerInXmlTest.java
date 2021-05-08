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
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test of using FreeMarker inside XML mapper config.
 *
 * @author elwood
 */
class FreeMarkerInXmlTest {
  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    Connection conn = null;

    try {
      Class.forName("org.hsqldb.jdbcDriver");
      conn = DriverManager.getConnection("jdbc:hsqldb:mem:db2", "sa", "");

      Reader reader = Resources.getResourceAsReader("org/mybatis/scripting/freemarker/create-db.sql");

      ScriptRunner runner = new ScriptRunner(conn);
      runner.setLogWriter(null);
      runner.setErrorLogWriter(null);
      runner.runScript(reader);
      conn.commit();
      reader.close();

      reader = Resources.getResourceAsReader("org/mybatis/scripting/freemarker/mapper-config.xml");
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      reader.close();
    } finally {
      if (conn != null) {
        conn.close();
      }
    }
  }

  @Test
  void testNoParamsCall() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      List<Name> allNames = sqlSession.selectList("getAllNames");
      Assertions.assertEquals(5, allNames.size());
    }
  }

  @Test
  void testMyBatisParamCall() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      HashMap<String, Object> paramsMap = new HashMap<>();
      paramsMap.put("name", "Pebbles");
      Name pebble = sqlSession.selectOne("findName", paramsMap);
      Assertions.assertTrue(pebble != null && pebble.getFirstName().equals("Pebbles"));
    }
  }

  @Test
  void testInQuery() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      HashMap<String, Object> paramsMap = new HashMap<>();
      paramsMap.put("ids", new ArrayList<Integer>() {
        private static final long serialVersionUID = 1L;
        {
          add(1);
          add(3);
          add(4);
        }
      });
      List<Name> namesByIds = sqlSession.selectList("findNamesByIds", paramsMap);
      Assertions.assertEquals(3, namesByIds.size());
    }
  }

  @Test
  void testParamObject() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Name name = sqlSession.selectOne("find", new NameParam(4));
      Assertions.assertTrue(name != null && name.getId() == 4);
    }
  }

  @Test
  void testStringParam() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      List<Name> stdLangResult = sqlSession.selectList("getNamesOddBehaviourStdLang", "Pebbles");
      List<Name> freeMarkerLangResult = sqlSession.selectList("getNamesOddBehaviourFreeMarkerLang", "Pebbles");
      Assertions.assertEquals(1, stdLangResult.size());
      Assertions.assertEquals("Pebbles", stdLangResult.get(0).getFirstName());
      Assertions.assertEquals(1, freeMarkerLangResult.size());
      Assertions.assertEquals("Pebbles", freeMarkerLangResult.get(0).getFirstName());
    }
  }
}
