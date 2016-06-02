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

    public String getNodeTest(){//�õ���ǰ preds �Ĳ��Խڵ�
        return _preds.getFirstStep().getNodeTest().toString();
    }

    public static ASTPreds getSinglePred(ASTPreds preds){
    //�õ���ǰν�ʵĵĵ�һ��ν�ʣ�Ҳ���ǡ�child::test preds�����ߡ�desc_or_self::test preds��
        ASTPreds single = new ASTPreds();
        single.setFirstStep(preds.getFirstStep());
        single.setRemainderPreds(ASTPreds.nil);
        return single;
    }

    public static State TranslateStateT3(ASTPreds preds){
    //����������ѡ���Եĵ���T3����
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
