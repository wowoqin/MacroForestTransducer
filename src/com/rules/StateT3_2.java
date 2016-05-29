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
public class StateT3_2 extends StateT3{
    protected  State _q31;//检查 preds
    protected  State _q32;//检查preds'
    protected  State _q2;//检查【child::test preds】

    protected  StateT3_2(ASTPreds preds,State q31,State q32,State q2){
        super(preds);
        _q31=q31;
        _q32=q32;
        _q2=q2;
        _q2.setLevel(getLevel());//检查【child::test preds】
        _q31.setLevel(getLevel()+1);//检查preds
        _q32.setLevel(getLevel());//检查preds'
        _predstack=new Stack();
    }

    public static StateT3 TranslateState(ASTPreds preds){//重新创建T3-2
        State q31=StateT3.TranslateStateT3(preds.getFirstStep().getPreds());
        State q32=StateT3.TranslateStateT3(preds.getRemainderPreds());
        State q2= StateT2.TranslateStateT2(StateT3.getSinglePred(preds));
        return new StateT3_2(preds,q31,q32,q2);
    }
    public void startElementDo(String tag,int layer,MyStateActor curactor) throws CloneNotSupportedException {
        if (getLevel() == layer) {
            WaitTask wtask;

            Stack stack=curactor.getMyStack();
            ActorTask atask=(ActorTask)stack.peek();
            int id=atask.getId();//当前栈顶（T3-2）的 id
            boolean isInSelf=atask.isInSelf();

            String name=((Integer)this._predstack.hashCode()).toString().concat("T3-2.prActor");
            Actor actor=(actors.get(name));// preds'的 actor

            if(tag.equals(_test)){  //T3-2 的 test 匹配
                //1. (id,T3-2) 换为（id,T2-2）&& push(layer,q')
                curactor.popFunction();
                stack.push(new ActorTask(id,_q2,isInSelf));
                //push(layer,q')
                stack.push(new ActorTask(layer, _q31,true));
                //2.push(layer,q'')
                if(actor==null){
                    stacklist.add(this._predstack);
                    actor=actorManager.createAndStartActor(MyStateActor.class, name);
                    actors.put(actor.getName(),actor);

                    dmessage=new DefaultMessage("resActor", null);
                    actorManager.send(dmessage, curactor, actor);

                    dmessage=new DefaultMessage("setCategory","T3PredsActor");
                    actorManager.send(dmessage,curactor,actor);

                    dmessage=new DefaultMessage("push",new ActorTask(layer,_q32,false));
                    actorManager.send(dmessage,curactor,actor);
                }else {
                    State currQ=(State)_q32.copy();
                    currQ.setLevel(layer + 1);
                    dmessage=new DefaultMessage("push",new ActorTask(layer,currQ,false));
                    actorManager.send(dmessage, curactor, actor);
                }
                //3.add(layer,false,false)
                curactor.addWTask(new WaitTask(layer,false,"false"));
            }else{// T3-2 的 test 不匹配
                //1. (id,T3-2) 换为（id,waitstate）
                State waitState=new WaitState();
                waitState.setLevel(((State) atask.getObject()).getLevel());
                curactor.popFunction();
                curactor.pushFunction(new ActorTask(id,waitState,isInSelf));
                if(isInSelf){   //T3-2约束 PC 轴的test
                    //push(layer,q''')
                    curactor.pushFunction(new ActorTask(layer,_q2,true));
                    //2.push(layer,q'')
                    if(actor==null){
                        stacklist.add(this._predstack);
                        actor=actorManager.createAndStartActor(MyStateActor.class,name);
                        actors.put(actor.getName(),actor);

                        dmessage=new DefaultMessage("resActor",null);
                        actorManager.send(dmessage, curactor, actor);

                        dmessage=new DefaultMessage("setCategory","T3PredsActor");
                        actorManager.send(dmessage,curactor,actor);

                        dmessage=new DefaultMessage("push",new ActorTask(layer,_q32,false));
                        actorManager.send(dmessage,curactor,actor);
                    }else {
                        State currQ=(State)_q32.copy();
                        currQ.setLevel(layer + 1);
                        dmessage=new DefaultMessage("pushTask",new ActorTask(layer,_q32,false));
                        actorManager.send(dmessage, curactor, actor);
                    }
                    //3.add(layer,false,false)
                    curactor.addWTask(new WaitTask(layer,false,"false"));
                }else{  //T3-2约束 AD 轴的test
                    //2.push（id,q'''）
                    curactor.pushFunction(new ActorTask(id,_q2,true));
                    //2.push(id,q'')
                    if(actor==null){
                        stacklist.add(this._predstack);
                        actor=actorManager.createAndStartActor(MyStateActor.class, name);
                        actors.put(actor.getName(),actor);

                        dmessage=new DefaultMessage("resActor",null);
                        actorManager.send(dmessage, curactor, actor);

                        dmessage=new DefaultMessage("setCategory","T3PredsActor");
                        actorManager.send(dmessage,curactor,actor);

                        dmessage=new DefaultMessage("push",new ActorTask(id,_q32,false));
                        actorManager.send(dmessage,curactor,actor);
                    }else {
                        State currQ=(State)_q32.copy();
                        currQ.setLevel(layer + 1);
                        dmessage=new DefaultMessage("pushTask",new ActorTask(id,currQ,false));
                        actorManager.send(dmessage, curactor, actor);
                    }
                    //3.add(id,false,false)
                    curactor.addWTask(new WaitTask(id,false,"false"));
                }
                //q''.startElementDo(tag,layer)
                dmessage=new DefaultMessage("startE",new ActorTask(layer,tag));
                actorManager.send(dmessage, curactor, actor);
            }
        }
    }
    //无论成败，q3-2 都被替换，所以 q3-2 不会遇到结束标签

}
