package com.rules;

import com.XPath.PathParser.ASTPreds;
import com.taskmodel.ActorTask;

import java.util.List;
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

    public void startElementDo(String tag,int layer,MyStateActor curactor) throws CloneNotSupportedException {// layer 是当前 tag 的层数
        if((layer >= getLevel()) && (tag.equals(_test))){
            Stack ss=curactor.getMyStack();
            ActorTask atask=((ActorTask) ss.peek());
            int id=atask.getId();
            boolean isInSelf=atask.isInSelf();

            List list=this.getList();
            if(!list.isEmpty()){  //T3-3 && T3-3.q'''检查成功
                WaitState waitState=new WaitState();
                waitState.setLevel(((State) atask.getObject()).getLevel());
                waitState.getList().add(list.get(0));
                curactor.popFunction();
                //(id,T2-3,isInself) 换为 （id,qw,isInself）
                curactor.pushTaskDo(new ActorTask(id, waitState, isInSelf));
                //设置 T3-3.q'''检查成功-->
                curactor.sendPredsResult(new ActorTask(id, true, true));//确定是给自己的
                /*在谓词全部满足或者q''不满足弹栈的时候--> 栈顶为(id,qw,isInSelf)：
                 *  1. 满足：wt（layer,true,"true"）
                *   2. 不满足：wt（layer,true,"false"）
                *                          弹 qw 了之后：：：-->栈顶(id,T2-3,false)
                * */
            }else{  //T2-3
                //发送谓词结果 && pop 当前栈顶
                curactor.popFunction();
                curactor.sendPredsResult(new ActorTask(id, true, isInSelf));
                if(ss.isEmpty()){
                    actorManager.detachActor(curactor);
                }else{
                    //pop 完了之后还是T2-3 && T2-3 是要传给上级actor的
                    atask=((ActorTask) ss.peek());
                    State state=(State)atask.getObject();
                    if((state instanceof StateT2_3) && !isInSelf){// T2-3 作为 AD 轴test的谓词
                       curactor.processSameADPred();
                    }
                }
            }
        }
    }

    //谓词要是能自己遇见上层结束标签，则表明自己是检查失败的
    public void endElementDo(String tag,int layer,MyStateActor curactor){
        // 自己能遇到上层结束标签，谓词检查失败，弹栈 && remove 等待当前栈顶T2-3结果的 wt
        if (layer == getLevel() - 1) {
            Stack ss=curactor.getMyStack();
            ActorTask atask=((ActorTask) ss.peek());//(id,T2-3,isInself)
            int id=atask.getId();
            boolean isInSelf=atask.isInSelf();
            //pop(id,T2-3,isInself)
            curactor.popFunction();
            //发消息（id,false,isInself）
            curactor.sendPredsResult(new ActorTask(id, false, isInSelf));
            //当前栈不为空，栈顶进行endElementDo 操作（输出（T1-2或者T1-6）/弹栈（相同结束标签的waitState）等）
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

