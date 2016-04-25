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
public class StateT3_3 extends StateT3{
    protected  State _q3;//��� preds'
    protected  State _q2;//��顾desc_or_self::test��

    protected  StateT3_3(ASTPreds preds,State q3,State q2){
        super(preds);
        _q3=q3;
        _q2=q2;
    }

    public static StateT3 TranslateState(ASTPreds preds){//���´���T3-3
        State q3 = StateT3.TranslateStateT3(preds.getRemainderPreds());
        State q2 = StateT2.TranslateStateT2(StateT3.getSinglePred(preds));
        return new StateT3_3(preds,q3,q2);//Ȼ��ѹ��ջ
    }
    public void startElementDo(String tag,int layer,MyStateActor curactor) throws CloneNotSupportedException {
        if (getLevel() >= layer) {//Ӧ��ƥ��Ĳ��� getLayer������ ��ǰ��ǩ tag �Ĳ������
            ActorTask atask;
            WaitTask wtask;

            _q2.setLevel(getLevel());//q2 ��顾child::test����Ӧ��ƥ��ı�ǩ�Ĳ��� ����
            _q3.setLevel(getLevel());// q3 ���preds'��Ӧ��ƥ��ı�ǩ�Ĳ����뵱ǰ [test] ͬһ��

            Stack stack=curactor.getMyStack();
            atask=(ActorTask)stack.peek();
            int id = atask.getId();//��ǰջ���� id

            String name=((Integer)this._predstack.hashCode()).toString().concat("T3-3.prActor");
            Actor actor=(actors.get(name));// preds'�� actor

            boolean isFindInThis=false;

            if (tag.equals(_test)) {
                //Ҫ��testƥ�䣬��ֱ�Ӽ��preds'��preds' �Ľ����Ϊ T3-3 �Ľ��
                //pop��id,T3-3��&& push��id,q''��
                atask = new ActorTask(id, _q3);
                curactor.popFunction();
                stack.push(atask);
            } else {// test��ƥ�䣬��� q'' �� q'''&& preds'.startElementDo(tag,layer)
                // PC:
                List list=curactor.tlist;
                if(!list.isEmpty()){
                    for(int i=(list.size()-1);i>=0;i--) {
                        wtask = (WaitTask) list.get(i);
                        if (wtask.getId() == id) {//�ҵ���-->T3-3Լ�� PC ���test
                            isFindInThis=true;
                            //1.(id,T3-3) ��Ϊ (id,waitstate)
                            State waitState=new WaitState();
                            waitState.setLevel(((State)atask.getObject()).getLevel());
                            atask=new ActorTask(id,waitState);
                            curactor.popFunction();
                            curactor.pushFunction(atask);
                            //2. add��layer,false,false��
                            wtask=new WaitTask(layer,false,"false");
                            curactor.addWTask(wtask);
                            //3.push��layer,q'''��
                            curactor.pushFunction(new ActorTask(layer,_q2));
                            //push(layer,q'')
                            if(actor==null){
                                actor=actorManager.createAndStartActor(MyStateActor.class, name);
                                actors.put(actor.getName(),actor);

                                dmessage=new DefaultMessage("stack",this._predstack);
                                actorManager.send(dmessage, curactor, actor);

                                dmessage=new DefaultMessage("setCategory","T3PredsActor");
                                actorManager.send(dmessage,curactor,actor);

                                dmessage=new DefaultMessage("pushTask",new ActorTask(layer,_q3));
                                actorManager.send(dmessage,curactor,actor);
                            }else {
                                State currQ=(State)_q3.copy();
                                currQ.setLevel(layer + 1);
                                atask=new ActorTask(layer,_q3);
                                dmessage=new DefaultMessage("pushTask",atask);
                                actorManager.send(dmessage, curactor, actor);
                            }
                            //q''.startElementDo(tag,layer)
                            atask=new ActorTask(layer,tag);
                            dmessage=new DefaultMessage("startE",atask);
                            actorManager.send(dmessage, curactor, actor);
                        }
                    }
                }
                if(!isFindInThis){
                    //AD:
                    //1.(id,T3-3) ��Ϊ (id,waitstate)
                    State waitState=new WaitState();
                    waitState.setLevel(((State)atask.getObject()).getLevel());
                    atask=new ActorTask(id,waitState);
                    curactor.popFunction();
                    curactor.pushFunction(atask);
                    //2. add��id,false,false��
                    wtask=new WaitTask(id,false,"false");
                    curactor.addWTask(wtask);
                    //3.push��id,q'''��
                    curactor.pushFunction(new ActorTask(layer,_q2));
                    //push(id,q'')
                    if(actor==null){
                        actor=actorManager.createAndStartActor(MyStateActor.class, name);
                        actors.put(actor.getName(),actor);

                        dmessage=new DefaultMessage("stack",this._predstack);
                        actorManager.send(dmessage, curactor, actor);

                        dmessage=new DefaultMessage("setCategory","T3PredsActor");
                        actorManager.send(dmessage,curactor,actor);

                        dmessage=new DefaultMessage("pushTask",new ActorTask(layer,_q3));
                        actorManager.send(dmessage,curactor,actor);
                    }else {
                        State currQ=(State)_q3.copy();
                        currQ.setLevel(layer + 1);
                        atask=new ActorTask(layer,_q3);
                        dmessage=new DefaultMessage("pushTask",atask);
                        actorManager.send(dmessage, curactor, actor);
                    }
                    //q''.startElementDo(tag,layer)
                    atask=new ActorTask(layer,tag);
                    dmessage=new DefaultMessage("startE",atask);
                    actorManager.send(dmessage, curactor, actor);
                }
            }
        }
    }
    //���۳ɰܣ�q3-3 �����滻������ q3-3 ��������������ǩ

}

