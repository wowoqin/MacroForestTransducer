package com.rules;

import com.XPath.PathParser.ASTPath;
import com.XPath.PathParser.AxisType;
import com.ibm.actor.DefaultMessage;
import com.taskmodel.ActorTask;
import com.taskmodel.WaitTask;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * Created by qin on 2015/10/10.
 */
public class StateT1 extends State implements Cloneable {

    protected  ASTPath _path;
    protected  String  _test;
    protected  Stack   _predstack;
    protected  Stack   _pathstack;

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
        List list=curactor.getTlist();//当前actor的list
        for(int i=list.size()-1;i>=0;i--){
            WaitTask wtask = (WaitTask)list.get(i);
            if (wtask.getId()==layer){//找到id==layer的 wt
                if(wtask.hasReturned()){
                    curactor.doNext(wtask);
                }else{//到自己的结束标签,后续actor还没做完，则当前actro应该等其它actor做完再判断；
                    actorManager.awaitMessage(curactor);
                    while (wtask.hasReturned()) {
                        curactor.doNext(wtask);
                    }
                }
            }else if(wtask.getId()<layer){
                return;
            }
            //id>layer,则下一次循环
        }
    }

}
