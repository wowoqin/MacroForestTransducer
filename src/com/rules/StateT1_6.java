package com.rules;

import com.XPath.PathParser.ASTPath;
import com.XPath.PathParser.ASTPreds;
import com.ibm.actor.Actor;
import com.ibm.actor.DefaultMessage;

import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

/**
 * Created by qin on 2015/10/10.
 */
public class StateT1_6 extends StateT1{
    protected  State  _q3;//��� preds
    protected  State  _q1;//������ path
    protected StateT1_6(ASTPath path,State q3,State q1){
        super(path);
        _q3=q3;
        _q1=q1;
        _pathstack =new Stack();
    }

    public static State TranslateState(ASTPath path){//���´���T1-6
        State q3=StateT3.TranslateStateT3(path.getFirstStep().getPreds());
        State q1=StateT1.TranslateStateT1(path.getRemainderPath());
        return new StateT1_6(path,q3,q1);//Ȼ��ѹ��ջ
    }
    @Override
    public void startElementDo(String tag,int layer,MyStateActor curactor) {// layer �ǵ�ǰ tag �Ĳ���
        if((getLevel() == layer)  && (tag.equals(_test))){//Ӧ��ƥ��Ĳ��� getLevel������ ��ǰ�������
            WaitTask wtask;
            ActorTask atask;
            Stack currStack=curactor.getMyStack();
            String name=((Integer)this._pathstack.hashCode()).toString().concat("T1-6.paActor");
            Actor actor=(actors.get(name));// path�� actor
            // �� tlist �������Ҫ�ȴ�ƥ�������ģ��
            wtask=new WaitTask(layer,false,null);
            curactor.addWTask(wtask);

            _q3.setLevel(getLevel() + 1);
            _q1.setLevel(getLevel() + 1);

            if(actor == null){  // ��pathActor ��û�д��� --> _pathstack һ��Ϊ��
                actor =actorManager.createAndStartActor(MyStateActor.class, name);
                actors.put(actor.getName(), actor);

                atask=new ActorTask(this._pathstack);
                dmessage=new DefaultMessage("stack", atask);
                actorManager.send(dmessage, curactor, actor);

                atask=new ActorTask(layer,_q3);
                currStack.push(atask);
                //���� q'' �� paActor
                atask=new ActorTask(layer,_q1);
                dmessage=new DefaultMessage("pushTask", atask);
                actorManager.send(dmessage,curactor,actor);
            } else{  // ��path  actor �Ѿ�������,���� q'' �� paActor����
                atask=new ActorTask(layer,_q3);
                currStack.push(atask);
                //���� q'' �� paActor
                atask=new ActorTask(layer,_q1);
                dmessage=new DefaultMessage("pushTask", atask);
                actorManager.send(dmessage, curactor, actor);
            }
        }
    }

    public void endElementDo(String tag,int layer,MyStateActor curactor){
        if (tag.equals(_test)) {// �����Լ��Ľ�����ǩ�����
            this.processSelfEndTag(layer,curactor);
        }else if (layer == getLevel() - 1) { // �����ϲ������ǩ(�϶���Ϊ����path)
            // (�������ϲ������ǩ����T1-6��Ϊһ��������path��T1-5 ��ʱ��Ҳ�����stackActor�У���T1-6~T1-8�ᱻ����paActor��)
            // T1-5 �ĺ�����pathʱ����T1-5 ����ͬһ��ջ��T1-6~T1-8 ����pathstack��
            curactor.popFunction();   // T1-5��ջ
            Stack currstack=curactor.getMyStack();
            if(currstack.isEmpty()) {   // ����֮��ǰactor ���ڵ�stack Ϊ���ˣ���ɾ����ǰ actor
                actorManager.detachActor(curactor);
            }else{                      // T1-6 ��Ϊ T1-5 �ĺ��� path
                State state =(State)((ActorTask)(currstack.peek())).getObject();
                if(state instanceof StateT1_5){
                    state.endElementDo(tag,layer,curactor);
                }
            }
        }
    }
}
