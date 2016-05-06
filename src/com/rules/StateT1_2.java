package com.rules;

import com.XPath.PathParser.ASTPath;
import com.ibm.actor.DefaultMessage;

import java.util.List;
import java.util.Stack;

/**
 * Created by qin on 2015/10/10.
 */
public class StateT1_2 extends StateT1 {
    protected State _q3;//��� preds

    protected StateT1_2(ASTPath path, State q3) {
        super(path);
        _q3 = q3;
    }

    public static State TranslateState(ASTPath path) {//���´���T1-2
        State q3 = StateT3.TranslateStateT3(path.getFirstStep().getPreds());
        return new StateT1_2(path, q3);
    }

    public void startElementDo(String tag, int layer,MyStateActor curactor) {// layer ��ʾ��ǰ��ǩ tag �Ĳ���
        if((getLevel() == layer) && (tag.equals(_test))) {//Ӧ��ƥ��Ĳ��� getLayer������ ��ǰ��ǩ tag �Ĳ������
            curactor.addWTask(new WaitTask(layer,false,tag));

            _q3.setLevel(getLevel() + 1);//����ν�ʵĲ����϶��ǵ�ǰӦ��ƥ���������Ӧ�ı�ǩ������Ĳ���
            curactor.getMyStack().push(new ActorTask(layer,_q3));

        }
    }

    public void endElementDo(String tag,int layer,MyStateActor curactor){
        if (tag.equals(_test)) {// �����Լ��Ľ�����ǩ�����
            this.processSelfEndTag(layer,curactor);
        }else if (layer == getLevel() - 1) { // �����ϲ������ǩ
            // (�������ϲ������ǩ����T1-2��Ϊһ��������path��T1-5 ��ʱ��Ҳ�����stackActor�У���T1-6~T1-8�ᱻ����paActor��)
            // T1-5 ʱ����T1-5 ����ͬһ��ջ��T1-6~T1-8 ����pathstack��
            curactor.popFunction();   // T1-2��ջ
            Stack ss=curactor.getMyStack();
            if(!ss.isEmpty()){
                ((State)((ActorTask)ss.peek()).getObject()).endElementDo(tag,layer,curactor);
            }else{
                actorManager.detachActor(curactor);
            }
        }
    }
}