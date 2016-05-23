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
public class StateT2_4 extends StateT2 implements Cloneable{
    protected  State _q3;//检查 preds

    protected  StateT2_4(ASTPreds preds,State q3){
        super(preds);
        _q3=q3;
        _predstack=new Stack();
    }

    public static StateT2 TranslateState(ASTPreds preds){//重新创建T2-4
        State q3=StateT3.TranslateStateT3(preds.getFirstStep().getPreds());
        return new StateT2_4(preds,q3);
    }
    public void startElementDo(String tag,int layer,MyStateActor curactor) throws CloneNotSupportedException{
        if ((layer >= getLevel()) && (tag.equals(_test))) {
            curactor.addWTask(new WaitTask(layer,false,"true"));

            String name=((Integer)this._predstack.hashCode()).toString().concat("T2-4.prActor");
            Actor actor=(actors.get(name));// preds的 actor

            if(actor == null){// 若 prActor还没有创建 ，predstack 一定为空
                actor =actorManager.createAndStartActor(MyStateActor.class, name);

                dmessage=new DefaultMessage("stack", new ActorTask(this._predstack));
                actorManager.send(dmessage, curactor, actor);
                //发送 q'给 prActor
                _q3.setLevel(layer + 1);
                dmessage=new DefaultMessage("pushTask", new ActorTask(layer,_q3));
                actorManager.send(dmessage,curactor,actor);
            }else{
                State currQ=(State)_q3.copy();
                currQ.setLevel(layer + 1);
                dmessage=new DefaultMessage("pushTask",new ActorTask(layer,currQ));
                actorManager.send(dmessage, curactor, actor);
            }
        }
    }

    public void endElementDo(String tag,int layer,MyStateActor curactor) {
        // 遇到上层结束标签，谓词检查失败，弹栈 && remove 等待当前栈顶T2-4结果的 wt
        if(layer==getLevel()-1){
            Stack ss=curactor.getMyStack();
            ActorTask atask=(ActorTask)ss.peek();
            //remove 等待当前栈顶T2-4结果的 wt-->与atask的id相等
            //自己的 list 中找有无相同 id 的 wt(或许会有多个)
            boolean isFindInThis = false;
            WaitTask wtask;
            List list=curactor.getTlist();
            String name=((Integer)this._predstack.hashCode()).toString().concat("T2-4.prActor");
            //自己的 list 中找有无相同 id 的 wt(或许会有多个)
            if(!list.isEmpty()){
                for(int i=0;i<list.size();i++) {
                    wtask = (WaitTask) list.get(i);
                    if (wtask.getId() == atask.getId()) {
                        // T2-4 修饰 PC 轴path或者preds，与被修饰的path或者preds放在同一个栈
                        isFindInThis = true;
                        list.remove(wtask);
                        actorManager.detachActor(actors.get(name));
                    }
                }
            }
            //在自己所在的list中没有找到相同 id 的 wt，则在上级 actor 中找
            if(isFindInThis) {  //PC
                //pop(T2-4)
                curactor.popFunction();
                //当前栈不为空，栈顶进行endElementDo 操作（输出/弹栈等）
                if (!ss.isEmpty()) {    // T1-2/T1-6/waitState
                    ((State) (((ActorTask) ss.peek()).getObject())).endElementDo(tag, layer, curactor);
                }
            }else {// T2-4 修饰 AD 轴path或者preds，放在单独的谓词栈中
                MyStateActor resActor=(MyStateActor)curactor.getResActor();
                list= resActor.getTlist();
                for(int i=0;i<list.size();i++){
                    wtask=(WaitTask)list.get(i);
                    if(wtask.getId()==atask.getId())
                        resActor.removeWTask(wtask);
                }
                //pop(T2-4)
                curactor.popFunction();
                //当前栈不为空，栈顶进行endElementDo 操作（输出/弹栈等）
                if (!ss.isEmpty()) {    // waitState
                    ((State) (((ActorTask) ss.peek()).getObject())).endElementDo(tag, layer, curactor);
                } else {
                    actorManager.detachActor(curactor);
                    actorManager.detachActor(actors.get(name));
                }
            }
        }
    }
}