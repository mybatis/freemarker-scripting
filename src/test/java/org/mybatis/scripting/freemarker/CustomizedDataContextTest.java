/**
 *    Copyright 2015-2019 the original author or authors.
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

import freemarker.template.SimpleScalar;
import freemarker.template.Template;
import freemarker.template.Version;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * @author elwood
 */
public class CustomizedDataContextTest {
  protected static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
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
    public CustomSqlSource(Template template, Configuration configuration, Version version) {
      super(template, configuration, version);
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
      return new CustomSqlSource(template, configuration, driverConfig.getIncompatibleImprovementsVersion());
    }
  }

  @Test
  public void test() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      CustomizedDataContextMapper mapper = sqlSession.getMapper(CustomizedDataContextMapper.class);
      List<Name> names = mapper.find();
      Assertions.assertTrue(names.size() == 1);
    }
  }
}
