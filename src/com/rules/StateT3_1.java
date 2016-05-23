package com.rules;

import com.XPath.PathParser.ASTPreds;
import com.ibm.actor.Actor;
import com.ibm.actor.DefaultMessage;

import java.util.List;
import java.util.Stack;

/**
 * Created by qin on 2015/10/10.
 */
public class StateT3_1 extends StateT3{
    protected  State _q3;//���preds'
    protected  State _q2;//��顾child::test��

    protected  StateT3_1(ASTPreds preds,State q3,State q2){
        super(preds);
        _q3=q3;
        _q2=q2;
        this._predstack=new Stack();
    }

    public static StateT3 TranslateState(ASTPreds preds){//���´���T3-1
        State q3=StateT3.TranslateStateT3(preds.getRemainderPreds());
        State q2=StateT2.TranslateStateT2(StateT3.getSinglePred(preds));
        return new StateT3_1(preds,q3,q2);
    }
    public void startElementDo(String tag,int layer,MyStateActor curactor) throws CloneNotSupportedException {
        //���� q3-1 �о�Ӧ�ö� preds' ͬʱ���м��
        if(getLevel() == layer) {//Ӧ��ƥ��Ĳ��� getLayer������ ��ǰ��ǩ tag �Ĳ������
            WaitTask wtask;
            boolean isFindInThis = false;

            _q2.setLevel(getLevel());//q2 ��顾child::test����Ӧ��ƥ��ı�ǩ�Ĳ��� ����
            _q3.setLevel(getLevel());// q3 ���preds'��Ӧ��ƥ��ı�ǩ�Ĳ����뵱ǰ [test] ͬһ��

            Stack stack=curactor.getMyStack();
            ActorTask atask=(ActorTask)stack.peek();
            int id=atask.getId();//��ǰջ���� id

            String name=((Integer)this._predstack.hashCode()).toString().concat("T3-1.prActor");
            Actor actor=(actors.get(name));// preds'�� actor


            if (tag .equals(_test)){
                //Ҫ��testƥ�䣬��ֱ�Ӽ��preds'��preds' �Ľ����Ϊ T3-1 �Ľ��
                curactor.popFunction();
                stack.push(new ActorTask(id,_q3));
            }
            else {// test��ƥ�䣬��� q'' �� q'''&& preds'.startElementDo(tag,layer)
                //1.(id,T3-1) ��Ϊ (id,waitstate)
                State waitState=new WaitState();
                waitState.setLevel(((State) atask.getObject()).getLevel());
                curactor.popFunction();
                curactor.pushFunction(new ActorTask(id, waitState));
                // PC:
                List list=curactor.tlist;
                if(!list.isEmpty()){
                    for(int i=(list.size()-1);i>=0;i--) {
                        wtask = (WaitTask) list.get(i);
                        if (wtask.getId() == id) {//���Լ���list���ҵ���id��ͬ��wt-->��T3-1Լ�� PC ���test
                            isFindInThis=true;
                            //2.push��layer,q'''��
                            curactor.pushFunction(new ActorTask(layer,_q2));
                            //push(layer,q'')
                            if(actor==null){
                                actor=actorManager.createAndStartActor(MyStateActor.class, name);

                                dmessage=new DefaultMessage("stack",this._predstack);
                                actorManager.send(dmessage, curactor, actor);

                                dmessage=new DefaultMessage("setCategory","T3PredsActor");
                                actorManager.send(dmessage,curactor,actor);

                                dmessage=new DefaultMessage("pushTask",new ActorTask(layer,_q3));
                                actorManager.send(dmessage,curactor,actor);
                            }else {
                                State currQ=(State)_q3.copy();
                                currQ.setLevel(layer + 1);
                                dmessage=new DefaultMessage("pushTask",new ActorTask(layer,currQ));
                                actorManager.send(dmessage, curactor, actor);
                            }
                            //3. add��layer,false,false��
                            curactor.addWTask(new WaitTask(layer,false,"false"));
                            break;
                        }
                    }
                }
                if(!isFindInThis){//AD:
                    //2.push��id,q'''��
                    curactor.pushFunction(new ActorTask(layer,_q2));
                    //push(id,q'')
                    if(actor==null){
                        actor=actorManager.createAndStartActor(MyStateActor.class, name);

                        dmessage=new DefaultMessage("stack",this._predstack);
                        actorManager.send(dmessage, curactor, actor);

                        dmessage=new DefaultMessage("setCategory","T3PredsActor");
                        actorManager.send(dmessage,curactor,actor);

                        dmessage=new DefaultMessage("pushTask",new ActorTask(layer,_q3));
                        actorManager.send(dmessage,curactor,actor);
                    }else {
                        State currQ=(State)_q3.copy();
                        currQ.setLevel(layer + 1);
                        dmessage=new DefaultMessage("pushTask",new ActorTask(layer,currQ));
                        actorManager.send(dmessage, curactor, actor);
                    }
                    //3. add��id,false,false��
                    curactor.addWTask(new WaitTask(id,false,"false"));
                }
                //q''.startElementDo(tag,layer)
                dmessage=new DefaultMessage("startE",new ActorTask(layer,tag));
                actorManager.send(dmessage, curactor, actor);
            }
        }
    }

    //���۳ɰܣ�q3-1 �����滻������ q3-1 ���������ϲ������ǩ
}

