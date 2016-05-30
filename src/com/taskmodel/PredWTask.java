package com.taskmodel;

/**
 * Created by qin on 2016/5/30.
 */
public class PredWTask {
    protected  int id;          // id
    protected PredWait predWait;
    protected PredWait ppredWait;

    public PredWTask(int id, PredWait predWait, PredWait ppredsWait) {
        this.id = id;
        this.predWait = predWait;
        this.ppredWait = ppredsWait;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public PredWait getPredWait() {
        return predWait;
    }

    public PredWait getPPredWait() {
        return ppredWait;
    }

    public void setPredWait(PredWait predWait) {
        this.predWait = predWait;
    }

    public void setPPredWait(PredWait ppredWait) {
        this.ppredWait = ppredWait;
    }

    public boolean getPredValue(){
        return this.getPredWait().getValue();
    }

    public boolean getPPredValue(){
        return this.getPPredWait().getValue();
    }

    public boolean prReturn(){
        return  this.getPredWait().isReturn;
    }

    public boolean pprReturn(){
        return this.getPPredWait().isReturn;
    }

    public boolean isPPredWaitSatisfied(){
        return (pprReturn() && getPPredValue());
    }

    public boolean isPredWaitSatisfied(){
        return (prReturn() && getPredValue());
    }

    public  boolean isSatisified(){ //谓词检查成功
        return (this.isPredWaitSatisfied()&&this.isPPredWaitSatisfied());
    }

    public  boolean isWaitT3FirstPreds(){ //谓词T3，q''成功，q'''还没检查成功 //(id,false,true)
        if(!getPredValue()){
            if(pprReturn() && getPPredValue())
                return true;
        }
        return false;
    }

    public  boolean isWaitT3ParallPreds(){ //谓词T3，q'''成功，q''还没检查成功 //(id,true,false)
        if(!getPPredValue()){
            if(prReturn() && getPredValue())
                return true;
        }
        return false;
    }
}
