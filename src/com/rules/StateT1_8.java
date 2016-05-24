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
        _q1.setLevel(this.getLevel()+1);
        _q3.setLevel(this.getLevel()+1);

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
            curactor.addWTask(new WaitTask(layer, false, null));

            String name=((Integer)this._predstack.hashCode()).toString().concat("T1-8.prActor");
            Actor actor=(actors.get(name));// preds�� actor

            if (actor == null) {  // ��predsActor ��û�д��� --> _predstack һ��Ϊ��
                stacklist.add(this._predstack);
                actor = actorManager.createAndStartActor(MyStateActor.class, "T1-8.prActor");
                actors.put(actor.getName(),actor);

                dmessage=new DefaultMessage("resActor", null);
                actorManager.send(dmessage, curactor, actor);
                //���� q' �� prActor
                _q3.setLevel(layer + 1);
                dmessage = new DefaultMessage("pushTask", new ActorTask(layer, _q3));
                actorManager.send(dmessage, curactor, actor);
            } else {  // ��preds �� actor �Ѿ�������,���� q'' �� paActor����
                State currQ = (State) _q3.copy();
                currQ.setLevel(layer + 1);
                dmessage = new DefaultMessage("pushTask", new ActorTask(layer, currQ));
                actorManager.send(dmessage, curactor, actor);
            }

            name=((Integer)this._pathstack.hashCode()).toString().concat("T1-8.paActor");
            actor=(actors.get(name));// path�� actor
            if (actor == null) {  // �� pathActor ��û�д��� --> _pathstack һ��Ϊ��
                stacklist.add(this._pathstack);
                actor = actorManager.createAndStartActor(MyStateActor.class, name);
                actors.put(actor.getName(),actor);

                dmessage=new DefaultMessage("resActor", null);
                actorManager.send(dmessage, curactor, actor);
                //���� q'' �� paActor
                _q1.setLevel(layer + 1);
                dmessage = new DefaultMessage("pushTask", new ActorTask(layer, _q1));
                actorManager.send(dmessage, curactor, actor);
            } else {  // ��path  actor �Ѿ�������,���� q'' �� paActor����
                State currQ = (State) _q1.copy();
                currQ.setLevel(layer + 1);
                dmessage = new DefaultMessage("pushTask", new ActorTask(layer, currQ));
                actorManager.send(dmessage, curactor, actor);
            }
        }
    }

    public void endElementDo(String tag, int layer, MyStateActor curactor) {

        WaitTask wtask;
        ActorTask atask;
        Stack currstack=curactor.getMyStack();

        if (tag.equals(_test)) {  // �����Լ��Ľ�����ǩ�����
            int id=((ActorTask)currstack.peek()).getId(); // ��ǰջ�� task �� id
            String name=curactor.getName();
            List list=curactor.getTlist();

            for(int i=0;i<list.size();i++){
                wtask = (WaitTask)list.get(i);
                if (wtask.getId()==layer) {
                    if (wtask.isSatisfied()) {
                        if(name.equals("stackActor")){//��stack��-==>PC��
                            if(currstack.size()==1){//���
                                curactor.output(wtask);
                                // �� T1-8.pathStack �е�ջ֮���� AD ��ĺ��� path �ļ�飬
                                // ����Ҫ�ѵ�ǰ����� wt.paResult���Ƶ�֮ǰ�ĵȴ��� wt.paResult
                                if(list.size()>1){
                                    name=((Integer)this._pathstack.hashCode()).toString().concat("T1-7.paActor");
                                    Actor actor=(actors.get(name));// path�� actor
                                    currstack=((MyStateActor)actor).getMyStack(); //T1-7.pathStack
                                    if(!currstack.isEmpty()){
                                        atask = ((ActorTask)(currstack.peek()));  // ��ǰջ�� ��task
                                        State state =(State) atask.getObject();
                                        if(state instanceof StateT1_3|| state instanceof StateT1_4
                                                ||state instanceof StateT1_7|| state instanceof StateT1_8){
                                            for(int j=0;j<i;j++){
                                                if(((WaitTask) list.get(j)).isWaitOutput()){
                                                    dmessage=new DefaultMessage("pathResult",wtask.getPathR());
                                                    actorManager.send(dmessage,curactor,curactor);
                                                }
                                            }

                                        }

                                    }
                                }
                            }else {//��stack�� && ��ΪT1-5�ĺ���path
                                for(int j=0;j<i;j++){
                                    wtask = (WaitTask) list.get(j);
                                    if(wtask.getId()==id){
                                        atask=new ActorTask(id,wtask.getPathR());
                                        dmessage=new DefaultMessage("pathResult",atask);
                                        actorManager.send(dmessage,curactor,curactor);
                                    }
                                }
                            }
                        }else {//��ΪAD �����path��һ����-->��paActor��
                            for(int j=0;j<i;j++){
                                wtask = (WaitTask)list.get(j);
                                atask=new ActorTask(id,wtask.getPathR());
                                if(wtask.getId()==id){//��paActor�� && ��ΪT1-5�ĺ���path
                                    dmessage=new DefaultMessage("pathResult",atask);
                                    actorManager.send(dmessage,curactor,curactor);
                                }else{//��T1-6��T1-7��T1-8�� paActor ��
                                    dmessage=new DefaultMessage("pathResult",atask);
                                    actorManager.send(dmessage,curactor,curactor.getResActor());
                                }
                            }
                        }
                    }
                    //���Լ��Ľ�����ǩ�����ܵ�ǰwt�Ƿ����㣬��Ҫɾ��
                    curactor.removeWTask(wtask);
                }
            }
        }else if (layer == getLevel() - 1) { // �����ϲ������ǩ(T1-8��Ϊһ��������path)
            // (�������ϲ������ǩ����T1-8��Ϊһ��������path
            // ��T1-5 ��ʱ��Ҳ�����stackActor�У���T1-6~T1-8 �ᱻ���� paActor��)
            // T1-5 �ĺ�����pathʱ����T1-5 ����ͬһ��ջ��T1-6~T1-8 ���� pathstack��
            curactor.popFunction();   // T1-8��ջ
            currstack=curactor.getMyStack();
            if(currstack.isEmpty()) {   // ����֮��ǰactor ���ڵ�stack Ϊ���ˣ���ɾ����ǰ actor
                actorManager.detachActor(curactor);
            }else{                      // T1-8��Ϊ T1-5 �ĺ��� path
                State state =(State)((ActorTask)(currstack.peek())).getObject();
                if(state instanceof StateT1_5){
                    state.endElementDo(tag, layer, curactor);
                }
            }
        }

    }
}