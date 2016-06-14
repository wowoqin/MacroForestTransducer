package com.rules;

import com.XPath.PathParser.ASTPath;
import com.taskmodel.ActorTask;
import com.taskmodel.WaitTask;

import java.util.Stack;

/**
 * Created by qin on 2015/10/10.
 */
public class StateT1_3 extends StateT1 implements Cloneable {

    protected StateT1_3(ASTPath path) {
        super(path);
    }

    public static State TranslateState(ASTPath path) {//���´���T1-3
        return new StateT1_3(path);
    }

    public void startElementDo(String tag, int layer, MyStateActor curactor) throws CloneNotSupportedException {
        if ((layer >= getLevel()) && (tag.equals(_test))) {//��ǰ�������ڵ���Ӧ��ƥ��Ĳ��� getLayer�����Ϳ���
            addWTask(new WaitTask(layer,true,tag));
        }
    }


    @Override
    public void endElementDo(String tag, int layer, MyStateActor curactor) {
        // T1-3 ����Ҫ�ȴ� && T1-3.list.size>=1
        if(tag.equals(_test)){//�����Լ��Ľ�����ǩ������Լ���list�е����һ�� wt -->���/�ϴ�/remove
            //�����ϴ�list�е����һ�� wt���ɣ���
            //T1-6.pathʱ��ν��δ���ɹ��ʹ�����ȥ��T1-3.list.size>=1;
            for(int i=(getList().size()-1);i>=0;i--){
                WaitTask wtask=(WaitTask) getList().get(i);
                if(wtask.getId()>=layer){//ֻ�ϴ�/�����ǰlayer����layer�µĸ��͵ı�ǩ
                    curactor.doNext(wtask);
                }else return;
            }
        }else if (layer == getLevel() - 1) { // �����ϲ������ǩ
            // (�������ϲ������ǩ����T1-3��Ϊһ��������path��T1-5 ��ʱ��Ҳ�����stackActor�У���T1-6~T1-8�ᱻ����paActor��)
            // T1-5 ʱ����T1-5 ����ͬһ��ջ��T1-6~T1-8 ����pathstack
            Stack ss=curactor.getMyStack();
            if(!getList().isEmpty()){   //T1-3��ΪT1-6�ĺ���path && T1-6��ν�ʺ������(��ջ)
                //���ʱ��������T1-6�Ľ�����ǩ���ȴ�����pop��
                ActorTask task=(ActorTask)ss.peek();//(id,T1-3,isInSelf)
                int id=task.getId(); // ��ǰջ�� taskmodel �� id
                boolean isInSelf=task.isInSelf();
                WaitTask wt=(WaitTask)getList().get(0);
                for(int i=0;i<list.size();i++){//�������ı�ǩ,
                    curactor.sendPathResult(new ActorTask(id,wt.getPathR(),isInSelf));
                }
            }
            //pop(T1-3)
            curactor.popFunction();   // T1-3��ջ
            if(ss.isEmpty()) {   // ����֮��ǰactor ���ڵ�stack Ϊ���ˣ���ɾ����ǰ actor
                actorManager.detachActor(curactor);
            }else{                      // T1-3 ��Ϊ T1-5 �ĺ��� path
                State state =(State)((ActorTask)(ss.peek())).getObject();
                if(state instanceof StateT1_5){
                    state.endElementDo(tag,layer,curactor);
                }
            }
        }
    }
}