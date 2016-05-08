package com.rules;

import com.XPath.PathParser.ASTPreds;
import com.ibm.actor.Actor;
import com.ibm.actor.DefaultMessage;

import java.util.List;
import java.util.Queue;
import java.util.Stack;

/**
 * Created by qin on 2015/10/10.
 */
public class StateT2_2 extends StateT2{
    protected  State _q3;

    protected  StateT2_2(ASTPreds preds,State q3){
        super(preds);
        _q3=q3;
    }

    public static StateT2 TranslateState(ASTPreds preds){//重新创建T2-2
        State q3=StateT3.TranslateStateT3(preds.getFirstStep().getPreds());
        return new StateT2_2(preds,q3);
    }

    public void startElementDo(String tag,int layer,MyStateActor curactor) {
        if((getLevel()==layer) && (tag.equals(_test))){// T2-2 的test匹配
            curactor.addWTask(new WaitTask(layer,false,"true"));

            //检查的谓词的层数肯定是当前应该匹配层数所对应的标签的子孙的层数
            _q3.setLevel(getLevel() + 1);
            curactor.getMyStack().push(new ActorTask(layer,_q3));// 对于当前栈的压栈操作，就可以直接压栈（不用发送消息）

        }
    }


    public void endElementDo(String tag,int layer,MyStateActor curactor) {
        // 遇到上层结束标签，谓词检查失败，弹栈 && remove 等待当前栈顶T2-2结果的 wt
        if (layer == getLevel() - 1) {
            Stack ss = curactor.getMyStack();
            //pop(T2-2)
            curactor.popFunction();
            //当前栈不为空，栈顶进行endElementDo 操作（输出/弹栈等）
            if (!ss.isEmpty()) {
                ((State) (((ActorTask) ss.peek()).getObject())).endElementDo(tag, layer, curactor);
            } else {
                actorManager.detachActor(curactor);
            }
        }
    }
}
