package com.taskmodel;

/**
 * Created by qin on 2016/5/30.
 */
public class PredWait {
    protected boolean isReturn;
    protected boolean value;

    public PredWait(boolean isReturn, boolean value) {
        this.isReturn = isReturn;
        this.value = value;
    }

    public boolean isReturn() {
        return isReturn;
    }

    public boolean getValue() {
        return value;
    }

    public void setIsReturn(boolean isReturn) {
        this.isReturn = isReturn;
    }

    public void setValue(boolean value) {
        this.value = value;
    }
}
