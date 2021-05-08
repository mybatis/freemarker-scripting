/*
 *    Copyright 2015-2021 the original author or authors.
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

/**
 * Class to test auto-generated prepared statement parameters.
 *
 * @author elwood
 */
public class PreparedParam {
  public static class InnerClass {
    private String strValue = "InnerString";

    public String getStrValue() {
      return strValue;
    }

    public void setStrValue(String strValue) {
      this.strValue = strValue;
    }
  }

  private InnerClass innerObject = new InnerClass();
  private Object nullValue = null;

  public InnerClass getInnerObject() {
    return innerObject;
  }

  public void setInnerObject(InnerClass innerObject) {
    this.innerObject = innerObject;
  }

  public Object getNullValue() {
    return nullValue;
  }

  public void setNullValue(Object nullValue) {
    this.nullValue = nullValue;
  }
}
