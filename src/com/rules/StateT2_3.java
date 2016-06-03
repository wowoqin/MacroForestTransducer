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
        if((layer >= getLevel()) && (tag.equals(_test))){
            Stack ss=curactor.getMyStack();
            ActorTask atask=((ActorTask) ss.peek());
            int id=atask.getId();
            boolean isInSelf=atask.isInSelf();

            List list=this.getList();
            if(!list.isEmpty()){  //T3-3 && T3-3.q'''���ɹ�
                WaitState waitState=new WaitState();
                waitState.setLevel(((State) atask.getObject()).getLevel());
                waitState.getList().add(list.get(0));
                curactor.popFunction();
                //(id,T2-3,isInself) ��Ϊ ��id,qw,isInself��
                curactor.pushTaskDo(new ActorTask(id, waitState, isInSelf));
                //���� T3-3.q'''���ɹ�-->
                curactor.sendPredsResult(new ActorTask(id, true, true));//ȷ���Ǹ��Լ���
                /*��ν��ȫ���������q''�����㵯ջ��ʱ��--> ջ��Ϊ(id,qw,isInSelf)��
                 *  1. ���㣺wt��layer,true,"true"��
                *   2. �����㣺wt��layer,true,"false"��
                *                          �� qw ��֮�󣺣���-->ջ��(id,T2-3,false)
                * */
            }else{  //T2-3
                //����ν�ʽ�� && pop ��ǰջ��
                curactor.popFunction();
                curactor.sendPredsResult(new ActorTask(id, true, isInSelf));
                if(ss.isEmpty()){
                    actorManager.detachActor(curactor);
                }else{
                    //pop ����֮����T2-3 && T2-3 ��Ҫ�����ϼ�actor��
                    atask=((ActorTask) ss.peek());
                    State state=(State)atask.getObject();
                    if((state instanceof StateT2_3) && !isInSelf){// T2-3 ��Ϊ AD ��test��ν��
                       curactor.processSameADPred();
                    }
                }
            }
        }
    }

    //ν��Ҫ�����Լ������ϲ������ǩ��������Լ��Ǽ��ʧ�ܵ�
    public void endElementDo(String tag,int layer,MyStateActor curactor){
        // �Լ��������ϲ������ǩ��ν�ʼ��ʧ�ܣ���ջ && remove �ȴ���ǰջ��T2-3����� wt
        if (layer == getLevel() - 1) {
            Stack ss=curactor.getMyStack();
            ActorTask atask=((ActorTask) ss.peek());//(id,T2-3,isInself)
            int id=atask.getId();
            boolean isInSelf=atask.isInSelf();
            //pop(id,T2-3,isInself)
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

