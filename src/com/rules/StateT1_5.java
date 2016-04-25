package com.rules;

import com.XPath.PathParser.ASTPath;
import com.ibm.actor.Actor;
import com.ibm.actor.DefaultMessage;

import java.util.List;
import java.util.Stack;

/**
 * Created by qin on 2015/10/10.
 */
public class StateT1_5 extends StateT1{
    protected  State  _q1;//��� ���� path

    protected StateT1_5(ASTPath path,State q1){
        super(path);
        _q1=q1;
    }

    public static State TranslateState(ASTPath path){//���´���T1-5
        State q1=StateT1.TranslateStateT1(path.getRemainderPath());
        return new StateT1_5(path,q1);
    }

    public void startElementDo(String tag,int layer,MyStateActor curactor) {
        if((getLevel() == layer) && (tag.equals(_test))) {//Ӧ��ƥ��Ĳ��� getLevel������ ��ǰ�������
            WaitTask wtask = new WaitTask(layer,true,null);
            curactor.addWTask(wtask);

            _q1.setLevel(getLevel() + 1); //q1 ������ path���϶��ǵ�ǰ��ǩ�������м��
            ActorTask atask=new ActorTask(layer,_q1);
            curactor.getMyStack().push(atask);

        }
    }

    public void endElementDo(String tag,int layer,MyStateActor curactor){
        if (tag.equals(_test)) {// �����Լ��Ľ�����ǩ�����
            this.processSelfEndTag(layer,curactor);
        }else if (layer == getLevel() - 1) { // �����ϲ������ǩ(�϶���Ϊ����path)
            // (�������ϲ������ǩ����T1-5��Ϊһ��������path��T1-5 ��ʱ��Ҳ�����stackActor�У���T1-6~T1-8�ᱻ����paActor��)
            // T1-5 �ĺ�����pathʱ����T1-5 ����ͬһ��ջ��T1-6~T1-8 ����pathstack��
            curactor.popFunction();   // T1-5��ջ
            Stack currstack=curactor.getMyStack();
            if(currstack.isEmpty()) {   // ����֮��ǰactor ���ڵ�stack Ϊ���ˣ���ɾ����ǰ actor
                actorManager.detachActor(curactor); // remove( T1-4.prActor )
            }else{                      // T1-5 ��Ϊ T1-5 �ĺ��� path
                State state =(State)((ActorTask)(currstack.peek())).getObject();
                if(state instanceof StateT1_5){
                    state.endElementDo(tag,layer,curactor);
                }
            }
        }
    }


}
