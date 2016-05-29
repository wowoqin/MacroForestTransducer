package com.rules;

import com.XPath.PathParser.ASTPreds;
import com.taskmodel.ActorTask;

import java.util.Stack;

/**
 * Created by qin on 2015/10/10.
 */
public class StateT2_3 extends StateT2{

    protected  StateT2_3(ASTPreds preds){
        super(preds);
    }

    public static StateT2 TranslateState(ASTPreds preds){//���´���T2-3
        return new StateT2_3(preds);
    }

    public void startElementDo(String tag,int layer,MyStateActor curactor) {// layer �ǵ�ǰ tag �Ĳ���
        if((getLevel()>=layer) && (tag.equals(_test))){
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

    //ν��Ҫ�����Լ������ϲ������ǩ��������Լ��Ǽ��ʧ�ܵ�
    public void endElementDo(String tag,int layer,MyStateActor curactor){
        // �����ϲ������ǩ��ν�ʼ��ʧ�ܣ���ջ && remove �ȴ���ǰջ��T2-3����� wt
        if(layer==getLevel()-1){
            Stack ss=curactor.getMyStack();
            ActorTask atask=(ActorTask)ss.peek();
            //remove �ȴ���ǰջ��T2-3����� wt-->��atask��id���
            curactor.FindAndRemoveFailedWTask(atask);
            //pop(T2-3)
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

