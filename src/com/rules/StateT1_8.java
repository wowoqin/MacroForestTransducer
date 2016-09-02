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
            //System.out.println("T1-8.startElementDo中，当前actor的数量：" + actors.length);
            // 在 tlist 中添加需要等待匹配的任务模型
            addWTask(new WaitTask(layer, null, null));

            String name=((Integer)this._predstack.hashCode()).toString().concat("T1-8.prActor");

            if (this._predstack.isEmpty()) {  // 若predsActor 还没有创建 --> _predstack 一定为空
                System.out.println("T1-8.test匹配 && 谓词actor == null");
                _q3.setLevel(layer + 1);
                curactor.createAnotherActor(name, this._predstack, new ActorTask(layer, _q3, false));

//                actor = actorManager.createAndStartActor(MyStateActor.class, name);
//                actors.put(name, actor);
//
//                actorManager.send(new DefaultMessage("resActor", null), curactor, actor);
//                //发送 q' 给 prActor
//                actorManager.send(new DefaultMessage("pushTask", new ActorTask(layer, _q3,false)), curactor, actor);
            } else {  // 若preds 的 actor 已经创建了,则发送 q'' 给 paActor即可
                //System.out.println("T1-8.test匹配 && 谓词actor != null" + "当前actor的数量：" + actors.length);
                State currQ = (State) _q3.copy();
                currQ.setLevel(layer + 1);
                dmessage=new DefaultMessage("pushTask", new ActorTask(layer, currQ,false));
//                for(int i=0;i<actors.length;i++){
//                    if(actors[i].getName().equals(name)){
//                        actorManager.send(dmessage, curactor, actors[i]);
//                        return;
//                    }
//                }
            }

            name=((Integer)this._pathstack.hashCode()).toString().concat("T1-8.paActor");
            if (this._pathstack.isEmpty()) {  // 若 pathActor 还没有创建 --> _pathstack 一定为空
                System.out.println("T1-8.test匹配 && pathactor == null");
                _q1.setLevel(layer + 1);
                curactor.createAnotherActor(name, this._pathstack, new ActorTask(layer, _q1, false));

//                actor = actorManager.createAndStartActor(MyStateActor.class, name);
//                actors.put(name, actor);
//
//                actorManager.send(new DefaultMessage("resActor", null), curactor, actor);
//                //发送 q'' 给 paActor
//                actorManager.send(new DefaultMessage("pushTask", new ActorTask(layer, _q1,false)), curactor, actor);
            } else {  // 若path  actor 已经创建了,则发送 q'' 给 paActor即可
                //System.out.println("T1-8.test匹配 && pathactor != null，当前actor的数量：" + actors.length);
                State currQ = (State) _q1.copy();
                currQ.setLevel(layer + 1);
                dmessage=new DefaultMessage("pushTask", new ActorTask(layer, currQ,false));
//                for(int i=0;i<actors.length;i++){
//                    if(actors[i].getName().equals(name)){
//                        actorManager.send(dmessage, curactor, actors[i]);
//                        return;
//                    }
//                }
            }
        }
    }

    public void endElementDo(String tag, int layer, MyStateActor curactor) {
        if (tag.equals(_test)) {  // 遇到自己的结束标签，检查
            for(int i=0;i<getList().size();i++){
                WaitTask wtask=(WaitTask) getList().get(i);
                if(wtask.hasReturned()){
                    curactor.doNext(wtask);
                }else{//等待--或许是谓词的消息还未传回来，或许是后续path的结果还未传回来，
                    // 当前结束标签先不处理
                    curactor.addMessage(new DefaultMessage("endE", new ActorTask(layer,tag)));
                    actorManager.awaitMessage(curactor);
                    while(wtask.hasReturned())
                        curactor.doNext(wtask);
                }
            }
        }else if (layer == getLevel() - 1) { // 遇到上层结束标签(T1-8作为一个后续的path)
            // (能遇到上层结束标签，即T1-8作为一个后续的path
            // （T1-5 的时候也会放在stackActor中），T1-6~T1-8 会被放在 paActor中)
            // T1-5 的后续的path时，与T1-5 放在同一个栈，T1-6~T1-8 放在 pathstack中
            curactor.popFunction();   // T1-8弹栈
            Stack ss=curactor.getMyStack();
            if(ss.isEmpty()) {   // 弹完之后当前actor 所在的stack 为空了，则删除当前 actor
                actorManager.detachActor(curactor);
            }else{                      // T1-8作为 T1-5 的后续 path
                State state =(State)((ActorTask)(ss.peek())).getObject();
                if(state instanceof StateT1_5){
                    state.endElementDo(tag, layer, curactor);
                }
            }
        }

    }
}