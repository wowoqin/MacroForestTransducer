package com.rules;

import com.XPath.PathParser.ASTPath;
import com.ibm.actor.DefaultMessage;
import com.taskmodel.ActorTask;
import com.taskmodel.WaitTask;

import java.util.Stack;

/**
 * Created by qin on 2015/10/10.
 */
public class StateT1_5 extends StateT1{
    protected  State  _q1;//��� ���� path

    protected StateT1_5(ASTPath path,State q1){
        super(path);
        _q1=q1;
    }

    public static State TranslateState(ASTPath path){//���´���T1-5
        State q1=StateT1.TranslateStateT1(path.getRemainderPath());
        return new StateT1_5(path,q1);
    }

    public void startElementDo(String tag,int layer,MyStateActor curactor) throws CloneNotSupportedException {
        if((getLevel() == layer) && (tag.equals(_test))) {//Ӧ��ƥ��Ĳ��� getLevel������ ��ǰ�������
            //System.out.println("T1-5.startElementDo�У���ǰactor��������" + actors.length);
            addWTask(new WaitTask(layer,true,null));
            _q1.setLevel(layer + 1); //q1 ������ path���϶��ǵ�ǰ��ǩ�������м��
            curactor.pushTaskDo(new ActorTask(layer,_q1,true));
        }
    }

    public void endElementDo(String tag,int layer,MyStateActor curactor){
        // T1-5�������Լ��Ľ�����ǩ����T1-5.q1 �Ѿ���ջ�ˣ���T1-5.q1 ��ջ��˵�� q1 �Ѿ��������

        if (tag.equals(_test)) { //�����Լ��Ľ�����ǩ������Լ���list�е�ÿ��wt -->���/�ϴ�/remove
            if(!getList().isEmpty()){
                for(int i=0;i<getList().size();i++){
                    WaitTask wtask=(WaitTask) getList().get(i);
                    if(wtask.hasReturned()){
                        System.out.println("T1-5�����Լ�������ǩ && path����Ѵ������");
                        curactor.doNext(wtask);
                    }else{
                        if(curactor.getMessageCount()==1){
                            if(curactor.getMessages()[0].getSubject().equals("pathResult"))
                                System.out.println("T1-5�����Լ�������ǩ && path��������˻�δ����");
                        }
                        System.out.println("T1-5.messages.add(T1-5�Ľ�����ǩ)-->��pathResult��������ٴ���");
                        //��ǰ������ǩ�Ȳ�����
                        curactor.addMessage(new DefaultMessage("endE", new ActorTask(layer, tag)));
                        //curactor.peekNext("pathResult");//���ȴ���path���ؽ������Ϣ
                    }
                }
            }
        }else if (layer == getLevel() - 1) { // �����ϲ������ǩ
            // (�������ϲ������ǩ����T1-5��Ϊһ��������path��T1-5 ��ʱ��Ҳ�����stackActor�У���T1-6~T1-8�ᱻ����paActor��)
            // T1-5 ʱ����T1-5 ����ͬһ��ջ��T1-6~T1-8 ����pathstack��
            curactor.popFunction();   // T1-5��ջ
            Stack ss=curactor.getMyStack();
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
