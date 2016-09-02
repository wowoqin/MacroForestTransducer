package com.rules;

import com.XPath.PathParser.ASTPreds;
import com.ibm.actor.Actor;
import com.ibm.actor.DefaultMessage;
import com.taskmodel.ActorTask;
import com.taskmodel.WaitTask;

import java.util.List;
import java.util.Stack;

/**
 * Created by qin on 2015/10/10.
 */
public class StateT3_2 extends StateT3{
    protected  State _q2;//检查【child::test preds】
    protected  State _q3;//检查preds'

    protected StateT3_2(ASTPreds preds, State _q2, State _q3) {
        super(preds);
        this._q2 = _q2;
        this._q3 = _q3;
    }

    public static StateT3 TranslateState(ASTPreds preds){//重新创建T3-2
        State q2= StateT2.TranslateStateT2(StateT3.getSinglePred(preds));
        State q3= StateT3.TranslateStateT3(preds.getRemainderPreds());
        return new StateT3_2(preds,q2,q3);
    }
}
