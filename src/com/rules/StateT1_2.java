package com.rules;

import com.XPath.PathParser.ASTPath;
import com.ibm.actor.DefaultMessage;
import com.taskmodel.ActorTask;
import com.taskmodel.WaitTask;

import java.util.Stack;

/**
 * Created by qin on 2015/10/10.
 */
public class StateT1_2 extends StateT1 {
    protected State _q3;//��� preds

    protected StateT1_2(ASTPath path, State q3) {
        super(path);
        _q3 = q3;
    }

    public static State TranslateState(ASTPath path) {//���´���T1-2
        State q3 = StateT3.TranslateStateT3(path.getFirstStep().getPreds());
        return new StateT1_2(path, q3);
    }

    public void startElementDo(String tag, int layer,MyStateActor curactor) throws CloneNotSupportedException {// layer ��ʾ��ǰ��ǩ tag �Ĳ���
        if((getLevel() == layer) && (tag.equals(_test))) {//Ӧ��ƥ��Ĳ��� getLayer������ ��ǰ��ǩ tag �Ĳ������
            addWTask(new WaitTask(layer, null, tag));
            _q3.setLevel(layer + 1);//����ν�ʵĲ����϶��ǵ�ǰӦ��ƥ���������Ӧ�ı�ǩ������Ĳ���
            curactor.pushTaskDo(new ActorTask(layer, _q3, true));
        }
    }

    public void endElementDo(String tag,int layer,MyStateActor curactor){
        if (tag.equals(_test)) {// �����Լ��Ľ�����ǩ�����
            //T1-6.pathʱ��ν��δ���ɹ��ʹ�����ȥ��T1-2.list.size>=1;
            for(int i=0;i<getList().size();i++){
                WaitTask wtask=(WaitTask) getList().get(i);
                if(wtask.hasReturned()){
                    curactor.doNext(wtask);
                }else{//�ȴ�--ν���ǵ�ջ�ˣ���ν�ʼ�����Ϣ�Ѿ�����ȥ�ˣ����ǻ���û���յ���������յ��˻�û�������
                    //��ǰ������ǩ�Ȳ�����
                    curactor.addMessage(new DefaultMessage("endE", new ActorTask(layer,tag)));
                    curactor.peekNext("predR");//���ȴ���ν�ʷ��ؽ������Ϣ
                    while(wtask.hasReturned())
                        curactor.doNext(wtask);
                }
            }
        }else if (layer == getLevel() - 1) { // �����ϲ������ǩ
            // (�������ϲ������ǩ����T1-2��Ϊһ��������path��T1-5 ��ʱ��Ҳ�����stackActor�У���T1-6~T1-8�ᱻ����paActor��)
            // T1-5 ʱ����T1-5 ����ͬһ��ջ��T1-6~T1-8 ����pathstack��
            Stack ss=curactor.getMyStack();
            if(!getList().isEmpty()){   //T1-2��ΪT1-6�ĺ���path && T1-6��ν�ʺ������(��ջ)
                //���ʱ��������T1-6�Ľ�����ǩ���ȴ�����pop��
                ActorTask task=(ActorTask)ss.peek();//(id,T1-1,isInSelf)
                int id=task.getId(); // ��ǰջ�� taskmodel �� id
                boolean isInSelf=task.isInSelf();
                WaitTask wt=(WaitTask)getList().get(0);
                for(int i=0;i<list.size();i++){//�������ı�ǩ,
                    curactor.sendPathResult(new ActorTask(id,wt.getPathR(),isInSelf));
                }
            }
            //pop(T1-2)
            curactor.popFunction();   // T1-2��ջ
            if(ss.isEmpty()) {   // ����֮��ǰactor ���ڵ�stack Ϊ���ˣ���ɾ����ǰ actor
                actorManager.detachActor(curactor);
            }else{                      // T1-2 ��Ϊ T1-5 �ĺ��� path
                State state =(State)((ActorTask)(ss.peek())).getObject();
                if(state instanceof StateT1_5){
                    state.endElementDo(tag,layer,curactor);
                }
            }
        }
    }
}
