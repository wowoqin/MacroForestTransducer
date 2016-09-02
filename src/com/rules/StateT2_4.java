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
public class StateT2_4 extends StateT2 implements Cloneable{
    protected  State _q3;//检查 preds

    protected  StateT2_4(ASTPreds preds,State q3){
        super(preds);
        _q3=q3;
        _q3.setLevel(this.getLevel()+1);
        _predstack=new Stack();
    }

    public static StateT2 TranslateState(ASTPreds preds){//重新创建T2-4
        State q3=StateT3.TranslateStateT3(preds.getFirstStep().getPreds());
        return new StateT2_4(preds,q3);
    }
    public void startElementDo(String tag,int layer,MyStateActor curactor) throws CloneNotSupportedException{
        if ((layer >= getLevel()) && (tag.equals(_test))) {
            // 等 q' 的结果
            addWTask(new WaitTask(layer, null, "true"));

            String name=((Integer)this._predstack.hashCode()).toString().concat("T2-4.prActor");
            if(this._predstack.isEmpty()){// 若 prActor还没有创建 ，predstack 一定为空
                curactor.createAnotherActor(name,this._predstack, new ActorTask(layer,_q3,false));
//                actor =actorManager.createAndStartActor(MyStateActor.class, name);
//                actors.put(actor.getName(),actor);
//
//                dmessage=new DefaultMessage("resActor", null);
//                actorManager.send(dmessage, curactor, actor);
//                //发送 q'给 prActor
//                _q3.setLevel(layer + 1);
//                actorManager.send(new DefaultMessage("pushTask", new ActorTask(layer,_q3,false)),
//                                                                                    curactor,actor);
            }else{
                State currQ=(State)_q3.copy();
                currQ.setLevel(layer + 1);
                dmessage=new DefaultMessage("pushTask",new ActorTask(layer,currQ,false));
//                for(int i=0;i<actors.length;i++){
//                    if(actors[i].getName().equals(name)){
//                        actorManager.send(dmessage, curactor, actors[i]);
//                        return;
//                    }
//                }
            }
        }
    }

    public void endElementDo(String tag,int layer,MyStateActor curactor) {
        if (layer == getLevel() - 1) {
            Stack ss=curactor.getMyStack();
            ActorTask atask=((ActorTask) ss.peek());//(id,T2-4,isInself)
            int id=atask.getId();
            boolean isInSelf=atask.isInSelf();
            //pop(T2-4)
            curactor.popFunction();
            //发消息（id,false,isInself）
            curactor.sendPredsResult(new ActorTask(id,false, isInSelf));
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