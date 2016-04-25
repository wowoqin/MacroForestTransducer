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
public class StateT2_3 extends StateT2{

    protected  StateT2_3(ASTPreds preds){
        super(preds);
    }

    public static StateT2 TranslateState(ASTPreds preds){//重新创建T2-3
        return new StateT2_3(preds);
    }

    public void startElementDo(String tag,int layer,MyStateActor curactor) {// layer 是当前 tag 的层数
        if((getLevel()>=layer) && (tag.equals(_test))){
            Stack ss=curactor.getMyStack();
            int id =((ActorTask) ss.peek()).getId(); // 当前栈顶 task 的 id
            ActorTask task = new ActorTask(id, true);
            //发送谓词结果 && pop 当前栈顶
            curactor.sendPredsResult(task);
            if(ss.isEmpty())
                actorManager.detachActor(curactor);
        }
    }

    //谓词要是能自己遇见上层结束标签，则表明自己是检查失败的
    public void endElementDo(String tag,int layer,MyStateActor curactor){
        // 遇到上层结束标签，谓词检查失败，弹栈 && remove 等待当前栈顶T2-3结果的 wt
        if(layer==getLevel()-1){
            Stack ss=curactor.getMyStack();
            ActorTask atask=(ActorTask)ss.peek();
            //remove 等待当前栈顶T2-3结果的 wt-->与atask的id相等
            curactor.FindAndRemoveFailedWTask(atask);
            //pop(T2-3)
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

