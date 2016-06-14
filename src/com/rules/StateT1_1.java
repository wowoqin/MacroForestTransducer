package com.rules;

import com.XPath.PathParser.ASTPath;
import com.ibm.actor.DefaultMessage;
import com.taskmodel.ActorTask;
import com.taskmodel.WaitTask;

import java.util.Stack;

/**
 * Created by qin on 2015/10/10.
 */
public class StateT1_1 extends StateT1 {

    protected StateT1_1(ASTPath path) {
        super(path);

    }

    public static State TranslateState(ASTPath path) {//���´���T1-1
        return new StateT1_1(path);
    }

    public void startElementDo(String tag, int layer, MyStateActor curactor) {
        if ((getLevel() == layer) && (tag.equals(_test))) {//Ӧ��ƥ��Ĳ���-->getLayer������ ��ǰ��ǩ-->tag �Ĳ������
            // �� list �������Ҫ���ɹ�������ģ��
            //curactor.addWTask(new WaitTask(layer,true,tag));
            addWTask(new WaitTask(layer, true, tag));
        }
    }

    public void endElementDo(String tag,int layer,MyStateActor curactor){
        // T1-1 ����Ҫ�ȴ�
        if(tag.equals(_test)){//�����Լ��Ľ�����ǩ������Լ���list�е� wt -->���/�ϴ�/remove
            //T1-6.pathʱ��ν��δ���ɹ��ʹ�����ȥ��T1-1.list.size>=1;
            for(int i=0;i<getList().size();i++){
                WaitTask wtask=(WaitTask) getList().get(i);
                curactor.doNext(wtask);
            }
        }else if (layer == getLevel() - 1) { // �����ϲ������ǩ
            // (�������ϲ������ǩ����T1-1��Ϊһ��������path��T1-5 ��ʱ��Ҳ�����stackActor�У���T1-6~T1-8�ᱻ����paActor��)
            // T1-5 ʱ����T1-5 ����ͬһ��ջ��T1-6~T1-8 ����pathstack
            Stack ss=curactor.getMyStack();
            if(!getList().isEmpty()){   //T1-1��ΪT1-6�ĺ���path && T1-6��ν�ʺ������(��ջ)
                //���ʱ��������T1-6�Ľ�����ǩ���ȴ�����pop��
                ActorTask task=(ActorTask)ss.peek();//(id,T1-1,isInSelf)
                int id=task.getId(); // ��ǰջ�� taskmodel �� id
                boolean isInSelf=task.isInSelf();
                WaitTask wt=(WaitTask)getList().get(0);
                for(int i=0;i<list.size();i++){//�������ı�ǩ,
                    curactor.sendPathResult(new ActorTask(id,wt.getPathR(),isInSelf));
                }
            }
            //pop(T1-1)
            curactor.popFunction();   // T1-1��ջ
            if(ss.isEmpty()) {   // ����֮��ǰactor ���ڵ�stack Ϊ���ˣ���ɾ����ǰ actor
                actorManager.detachActor(curactor);
            }else{                      // T1-1 ��Ϊ T1-5 �ĺ��� path
                State state =(State)((ActorTask)(ss.peek())).getObject();
                if(state instanceof StateT1_5){
                    state.endElementDo(tag,layer,curactor);
                }
            }
        }
    }



}

