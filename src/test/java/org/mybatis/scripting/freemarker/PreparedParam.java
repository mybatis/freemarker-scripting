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
