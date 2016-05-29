package com.rules;

import com.XPath.PathParser.ASTPreds;
import com.taskmodel.ActorTask;

import java.util.Stack;

/**
 * Created by qin on 2015/10/10.
 */
public class StateT2_1 extends StateT2 {

    protected StateT2_1(ASTPreds preds) {
        super(preds);
    }

    public static StateT2 TranslateState(ASTPreds preds) {//���´���T2-1
        return new StateT2_1(preds);
    }

    public void startElementDo(String tag, int layer, MyStateActor curactor) { // layer �ǵ�ǰ tag �Ĳ���
        if ((getLevel() == layer) && (tag.equals(_test))) {// T2-1 ���ɹ�
            Stack ss=curactor.getMyStack();
            //����ν�ʽ�� && pop ��ǰջ��
            ActorTask atask=((ActorTask) ss.peek());
            int id=atask.getId();
            boolean isInSelf=atask.isInSelf();
            curactor.popFunction();
            curactor.sendPredsResult(new ActorTask(id, true,isInSelf));
            if(ss.isEmpty())
                actorManager.detachActor(curactor);
        }
    }

    public void endElementDo(String tag, int layer, MyStateActor curactor) {
        // �Լ��������ϲ������ǩ��ν�ʼ��ʧ�ܣ���ջ && remove �ȴ���ǰջ��T2-1����� wt
        if (layer == getLevel() - 1) {
            Stack ss = curactor.getMyStack();
            //pop(T2-1)
            curactor.popFunction();
            //��ǰջ��Ϊ�գ�ջ������endElementDo �����������T1-2����T1-6��/��ջ����ͬ������ǩ��waitState���ȣ�
            if (!ss.isEmpty()) {
                ((State) (((ActorTask) ss.peek()).getObject())).endElementDo(tag, layer, curactor);
            }else {
                actorManager.detachActor(curactor);
            }
        }
    }
}




