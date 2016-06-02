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
public class StateT3_1 extends StateT3{
    protected  State _q2;//检查【child::test】
    protected  State _q3;//检查preds'

    protected  StateT3_1(ASTPreds preds,State q2,State q3){
        super(preds);
        _q2=q2;
        _q3=q3;
        _q2.setLevel(this.getLevel());//q2 检查【child::test】，应该匹配的标签的层数 不变
        _q3.setLevel(this.getLevel());// q3 检查preds'，应该匹配的标签的层数与当前 [test] 同一层
        this._predstack=new Stack();
    }

    public static StateT3 TranslateState(ASTPreds preds){//重新创建T3-1
        State q2=StateT2.TranslateStateT2(StateT3.getSinglePred(preds));
        State q3=StateT3.TranslateStateT3(preds.getRemainderPreds());
        return new StateT3_1(preds,q2,q3);
    }
    //无论成败，q3-1 都被替换，所以 q3-1 不会遇到上层结束标签
}

