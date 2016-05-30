package com.taskmodel;

/**
 * Created by qin on 2016/5/30.
 */
public class PathWTask {
    protected  int id;          // id
    protected PredWait predWait;
    protected PathWait pathWait;

    public PathWTask(int id, PredWait predWait, PathWait pathWait) {
        this.id = id;
        this.predWait = predWait;
        this.pathWait = pathWait;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public PathWait getPathWait() {
        return pathWait;
    }

    public PredWait getPredWait() {
        return predWait;
    }

    public void setPathWait(PathWait pathWait) {
        this.pathWait = pathWait;
    }

    public void setPredWait(PredWait predWait) {
        this.predWait = predWait;
    }

    public boolean getPredValue(){
        return this.getPredWait().getValue();
    }

    public String getPathValue(){
        return this.getPathWait().getValue();
    }

    public boolean prReturn(){
        return  this.getPredWait().isReturn;
    }

    public boolean paReturn(){
        return this.getPathWait().isReturn;
    }

    public boolean isPathWaitSatisfied(){
        if(paReturn() && getPathValue()!= null)
            return true;
        return false;
    }

    public boolean isPredWaitSatisfied(){
        if(prReturn() && getPredValue())
            return true;
        return false;
    }

    public boolean isSatisfied() { // ��鵱ǰ waitTask �ǲ����Ѿ�����������������Խ�����������ˣ�
        return (this.isPredWaitSatisfied() && this.isPathWaitSatisfied());
    }

    public  boolean isWaitT3FirstPreds(){ //ν��T3��q''�ɹ���q'''��û���ɹ� //(id,false,"true")
        return false;
    }

    public  boolean isWaitT3ParallPreds(){ //ν��T3��q'''�ɹ���q''��û���ɹ� //(id,true,"false")
        return false;
    }

    public void output(){ //������յļ����
        System.out.println(this.getPathValue());
    }

}
