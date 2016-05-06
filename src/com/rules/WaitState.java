package com.rules;

import java.util.Stack;

/**
 * Created by qin on 2016/4/18.
 */
public class WaitState extends State {
    @Override
    public void startElementDo(String tag, int layer, MyStateActor curactor) throws CloneNotSupportedException {

    }

    @Override
    public void endElementDo(String tag, int layer, MyStateActor curactor) {
        // �����ϲ������ǩ��ν�ʼ��ʧ�ܣ���ջ && remove �ȴ���ǰջ�� qw ����� wt
        if(layer==getLevel()-1){
            Stack ss=curactor.getMyStack();
            ActorTask atask=(ActorTask)ss.peek();
            //remove �ȴ���ǰջ�� qw ����� wt
            curactor.FindAndRemoveFailedWTask(atask);
            //pop(qw)
            curactor.popFunction();
            //��ǰջ��Ϊ�գ���ջ��ΪstateT1��ջ������endElementDo ���������/��ջ�ȣ�
            if (!ss.isEmpty()) {
                ((State) (((ActorTask) ss.peek()).getObject())).endElementDo(tag, layer, curactor);
            } else {
                actorManager.detachActor(curactor);
            }
        }
    }

    @Override
    public String getNodeTest() {
        return null;
    }

    @Override
    public Object copy() throws CloneNotSupportedException {
        return null;
    }
}
