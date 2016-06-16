package com.rules;

import com.XPath.PathParser.ASTPreds;
import com.taskmodel.ActorTask;
import com.taskmodel.WaitTask;

import java.util.List;
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

    public void startElementDo(String tag, int layer, MyStateActor curactor) throws CloneNotSupportedException { // layer �ǵ�ǰ tag �Ĳ���
        if ((getLevel() == layer) && (tag.equals(_test))) {// T2-1 ���ɹ�
            Stack ss=curactor.getMyStack();
            ActorTask atask=((ActorTask) ss.peek());//(id,T2-1,inInSelf)
            int id=atask.getId();
            boolean isInSelf=atask.isInSelf();

            List list=this.getList();//T2-1.list
            if(!list.isEmpty()){  //T3-1
                WaitState waitState=new WaitState();
                waitState.setLevel(((State) atask.getObject()).getLevel());
                waitState.getList().add(list.get(0));
                curactor.popFunction();
                //(id,T2-1,isInself) ��Ϊ ��id,qw,isInself��
                curactor.pushTaskDo(new ActorTask(id, waitState, isInSelf));
                //���� T3-1.q'''���ɹ�������Ϣ����Ϊ��һq''�Ѿ��Ǽ��ɹ������أ�
                curactor.sendPredsResult(new ActorTask(id, true, true));//ȷ���Ǹ��Լ���
            }else{  //T2-1
                //����ν�ʽ�� && pop ��ǰջ��
                curactor.popFunction();
                curactor.sendPredsResult(new ActorTask(id, true,isInSelf));
                if(ss.isEmpty())
                    actorManager.detachActor(curactor);
            }
        }
    }

    public void endElementDo(String tag, int layer, MyStateActor curactor) {
        if (layer == getLevel() - 1) {//�����ϲ������ǩ
            Stack ss=curactor.getMyStack();
            ActorTask atask=((ActorTask) ss.peek());//ջ��(id,T2-1,isInself)
            //pop(T2-1)
            curactor.popFunction();
            //����Ϣ��id,false,isInself��
            curactor.sendPredsResult(new ActorTask(atask.getId(),false, atask.isInSelf()));
            //��ʵ�ڴ˴���Ӧ�ÿ�T3-1.q''������������-->terminate

            //��ǰջ��Ϊ�գ�ջ��Ϊ T1-2����T1-6-->����endElementDo ����:���/�ϴ�/remove/�ȴ�
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




