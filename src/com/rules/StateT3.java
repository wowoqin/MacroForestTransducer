package com.rules;

import com.XPath.PathParser.ASTPreds;
import com.XPath.PathParser.AxisType;
import com.ibm.actor.Message;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

/**
 * Created by qin on 2015/10/10.
 */
public class StateT3 extends  State implements Cloneable{

    protected  ASTPreds _preds;
    protected  String   _test;

    protected Stack _predstack;


    protected StateT3(ASTPreds preds){
        _preds=preds;
        _test=_preds.getFirstStep().getNodeTest().toString();
        _predstack=new Stack();
    }

    public String getNodeTest(){//得到当前 preds 的测试节点
        return _preds.getFirstStep().getNodeTest().toString();
    }

    public static ASTPreds getSinglePred(ASTPreds preds){
    //得到当前谓词的的第一个谓词，也就是【child::test preds】或者【desc_or_self::test preds】
        ASTPreds single = new ASTPreds();
        single.setFirstStep(preds.getFirstStep());
        single.setRemainderPreds(ASTPreds.nil);
        return single;
    }

    public static State TranslateStateT3(ASTPreds preds){
    //根据轴类型选择性的调用T3规则
        if(preds.getRemainderPreds().toString().equals(""))
            return StateT2.TranslateStateT2(preds);

        if(preds.getFirstStep().getAxisType()== AxisType.PC)
        {
            if (preds.getFirstStep().getPreds().toString().equals(""))
                return StateT3_1.TranslateState(preds);
            return StateT3_2.TranslateState(preds);
        }
        if (preds.getFirstStep().getPreds().toString().equals(""))//AD
            return StateT3_4.TranslateState(preds);
        return StateT3_4.TranslateState(preds);

    }


    public State copy() throws CloneNotSupportedException {
        return (State)this.clone();
    }

    @Override
    public void startElementDo(String tag, int layer,MyStateActor curactor) throws CloneNotSupportedException {}

    public void endElementDo(String tag,int layer,MyStateActor curactor){}


}
