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
        this._predstack = new Stack();
    }

    public static State TranslateState(ASTPath path) {//����T1-4
        State q3 = StateT3.TranslateStateT3(path.getFirstStep().getPreds());
        return new StateT1_4(path, q3);
    }

    public void startElementDo(String tag,int layer,MyStateActor curactor) throws CloneNotSupportedException{
        if ((layer >= getLevel()) && (tag.equals(_test))) {
            WaitTask wtask;
            ActorTask atask;
            String name=((Integer)this._predstack.hashCode()).toString().concat("T1-4.prActor");
            Actor actor=(actors.get(name));// preds�� actor
            // �� tlist �������Ҫ�ȴ�ƥ�������ģ��
            wtask=new WaitTask(layer,false,tag);
            curactor.addWTask(wtask);

            if(actor == null){// ��ν�� actor ��û�д��� --> _predstack һ��Ϊ��
                _q3.setLevel(layer + 1);
                actor =actorManager.createAndStartActor(MyStateActor.class, name);
                actors.put(actor.getName(), actor);

                atask=new ActorTask(this._predstack);
                dmessage=new DefaultMessage("stack", atask);
                actorManager.send(dmessage, curactor, actor);
                //���� q' �� prActor
                atask=new ActorTask(layer,_q3);
                dmessage=new DefaultMessage("pushTask", atask);
                actorManager.send(dmessage,curactor,actor);
            }
            else{  // ��ν�� actor �Ѿ�������,���� q' �� prActor����
                State currQ=(State)_q3.copy();
                currQ.setLevel(layer + 1);
                atask=new ActorTask(layer,_q3);
                dmessage=new DefaultMessage("pushTask",atask);
                actorManager.send(dmessage, curactor, actor);
            }
            // ��Ҫ����һ��ν��actor������Ϣ��subject
            //actor.willReceive("startE");
            //actor.peekNext("startE",isX1(getLevel()+1));// ν��actor���ܵı�ǩӦ���ǵ�ǰ��ǩ�� child
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

            for (int i = 0; i < list.size(); i++) {
                wtask = (WaitTask) list.get(i);
                if (wtask.getId() == layer) {
                    if (wtask.isSatisfied()) {
                        if (name.equals("stackActor")) {//��stack��-==>PC��
                            if (currstack.size() == 1) {//���
                                curactor.output(wtask);
                                if (list.size() > 1) {
                                    name = ((Integer) this._predstack.hashCode()).toString().concat("T1-4.paActor");
                                    Actor actor = (actors.get(name));// path�� actor
                                    if (this._q3 instanceof StateT2_3 || this._q3 instanceof StateT2_4
                                            || this._q3 instanceof StateT3_3 || this._q3 instanceof StateT3_4) {
                                        //T1-4 ������ν�� _q3 ���� AD ��ν��
                                        // �� list ���ҵ���صȴ��Ŀ�������� wt���� predResult �� wt
                                        // && pop(����AD��ν��) && remove (��Щν����صĵȴ� wt)
                                        if (actor != null) {//T1-4 �� prActor����-->����predstack��Ϊ�գ�����ν�ʵȴ�
                                            currstack = ((MyStateActor) actor).getMyStack(); //T1-4.predStack
                                            if (!currstack.isEmpty()) {
                                                for (int j = 0; j < i; j++) {
                                                    if (((WaitTask) list.get(j)).isWaitOutput()) {
                                                        dmessage = new DefaultMessage("predResult", wtask.getPredR());
                                                        actorManager.send(dmessage, curactor, curactor);
                                                    }
                                                    atask = ((ActorTask) (currstack.peek()));  // ��ǰջ�� ��task
                                                    State state = (State) atask.getObject();

                                                }
                                            }
                                        }
                                    }
                                } else {//��stack�� && ��ΪT1-5�ĺ���path
                                    for (int j = 0; j < i; j++) {
                                        wtask = (WaitTask) curactor.tlist.get(j);
                                        if (wtask.getId() == id) {
                                            atask = new ActorTask(id, wtask.getPathR());
                                            dmessage = new DefaultMessage("pathResult", atask);
                                            actorManager.send(dmessage, curactor, curactor);
                                        }
                                    }
                                }
                            } else {//��ΪAD �����path��һ����-->��paActor��
                                for (int j = 0; j < i; j++) {
                                    wtask = (WaitTask) curactor.tlist.get(j);
                                    atask = new ActorTask(id, wtask.getPathR());
                                    if (wtask.getId() == id) {//��paActor�� && ��ΪT1-5�ĺ���path
                                        dmessage = new DefaultMessage("pathResult", atask);
                                        actorManager.send(dmessage, curactor, curactor);
                                    } else {//��T1-6��T1-7��T1-8�� paActor ��
                                        dmessage = new DefaultMessage("pathResult", atask);
                                        actorManager.send(dmessage, curactor, curactor.getResActor());
                                    }
                                }
                            }
                        }
                        //���Լ��Ľ�����ǩ�����ܵ�ǰwt�Ƿ����㣬��Ҫɾ��
                        curactor.removeWTask(wtask);
                    }
                }
            }
        } else if (layer == getLevel() - 1) { // �����ϲ������ǩ(T1-7��Ϊһ��������path)
            // (�������ϲ������ǩ����T1-7��Ϊһ��������path
            // ��T1-5 ��ʱ��Ҳ�����stackActor�У���T1-6~T1-8�ᱻ����paActor��)
            // T1-5 �ĺ�����pathʱ����T1-5 ����ͬһ��ջ��T1-6~T1-8 ����pathstack��
            curactor.popFunction();   // T1-7��ջ
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


