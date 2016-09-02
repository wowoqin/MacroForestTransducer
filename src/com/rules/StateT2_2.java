package com.rules;

import com.XPath.PathParser.ASTPreds;
import com.taskmodel.ActorTask;
import com.taskmodel.WaitTask;

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

    public void startElementDo(String tag,int layer,MyStateActor curactor) throws CloneNotSupportedException {
        if((getLevel()==layer) && (tag.equals(_test))){// T2-2 的test匹配
            addWTask(new WaitTask(layer, null, "true"));
            _q3.setLevel(layer + 1);
            curactor.pushTaskDo(new ActorTask(layer, _q3, true));//确定是给自己的
        }
    }


    public void endElementDo(String tag,int layer,MyStateActor curactor) {
       /* 遇到自己的结束标签 && 进入endElementDo操作:
          1. 该actor之前的消息肯定已经处理完毕，不存在还在等待T2-2.q3结果的情况：因为 T2-2.q3 压在 T2-2的上面-->同一个actor
          2. 自己能遇到上层结束标签，谓词检查失败，弹栈 && remove 等待当前栈顶T2-2结果的 wt  */
        if (layer == getLevel() - 1) {
            Stack ss=curactor.getMyStack();
            ActorTask atask=((ActorTask) ss.peek());//(id,T2-2,isInself)
            int id=atask.getId();
            boolean isInSelf=atask.isInSelf();
            //pop(T2-2)
            curactor.popFunction();
            //发消息（id,false,isInself）
            curactor.sendPredsResult(new ActorTask(id, false, isInSelf));
            //当前栈不为空，栈顶进行endElementDo ：输出/上传/remove-->（T1-2或者T1-6）
            if (!ss.isEmpty()) {
                State state=((State) (((ActorTask) ss.peek()).getObject()));
                // T1-2 、T1-6的结束标签
                if(state instanceof StateT1_2 || state instanceof StateT1_6 ){
                    state.endElementDo(tag, layer, curactor);
                }
            }else {
                actorManager.detachActor(curactor);
            }
        }
    }
}
