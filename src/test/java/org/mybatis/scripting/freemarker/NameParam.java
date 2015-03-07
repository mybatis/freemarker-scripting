package org.mybatis.scripting.freemarker;

/**
 * Class to test queries using parameter objects.
 *
 * @author elwood
 */
public class NameParam {
    private int id;

    public NameParam(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
