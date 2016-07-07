package com.rules;

import com.XPath.PathParser.ASTPath;
import com.ibm.actor.Actor;
import com.ibm.actor.DefaultMessage;
import com.taskmodel.ActorTask;
import com.taskmodel.WaitTask;

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
            System.out.println("T1-7.startElementDo中，当前actor的数量：" + actors.size());
            // 在 tlist 中添加需要等待匹配的任务模型
            addWTask(new WaitTask(layer,true,null));

            String name=((Integer)this._pathstack.hashCode()).toString().concat("T1-7.paActor");
            Actor actor=actors.get(name);
            if(this._pathstack.isEmpty()){  // 若pathActor 还没有创建 --> _pathstack 一定为空
                System.out.println("T1-7.test匹配 && pathactor == null");
                _q1.setLevel(layer + 1);
                curactor.createAnotherActor(name, this._pathstack, new ActorTask(layer, _q1, false));
//                actor =actorManager.createAndStartActor(MyStateActor.class, name);
//                actors.put(actor.getName(), actor);
//
//                actorManager.send(new DefaultMessage("resActor", null), curactor, actor);
//                //发送 q'' 给 paActor
//                _q1.setLevel(layer + 1);
//                actorManager.send(new DefaultMessage("pushTask", new ActorTask(layer,_q1,false)),curactor,actor);
            } else{  // 若path  actor 已经创建了,则发送 q'' 给 paActor即可
                //System.out.println("T1-7.test匹配 && pathactor != null，当前actor的数量：" + actors.length);
                State currQ=(State)_q1.copy();
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

    public void endElementDo(String tag,int layer,MyStateActor curactor){
        if (tag.equals(_test)) {  // 遇到自己的结束标签，检查
            for(int i=0;i<getList().size();i++){
                WaitTask wtask=(WaitTask) getList().get(i);
                if(wtask.hasReturned()){
                    curactor.doNext(wtask);
                }else{//等待--后续path的结果还未传回来，
                    //当前结束标签先不处理
                    curactor.addMessage(new DefaultMessage("endE", new ActorTask(layer, tag)));
                    curactor.peekNext("pathR");//优先处理path返回结果的消息
                    actorManager.awaitMessage(curactor);
                }
            }
        }else if (layer == getLevel() - 1) { // 遇到上层结束标签(T1-7作为一个后续的path)
            // (能遇到上层结束标签，即T1-7作为一个后续的path
            // （T1-5 的时候也会放在stackActor中），T1-6~T1-8会被放在paActor中)
            // T1-5 的后续的path时，与T1-5 放在同一个栈，T1-6~T1-8 放在pathstack中
            curactor.popFunction();   // T1-7弹栈
            Stack ss=curactor.getMyStack();
            if(ss.isEmpty()) {   // 弹完之后当前actor 所在的stack 为空了，则删除当前 actor
                actorManager.detachActor(curactor);
            }else{                      // T1-7 作为 T1-5 的后续 path
                State state =(State)((ActorTask)(ss.peek())).getObject();
                if(state instanceof StateT1_5){
                    state.endElementDo(tag,layer,curactor);
                }
            }
        }
    }
}
