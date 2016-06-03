package com.rules;

import com.taskmodel.ActorTask;

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
        // 自己能遇到上层结束标签，谓词检查失败，弹栈 && remove 等待当前栈顶 qw 结果的 wt
        if (layer == getLevel() - 1) {
            Stack ss=curactor.getMyStack();
            ActorTask atask=((ActorTask) ss.peek());//(id,qw,isInself)
            int id=atask.getId();
            boolean isInSelf=atask.isInSelf();
            //pop(qw)
            curactor.popFunction();
            //发消息（id,false,isInself）
            curactor.sendPredsResult(new ActorTask(id, false, isInSelf));
            //当前栈不为空，栈顶进行endElementDo 操作（输出（T1-2或者T1-6）/弹栈（相同结束标签的waitState）等）
            if (!ss.isEmpty()) {
                atask=((ActorTask) ss.peek());
                State state=((State) (atask.getObject()));
                // T1-2 、T1-6的结束标签
                if(state instanceof StateT1_2 || state instanceof StateT1_6){
                    state.endElementDo(tag, layer, curactor);
                }else if(((state instanceof StateT2_3) || (state instanceof StateT2_4))
                                                && (!atask.isInSelf())){// T2-3 作为 AD 轴test的谓词
                    curactor.processSameADPred();
                }
            }else {
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
