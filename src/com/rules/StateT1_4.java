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
        if (tag.equals(_test)) {// �����Լ��Ľ�����ǩ�����
            this.processSelfEndTag(layer,curactor);
        }else if (layer == getLevel() - 1) { // �����ϲ������ǩ(�϶���Ϊ����path)
            // (�������ϲ������ǩ����T1-4��Ϊһ��������path��T1-5 ��ʱ��Ҳ�����stackActor�У���T1-6~T1-8�ᱻ����paActor��)
            // T1-5 ʱ����T1-5 ����ͬһ��ջ��T1-6~T1-8 ����pathstack��
            curactor.popFunction();   // T1-4��ջ
            Stack currstack=curactor.getMyStack();
            if(currstack.isEmpty()) {   // ����֮��ǰactor ���ڵ�stack Ϊ���ˣ���ɾ����ǰ actor
                actorManager.detachActor(curactor); // remove( T1-4.prActor )
            }else{                      // T1-4 ��Ϊ T1-5 �ĺ��� path
                State state =(State)((ActorTask)(currstack.peek())).getObject();
                if(state instanceof StateT1_5){
                    state.endElementDo(tag,layer,curactor);
                }
            }
        }
    }
}


