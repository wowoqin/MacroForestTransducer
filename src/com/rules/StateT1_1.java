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
            addWTask(new WaitTask(layer, true, tag));
        }
    }

    public void endElementDo(String tag,int layer,MyStateActor curactor){
        // T1-1 ����Ҫ�ȴ�
        if(tag.equals(_test)){//�����Լ��Ľ�����ǩ������Լ���list�е� wt -->���/remove
            System.out.println("T1-1�����Լ�������ǩ-->doNext");
            List list=getList();
            curactor.doNext((WaitTask) list.get(0));
        }else if (layer == getLevel() - 1) { // �����ϲ������ǩ
            // (�������ϲ������ǩ����T1-1��Ϊһ��������path��T1-5 ��ʱ��Ҳ�����stackActor�У���T1-6~T1-8�ᱻ����paActor��)
            // T1-5 ʱ����T1-5 ����ͬһ��ջ��T1-6~T1-8 ����pathstack
            Stack ss=curactor.getMyStack();
            ActorTask task=(ActorTask)ss.peek();//(id,T1-1,isInSelf)
            List list=getList();
            if(!list.isEmpty()){  //�ϴ�T1-1.test
                System.out.println("T1-1�����ϲ������ǩ-->���ݽ��");
                WaitTask wt=(WaitTask)list.get(0);
                for(int i=0;i<list.size();i++){//Ҳ����ж������ı�ǩ, /a/b  ���� //a/b-->������a������b����
                    //���ϴ�
                    boolean isInself=task.isInSelf();
                    if(isInself){//T1-5�ĺ���path�������ȴ���path�ķ��ؽ����������T1-5 �Ľ�����ǩ
                        System.out.println("T1-1��Ϊ T1-5 �� path-->T1-5Ӧ���ȴ��� pathR");
                        curactor.peekNext("pathResult");
                    }
                    else{
                        State state =(State)((ActorTask)
                                (((MyStateActor)(curactor.getResActor())).getMyStack().peek())).getObject();
                        if (state instanceof StateT1_7){
                            System.out.println("T1-1��Ϊ T1-7 �� path--> T1-7Ӧ���ȴ��� pathR");
                            curactor.peekNext("pathResult");
                        }
                    }
                    curactor.sendPathResults(new ActorTask(task.getId(), wt.getPathR(), isInself));
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

