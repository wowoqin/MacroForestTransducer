package com.rules;

import com.XPath.PathParser.ASTPath;
import com.ibm.actor.Actor;
import com.ibm.actor.DefaultMessage;

import java.util.*;

/**
 * Created by qin on 2015/10/10.
 */
public class StateT1_7 extends StateT1 implements Cloneable{
    protected State _q1;//检查后续 path

    protected StateT1_7(ASTPath path, State q1) {
        super(path);
        _q1 = q1;
        this._pathstack=new Stack();
    }

    public static State TranslateState(ASTPath path) {//重新创建T1-7
        State q1 = StateT1.TranslateStateT1(path.getRemainderPath());
        return new StateT1_7(path, q1);
    }

    public void startElementDo(String tag, int layer,MyStateActor curactor) throws CloneNotSupportedException{
        if ((layer >= getLevel()) && (tag.equals(_test))) {
            WaitTask wtask;
            ActorTask atask;
            String name=((Integer)this._pathstack.hashCode()).toString().concat("T1-7.paActor");
            Actor actor=(actors.get(name));// path的 actor
            // 在 tlist 中添加需要等待匹配的任务模型
            wtask=new WaitTask(layer,true,null);
            curactor.addWTask(wtask);

            if(actor == null){  // 若pathActor 还没有创建 --> _pathstack 一定为空
                actor =actorManager.createAndStartActor(MyStateActor.class, name);
                actors.put(actor.getName(), actor);

                atask=new ActorTask(this._pathstack);
                dmessage=new DefaultMessage("stack", atask);
                actorManager.send(dmessage, curactor, actor);
                //发送 q'' 给 paActor
                _q1.setLevel(layer + 1);
                atask=new ActorTask(layer,_q1);
                dmessage=new DefaultMessage("pushTask", atask);
                actorManager.send(dmessage,curactor,actor);
            } else{  // 若path  actor 已经创建了,则发送 q'' 给 paActor即可
                State currQ=(State)_q1.copy();
                currQ.setLevel(layer + 1);
                atask=new ActorTask(layer,_q1);
                dmessage=new DefaultMessage("pushTask",atask);
                actorManager.send(dmessage, curactor, actor);

            }
            // 还要设置一下path actor接收消息的subject
            //actor.willReceive("startE");
            //actor.peekNext("startE",isX1(getLevel()+1));// path actor接受的标签应该是当前标签的 child
        }
    }

    public void endElementDo(String tag,int layer,MyStateActor curactor){

        WaitTask wtask;
        ActorTask atask;
        Stack currstack;


        if (tag.equals(_test)) {  // 遇到自己的结束标签，检查
            int id=((ActorTask)(curactor.getMyStack().peek())).getId(); // 当前栈顶 task 的 id
            for (int i = 0; i < curactor.tlist.size(); i++) {
                wtask = (WaitTask) curactor.tlist.get(i);
                if (wtask.getId()==layer){
                    if(wtask.isSatisfied()) { // 在当前currActor 的 tlist 中有满足条件的 wt
                        if (curactor.getResActor() != null) { // 当前 actor 的上级 actor 不为空（包括自己这个actor）
                            atask = new ActorTask(id, wtask.getPathR());
                            dmessage = new DefaultMessage("pathResult", atask);
                            actorManager.send(dmessage, curactor, curactor.getResActor());
                        }else { // 当前 actor 的 resActor 为空--> 当前 actor是 stackActor，检查输出
                            curactor.output(wtask);
                            // 若 T1-7.pathStack 中弹栈之后还有 AD 轴的后续 path 的检查，
                            // 则需要把当前满足的 wt.paResult复制到之前的等待的 wt.paResult
                            if(curactor.tlist.size()>1){
                                Actor actor=actors.get("T1-7.paActor");
                                currstack=((MyStateActor)actor).getMyStack(); //T1-7.pathStack
                                if(!currstack.isEmpty()){
                                    atask = ((ActorTask)(currstack.peek()));  // 当前栈顶 的task
                                    State state =(State) atask.getObject();
                                    if(state instanceof StateT1_3|| state instanceof StateT1_4
                                            ||state instanceof StateT1_7|| state instanceof StateT1_8)
                                        for(int j=0;j<curactor.tlist.size()-2;j++)
                                            ((WaitTask) curactor.tlist.get(j)).setPathR(wtask.getPathR());
                                }
                            }

                        }
                    }
                    curactor.removeWTask(wtask);
                }
            }
        }else if (layer == getLevel() - 1) { // 遇到上层结束标签
        // (能遇到上层结束标签，即T1-7作为一个后续的path（T1-5 的时候也会放在stackActor中），T1-6~T1-8会被放在paActor中)
            // T1-5 时，与T1-5 放在同一个栈，T1-6~T1-8 放在pathstack中
            currstack = curactor.getMyStack();

            curactor.popFunction();   // 弹栈
            if(currstack.isEmpty())   // 弹完之后当前actor 所在的stack 为空了，则删除当前actor 的 paActor
                actorManager.detachActor(actors.get("T1-7.paActor")); // remove( T1-7.paActor )
            if(!currstack.isEmpty()){ // T1-7 作为 T1-5 的后续 path
                atask = ((ActorTask)(currstack.peek()));  // 当前栈顶 的task
                State state =(State) atask.getObject();
                if(state instanceof StateT1_5)
                actorManager.detachActor(actors.get("T1-7.paActor")); // remove( T1-7.paActor )
            }


        }
    }
}
