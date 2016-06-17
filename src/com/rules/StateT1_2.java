package com.rules;

import com.XPath.PathParser.ASTPath;
import com.ibm.actor.DefaultMessage;
import com.taskmodel.ActorTask;
import com.taskmodel.WaitTask;

import java.util.List;
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
        //T1-2.predsѹ��T1-2 �����棬T1-2Ҫ�������Լ��Ľ�����ǩ����preds�Ѿ���ջ&&�����˼������
        // ������ν�ʼ������ʱ��Լ�����ȴ���ν�ʷ��ؽ���������ڴ����Լ��Ľ�����ǩʱ��ν���Ѿ��������
        if (tag.equals(_test)) {// �����Լ��Ľ�����ǩ�����
            //T1-6.pathʱ��ν��δ���ɹ��ʹ�����ȥ��T1-2.list.size>=1;
            List list=getList();
            if(!list.isEmpty()){
                WaitTask wtask=(WaitTask) list.get(0);
                if(wtask.hasReturned()){
                    System.out.println("T1-2�����Լ�������ǩ && ν�ʽ���Ѵ������");
                    curactor.doNext(wtask);
                }
                else{//�ȴ�--ν���ǵ�ջ�ˣ���ν�ʼ�����Ϣ�Ѿ�����ȥ�ˣ����ǻ���û���յ���������յ��˻�û�������
                //��ǰ������ǩ�Ȳ�����
                    if(curactor.getMessageCount()==1){
                        if(curactor.getMessages()[0].getSubject().equals("predResult"))
                            System.out.println("T1-2�����Լ�������ǩ && ν�ʽ�����ػ�δ����");
                    }
                    System.out.println("T1-2.messages.add(T1-2�Ľ�����ǩ)-->��predResult��������ٴ���");
                    curactor.addMessage(new DefaultMessage("endE", new ActorTask(layer, tag)));
                }
            }
        }else if (layer == getLevel() - 1) { // �����ϲ������ǩ-->��Ϊ����path
            // (�������ϲ������ǩ����T1-2��Ϊһ��������path��T1-5 ��ʱ��Ҳ�����stackActor�У���T1-6~T1-8�ᱻ����paActor��)
            // T1-5 ʱ����T1-5 ����ͬһ��ջ��T1-6~T1-8 ����pathstack��
            Stack ss=curactor.getMyStack();
            ActorTask task=(ActorTask)ss.peek();//(id,T1-1,isInSelf)
            List list=((StateT1)task.getObject()).getList();
            if(!list.isEmpty()){  //�ϴ�T1-2.test
                WaitTask wt=(WaitTask)list.get(0);
                for(int i=0;i<list.size();i++){//�������ı�ǩ,
                    //��ʱ������ζ���Ҫ����Ϣ����ȥ����ʹ�Ȳ�����(ν�����ȣ�path���ص���Ϣ����messages��)
                    curactor.sendPathResults(new ActorTask(task.getId(), wt.getPathR(), task.isInSelf()));
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
