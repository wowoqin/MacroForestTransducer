package com.rules;

import com.XPath.PathParser.ASTPreds;
import com.taskmodel.ActorTask;
import com.taskmodel.WaitTask;

import java.util.List;
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

    public void startElementDo(String tag, int layer, MyStateActor curactor) throws CloneNotSupportedException { // layer 是当前 tag 的层数
        if ((getLevel() == layer) && (tag.equals(_test))) {// T2-1 检查成功
            Stack ss=curactor.getMyStack();
            ActorTask atask=((ActorTask) ss.peek());//(id,T2-1,inInSelf)
            int id=atask.getId();
            boolean isInSelf=atask.isInSelf();

            List list=this.getList();//T2-1.list
            if(!list.isEmpty()){  //T3-1
                WaitState waitState=new WaitState();
                waitState.setLevel(((State) atask.getObject()).getLevel());
                waitState.getList().add(list.get(0));
                curactor.popFunction();
                //(id,T2-1,isInself) 换为 （id,qw,isInself）
                curactor.pushTaskDo(new ActorTask(id, waitState, isInSelf));
                //设置 T3-1.q'''检查成功（发消息是因为万一q''已经是检查成功的了呢）
                curactor.sendPredsResult(new ActorTask(id, true, true));//确定是给自己的
            }else{  //T2-1
                //发送谓词结果 && pop 当前栈顶
                curactor.popFunction();
                curactor.sendPredsResult(new ActorTask(id, true,isInSelf));
                if(ss.isEmpty())
                    actorManager.detachActor(curactor);
            }
        }
    }

    public void endElementDo(String tag, int layer, MyStateActor curactor) {
        if (layer == getLevel() - 1) {//遇到上层结束标签
            Stack ss=curactor.getMyStack();
            ActorTask atask=((ActorTask) ss.peek());//栈顶(id,T2-1,isInself)
            //pop(T2-1)
            curactor.popFunction();
            //发消息（id,false,isInself）
            curactor.sendPredsResult(new ActorTask(atask.getId(),false, atask.isInSelf()));
            //其实在此处还应该看T3-1.q''还在做检查否？是-->terminate

            //当前栈不为空，栈顶为 T1-2或者T1-6-->进行endElementDo 操作:输出/上传/remove/等待
            if (!ss.isEmpty()) {
                State state=((State) (((ActorTask) ss.peek()).getObject()));
                // T1-2 、T1-6的结束标签
                if(state instanceof StateT1_2 || state instanceof StateT1_6){
                    state.endElementDo(tag, layer, curactor);
                }
            }else {
                actorManager.detachActor(curactor);
            }
        }
    }
}




