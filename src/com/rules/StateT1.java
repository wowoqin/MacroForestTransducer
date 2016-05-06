package com.rules;

import com.XPath.PathParser.ASTPath;
import com.XPath.PathParser.AxisType;
import com.ibm.actor.AbstractActor;
import com.ibm.actor.Actor;
import com.ibm.actor.DefaultMessage;
import com.ibm.actor.Message;

import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

/**
 * Created by qin on 2015/10/10.
 */
public class StateT1 extends State implements Cloneable {

    protected  ASTPath _path;
    protected  String  _test;

    protected Stack _predstack;
    protected Stack _pathstack;



    protected StateT1(ASTPath path) {
        _path = path;
        _test=_path.getFirstStep().getNodeTest().toString();
    }


    @Override
    public String getNodeTest() {//得到当前 XPath 的测试节点
        return _path.getFirstStep().getNodeTest().toString();
    }

    public static State TranslateStateT1(ASTPath path) {
        //根据轴类型、剩余path选择性的调用相应的T1规则
        if (path.getFirstStep().getAxisType() == AxisType.PC) {//PC 轴
            if (path.getRemainderPath().toString().equals("")){ //无后续路径
                if (path.getFirstStep().getPreds().toString().equals("")){//无谓词
                    return StateT1_1.TranslateState(path);     //T1_1
                }
                else{
                    return StateT1_2.TranslateState(path);//有谓词
                }
            }
            else {
                if (path.getFirstStep().getPreds().toString().equals("")){//有后续路径，无谓词
                    return StateT1_5.TranslateState(path);
                }
                else{
                    return StateT1_6.TranslateState(path);//有谓词
                }
            }
        }
        //AD 轴
        else{
            if (path.getRemainderPath().toString().equals("")){//无后续路径
                if (path.getFirstStep().getPreds().toString().equals("")) //无谓词
                    return StateT1_3.TranslateState(path);
                else return StateT1_4.TranslateState(path);//有谓词
            }
            else {
                if (path.getFirstStep().getPreds().toString().equals(""))//有后续路径，无谓词
                    return StateT1_7.TranslateState(path);
                else return StateT1_8.TranslateState(path);//有谓词
            }
        }
    }


    public State copy() throws CloneNotSupportedException {
        return (State)this.clone();
    }

    @Override
    public void startElementDo(String tag, int layer,MyStateActor curactor) throws CloneNotSupportedException {}


    @Override
    public void endElementDo(String tag,int layer,MyStateActor curactor){}

    public void processSelfEndTag(int layer,MyStateActor curactor){
        WaitTask wtask;
        ActorTask atask;
        Stack currstack=curactor.getMyStack();
        int id=((ActorTask)currstack.peek()).getId(); // 当前栈顶 task 的 id
        List list=curactor.getTlist();//当前actor的list

        for(int i=0;i<list.size();i++){
            wtask = (WaitTask)list.get(i);
            if (wtask.getId()==layer) {//找到id==layer的 wt
                if (wtask.isSatisfied()) {//当前 wt 满足输出条件
                    if(curactor.getName().equals("stackActor")){//在stack中==>PC轴
                        if(currstack.size()==1){//输出
                            curactor.output(wtask);//也许有多个输出，此时不return；
                        }else {//在stack中 && 作为T1-5的后续path
                            //则把(wt.id，wt.getPathR())给自己这个list中id=wt.id的 wt1
                            for(int j=0;j<i;j++){
                                WaitTask wtask1 = (WaitTask) list.get(j);
                                if(wtask1.getId()==id){//找到相同id的 wt，把结果传给 wt
                                    atask=new ActorTask(id,wtask.getPathR());
                                    dmessage=new DefaultMessage("paResult",atask);
                                    actorManager.send(dmessage,curactor,curactor);
                                    //此时需要跳出循环，是因为也许会T1-5下面有多个符合的T1-1
                                    // /a/b : 若a下面有多个b，找到最后一个时，前面已经有很多的相同id的(0,true,b)了，
                                    //       它们的id都等于0，所以此时就不需要再把之前已经检查好的wt再次赋值pathResult了
                                    curactor.removeWTask(wtask);
                                    return;//跳出这个方法（即跳出了小循环j，又跳出了大循环i）
                                }
                            }
                        }
                    }else { //作为AD 轴后续 path 的一部分
                        boolean isFind=false;
                        atask=new ActorTask(id,wtask.getPathR());
                        for(int j=0;j<i;j++){//在自己的list中找相同id的wt
                            WaitTask wtask1 = (WaitTask) list.get(j);
                            if(wtask1.getId()==id){//找到了==>不在stack中 && 作为T1-5的后续path
                                dmessage=new DefaultMessage("paResult",atask);
                                actorManager.send(dmessage, curactor, curactor);
                                isFind=true;
                                break;
                            }
                        }
                        if(!isFind){//在T1-6、T1-7、T1-8的path栈中
                            dmessage=new DefaultMessage("paResult",atask);
                            actorManager.send(dmessage,curactor,curactor.getResActor());
                        }
                        curactor.removeWTask(wtask);
                        return;
                    }
                }
                //到自己的结束标签，不管当前wt是否满足，都要删除
                curactor.removeWTask(wtask);
            }
            //id!=layer,则下一次循环
        }
    }
}
