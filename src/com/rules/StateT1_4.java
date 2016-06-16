package com.rules;

import com.XPath.PathParser.ASTPath;
import com.ibm.actor.Actor;
import com.ibm.actor.DefaultMessage;
import com.taskmodel.ActorTask;
import com.taskmodel.WaitTask;

import java.util.List;
import java.util.Stack;

/**
 * Created by qin on 2015/10/10.
 */
public class StateT1_4 extends StateT1 implements Cloneable {
    protected State _q3;//��� preds

    protected StateT1_4(ASTPath path, State q3) {
        super(path);
        _q3 = q3;
        this._predstack = new Stack();
    }

    public static State TranslateState(ASTPath path) {//����T1-4
        State q3 = StateT3.TranslateStateT3(path.getFirstStep().getPreds());
        return new StateT1_4(path, q3);
    }

    public void startElementDo(String tag,int layer,MyStateActor curactor) throws CloneNotSupportedException{
        if ((layer >= getLevel()) && (tag.equals(_test))) {
            // �� list �������Ҫ�ȴ�ƥ�������ģ��
            addWTask(new WaitTask(layer, null, tag));

            String name=((Integer)this._predstack.hashCode()).toString().concat("T1-4.prActor");
            Actor actor=(actors.get(name));// preds �� actor
            if(actor == null){// ��ν�� actor ��û�д��� --> _predstack һ��Ϊ��
                stacklist.add(this._predstack);
                actor =actorManager.createAndStartActor(MyStateActor.class, name);
                actors.put(name, actor);

                actorManager.send(new DefaultMessage("resActor", null), curactor, actor);
                //���� q' �� prActor
                _q3.setLevel(layer + 1);
                actorManager.send(new DefaultMessage("pushTask", new ActorTask(layer,_q3,false)),curactor,actor);
            }
            else{  // ��ν�� actor �Ѿ�������,���� q' �� prActor����
                State currQ=(State)_q3.copy();
                currQ.setLevel(layer + 1);
                actorManager.send(new DefaultMessage("pushTask",new ActorTask(layer,currQ,false)), curactor, actor);
            }
        }
    }

    @Override
    public void endElementDo(String tag,int layer,MyStateActor curactor) {
        if (tag.equals(_test)) {//�����Լ��Ľ�����ǩ������Լ���list�е����һ�� wt -->���/remove
            //T1-6.pathʱ��ν��δ���ɹ��ʹ�����ȥ��T1-4.list.size>=1;
//            for(int i=(getList().size()-1);i>=0;i--) {
//                WaitTask wtask = (WaitTask) getList().get(i);
//                if (wtask.getId() >= layer) { //ֻ�ϴ�/�����ǰlayer����layer�µķ��ϵı�ǩ
//                    if(wtask.hasReturned()){
//                        curactor.doNext(wtask);
//                    }else{
//                        //����ǰ������ǩ
//                        curactor.addMessage(new DefaultMessage("endE", new ActorTask(layer,tag)));
//                        //-->//a[][][],�ڵȴ�ν�ʷ�����Ϣ��ʱ��Ҳ��������test��
//                        // ��Ҫ���������ı�ǩ��������Ϊ�ý�����ǩ��һ��Ҫ������д���ģ���Ҫ���ȴ���ν�ʷ��ؽ��
//                        actorManager.awaitMessage(curactor);
//                        curactor.peekNext("predR");//���ȴ���ν�ʷ��ؽ������Ϣ
//                        while(wtask.hasReturned())
//                            curactor.doNext(wtask);
//                    }
//                }else return;
//            }

            List list=getList();
            WaitTask wtask = (WaitTask) getList().get(list.size()-1);
            if(wtask.hasReturned()){
                curactor.doNext(wtask);
            }else{
                //����ǰ������ǩ
                curactor.addMessage(new DefaultMessage("endE", new ActorTask(layer,tag)));
                //-->//a[][][],�ڵȴ�ν�ʷ�����Ϣ��ʱ��Ҳ��������test��
                // ��Ҫ���������ı�ǩ��������Ϊ�ý�����ǩ��һ��Ҫ������д���ģ���Ҫ���ȴ���ν�ʷ��ؽ��
               // actorManager.awaitMessage(curactor);
                curactor.peekNext(null);//���ȴ���ν�ʷ��ؽ������Ϣ
                while(wtask.hasReturned())
                    curactor.doNext(wtask);
            }
        }else if (layer == getLevel() - 1) { // �����ϲ������ǩ
            // (�������ϲ������ǩ����T1-2��Ϊһ��������path��T1-5 ��ʱ��Ҳ�����stackActor�У���T1-6~T1-8�ᱻ����paActor��)
            // T1-5 ʱ����T1-5 ����ͬһ��ջ��T1-6~T1-8 ����pathstack��
            Stack ss=curactor.getMyStack();
            List list=getList();
            ActorTask task=(ActorTask)ss.peek();//(id,T1-3,isInSelf)
            int id=task.getId(); // ��ǰջ�� taskmodel �� id
            boolean isInSelf=task.isInSelf();
            if(!list.isEmpty()){   //�ϴ�
                WaitTask wt=(WaitTask)getList().get(0);
                for(int i=0;i<list.size();i++){//�������ı�ǩ,
                    curactor.sendPathResult(new ActorTask(id,wt.getPathR(),isInSelf));
                }
            }
            //pop(T1-4)
            curactor.popFunction();   // T1-4��ջ
            if(ss.isEmpty()) {   // ����֮��ǰactor ���ڵ�stack Ϊ���ˣ���ɾ����ǰ actor
                actorManager.detachActor(curactor);
            }else{                      // T1-4 ��Ϊ T1-5 �ĺ��� path
                task=(ActorTask)(ss.peek());
                State currstate =(State)task.getObject();
                if(currstate instanceof StateT1_5){
                    currstate.endElementDo(tag,layer,curactor);
                }else if(currstate instanceof StateT1_3){
                    //T1-4��ΪAD��test�ĺ���path����T1-7/T1-8
                    curactor.processSameADPath(currstate,list);
                }
            }
        }
    }
}


