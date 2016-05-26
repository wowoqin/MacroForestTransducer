package com.rules;

import com.XPath.PathParser.ASTPath;
import com.ibm.actor.Actor;
import com.ibm.actor.DefaultMessage;

import java.util.List;
import java.util.Stack;

/**
 * Created by qin on 2015/10/10.
 */
public class StateT1_4 extends StateT1 implements Cloneable {
    protected State _q3;//��� preds

    protected StateT1_4(ASTPath path, State q3) {
        super(path);
        _q3 = q3;
        _q3.setLevel(this.getLevel() + 1);
        this._predstack = new Stack();
    }

    public static State TranslateState(ASTPath path) {//����T1-4
        State q3 = StateT3.TranslateStateT3(path.getFirstStep().getPreds());
        return new StateT1_4(path, q3);
    }

    public void startElementDo(String tag,int layer,MyStateActor curactor) throws CloneNotSupportedException{
        if ((layer >= getLevel()) && (tag.equals(_test))) {
            String name=((Integer)this._predstack.hashCode()).toString().concat("T1-4.prActor");
            Actor actor=(actors.get(name));// preds �� actor
            // �� tlist �������Ҫ�ȴ�ƥ�������ģ��
            curactor.addWTask(new WaitTask(layer,false,tag));

            if(actor == null){// ��ν�� actor ��û�д��� --> _predstack һ��Ϊ��
                stacklist.add(this._predstack);
                actor =actorManager.createAndStartActor(MyStateActor.class, name);
                actors.put(actor.getName(),actor);

                dmessage=new DefaultMessage("resActor", null);
                actorManager.send(dmessage, curactor, actor);
                //���� q' �� prActor
                _q3.setLevel(layer + 1);
                dmessage=new DefaultMessage("push", new ActorTask(layer,_q3));
                actorManager.send(dmessage,curactor,actor);
            }
            else{  // ��ν�� actor �Ѿ�������,���� q' �� prActor����
                State currQ=(State)_q3.copy();
                currQ.setLevel(layer + 1);
                dmessage=new DefaultMessage("pushTask",new ActorTask(layer,currQ));
                actorManager.send(dmessage, curactor, actor);
            }
        }
    }

    @Override
    public void endElementDo(String tag,int layer,MyStateActor curactor) {
        WaitTask wtask;
        ActorTask atask;
        Stack currstack = curactor.getMyStack();

        if (tag.equals(_test)) {  // �����Լ��Ľ�����ǩ�����
            int id = ((ActorTask) currstack.peek()).getId(); // ��ǰջ�� task �� id
            String name = curactor.getName();
            List list = curactor.getTlist();

            for (int i =list.size()-1; i >=0; i--) {
                wtask = (WaitTask) list.get(i);
                if (wtask.getId() == layer) {
                    if (wtask.isSatisfied()) {
                        if (name.equals("stackActor")) {
                            if (currstack.size() == 1) {//q0==T1-4-->���
                                curactor.output(wtask);
                                curactor.removeWTask(wtask);
                                //��ʱ��Ҫ�ж�T1-4 ��ν���Ƿ���AD�ᣬ���ǣ���T1-4.predstack����Ӧ��task��Ӧ��wt.predR��ΪTrue
                                if (list.size() > 1) {
                                    name = ((Integer) this._predstack.hashCode()).toString().concat("T1-4.prActor");
                                    Actor actor = (actors.get(name));// preds�� actor
                                    if (this._q3 instanceof StateT2_3){
                                        for(int j=i-1;j>=0;j--){
                                            ((WaitTask) list.get(j)).setPredR(true);
                                        }
                                        ((MyStateActor)actor).getMyStack().clear();
                                        actorManager.detachActor(actor);
                                    }else if (this._q3 instanceof StateT2_4) {
                                        for(int j=i-1;j>=0;j--){
                                            ((WaitTask) list.get(j)).setPredR(true);
                                        }
                                        ((MyStateActor)actor).getMyStack().clear();
                                        if(!((MyStateActor)actor).getTlist().isEmpty())
                                            ((MyStateActor)actor).getTlist().clear();
                                        actorManager.detachActor(actor);
                                    }else if(this._q3 instanceof StateT3_3){
                                        //����q3���ڵ�actor��list.wt.predR=true

                                        //�鿴preds'������

                                    }else if(this._q3 instanceof StateT3_4){

                                    }
                                } else {//��stack�� && T1-4��ΪT1-5�ĺ���path-->�ϴ����-->ֻ��һ�飬��ͬid�Ĳ�����
                                    atask = new ActorTask(id, wtask.getPathR());
                                    for (int j = i-1; j >=0; j--) {
                                        WaitTask wtask1 = (WaitTask) curactor.tlist.get(j);
                                        if (wtask1.getId() == id) {
                                            dmessage = new DefaultMessage("pathResult", atask);
                                            actorManager.send(dmessage, curactor, curactor);
                                            curactor.removeWTask(wtask);
                                            return;//���������������������Сѭ��j���������˴�ѭ��i��
                                        }
                                    }
                                }
                            } else {//��ΪAD �����path��һ����-->��paActor��
                                boolean inInThis=false;
                                atask = new ActorTask(id, wtask.getPathR());
                                for (int j = i-1; j >=0 && !inInThis; j--) {
                                    WaitTask wtask1 = (WaitTask) curactor.tlist.get(j);
                                    if (wtask1.getId() == id) {//��paActor�� && ��ΪT1-5�ĺ���path
                                        inInThis=true;
                                        dmessage = new DefaultMessage("pathResult", atask);
                                        actorManager.send(dmessage, curactor, curactor);
                                    }
                                }
                                if(!inInThis){
                                    //��T1-6��T1-7��T1-8�� paActor ��
                                    dmessage = new DefaultMessage("pathResult", atask);
                                    actorManager.send(dmessage, curactor, curactor.getResActor());
                                }
                                curactor.removeWTask(wtask);
                                return;//���������������������Сѭ��j���������˴�ѭ��i��
                            }
                        }
                    }
                    //���Լ��Ľ�����ǩ����ǰwt�������������-->Ӧ�ü������ν������Ӧ��Actor�Ƿ������˹�����
                    // �������ˣ���ɾ���������wt��
                    // ����û���꣬��ǰactroӦ�õ�ν��actor�������жϣ�

                }
            }
        } else if (layer == getLevel() - 1) { // �����ϲ������ǩ(T1-7��Ϊһ��������path)
            // (�������ϲ������ǩ����T1-7��Ϊһ��������path
            // ��T1-5 ��ʱ��Ҳ�����stackActor�У���T1-6~T1-8�ᱻ����paActor��)
            // T1-5 �ĺ�����pathʱ����T1-5 ����ͬһ��ջ��T1-6~T1-8 ����pathstack��
            curactor.popFunction();   // T1-4��ջ
            currstack = curactor.getMyStack();
            if (currstack.isEmpty()) {   // ����֮��ǰactor ���ڵ�stack Ϊ���ˣ���ɾ����ǰ actor
                actorManager.detachActor(curactor);
            } else {                      // T1-7 ��Ϊ T1-5 �ĺ��� path
                State state = (State) ((ActorTask) (currstack.peek())).getObject();
                if (state instanceof StateT1_5) {
                    state.endElementDo(tag, layer, curactor);
                }
            }
        }
    }
}


