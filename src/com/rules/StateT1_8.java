package com.rules;

import com.XPath.PathParser.ASTPath;
import com.ibm.actor.Actor;
import com.ibm.actor.DefaultMessage;

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
            WaitTask wtask;
            ActorTask atask;
            Actor actor;
            // �� tlist �������Ҫ�ȴ�ƥ�������ģ��
            wtask = new WaitTask(layer, false, null);
            curactor.addWTask(wtask);

            String name=((Integer)this._pathstack.hashCode()).toString().concat("T1-8.paActor");
            actor=(actors.get(name));// path�� actor
            if (actor == null) {  // ��pathActor ��û�д��� --> _pathstack һ��Ϊ��
                actor = actorManager.createAndStartActor(MyStateActor.class, name);
                actors.put(actor.getName(), actor);

                atask = new ActorTask(this._pathstack);
                dmessage = new DefaultMessage("stack", atask);
                actorManager.send(dmessage, curactor, actor);
                //���� q'' �� paActor
                _q1.setLevel(layer + 1);
                atask = new ActorTask(layer, _q1);
                dmessage = new DefaultMessage("pushTask", atask);
                actorManager.send(dmessage, curactor, actor);
            } else {  // ��path  actor �Ѿ�������,���� q'' �� paActor����
                State currQ = (State) _q1.copy();
                currQ.setLevel(layer + 1);
                atask = new ActorTask(layer, _q1);
                dmessage = new DefaultMessage("pushTask", atask);
                actorManager.send(dmessage, curactor, actor);
            }

            name=((Integer)this._predstack.hashCode()).toString().concat("T1-8.prActor");
            actor=(actors.get(name));// preds�� actor
            if (actor == null) {  // ��predActor ��û�д��� --> _predstack һ��Ϊ��
                actor = actorManager.createAndStartActor(MyStateActor.class, "T1-8.prActor");
                actors.put(actor.getName(), actor);

                atask = new ActorTask(this._predstack);
                dmessage = new DefaultMessage("stack", atask);
                actorManager.send(dmessage, curactor, actor);
                //���� q' �� prActor
                _q3.setLevel(layer + 1);
                atask = new ActorTask(layer, _q3);
                dmessage = new DefaultMessage("pushTask", atask);
                actorManager.send(dmessage, curactor, actor);
            } else {  // ��preds  actor �Ѿ�������,���� q'' �� paActor����
                State currQ = (State) _q3.copy();
                currQ.setLevel(layer + 1);
                atask = new ActorTask(layer, _q3);
                dmessage = new DefaultMessage("pushTask", atask);
                actorManager.send(dmessage, curactor, actor);
            }
        }
    }

    public void endElementDo(String tag, int layer, MyStateActor curactor) {

        WaitTask wtask;
        ActorTask atask;
        Stack currstack;

        if (tag.equals(_test)) {  // �����Լ��Ľ�����ǩ�����
            int id = ((ActorTask) (curactor.getMyStack().peek())).getId(); // ��ǰջ�� task �� id
            for (int i = 0; i < curactor.tlist.size(); i++) {
                wtask = (WaitTask) curactor.tlist.get(i);
                if (wtask.getId() == layer){
                    if(wtask.isSatisfied()) { // �ڵ�ǰcurrActor �� tlist �������������� wt
                        if (curactor.getResActor() != null) { // ��ǰ actor ���ϼ� actor ��Ϊ�գ������Լ����actor��
                            atask = new ActorTask(id, wtask.getPathR());
                            dmessage = new DefaultMessage("pathResult", atask);
                            actorManager.send(dmessage, curactor, curactor.getResActor());
                        } else { // ��ǰ actor �� resActor Ϊ��--> ��ǰ actor�� stackActor��������
                            curactor.output(wtask);
                            // �� T1-7.pathStack �е�ջ֮���� AD ��ĺ��� path �ļ�飬
                            // ����Ҫ�ѵ�ǰ����� wt.paResult���Ƶ�֮ǰ�ĵȴ��� wt.paResult
                            if (curactor.tlist.size() > 1) {
                                Actor actor = actors.get("T1-8.paActor");
                                currstack = ((MyStateActor) actor).getMyStack(); //T1-8.pathStack
                                if (!currstack.isEmpty()) {
                                    atask = ((ActorTask) (currstack.peek()));  // ��ǰջ�� ��task
                                    State state = (State) atask.getObject();
                                    if (state instanceof StateT1_3 || state instanceof StateT1_4
                                            || state instanceof StateT1_7 || state instanceof StateT1_8)
                                        for (int j = 0; j < curactor.tlist.size() - 2; j++)
                                            ((WaitTask) curactor.tlist.get(j)).setPathR(wtask.getPathR());
                                }

                                actor=(actors.get("T1-8.prActor"));
                                currstack=((MyStateActor)actor).getMyStack(); //T1-8.predStack
                                if(!currstack.isEmpty()){
                                    atask = ((ActorTask)(currstack.peek()));  // ��ǰջ�� ��task
                                    State state =(State) atask.getObject();   // ջ�� task �е� state
                                    if(state instanceof StateT2_3|| state instanceof StateT2_4
                                            ||state instanceof StateT3_3|| state instanceof StateT3_4)
                                        for(int j=0;j<curactor.tlist.size()-2;j++)
                                            ((WaitTask) curactor.tlist.get(j)).setPredR(wtask.getPredR());
                                }
                            }
                        }
                    }
                    curactor.removeWTask(wtask);
                }
            }
        } else if (layer == getLevel() - 1) { // �����ϲ������ǩ
            // (�������ϲ������ǩ����T1-8��Ϊһ��������path��T1-5 ��ʱ��Ҳ�����stackActor�У���T1-6~T1-8�ᱻ����paActor��)
            // T1-5 ʱ����T1-5 ����ͬһ��ջ��T1-6~T1-8 ����pathstack��
            currstack = curactor.getMyStack();

            curactor.popFunction();   // ��ջ

            if (currstack.isEmpty()) {
                // ����֮��ǰactor ���ڵ�stack Ϊ���ˣ���ɾ����ǰactor �� paActor
                actorManager.detachActor(actors.get("T1-8.prActor")); // remove( T1-8.prActor )
                actorManager.detachActor(actors.get("T1-8.paActor")); // remove( T1-8.paActor )
            }
            if (!currstack.isEmpty()) { // T1-8 ��Ϊ T1-5 �ĺ��� path
                atask = ((ActorTask) (currstack.peek()));  // ��ǰջ�� ��task
                State state = (State) atask.getObject();
                if (state instanceof StateT1_5){
                    actorManager.detachActor(actors.get("T1-8.prActor")); // remove( T1-8.prActor )
                    actorManager.detachActor(actors.get("T1-8.paActor")); // remove( T1-8.paActor )
                }
            }


        }

    }
}