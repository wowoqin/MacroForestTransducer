package com.taskmodel;

/**
 * Created by qin on 2016/5/30.
 */
public class PathWait {
    protected boolean isReturn;
    protected String value;

    public PathWait(boolean isReturn, String value) {
        this.isReturn = isReturn;
        this.value = value;
    }

    public boolean isReturn() {
        return isReturn;
    }

    public String getValue() {
        return value;
    }

    public void setIsReturn(boolean isReturn) {
        this.isReturn = isReturn;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
