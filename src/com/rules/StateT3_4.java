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
public class StateT3_4 extends StateT3{
    protected  State _q31;//��� preds
    protected  State _q32;//���preds'
    protected  State _q2;//��顾desc_or_self::test preds��

    protected StateT3_4(ASTPreds preds, State q31, State q32, State q2){
        super(preds);
        _q31=q31;
        _q32=q32;
        _q2=q2;
        _q2.setLevel(this.getLevel());
        _q32.setLevel(this.getLevel());
        _q31.setLevel(this.getLevel() + 1);
        _predstack=new Stack();
    }

    public static StateT3 TranslateState(ASTPreds preds){//���´���T3-4
        State q31=StateT3.TranslateStateT3(preds.getFirstStep().getPreds());
        State q32=StateT3.TranslateStateT3(preds.getRemainderPreds());
        State q2= StateT2.TranslateStateT2(StateT3.getSinglePred(preds));
        return new StateT3_4(preds,q31,q32,q2);//Ȼ��ѹ��ջ
    }


    
    public void startElementDo(String tag,int layer,MyStateActor curactor) throws CloneNotSupportedException {
        if (layer >= getLevel()) {
            _q31.setLevel(layer + 1);
            Stack stack = curactor.getMyStack();
            ActorTask atask = (ActorTask) stack.peek();
            int id = atask.getId();//��ǰջ����T3-4���� id

            String name = ((Integer) this._predstack.hashCode()).toString().concat("T3-4.prActor");
            Actor actor = (actors.get(name));// preds'�� actor

            // q'�� actor-->T2-4 ��ν�� Actro
            String name1 = ((Integer) (((StateT2) _q2)._predstack.hashCode())).toString().concat("T2-4.prActor");
            Actor actor1 = (actors.get(name1));// q'�� actor

            boolean isFindInThis = false;

            if (tag.equals(_test)) {  //T3-4 �� test ƥ��
                //1. (id,T3-4) ��Ϊ��id,waitstate��
                State waitState = new WaitState();
                waitState.setLevel(((State) atask.getObject()).getLevel());
                curactor.popFunction();
                stack.push(new ActorTask(id, waitState));
                //push(layer,q''')
                stack.push(new ActorTask(layer, _q2));
                //push(layer,q')--> ��T2-4 ��ν�� actor ��
                if (actor1 == null) {
                    stacklist.add(((StateT2_4)_q2)._predstack);
                    actor1 = actorManager.createAndStartActor(MyStateActor.class, name1);
                    actors.put(actor1.getName(),actor1);

                    dmessage = new DefaultMessage("resActor", null);
                    actorManager.send(dmessage, curactor, actor1);

                    dmessage = new DefaultMessage("pushTask", new ActorTask(layer, _q31));
                    actorManager.send(dmessage, curactor, actor1);
                } else {
                    State currQ = (State) _q31.copy();
                    currQ.setLevel(layer + 1);
                    dmessage = new DefaultMessage("pushTask", new ActorTask(layer,currQ));
                    actorManager.send(dmessage, curactor, actor1);
                }
                //2.push(layer,q'')
                if (actor == null) {
                    stacklist.add(this._predstack);
                    actor = actorManager.createAndStartActor(MyStateActor.class, name);
                    actors.put(actor.getName(),actor);

                    dmessage = new DefaultMessage("resActor", null);
                    actorManager.send(dmessage, curactor, actor);

                    dmessage = new DefaultMessage("setCategory", "T3PredsActor");
                    actorManager.send(dmessage, curactor, actor);

                    dmessage = new DefaultMessage("pushTask", new ActorTask(layer, _q32));
                    actorManager.send(dmessage, curactor, actor);
                } else {
                    State currQ = (State) _q32.copy();
                    currQ.setLevel(layer + 1);
                    dmessage = new DefaultMessage("pushTask", new ActorTask(layer,currQ));
                    actorManager.send(dmessage, curactor, actor);
                }
                //3.add(layer,false,false)
                curactor.addWTask(new WaitTask(layer, false, "false"));
            } else {// T3-2 �� test ��ƥ��
                // PC:
                List list = curactor.tlist;
                if (!list.isEmpty()) {
                    for (int i = (list.size() - 1); i >= 0; i--) {
                        WaitTask wtask = (WaitTask) list.get(i);
                        if (wtask.getId() == id) {//�ҵ���-->T3-3Լ�� PC ���test
                            isFindInThis = true;
                            //1. (id,T3-4) ��Ϊ��id,waitstate��&& push(layer,q''')
                            State waitState = new WaitState();
                            waitState.setLevel(((State) atask.getObject()).getLevel());
                            atask = new ActorTask(id, waitState);
                            curactor.popFunction();
                            curactor.pushFunction(atask);
                            //push(layer,q''')
                            curactor.pushFunction(new ActorTask(layer, _q2));
                            //2.push(layer,q'')
                            if (actor == null) {
                                stacklist.add(this._predstack);
                                actor = actorManager.createAndStartActor(MyStateActor.class, name);
                                actors.put(actor.getName(),actor);

                                dmessage = new DefaultMessage("resActor", null);
                                actorManager.send(dmessage, curactor, actor);

                                dmessage = new DefaultMessage("setCategory", "T3PredsActor");
                                actorManager.send(dmessage, curactor, actor);

                                dmessage = new DefaultMessage("pushTask", new ActorTask(layer, _q32));
                                actorManager.send(dmessage, curactor, actor);
                            } else {
                                State currQ = (State) _q32.copy();
                                currQ.setLevel(layer + 1);
                                dmessage = new DefaultMessage("pushTask", new ActorTask(layer, currQ));
                                actorManager.send(dmessage, curactor, actor);
                            }
                            //3.add(layer,false,false)
                            curactor.addWTask(new WaitTask(layer, false, "false"));
                            break;
                        }
                    }
                }
                if (!isFindInThis) {
                    //1. (id,T3-4) ��Ϊ��id,waitstate��
                    State waitState = new WaitState();
                    waitState.setLevel(((State) atask.getObject()).getLevel());
                    curactor.popFunction();
                    curactor.pushFunction(new ActorTask(id, waitState));
                    //push(id,q''')
                    curactor.pushFunction(new ActorTask(id, _q2));
                    //2.push(id,q'')
                    if (actor == null) {
                        stacklist.add(this._predstack);
                        actor = actorManager.createAndStartActor(MyStateActor.class, name);
                        actors.put(actor.getName(),actor);

                        dmessage = new DefaultMessage("resActor", null);
                        actorManager.send(dmessage, curactor, actor);

                        dmessage = new DefaultMessage("setCategory", "T3PredsActor");
                        actorManager.send(dmessage, curactor, actor);

                        dmessage = new DefaultMessage("pushTask", new ActorTask(layer, _q32));
                        actorManager.send(dmessage, curactor, actor);
                    } else {
                        State currQ = (State)_q32.copy();
                        currQ.setLevel(layer + 1);
                        dmessage = new DefaultMessage("pushTask", new ActorTask(layer, currQ));
                        actorManager.send(dmessage, curactor, actor);
                    }
                    //3.add(id,false,false)
                    curactor.addWTask(new WaitTask(layer, false, "false"));
                }
                dmessage = new DefaultMessage("startE", new ActorTask(layer, tag));
                actorManager.send(dmessage, curactor, actor);
            }
        }
    }
    //���۳ɰܣ�q3-4 ����waitState�滻������ q3-4 ��������������ǩ
}

