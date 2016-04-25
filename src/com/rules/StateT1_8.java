package com.rules;

import com.XPath.PathParser.ASTPath;
import com.ibm.actor.Actor;
import com.ibm.actor.DefaultMessage;

import java.util.*;

/**
 * Created by qin on 2015/10/10.
 */
public class StateT1_8 extends StateT1 {
    protected State _q3;//检查 preds
    protected State _q1;//检查后续 path


    protected StateT1_8(ASTPath path, State q3, State q1) {
        super(path);
        _q3 = q3;
        _q1 = q1;
        this._predstack = new Stack();
        this._pathstack = new Stack();
    }

    public static State TranslateState(ASTPath path) {//重新创建T1-8
        State q3 = StateT3.TranslateStateT3(path.getFirstStep().getPreds());
        State q1 = StateT1.TranslateStateT1(path.getRemainderPath());
        return new StateT1_8(path, q3, q1);//然后压入栈
    }

    public void startElementDo(String tag, int layer, MyStateActor curactor) throws CloneNotSupportedException {
        if ((layer >= getLevel()) && (tag.equals(_test))) {///当前层数大于等于应该匹配的层数 getLayer（）就可以
            WaitTask wtask;
            ActorTask atask;
            Actor actor;
            // 在 tlist 中添加需要等待匹配的任务模型
            wtask = new WaitTask(layer, false, null);
            curactor.addWTask(wtask);

            String name=((Integer)this._pathstack.hashCode()).toString().concat("T1-8.paActor");
            actor=(actors.get(name));// path的 actor
            if (actor == null) {  // 若pathActor 还没有创建 --> _pathstack 一定为空
                actor = actorManager.createAndStartActor(MyStateActor.class, name);
                actors.put(actor.getName(), actor);

                atask = new ActorTask(this._pathstack);
                dmessage = new DefaultMessage("stack", atask);
                actorManager.send(dmessage, curactor, actor);
                //发送 q'' 给 paActor
                _q1.setLevel(layer + 1);
                atask = new ActorTask(layer, _q1);
                dmessage = new DefaultMessage("pushTask", atask);
                actorManager.send(dmessage, curactor, actor);
            } else {  // 若path  actor 已经创建了,则发送 q'' 给 paActor即可
                State currQ = (State) _q1.copy();
                currQ.setLevel(layer + 1);
                atask = new ActorTask(layer, _q1);
                dmessage = new DefaultMessage("pushTask", atask);
                actorManager.send(dmessage, curactor, actor);
            }

            name=((Integer)this._predstack.hashCode()).toString().concat("T1-8.prActor");
            actor=(actors.get(name));// preds的 actor
            if (actor == null) {  // 若predActor 还没有创建 --> _predstack 一定为空
                actor = actorManager.createAndStartActor(MyStateActor.class, "T1-8.prActor");
                actors.put(actor.getName(), actor);

                atask = new ActorTask(this._predstack);
                dmessage = new DefaultMessage("stack", atask);
                actorManager.send(dmessage, curactor, actor);
                //发送 q' 给 prActor
                _q3.setLevel(layer + 1);
                atask = new ActorTask(layer, _q3);
                dmessage = new DefaultMessage("pushTask", atask);
                actorManager.send(dmessage, curactor, actor);
            } else {  // 若preds  actor 已经创建了,则发送 q'' 给 paActor即可
                State currQ = (State) _q3.copy();
                currQ.setLevel(layer + 1);
                atask = new ActorTask(layer, _q3);
                dmessage = new DefaultMessage("pushTask", atask);
                actorManager.send(dmessage, curactor, actor);
            }
        }
    }

    public void endElementDo(String tag, int layer, MyStateActor curactor) {

        WaitTask wtask;
        ActorTask atask;
        Stack currstack;

        if (tag.equals(_test)) {  // 遇到自己的结束标签，检查
            int id = ((ActorTask) (curactor.getMyStack().peek())).getId(); // 当前栈顶 task 的 id
            for (int i = 0; i < curactor.tlist.size(); i++) {
                wtask = (WaitTask) curactor.tlist.get(i);
                if (wtask.getId() == layer){
                    if(wtask.isSatisfied()) { // 在当前currActor 的 tlist 中有满足条件的 wt
                        if (curactor.getResActor() != null) { // 当前 actor 的上级 actor 不为空（包括自己这个actor）
                            atask = new ActorTask(id, wtask.getPathR());
                            dmessage = new DefaultMessage("pathResult", atask);
                            actorManager.send(dmessage, curactor, curactor.getResActor());
                        } else { // 当前 actor 的 resActor 为空--> 当前 actor是 stackActor，检查输出
                            curactor.output(wtask);
                            // 若 T1-7.pathStack 中弹栈之后还有 AD 轴的后续 path 的检查，
                            // 则需要把当前满足的 wt.paResult复制到之前的等待的 wt.paResult
                            if (curactor.tlist.size() > 1) {
                                Actor actor = actors.get("T1-8.paActor");
                                currstack = ((MyStateActor) actor).getMyStack(); //T1-8.pathStack
                                if (!currstack.isEmpty()) {
                                    atask = ((ActorTask) (currstack.peek()));  // 当前栈顶 的task
                                    State state = (State) atask.getObject();
                                    if (state instanceof StateT1_3 || state instanceof StateT1_4
                                            || state instanceof StateT1_7 || state instanceof StateT1_8)
                                        for (int j = 0; j < curactor.tlist.size() - 2; j++)
                                            ((WaitTask) curactor.tlist.get(j)).setPathR(wtask.getPathR());
                                }

                                actor=(actors.get("T1-8.prActor"));
                                currstack=((MyStateActor)actor).getMyStack(); //T1-8.predStack
                                if(!currstack.isEmpty()){
                                    atask = ((ActorTask)(currstack.peek()));  // 当前栈顶 的task
                                    State state =(State) atask.getObject();   // 栈顶 task 中的 state
                                    if(state instanceof StateT2_3|| state instanceof StateT2_4
                                            ||state instanceof StateT3_3|| state instanceof StateT3_4)
                                        for(int j=0;j<curactor.tlist.size()-2;j++)
                                            ((WaitTask) curactor.tlist.get(j)).setPredR(wtask.getPredR());
                                }
                            }
                        }
                    }
                    curactor.removeWTask(wtask);
                }
            }
        } else if (layer == getLevel() - 1) { // 遇到上层结束标签
            // (能遇到上层结束标签，即T1-8作为一个后续的path（T1-5 的时候也会放在stackActor中），T1-6~T1-8会被放在paActor中)
            // T1-5 时，与T1-5 放在同一个栈，T1-6~T1-8 放在pathstack中
            currstack = curactor.getMyStack();

            curactor.popFunction();   // 弹栈

            if (currstack.isEmpty()) {
                // 弹完之后当前actor 所在的stack 为空了，则删除当前actor 的 paActor
                actorManager.detachActor(actors.get("T1-8.prActor")); // remove( T1-8.prActor )
                actorManager.detachActor(actors.get("T1-8.paActor")); // remove( T1-8.paActor )
            }
            if (!currstack.isEmpty()) { // T1-8 作为 T1-5 的后续 path
                atask = ((ActorTask) (currstack.peek()));  // 当前栈顶 的task
                State state = (State) atask.getObject();
                if (state instanceof StateT1_5){
                    actorManager.detachActor(actors.get("T1-8.prActor")); // remove( T1-8.prActor )
                    actorManager.detachActor(actors.get("T1-8.paActor")); // remove( T1-8.paActor )
                }
            }


        }

    }
}