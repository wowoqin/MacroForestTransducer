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
    protected State _q3;//��� preds
    protected State _q1;//������ path


    protected StateT1_8(ASTPath path, State q3, State q1) {
        super(path);
        _q3 = q3;
        _q1 = q1;
        this._predstack = new Stack();
        this._pathstack = new Stack();
    }

    public static State TranslateState(ASTPath path) {//���´���T1-8
        State q3 = StateT3.TranslateStateT3(path.getFirstStep().getPreds());
        State q1 = StateT1.TranslateStateT1(path.getRemainderPath());
        return new StateT1_8(path, q3, q1);//Ȼ��ѹ��ջ
    }

    public void startElementDo(String tag, int layer, MyStateActor curactor) throws CloneNotSupportedException {
        if ((layer >= getLevel()) && (tag.equals(_test))) {///��ǰ�������ڵ���Ӧ��ƥ��Ĳ��� getLayer�����Ϳ���
            // �� tlist �������Ҫ�ȴ�ƥ�������ģ��
            addWTask(new WaitTask(layer, null, null));

            String name=((Integer)this._predstack.hashCode()).toString().concat("T1-8.prActor");
            Actor actor=(actors.get(name));// preds�� actor

            if (actor == null) {  // ��predsActor ��û�д��� --> _predstack һ��Ϊ��
                stacklist.add(this._predstack);
                actor = actorManager.createAndStartActor(MyStateActor.class, name);
                actors.put(name, actor);

                actorManager.send(new DefaultMessage("resActor", null), curactor, actor);
                //���� q' �� prActor
                _q3.setLevel(layer + 1);
                actorManager.send(new DefaultMessage("pushTask", new ActorTask(layer, _q3,false)), curactor, actor);
            } else {  // ��preds �� actor �Ѿ�������,���� q'' �� paActor����
                State currQ = (State) _q3.copy();
                currQ.setLevel(layer + 1);
                actorManager.send(new DefaultMessage("pushTask", new ActorTask(layer, currQ,false)), curactor, actor);
            }

            name=((Integer)this._pathstack.hashCode()).toString().concat("T1-8.paActor");
            actor=(actors.get(name));// path�� actor
            if (actor == null) {  // �� pathActor ��û�д��� --> _pathstack һ��Ϊ��
                stacklist.add(this._pathstack);
                actor = actorManager.createAndStartActor(MyStateActor.class, name);
                actors.put(name, actor);

                actorManager.send(new DefaultMessage("resActor", null), curactor, actor);
                //���� q'' �� paActor
                _q1.setLevel(layer + 1);
                actorManager.send(new DefaultMessage("pushTask", new ActorTask(layer, _q1,false)), curactor, actor);
            } else {  // ��path  actor �Ѿ�������,���� q'' �� paActor����
                State currQ = (State) _q1.copy();
                currQ.setLevel(layer + 1);
                actorManager.send(new DefaultMessage("pushTask", new ActorTask(layer, currQ,false)), curactor, actor);
            }
        }
    }

    public void endElementDo(String tag, int layer, MyStateActor curactor) {
        if (tag.equals(_test)) {  // �����Լ��Ľ�����ǩ�����
            for(int i=0;i<getList().size();i++){
                WaitTask wtask=(WaitTask) getList().get(i);
                if(wtask.hasReturned()){
                    curactor.doNext(wtask);
                }else{//�ȴ�--������ν�ʵ���Ϣ��δ�������������Ǻ���path�Ľ����δ��������
                    // ��ǰ������ǩ�Ȳ�����
                    curactor.addMessage(new DefaultMessage("endE", new ActorTask(layer,tag)));
                    actorManager.awaitMessage(curactor);
                    while(wtask.hasReturned())
                        curactor.doNext(wtask);
                }
            }
        }else if (layer == getLevel() - 1) { // �����ϲ������ǩ(T1-8��Ϊһ��������path)
            // (�������ϲ������ǩ����T1-8��Ϊһ��������path
            // ��T1-5 ��ʱ��Ҳ�����stackActor�У���T1-6~T1-8 �ᱻ���� paActor��)
            // T1-5 �ĺ�����pathʱ����T1-5 ����ͬһ��ջ��T1-6~T1-8 ���� pathstack��
            curactor.popFunction();   // T1-8��ջ
            Stack ss=curactor.getMyStack();
            if(ss.isEmpty()) {   // ����֮��ǰactor ���ڵ�stack Ϊ���ˣ���ɾ����ǰ actor
                actorManager.detachActor(curactor);
            }else{                      // T1-8��Ϊ T1-5 �ĺ��� path
                State state =(State)((ActorTask)(ss.peek())).getObject();
                if(state instanceof StateT1_5){
                    state.endElementDo(tag, layer, curactor);
                }
            }
        }

    }
}