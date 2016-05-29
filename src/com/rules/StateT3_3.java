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
public class StateT3_3 extends StateT3{
    protected  State _q3;//检查 preds'
    protected  State _q2;//检查【desc_or_self::test】

    protected  StateT3_3(ASTPreds preds,State q3,State q2){
        super(preds);
        _q3=q3;
        _q2=q2;
        _q2.setLevel(this.getLevel());//q2 检查【child::test】，应该匹配的标签的层数 不变
        _q3.setLevel(this.getLevel());// q3 检查preds'，应该匹配的标签的层数与当前 [test] 同一层
        this._predstack=new Stack();
    }

    public static StateT3 TranslateState(ASTPreds preds){//重新创建T3-3
        State q3 = StateT3.TranslateStateT3(preds.getRemainderPreds());
        State q2 = StateT2.TranslateStateT2(StateT3.getSinglePred(preds));
        return new StateT3_3(preds,q3,q2);//然后压入栈
    }
    public void startElementDo(String tag,int layer,MyStateActor curactor) throws CloneNotSupportedException {
        if (layer>=getLevel()) {//应该匹配的层数 getLayer（）和 当前标签 tag 的层数相等
            Stack stack = curactor.getMyStack();
            ActorTask atask = (ActorTask) stack.peek();
            int id = atask.getId();//当前栈顶的 id
            boolean isInSelf = atask.isInSelf();

            if (tag.equals(_test)) {
                //要是test匹配，则直接检查preds'，preds' 的结果作为 T3-1 的结果
                curactor.popFunction();
                stack.push(new ActorTask(id, _q3, isInSelf));
            } else {// test不匹配，检查 q'' 和 q'''&& preds'.startElementDo(tag,layer)
                String name = ((Integer) this._predstack.hashCode()).toString().concat("T3-1.prActor");
                Actor actor = (actors.get(name));// preds'的 actor
                //1.(id,T3-1,flg) 替换为 (id,waitstate,flg)
                State waitState = new WaitState();
                waitState.setLevel(((State) atask.getObject()).getLevel());
                curactor.popFunction();
                curactor.pushFunction(new ActorTask(id, waitState, isInSelf));
                if (isInSelf) {   //则T3-1约束 PC 轴的test
                    //2.push（layer,q'''）
                    curactor.pushFunction(new ActorTask(layer, _q2, true));
                    //push(layer,q'')
                    if (actor == null) {
                        stacklist.add(this._predstack);
                        actor = actorManager.createAndStartActor(MyStateActor.class, name);
                        actors.put(actor.getName(), actor);

                        dmessage = new DefaultMessage("resActor", null);
                        actorManager.send(dmessage, curactor, actor);

                        dmessage = new DefaultMessage("setCategory", "T3PredsActor");
                        actorManager.send(dmessage, curactor, actor);

                        dmessage = new DefaultMessage("push", new ActorTask(layer, _q3, false));
                        actorManager.send(dmessage, curactor, actor);
                    } else {
                        State currQ = (State) _q3.copy();
                        currQ.setLevel(layer + 1);
                        dmessage = new DefaultMessage("pushTask", new ActorTask(layer, currQ, false));
                        actorManager.send(dmessage, curactor, actor);
                    }
                    //3. add（layer,false,false）
                    curactor.addWTask(new WaitTask(layer, false, "false"));
                } else {  //则T3-1约束 AD 轴的test
                    //2.push（id,q'''）
                    curactor.pushFunction(new ActorTask(layer, _q2, true));
                    //push(id,q'')
                    if (actor == null) {
                        stacklist.add(this._predstack);
                        actor = actorManager.createAndStartActor(MyStateActor.class, name);
                        actors.put(actor.getName(), actor);

                        dmessage = new DefaultMessage("stack", this._predstack);
                        actorManager.send(dmessage, curactor, actor);

                        dmessage = new DefaultMessage("setCategory", "T3PredsActor");
                        actorManager.send(dmessage, curactor, actor);

                        dmessage = new DefaultMessage("push", new ActorTask(layer, _q3, false));
                        actorManager.send(dmessage, curactor, actor);
                    } else {
                        State currQ = (State) _q3.copy();
                        currQ.setLevel(layer + 1);
                        dmessage = new DefaultMessage("pushTask", new ActorTask(layer, currQ, false));
                        actorManager.send(dmessage, curactor, actor);
                    }
                    //3. add（id,false,false）
                    curactor.addWTask(new WaitTask(id, false, "false"));
                }
                //q''.startElementDo(tag,layer)
                dmessage = new DefaultMessage("startE", new ActorTask(layer, tag));
                actorManager.send(dmessage, curactor, actor);
            }
        }
    }
    //无论成败，q3-3 都被替换，所以 q3-3 不会遇到结束标签

}

