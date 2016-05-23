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
public class StateT2_4 extends StateT2 implements Cloneable{
    protected  State _q3;//��� preds

    protected  StateT2_4(ASTPreds preds,State q3){
        super(preds);
        _q3=q3;
        _predstack=new Stack();
    }

    public static StateT2 TranslateState(ASTPreds preds){//���´���T2-4
        State q3=StateT3.TranslateStateT3(preds.getFirstStep().getPreds());
        return new StateT2_4(preds,q3);
    }
    public void startElementDo(String tag,int layer,MyStateActor curactor) throws CloneNotSupportedException{
        if ((layer >= getLevel()) && (tag.equals(_test))) {
            curactor.addWTask(new WaitTask(layer,false,"true"));

            String name=((Integer)this._predstack.hashCode()).toString().concat("T2-4.prActor");
            Actor actor=(actors.get(name));// preds�� actor

            if(actor == null){// �� prActor��û�д��� ��predstack һ��Ϊ��
                actor =actorManager.createAndStartActor(MyStateActor.class, name);

                dmessage=new DefaultMessage("stack", new ActorTask(this._predstack));
                actorManager.send(dmessage, curactor, actor);
                //���� q'�� prActor
                _q3.setLevel(layer + 1);
                dmessage=new DefaultMessage("pushTask", new ActorTask(layer,_q3));
                actorManager.send(dmessage,curactor,actor);
            }else{
                State currQ=(State)_q3.copy();
                currQ.setLevel(layer + 1);
                dmessage=new DefaultMessage("pushTask",new ActorTask(layer,currQ));
                actorManager.send(dmessage, curactor, actor);
            }
        }
    }

    public void endElementDo(String tag,int layer,MyStateActor curactor) {
        // �����ϲ������ǩ��ν�ʼ��ʧ�ܣ���ջ && remove �ȴ���ǰջ��T2-4����� wt
        if(layer==getLevel()-1){
            Stack ss=curactor.getMyStack();
            ActorTask atask=(ActorTask)ss.peek();
            //remove �ȴ���ǰջ��T2-4����� wt-->��atask��id���
            //�Լ��� list ����������ͬ id �� wt(������ж��)
            boolean isFindInThis = false;
            WaitTask wtask;
            List list=curactor.getTlist();
            String name=((Integer)this._predstack.hashCode()).toString().concat("T2-4.prActor");
            //�Լ��� list ����������ͬ id �� wt(������ж��)
            if(!list.isEmpty()){
                for(int i=0;i<list.size();i++) {
                    wtask = (WaitTask) list.get(i);
                    if (wtask.getId() == atask.getId()) {
                        // T2-4 ���� PC ��path����preds���뱻���ε�path����preds����ͬһ��ջ
                        isFindInThis = true;
                        list.remove(wtask);
                        actorManager.detachActor(actors.get(name));
                    }
                }
            }
            //���Լ����ڵ�list��û���ҵ���ͬ id �� wt�������ϼ� actor ����
            if(isFindInThis) {  //PC
                //pop(T2-4)
                curactor.popFunction();
                //��ǰջ��Ϊ�գ�ջ������endElementDo ���������/��ջ�ȣ�
                if (!ss.isEmpty()) {    // T1-2/T1-6/waitState
                    ((State) (((ActorTask) ss.peek()).getObject())).endElementDo(tag, layer, curactor);
                }
            }else {// T2-4 ���� AD ��path����preds�����ڵ�����ν��ջ��
                MyStateActor resActor=(MyStateActor)curactor.getResActor();
                list= resActor.getTlist();
                for(int i=0;i<list.size();i++){
                    wtask=(WaitTask)list.get(i);
                    if(wtask.getId()==atask.getId())
                        resActor.removeWTask(wtask);
                }
                //pop(T2-4)
                curactor.popFunction();
                //��ǰջ��Ϊ�գ�ջ������endElementDo ���������/��ջ�ȣ�
                if (!ss.isEmpty()) {    // waitState
                    ((State) (((ActorTask) ss.peek()).getObject())).endElementDo(tag, layer, curactor);
                } else {
                    actorManager.detachActor(curactor);
                    actorManager.detachActor(actors.get(name));
                }
            }
        }
    }
}