package com.rules;

import com.taskmodel.ActorTask;

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
        // �Լ��������ϲ������ǩ��ν�ʼ��ʧ�ܣ���ջ && remove �ȴ���ǰջ�� qw ����� wt
        if (layer == getLevel() - 1) {
            Stack ss=curactor.getMyStack();
            ActorTask atask=((ActorTask) ss.peek());//(id,qw,isInself)
            int id=atask.getId();
            boolean isInSelf=atask.isInSelf();
            //pop(qw)
            curactor.popFunction();
            //����Ϣ��id,false,isInself��
            curactor.sendPredsResult(new ActorTask(id, false, isInSelf));
            //��ǰջ��Ϊ�գ�ջ������endElementDo �����������T1-2����T1-6��/��ջ����ͬ������ǩ��waitState���ȣ�
            if (!ss.isEmpty()) {
                atask=((ActorTask) ss.peek());
                State state=((State) (atask.getObject()));
                // T1-2 ��T1-6�Ľ�����ǩ
                if(state instanceof StateT1_2 || state instanceof StateT1_6){
                    state.endElementDo(tag, layer, curactor);
                }else if(((state instanceof StateT2_3) || (state instanceof StateT2_4))
                                                && (!atask.isInSelf())){// T2-3 ��Ϊ AD ��test��ν��
                    curactor.processSameADPred();
                }
            }else {
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
