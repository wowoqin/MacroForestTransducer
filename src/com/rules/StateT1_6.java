package com.rules;

import com.XPath.PathParser.ASTPath;
import com.ibm.actor.Actor;
import com.ibm.actor.DefaultMessage;
import com.taskmodel.ActorTask;
import com.taskmodel.WaitTask;

import java.util.Stack;

/**
 * Created by qin on 2015/10/10.
 */
public class StateT1_6 extends StateT1{
    protected  State  _q3;//��� preds
    protected  State  _q1;//������ path
    protected StateT1_6(ASTPath path,State q3,State q1){
        super(path);
        _q3=q3;
        _q1=q1;
        _pathstack =new Stack();
    }

    public static State TranslateState(ASTPath path){//���´���T1-6
        State q3=StateT3.TranslateStateT3(path.getFirstStep().getPreds());
        State q1=StateT1.TranslateStateT1(path.getRemainderPath());
        return new StateT1_6(path,q3,q1);//Ȼ��ѹ��ջ
    }
    @Override
    public void startElementDo(String tag,int layer,MyStateActor curactor) throws CloneNotSupportedException {// layer �ǵ�ǰ tag �Ĳ���
        if((getLevel() == layer)  && (tag.equals(_test))){//Ӧ��ƥ��Ĳ��� getLevel������ ��ǰ�������
            // �� tlist �������Ҫ�ȴ�ƥ�������ģ��
            addWTask(new WaitTask(layer, null, null));
            _q3.setLevel(layer + 1);
            curactor.pushTaskDo(new ActorTask(layer, _q3, true));

            String name=((Integer)this._pathstack.hashCode()).toString().concat("T1-6.paActor");
            Actor actor=(actors.get(name));// path�� actor

            if(actor == null){  // ��pathActor ��û�д��� --> _pathstack һ��Ϊ��
                stacklist.add(this._pathstack);
                actor =actorManager.createAndStartActor(MyStateActor.class, name);
                actors.put(actor.getName(), actor);

                actorManager.send(new DefaultMessage("resActor", null), curactor, actor);

                //���� q'' �� paActor
                _q1.setLevel(layer + 1);
                actorManager.send(new DefaultMessage("pushTask", new ActorTask(layer,_q1,false)),curactor,actor);
            } else{  // ��path  actor �Ѿ�������,���� q'' �� paActor����
                //���� q'' �� paActor
                State currQ=(State)_q1.copy();
                currQ.setLevel(layer+1);
                actorManager.send(new DefaultMessage("pushTask", new ActorTask(layer,currQ,false)), curactor, actor);
            }
        }
    }

    public void endElementDo(String tag,int layer,MyStateActor curactor){
        //T1-6 �����Լ��Ľ�����ǩ����֤��T1-6.q3 �Ѿ�������ˣ�ֻ���� T1-6.q1 �Ƿ���Ҫ�ȴ�
        if (tag.equals(_test)) {// �����Լ��Ľ�����ǩ�����
            for(int i=0;i<getList().size();i++){
                WaitTask wtask=(WaitTask) getList().get(i);
                if(wtask.hasReturned()){
                    curactor.doNext(wtask);
                }else{//�ȴ�
                    actorManager.awaitMessage(curactor);
                    while(wtask.hasReturned())
                        curactor.doNext(wtask);
                }
            }
        }else if (layer == getLevel() - 1) { // �����ϲ������ǩ(�϶�����Ϊ����path)
            // (�������ϲ������ǩ����T1-6��Ϊһ��������path��T1-5 ��ʱ��Ҳ�����stackActor�У���T1-6~T1-8�ᱻ����paActor��)
            // T1-5 �ĺ�����pathʱ����T1-5 ����ͬһ��ջ��T1-6~T1-8 ����pathstack��
            curactor.popFunction();   // T1-6��ջ
            Stack ss=curactor.getMyStack();
            if(ss.isEmpty()) {   // ����֮��ǰactor ���ڵ�stack Ϊ���ˣ���ɾ����ǰ actor
                actorManager.detachActor(curactor);
            }else{                      // T1-6 ��Ϊ T1-5 �ĺ��� path
                State state =(State)((ActorTask)(ss.peek())).getObject();
                if(state instanceof StateT1_5){
                    state.endElementDo(tag,layer,curactor);
                }
            }
        }
    }
}
