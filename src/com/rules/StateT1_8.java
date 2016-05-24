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
        _q1.setLevel(this.getLevel()+1);
        _q3.setLevel(this.getLevel()+1);

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
            // 在 tlist 中添加需要等待匹配的任务模型
            curactor.addWTask(new WaitTask(layer, false, null));

            String name=((Integer)this._predstack.hashCode()).toString().concat("T1-8.prActor");
            Actor actor=(actors.get(name));// preds的 actor

            if (actor == null) {  // 若predsActor 还没有创建 --> _predstack 一定为空
                stacklist.add(this._predstack);
                actor = actorManager.createAndStartActor(MyStateActor.class, "T1-8.prActor");
                actors.put(actor.getName(),actor);

                dmessage=new DefaultMessage("resActor", null);
                actorManager.send(dmessage, curactor, actor);
                //发送 q' 给 prActor
                _q3.setLevel(layer + 1);
                dmessage = new DefaultMessage("pushTask", new ActorTask(layer, _q3));
                actorManager.send(dmessage, curactor, actor);
            } else {  // 若preds 的 actor 已经创建了,则发送 q'' 给 paActor即可
                State currQ = (State) _q3.copy();
                currQ.setLevel(layer + 1);
                dmessage = new DefaultMessage("pushTask", new ActorTask(layer, currQ));
                actorManager.send(dmessage, curactor, actor);
            }

            name=((Integer)this._pathstack.hashCode()).toString().concat("T1-8.paActor");
            actor=(actors.get(name));// path的 actor
            if (actor == null) {  // 若 pathActor 还没有创建 --> _pathstack 一定为空
                stacklist.add(this._pathstack);
                actor = actorManager.createAndStartActor(MyStateActor.class, name);
                actors.put(actor.getName(),actor);

                dmessage=new DefaultMessage("resActor", null);
                actorManager.send(dmessage, curactor, actor);
                //发送 q'' 给 paActor
                _q1.setLevel(layer + 1);
                dmessage = new DefaultMessage("pushTask", new ActorTask(layer, _q1));
                actorManager.send(dmessage, curactor, actor);
            } else {  // 若path  actor 已经创建了,则发送 q'' 给 paActor即可
                State currQ = (State) _q1.copy();
                currQ.setLevel(layer + 1);
                dmessage = new DefaultMessage("pushTask", new ActorTask(layer, currQ));
                actorManager.send(dmessage, curactor, actor);
            }
        }
    }

    public void endElementDo(String tag, int layer, MyStateActor curactor) {

        WaitTask wtask;
        ActorTask atask;
        Stack currstack=curactor.getMyStack();

        if (tag.equals(_test)) {  // 遇到自己的结束标签，检查
            int id=((ActorTask)currstack.peek()).getId(); // 当前栈顶 task 的 id
            String name=curactor.getName();
            List list=curactor.getTlist();

            for(int i=0;i<list.size();i++){
                wtask = (WaitTask)list.get(i);
                if (wtask.getId()==layer) {
                    if (wtask.isSatisfied()) {
                        if(name.equals("stackActor")){//在stack中-==>PC轴
                            if(currstack.size()==1){//输出
                                curactor.output(wtask);
                                // 若 T1-8.pathStack 中弹栈之后还有 AD 轴的后续 path 的检查，
                                // 则需要把当前满足的 wt.paResult复制到之前的等待的 wt.paResult
                                if(list.size()>1){
                                    name=((Integer)this._pathstack.hashCode()).toString().concat("T1-7.paActor");
                                    Actor actor=(actors.get(name));// path的 actor
                                    currstack=((MyStateActor)actor).getMyStack(); //T1-7.pathStack
                                    if(!currstack.isEmpty()){
                                        atask = ((ActorTask)(currstack.peek()));  // 当前栈顶 的task
                                        State state =(State) atask.getObject();
                                        if(state instanceof StateT1_3|| state instanceof StateT1_4
                                                ||state instanceof StateT1_7|| state instanceof StateT1_8){
                                            for(int j=0;j<i;j++){
                                                if(((WaitTask) list.get(j)).isWaitOutput()){
                                                    dmessage=new DefaultMessage("pathResult",wtask.getPathR());
                                                    actorManager.send(dmessage,curactor,curactor);
                                                }
                                            }

                                        }

                                    }
                                }
                            }else {//在stack中 && 作为T1-5的后续path
                                for(int j=0;j<i;j++){
                                    wtask = (WaitTask) list.get(j);
                                    if(wtask.getId()==id){
                                        atask=new ActorTask(id,wtask.getPathR());
                                        dmessage=new DefaultMessage("pathResult",atask);
                                        actorManager.send(dmessage,curactor,curactor);
                                    }
                                }
                            }
                        }else {//作为AD 轴后续path的一部分-->在paActor中
                            for(int j=0;j<i;j++){
                                wtask = (WaitTask)list.get(j);
                                atask=new ActorTask(id,wtask.getPathR());
                                if(wtask.getId()==id){//在paActor中 && 作为T1-5的后续path
                                    dmessage=new DefaultMessage("pathResult",atask);
                                    actorManager.send(dmessage,curactor,curactor);
                                }else{//在T1-6、T1-7、T1-8的 paActor 中
                                    dmessage=new DefaultMessage("pathResult",atask);
                                    actorManager.send(dmessage,curactor,curactor.getResActor());
                                }
                            }
                        }
                    }
                    //到自己的结束标签，不管当前wt是否满足，都要删除
                    curactor.removeWTask(wtask);
                }
            }
        }else if (layer == getLevel() - 1) { // 遇到上层结束标签(T1-8作为一个后续的path)
            // (能遇到上层结束标签，即T1-8作为一个后续的path
            // （T1-5 的时候也会放在stackActor中），T1-6~T1-8 会被放在 paActor中)
            // T1-5 的后续的path时，与T1-5 放在同一个栈，T1-6~T1-8 放在 pathstack中
            curactor.popFunction();   // T1-8弹栈
            currstack=curactor.getMyStack();
            if(currstack.isEmpty()) {   // 弹完之后当前actor 所在的stack 为空了，则删除当前 actor
                actorManager.detachActor(curactor);
            }else{                      // T1-8作为 T1-5 的后续 path
                State state =(State)((ActorTask)(currstack.peek())).getObject();
                if(state instanceof StateT1_5){
                    state.endElementDo(tag, layer, curactor);
                }
            }
        }

    }
}