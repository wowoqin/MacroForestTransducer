package com.rules;

import com.XPath.PathParser.ASTPreds;
import com.taskmodel.ActorTask;
import com.taskmodel.WaitTask;

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

    public void startElementDo(String tag,int layer,MyStateActor curactor) throws CloneNotSupportedException {
        if((getLevel()==layer) && (tag.equals(_test))){// T2-2 ��testƥ��
            addWTask(new WaitTask(layer, null, "true"));
            _q3.setLevel(layer + 1);
            curactor.pushTaskDo(new ActorTask(layer, _q3, true));//ȷ���Ǹ��Լ���
        }
    }


    public void endElementDo(String tag,int layer,MyStateActor curactor) {
        // �Լ��������ϲ������ǩ��ν�ʼ��ʧ�ܣ���ջ && remove �ȴ���ǰջ��T2-2����� wt
        if (layer == getLevel() - 1) {
            Stack ss=curactor.getMyStack();
            ActorTask atask=((ActorTask) ss.peek());//(id,T2-2,isInself)
            int id=atask.getId();
            boolean isInSelf=atask.isInSelf();
            //pop(T2-2)
            curactor.popFunction();
            //����Ϣ��id,false,isInself��
            curactor.sendPredsResult(new ActorTask(id, false, isInSelf));
            //��ǰջ��Ϊ�գ�ջ������endElementDo �����������T1-2����T1-6��/��ջ����ͬ������ǩ��waitState���ȣ�
            if (!ss.isEmpty()) {
                State state=((State) (((ActorTask) ss.peek()).getObject()));
                // T1-2 ��T1-6�Ľ�����ǩ
                if(state instanceof StateT1_2 || state instanceof StateT1_6){
                    state.endElementDo(tag, layer, curactor);
                }
            }else {
                actorManager.detachActor(curactor);
            }
        }
    }
}
