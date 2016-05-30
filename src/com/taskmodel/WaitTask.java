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
        return (isPredsTrue() && getPathR().equals("false"));
    }

    public void output(){ //输出最终的检查结果
        System.out.println(this.getPathR());
    }

//    public void doNext(){
//        if (this.isSatisfiedOut()) {//当前 wt 满足输出条件
//            if(curactor.getName().equals("stackActor")){//在stack中
//                if(currstack.size()==1){//输出
//                    curactor.output(wtask);//也许有多个输出，此时不return；
//                    break;
//                }else {//在stack中 && 作为T1-5的后续path
//                    //则把(wt.id，wt.getValue())给自己这个list中id=wt.id的 wt1
//                    for(int j=i-1;j>=0;j--){
//                        if(((WaitTask) list.get(j)).getId()==id){//找到相同id的 wt，把结果传给 wt
//                            dmessage=new DefaultMessage("pathResult",new ActorTask(id,wtask.getPathR()));
//                            actorManager.send(dmessage,curactor,curactor);
//                            //此时需要跳出循环，是因为也许会T1-5下面有多个符合的T1-1
//                            // /a/b : 若a下面有多个b，遇到/b时，即找到最后一个b时，前面已经有很多的相同id的(0,true,b)了，
//                            //       它们的id都等于0，所以此时就不需要再把之前已经检查好的wt再次赋值pathResult了
//                            curactor.removeWTask(wtask);
//                            return;//跳出这个方法（即跳出了小循环j，又跳出了大循环i）
//                        }
//                    }
//                }
//            }else { //作为AD 轴后续 path 的一部分
//                if(isInSelf){
//                    dmessage=new DefaultMessage("pathResult",new ActorTask(id,wtask.getPathR()));
//                    actorManager.send(dmessage, curactor, curactor);
//                }else{
//                    dmessage=new DefaultMessage("paResult",new ActorTask(id,wtask.getPathR()));
//                    actorManager.send(dmessage,curactor,curactor.getResActor());
//                }
//                curactor.removeWTask(wtask);
//                return;//结束大循环
//            }
//        }else{//到自己的结束标签，当前wt不满足输出条件
//            curactor.removeWTask(wtask);
//            return;//结束大循环
//        }
//    }
}
