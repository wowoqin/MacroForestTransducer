package com.rules;

import com.XPath.PathParser.ASTPath;
import com.XPath.PathParser.ASTPreds;
import com.ibm.actor.Actor;
import com.ibm.actor.DefaultMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

/**
 * Created by qin on 2015/10/10.
 */
public class StateT2_1 extends StateT2 {

    protected StateT2_1(ASTPreds preds) {
        super(preds);
    }

    public static StateT2 TranslateState(ASTPreds preds) {//重新创建T2-1
        return new StateT2_1(preds);
    }

    public void startElementDo(String tag, int layer, MyStateActor curactor) { // layer 是当前 tag 的层数

        if ((getLevel() == layer) && (tag.equals(_test))) {// T2-1 检查成功
            Stack ss=curactor.getMyStack();
            //发送谓词结果 && pop 当前栈顶
            curactor.popFunction();
            curactor.sendPredsResult(new ActorTask(((ActorTask) ss.peek()).getId(), true));
            if(ss.isEmpty())
                actorManager.detachActor(curactor);
        }
    }

    public void endElementDo(String tag, int layer, MyStateActor curactor) {
        // 遇到上层结束标签，谓词检查失败，弹栈 && remove 等待当前栈顶T2-1结果的 wt
        if (layer == getLevel() - 1) {
            Stack ss = curactor.getMyStack();
            //pop(T2-1)
            curactor.popFunction();
            //当前栈不为空，栈顶进行endElementDo 操作（输出（T1-2或者T1-6）/弹栈（相同结束标签的waitState）等）
            if (!ss.isEmpty()) {
                ((State) (((ActorTask) ss.peek()).getObject())).endElementDo(tag, layer, curactor);
            } else {
                actorManager.detachActor(curactor);
            }
        }
    }
}




