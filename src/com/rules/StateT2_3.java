package com.rules;

import com.XPath.PathParser.ASTPreds;
import com.taskmodel.ActorTask;

import java.util.List;
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

    public void startElementDo(String tag,int layer,MyStateActor curactor) throws CloneNotSupportedException {// layer �ǵ�ǰ tag �Ĳ���
        if((layer>=getLevel()) && (tag.equals(_test))){
            Stack ss=curactor.getMyStack();
            ActorTask atask=((ActorTask) ss.peek());
            int id=atask.getId();
            boolean isInSelf=atask.isInSelf();

            List list=this.getList();
            if(!list.isEmpty()){  //T3-3
                WaitState waitState=new WaitState();
                waitState.setLevel(((State) atask.getObject()).getLevel());
                waitState.getList().add(list.get(0));
                curactor.popFunction();
                curactor.pushTaskDo(new ActorTask(id, waitState, isInSelf));
                curactor.sendPredsResult(new ActorTask(id, true, isInSelf));
            }else{  //T2-3
                //����ν�ʽ�� && pop ��ǰջ��
                curactor.popFunction();
                curactor.sendPredsResult(new ActorTask(id, true,isInSelf));
                if(ss.isEmpty())
                    actorManager.detachActor(curactor);
            }
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

