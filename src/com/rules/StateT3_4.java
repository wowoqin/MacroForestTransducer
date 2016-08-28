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
public class StateT3_4 extends StateT3{
    protected  State _q2;//��顾desc_or_self::test preds��
    protected  State _q3;//���preds'

    protected StateT3_4(ASTPreds preds, State _q2, State _q3) {
        super(preds);
        this._q2 = _q2;
        this._q3 = _q3;
    }

    public static StateT3 TranslateState(ASTPreds preds){//���´���T3-4
        State q2= StateT2.TranslateStateT2(StateT3.getSinglePred(preds));
        State q3= StateT3.TranslateStateT3(preds.getRemainderPreds());
        return new StateT3_4(preds,q2,q3);
    }
}

