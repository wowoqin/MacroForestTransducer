package com.rules;

import com.XPath.PathParser.ASTPath;
import com.ibm.actor.Actor;
import com.ibm.actor.DefaultMessage;
import com.taskmodel.ActorTask;
import com.taskmodel.WaitTask;

import java.util.*;

/**
 * Created by qin on 2015/10/10.
 */
public class StateT1_7 extends StateT1 implements Cloneable{
    protected State _q1;//������ path

    protected StateT1_7(ASTPath path, State q1) {
        super(path);
        _q1 = q1;
        _q1.setLevel(this.getLevel()+1);
        this._pathstack=new Stack();
    }

    public static State TranslateState(ASTPath path) {//���´���T1-7
        State q1 = StateT1.TranslateStateT1(path.getRemainderPath());
        return new StateT1_7(path, q1);
    }

    public void startElementDo(String tag, int layer,MyStateActor curactor) throws CloneNotSupportedException{
        if ((layer >= getLevel()) && (tag.equals(_test))) {
            String name=((Integer)this._pathstack.hashCode()).toString().concat("T1-7.paActor");
            Actor actor=(actors.get(name));// path�� actor
            // �� tlist �������Ҫ�ȴ�ƥ�������ģ��
            curactor.addWTask(new WaitTask(layer,true,null));

            if(actor == null){  // ��pathActor ��û�д��� --> _pathstack һ��Ϊ��
                stacklist.add(this._pathstack);
                actor =actorManager.createAndStartActor(MyStateActor.class, name);
                actors.put(actor.getName(),actor);

                dmessage=new DefaultMessage("resActor", null);
                actorManager.send(dmessage, curactor, actor);
                //���� q'' �� paActor
                _q1.setLevel(layer + 1);
                dmessage=new DefaultMessage("push", new ActorTask(layer,_q1,false));
                actorManager.send(dmessage,curactor,actor);
            } else{  // ��path  actor �Ѿ�������,���� q'' �� paActor����
                State currQ=(State)_q1.copy();
                currQ.setLevel(layer + 1);
                dmessage=new DefaultMessage("push",new ActorTask(layer,currQ,false));
                actorManager.send(dmessage, curactor, actor);

            }
            // ��Ҫ����һ��path actor������Ϣ��subject
            //actor.willReceive("startE");
            //actor.peekNext("startE",isX1(getLevel()+1));// path actor���ܵı�ǩӦ���ǵ�ǰ��ǩ�� child
        }
    }

    public void endElementDo(String tag,int layer,MyStateActor curactor){
        Stack currstack = curactor.getMyStack();
        ActorTask task=(ActorTask)currstack.peek();
        boolean isInSelf=task.isInSelf();

        if (tag.equals(_test)) {  // �����Լ��Ľ�����ǩ�����
            int id=((ActorTask)currstack.peek()).getId(); // ��ǰջ�� taskmodel �� id
            String name=curactor.getName();
            List list=curactor.getTlist();

            for(int i=list.size()-1;i>=0;i--){
                WaitTask wtask = (WaitTask)list.get(i);
                if (wtask.getId()==layer) {
                    if (wtask.isSatisfiedOut()) {
                        if(name.equals("stackActor")){//��stack��-==>���/�����
                            if(currstack.size()==1){//���
                                curactor.output(wtask);
                                // �� T1-7.pathStack �е�ջ֮���� AD ��ĺ��� path �ļ�飬
                                // ����Ҫ�ѵ�ǰ����� wt.paResult���Ƶ�֮ǰ�ĵȴ��� wt.paResult
                                //ֱ����T1-7���ڵ�list�а�wt.pathR����Ϊwtask.value.���Ҳ��õ�ջ
                                if(list.size()>1){
                                    if (this._q1 instanceof StateT1_3 || this._q1 instanceof StateT1_4
                                            || this._q1 instanceof StateT1_7 || this._q1 instanceof StateT1_8){
                                        for(int j=i-1;j>=0;j--){
                                            WaitTask wt = (WaitTask)(list.get(j));
                                            if(wt.getPathR()!=null){
                                                list.add(j+1,wt);

                                            }else{
                                                wt.setPathR(wtask.getPathR());
                                            }
                                        }
                                    }
                                }
                                curactor.removeWTask(wtask);
                            }else {//��stack�� && ��ΪT1-5�ĺ���path
                                for(int j = i-1; j >=0; j--){
                                    if(((WaitTask) list.get(j)).getId()==id){
                                        dmessage=new DefaultMessage("pathResult",new ActorTask(id,wtask.getPathR()));
                                        actorManager.send(dmessage,curactor,curactor);
                                        curactor.removeWTask(wtask);
                                        return;//���������������������Сѭ��j���������˴�ѭ��i��
                                    }
                                }
                            }
                        }else {//��ΪAD �����path��һ����-->��paActor��
                            if(isInSelf){
                                dmessage=new DefaultMessage("pathResult",new ActorTask(id,wtask.getPathR()));
                                actorManager.send(dmessage, curactor, curactor);
                            }else{
                                dmessage=new DefaultMessage("paResult",new ActorTask(id,wtask.getPathR()));
                                actorManager.send(dmessage, curactor, curactor.getResActor());
                            }
                            curactor.removeWTask(wtask);
                            return;//���������������������Сѭ��j���������˴�ѭ��i��
                        }
                    }
                    //���Լ��Ľ�����ǩ����ǰwt�������������-->Ӧ�ü������ν������Ӧ��Actor�Ƿ������˹�����
                    // �������ˣ���ɾ���������wt��
                    // ����û���꣬��ǰactroӦ�õ�ν��actor�������жϣ�
                    curactor.removeWTask(wtask);

                }
            }
        }else if (layer == getLevel() - 1) { // �����ϲ������ǩ(T1-7��Ϊһ��������path)
            // (�������ϲ������ǩ����T1-7��Ϊһ��������path
            // ��T1-5 ��ʱ��Ҳ�����stackActor�У���T1-6~T1-8�ᱻ����paActor��)
            // T1-5 �ĺ�����pathʱ����T1-5 ����ͬһ��ջ��T1-6~T1-8 ����pathstack��
            curactor.popFunction();   // T1-7��ջ
            if(currstack.isEmpty()) {   // ����֮��ǰactor ���ڵ�stack Ϊ���ˣ���ɾ����ǰ actor
                actorManager.detachActor(curactor);
            }else{                      // T1-7 ��Ϊ T1-5 �ĺ��� path
                State state =(State)((ActorTask)(currstack.peek())).getObject();
                if(state instanceof StateT1_5){
                    state.endElementDo(tag,layer,curactor);
                }
            }
        }
    }
}
