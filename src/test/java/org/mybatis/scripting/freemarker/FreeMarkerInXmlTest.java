/**
 *    Copyright 2015-2017 the original author or authors.
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

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Test of using FreeMarker inside XML mapper config.
 *
 * @author elwood
 */
public class FreeMarkerInXmlTest {
  protected static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
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
  public void testNoParamsCall() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      List<Name> allNames = sqlSession.selectList("getAllNames");
      Assert.assertTrue(allNames.size() == 5);
    }
  }

  @Test
  public void testMyBatisParamCall() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      HashMap<String, Object> paramsMap = new HashMap<>();
      paramsMap.put("name", "Pebbles");
      Name pebble = sqlSession.selectOne("findName", paramsMap);
      Assert.assertTrue(pebble != null && pebble.getFirstName().equals("Pebbles"));
    }
  }

  @Test
  public void testInQuery() {
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
      Assert.assertTrue(namesByIds.size() == 3);
    }
  }

  @Test
  public void testParamObject() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Name name = sqlSession.selectOne("find", new NameParam(4));
      Assert.assertTrue(name != null && name.getId() == 4);
    }
  }

  @Test
  public void testStringParam() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      List<Name> stdLangResult = sqlSession.selectList("getNamesOddBehaviourStdLang", "Pebbles");
      List<Name> freeMarkerLangResult = sqlSession.selectList("getNamesOddBehaviourFreeMarkerLang", "Pebbles");
      Assert.assertTrue(stdLangResult.size() == 1);
      Assert.assertTrue(stdLangResult.get(0).getFirstName().equals("Pebbles"));
      Assert.assertTrue(freeMarkerLangResult.size() == 1);
      Assert.assertTrue(freeMarkerLangResult.get(0).getFirstName().equals("Pebbles"));
    }
  }
}
