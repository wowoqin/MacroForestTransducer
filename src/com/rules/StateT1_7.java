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
    protected State _q1;//������ path

    protected StateT1_7(ASTPath path, State q1) {
        super(path);
        _q1 = q1;
        this._pathstack=new Stack();
    }

    public static State TranslateState(ASTPath path) {//���´���T1-7
        State q1 = StateT1.TranslateStateT1(path.getRemainderPath());
        return new StateT1_7(path, q1);
    }

    public void startElementDo(String tag, int layer,MyStateActor curactor) throws CloneNotSupportedException{
        if ((layer >= getLevel()) && (tag.equals(_test))) {
            System.out.println("T1-7.startElementDo�У���ǰactor��������" + actors.size());
            // �� tlist �������Ҫ�ȴ�ƥ�������ģ��
            addWTask(new WaitTask(layer,true,null));

            String name=((Integer)this._pathstack.hashCode()).toString().concat("T1-7.paActor");
            Actor actor=actors.get(name);
            if(this._pathstack.isEmpty()){  // ��pathActor ��û�д��� --> _pathstack һ��Ϊ��
                System.out.println("T1-7.testƥ�� && pathactor == null");
                _q1.setLevel(layer + 1);
                curactor.createAnotherActor(name, this._pathstack, new ActorTask(layer, _q1, false));
//                actor =actorManager.createAndStartActor(MyStateActor.class, name);
//                actors.put(actor.getName(), actor);
//
//                actorManager.send(new DefaultMessage("resActor", null), curactor, actor);
//                //���� q'' �� paActor
//                _q1.setLevel(layer + 1);
//                actorManager.send(new DefaultMessage("pushTask", new ActorTask(layer,_q1,false)),curactor,actor);
            } else{  // ��path  actor �Ѿ�������,���� q'' �� paActor����
                //System.out.println("T1-7.testƥ�� && pathactor != null����ǰactor��������" + actors.length);
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
        if (tag.equals(_test)) {  // �����Լ��Ľ�����ǩ�����
            for(int i=0;i<getList().size();i++){
                WaitTask wtask=(WaitTask) getList().get(i);
                if(wtask.hasReturned()){
                    curactor.doNext(wtask);
                }else{//�ȴ�--����path�Ľ����δ��������
                    //��ǰ������ǩ�Ȳ�����
                    curactor.addMessage(new DefaultMessage("endE", new ActorTask(layer, tag)));
                    curactor.peekNext("pathR");//���ȴ���path���ؽ������Ϣ
                    actorManager.awaitMessage(curactor);
                }
            }
        }else if (layer == getLevel() - 1) { // �����ϲ������ǩ(T1-7��Ϊһ��������path)
            // (�������ϲ������ǩ����T1-7��Ϊһ��������path
            // ��T1-5 ��ʱ��Ҳ�����stackActor�У���T1-6~T1-8�ᱻ����paActor��)
            // T1-5 �ĺ�����pathʱ����T1-5 ����ͬһ��ջ��T1-6~T1-8 ����pathstack��
            curactor.popFunction();   // T1-7��ջ
            Stack ss=curactor.getMyStack();
            if(ss.isEmpty()) {   // ����֮��ǰactor ���ڵ�stack Ϊ���ˣ���ɾ����ǰ actor
                actorManager.detachActor(curactor);
            }else{                      // T1-7 ��Ϊ T1-5 �ĺ��� path
                State state =(State)((ActorTask)(ss.peek())).getObject();
                if(state instanceof StateT1_5){
                    state.endElementDo(tag,layer,curactor);
                }
            }
        }
    }
}
