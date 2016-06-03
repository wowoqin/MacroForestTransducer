package com.taskmodel;

import com.ibm.actor.DefaultMessage;

/**
 * Created by qin on 2016/3/28.
 */
public class WaitTask {   // 在 actor 的list 中添加的任务
    protected  int id;          // id
    protected  Boolean predR;   // 谓词的返回结果
    protected  String  pathR;   // 后续 path 的返回结果或者是preds'的返回结果

    /*
    * 在此：T1-1 ~ T1-4 : pathR 中存放第一步的匹配结果 --> test
    *      T1-5 ~ T1-8 : pathR 中存放后续 path 的检查结果
    *      T2-1 ~ T2-4 : pathR 中直接存放“True”
    *      T3-1 ~ T3-4 : pathR 中存放 preds'的检查结果
    *
    *      返回的检查结果（ActorTask）的 id 与 tlist 中的等待任务模型（WaitTask）的id 相匹配
    * */

    public WaitTask(int id, Boolean predR, String pathR) {
        this.id = id;
        this.predR = predR;
        this.pathR = pathR;
    }

    public Boolean getPredR() {
        return predR;
    }

    public int getId() {
        return id;
    }

    public String getPathR() {
        return pathR;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPredR(Boolean predR) {
        this.predR = predR;
    }

    public void setPathR(String pathR) {
        this.pathR = pathR;
    }

    public boolean isPredsTrue(){
        if (getPredR()==true)
            return true;
        return false;
    }

    public boolean hasReturned(){
        return (getPredR()!=null && getPathR()!=null);
    }

    public boolean isSatisfiedOut() { // 检查当前 waitTask 是不是已经满足输出条件（可以进行输出操作了）
        return (isPredsTrue() && (getPathR()!=null) && (!getPathR().equals("NF")));
    }

    public  boolean isPredsSatisified(){ //wt作为一个谓词，检查成功 (id,true,"true")
        return (isPredsTrue() && getPathR().equals("true"));
    }

    public  boolean isWaitT3FirstPreds(){ //wt作为一个谓词T3，q''成功，q'''还没检查成功 //(id,false,"true")
        return (!isPredsTrue() && getPathR().equals("true"));
    }

    public  boolean isWaitT3ParallPreds(){ //wt作为一个谓词T3，q'''成功，q''还没检查成功 //(id,true,"false")
        return (isPredsTrue() && getPathR()==null);
    }

    public void output(){ //输出最终的检查结果
        System.out.println(this.getPathR());
    }
}
