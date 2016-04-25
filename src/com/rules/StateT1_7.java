package com.rules;

import com.XPath.PathParser.ASTPath;
import com.ibm.actor.Actor;
import com.ibm.actor.DefaultMessage;

import java.util.*;

/**
 * Created by qin on 2015/10/10.
 */
public class StateT1_7 extends StateT1 implements Cloneable{
    protected State _q1;//������ path

    protected StateT1_7(ASTPath path, State q1) {
        super(path);
        _q1 = q1;
        this._pathstack=new Stack();
    }

    public static State TranslateState(ASTPath path) {//���´���T1-7
        State q1 = StateT1.TranslateStateT1(path.getRemainderPath());
        return new StateT1_7(path, q1);
    }

    public void startElementDo(String tag, int layer,MyStateActor curactor) throws CloneNotSupportedException{
        if ((layer >= getLevel()) && (tag.equals(_test))) {
            WaitTask wtask;
            ActorTask atask;
            String name=((Integer)this._pathstack.hashCode()).toString().concat("T1-7.paActor");
            Actor actor=(actors.get(name));// path�� actor
            // �� tlist �������Ҫ�ȴ�ƥ�������ģ��
            wtask=new WaitTask(layer,true,null);
            curactor.addWTask(wtask);

            if(actor == null){  // ��pathActor ��û�д��� --> _pathstack һ��Ϊ��
                actor =actorManager.createAndStartActor(MyStateActor.class, name);
                actors.put(actor.getName(), actor);

                atask=new ActorTask(this._pathstack);
                dmessage=new DefaultMessage("stack", atask);
                actorManager.send(dmessage, curactor, actor);
                //���� q'' �� paActor
                _q1.setLevel(layer + 1);
                atask=new ActorTask(layer,_q1);
                dmessage=new DefaultMessage("pushTask", atask);
                actorManager.send(dmessage,curactor,actor);
            } else{  // ��path  actor �Ѿ�������,���� q'' �� paActor����
                State currQ=(State)_q1.copy();
                currQ.setLevel(layer + 1);
                atask=new ActorTask(layer,_q1);
                dmessage=new DefaultMessage("pushTask",atask);
                actorManager.send(dmessage, curactor, actor);

            }
            // ��Ҫ����һ��path actor������Ϣ��subject
            //actor.willReceive("startE");
            //actor.peekNext("startE",isX1(getLevel()+1));// path actor���ܵı�ǩӦ���ǵ�ǰ��ǩ�� child
        }
    }

    public void endElementDo(String tag,int layer,MyStateActor curactor){

        WaitTask wtask;
        ActorTask atask;
        Stack currstack;


        if (tag.equals(_test)) {  // �����Լ��Ľ�����ǩ�����
            int id=((ActorTask)(curactor.getMyStack().peek())).getId(); // ��ǰջ�� task �� id
            for (int i = 0; i < curactor.tlist.size(); i++) {
                wtask = (WaitTask) curactor.tlist.get(i);
                if (wtask.getId()==layer){
                    if(wtask.isSatisfied()) { // �ڵ�ǰcurrActor �� tlist �������������� wt
                        if (curactor.getResActor() != null) { // ��ǰ actor ���ϼ� actor ��Ϊ�գ������Լ����actor��
                            atask = new ActorTask(id, wtask.getPathR());
                            dmessage = new DefaultMessage("pathResult", atask);
                            actorManager.send(dmessage, curactor, curactor.getResActor());
                        }else { // ��ǰ actor �� resActor Ϊ��--> ��ǰ actor�� stackActor��������
                            curactor.output(wtask);
                            // �� T1-7.pathStack �е�ջ֮���� AD ��ĺ��� path �ļ�飬
                            // ����Ҫ�ѵ�ǰ����� wt.paResult���Ƶ�֮ǰ�ĵȴ��� wt.paResult
                            if(curactor.tlist.size()>1){
                                Actor actor=actors.get("T1-7.paActor");
                                currstack=((MyStateActor)actor).getMyStack(); //T1-7.pathStack
                                if(!currstack.isEmpty()){
                                    atask = ((ActorTask)(currstack.peek()));  // ��ǰջ�� ��task
                                    State state =(State) atask.getObject();
                                    if(state instanceof StateT1_3|| state instanceof StateT1_4
                                            ||state instanceof StateT1_7|| state instanceof StateT1_8)
                                        for(int j=0;j<curactor.tlist.size()-2;j++)
                                            ((WaitTask) curactor.tlist.get(j)).setPathR(wtask.getPathR());
                                }
                            }

                        }
                    }
                    curactor.removeWTask(wtask);
                }
            }
        }else if (layer == getLevel() - 1) { // �����ϲ������ǩ
        // (�������ϲ������ǩ����T1-7��Ϊһ��������path��T1-5 ��ʱ��Ҳ�����stackActor�У���T1-6~T1-8�ᱻ����paActor��)
            // T1-5 ʱ����T1-5 ����ͬһ��ջ��T1-6~T1-8 ����pathstack��
            currstack = curactor.getMyStack();

            curactor.popFunction();   // ��ջ
            if(currstack.isEmpty())   // ����֮��ǰactor ���ڵ�stack Ϊ���ˣ���ɾ����ǰactor �� paActor
                actorManager.detachActor(actors.get("T1-7.paActor")); // remove( T1-7.paActor )
            if(!currstack.isEmpty()){ // T1-7 ��Ϊ T1-5 �ĺ��� path
                atask = ((ActorTask)(currstack.peek()));  // ��ǰջ�� ��task
                State state =(State) atask.getObject();
                if(state instanceof StateT1_5)
                actorManager.detachActor(actors.get("T1-7.paActor")); // remove( T1-7.paActor )
            }


        }
    }
}
