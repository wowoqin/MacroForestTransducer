package com.rules;

import java.util.Stack;

/**
 * Created by qin on 2016/4/18.
 */
public class WaitState extends State {
    @Override
    public void startElementDo(String tag, int layer, MyStateActor curactor) throws CloneNotSupportedException {

    }

    @Override
    public void endElementDo(String tag, int layer, MyStateActor curactor) {
        // 遇到上层结束标签，谓词检查失败，弹栈 && remove 等待当前栈顶 qw 结果的 wt
        if(layer==getLevel()-1){
            Stack ss=curactor.getMyStack();
            ActorTask atask=(ActorTask)ss.peek();
            //remove 等待当前栈顶 qw 结果的 wt
            curactor.FindAndRemoveFailedWTask(atask);
            //pop(qw)
            curactor.popFunction();
            //当前栈不为空，若栈顶为stateT1，栈顶进行endElementDo 操作（输出/弹栈等）
            if (!ss.isEmpty()) {
                ((State) (((ActorTask) ss.peek()).getObject())).endElementDo(tag, layer, curactor);
            } else {
                actorManager.detachActor(curactor);
            }
        }
    }

    @Override
    public String getNodeTest() {
        return null;
    }

    @Override
    public Object copy() throws CloneNotSupportedException {
        return null;
    }
}
