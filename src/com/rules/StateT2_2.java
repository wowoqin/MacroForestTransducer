package com.rules;

import com.XPath.PathParser.ASTPreds;
import com.ibm.actor.Actor;
import com.ibm.actor.DefaultMessage;

import java.util.List;
import java.util.Queue;
import java.util.Stack;

/**
 * Created by qin on 2015/10/10.
 */
public class StateT2_2 extends StateT2{
    protected  State _q3;

    protected  StateT2_2(ASTPreds preds,State q3){
        super(preds);
        _q3=q3;
    }

    public static StateT2 TranslateState(ASTPreds preds){//���´���T2-2
        State q3=StateT3.TranslateStateT3(preds.getFirstStep().getPreds());
        return new StateT2_2(preds,q3);
    }

    public void startElementDo(String tag,int layer,MyStateActor curactor) {
        if((getLevel()==layer) && (tag.equals(_test))){// T2-2 ��testƥ��
            curactor.addWTask(new WaitTask(layer,false,"true"));

            //����ν�ʵĲ����϶��ǵ�ǰӦ��ƥ���������Ӧ�ı�ǩ������Ĳ���
            _q3.setLevel(getLevel() + 1);
            curactor.getMyStack().push(new ActorTask(layer,_q3));// ���ڵ�ǰջ��ѹջ�������Ϳ���ֱ��ѹջ�����÷�����Ϣ��

        }
    }


    public void endElementDo(String tag,int layer,MyStateActor curactor) {
        // �����ϲ������ǩ��ν�ʼ��ʧ�ܣ���ջ && remove �ȴ���ǰջ��T2-2����� wt
        if (layer == getLevel() - 1) {
            Stack ss = curactor.getMyStack();
            //pop(T2-2)
            curactor.popFunction();
            //��ǰջ��Ϊ�գ�ջ������endElementDo ���������/��ջ�ȣ�
            if (!ss.isEmpty()) {
                ((State) (((ActorTask) ss.peek()).getObject())).endElementDo(tag, layer, curactor);
            } else {
                actorManager.detachActor(curactor);
            }
        }
    }
}
