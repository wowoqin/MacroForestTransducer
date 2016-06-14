package com.rules;

import com.ibm.actor.AbstractActor;
import com.ibm.actor.Actor;
import com.ibm.actor.DefaultMessage;
import com.ibm.actor.Message;
import com.taskmodel.ActorTask;
import com.taskmodel.WaitTask;

import java.util.List;
import java.util.Stack;

/**
 * Created by qin on 2016/3/22.
 */
public class MyStateActor extends AbstractActor {

    protected Stack myStack;//ÿ��actor ��Ӧ����һ�� stack ��Ҳ����һ�� stack ��Ӧ��һ�� actor
    protected Actor resActor;//�ϼ� Actor


    public MyStateActor(){}

    public Stack getMyStack() {
        return myStack;
    }



    public void setMyStack(Stack myStack) {
        this.myStack = myStack;
    }

    @Override
    public void activate() {
        super.activate();
    }

    public void setResActor(Actor resActor) {
        this.resActor = resActor;
    }

    public Actor getResActor() {
        return resActor;
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public boolean willReceive(String subject) {
        return super.willReceive(subject);
    }

    @Override
    public int getMaxMessageCount() {
        return super.getMaxMessageCount();
    }

    @Override
    protected void runBody() {
        List list=State.stacklist;
        this.setMyStack((Stack) list.get(list.size() - 1));
    }

    /* stateActor �ܹ����յ�����Ϣ��
        * stateActor ֮�䣺resActor:    �����ϼ� actor
        *                 pushTask��    q'/ q''
        *                 predResult:   True/false
        *                 pathResult:   һ������ / null
        *                 setCategory��  T3ν���� preds' ����Ӧ�� actor�����ͣ�String��
        *                 qName��        startE��endE
        *  */
    @Override
    protected void loopBody(Message message) {
        sleep(1);
        String subject = message.getSubject();
        Object data=message.getData();
        if("resActor".equals(subject)){ //  data = null
            this.setResActor(message.getSource());
        }else if("setCategory".equals(subject)){    // actorTask,���� data ��һ�� string
            this.setCategory((String) data);
        }else{
            ActorTask task=(ActorTask)data;// task
            // ��һ��actorTask
            if(task!=null){
                // object �� q��State����qName��String����q'�ķ��ؽ����True/False����q''�ķ��ؽ����String��
                Object object = task.getObject();
                Stack  ss = this.getMyStack();
                State  currQ;

               if("pushTask".equals(subject)){       // actorTask,���� data ��һ�� q3
                   try {
                       this.pushTaskDo(task);
                   } catch (CloneNotSupportedException e) {
                       e.printStackTrace();
                   }
               }else if("predResult".equals(subject)){     // actorTask,���� data ��һ��q'�ķ��ؽ����True��
                   // �յ�ν�ʷ��ؽ�����ڵ�ǰջ����state.list���ҵ���Ӧ��wt��ID��Ȼ��ν�ʼ��Ľ�����¸�ֵ
                   ActorTask atask = (ActorTask) ss.peek();//ջ��task
                   int id = atask.getId();
                   State state = (State) atask.getObject();//ջ�� state
                   List list=state.getList();

                   for(int i=list.size()-1;i>=0;i--){//Ҫ���ж���ȴ�ͬһν�ʼ������wt(T1-6��T1-8)����Ҫ��ÿһ���Ľ����������
                       WaitTask wt = (WaitTask) (list.get(i));
                       if(message.getSource()==this){   //��Ϣ�����Լ�
                           wt.setPredR((Boolean)(task.getObject()));
                       }else {//��Ϣ�����¼� actor
                           if(message.getSource().getCategory().equals("T3PredsActor")){//T3��ν��ջ
                               wt.setPathR((String) (task.getObject()));
                           }else{  // T2
                               wt.setPredR((Boolean)(task.getObject()));
                           }
                       }
                       //�����������֮�󿴵�ǰ�������wt�ǲ���ν�������-->˵��֮ǰ���ص�ν�ʽ��ֻ����Ϊν�ʵ�ν��
                       if (wt.isPredsSatisified()){//ν�ʳɹ�
                           if(state instanceof StateT2_1){       //ԭ����T3-1
                               //popջ�������� true
                               this.popFunction();
                               this.sendPredsResult(new ActorTask(id,true,atask.isInSelf()));
                           }else if(state instanceof StateT2_2){
                               if(list.size()==1){ //T2-2
                                   //popջ�������� true
                                   this.popFunction();
                                   this.sendPredsResult(new ActorTask(id,true,atask.isInSelf()));
                               }else{//ԭ����T3-2,����ֻ��T2-2���ɹ�
                                   list.remove(wt);
                                   if(i==0){//T3-2���ɹ�(q''֮ǰ�ͼ��ɹ���)
                                       this.sendPredsResult(new ActorTask(id,true,atask.isInSelf()));
                                   }else{//i==1��T2-2���ɹ�(����֮ǰq''�Ƿ���ɹ������ȸ��Լ�setPredR=true)
                                       WaitState qw=new WaitState();
                                       qw.setLevel(state.getLevel());
                                       qw.getList().add(state.getList().get(0));
                                       this.popFunction();
                                       try {
                                           this.pushTaskDo(new ActorTask(id, qw, atask.isInSelf()));
                                       } catch (CloneNotSupportedException e) {
                                           e.printStackTrace();
                                       }
                                       this.sendPredsResult(new ActorTask(id,true,true));
                                   }
                               }
                           }else if(state instanceof StateT2_3){ //ԭ����T3-3 && q''�ȼ��ɹ�--(id,null,"true")
                               //popջ�������� true
                               this.popFunction();
                               this.sendPredsResult(new ActorTask(id, true, atask.isInSelf()));
                               atask = (ActorTask) ss.peek();//ջ��task
                               state = (State) atask.getObject();//ջ�� state
                               if(!ss.isEmpty()){
                                   if((state instanceof StateT2_3) && (!atask.isInSelf())){// T2-3 ��Ϊ AD ��test��ν��
                                       this.processSameADPred();
                                   }
                               }
                           }else if(state instanceof StateT2_4){
                               WaitTask wts=(WaitTask)list.get(0);
                               if(wts.isWaitT3ParallPreds()){ //(id,true,null)--ԭ����T3-4 && q''��δ���ɹ�
                                   //T2-4 ��Ϊ qw
                                   WaitState wq=new WaitState();
                                   wq.setLevel(state.getLevel());
                                   wq.getList().add(wts);
                                   this.popFunction();//pop ��ʱ��˳��Ҳ clear �� list
                                   try {
                                       this.pushTaskDo(new ActorTask(id,wq,atask.isInSelf()));
                                   } catch (CloneNotSupportedException e) {
                                       e.printStackTrace();
                                   }
                               }else{//(id,null,"true")---��ԭ����T2-4��
                                                        // ���� ��ԭ����T3-4 && q''�ȼ��ɹ�-->��������T3-4�����ɹ���
                                   //popջ�������� true
                                   this.popFunction();
                                   this.sendPredsResult(new ActorTask(id,true,atask.isInSelf()));
                                   if(!ss.isEmpty()){
                                       atask = (ActorTask) ss.peek();//ջ��task
                                       state = (State) atask.getObject();//ջ�� state
                                       if((state instanceof StateT2_4) && (!atask.isInSelf())){// T2-4 ��Ϊ AD ��test��ν��
                                           this.processSameADPred();
                                       }
                                   }
                               }
                           }else if(state instanceof WaitState){//q'''�Ѿ��ȼ��ɹ�������q''Ҳ���ɹ���
                               //popջ�������� true
                               this.popFunction();
                               this.sendPredsResult(new ActorTask(id,true,atask.isInSelf()));
                               if(!ss.isEmpty()){
                                   atask = (ActorTask) ss.peek();//ջ��task
                                   state = (State) atask.getObject();//ջ�� state
                                   if(((state instanceof StateT2_3) || (state instanceof StateT2_4)) && (!atask.isInSelf())){// T2-3/T2-4 ��Ϊ AD ��test��ν��
                                       this.processSameADPred();
                                   }
                               }
                           }
                       }else if(wt.isWaitT3ParallPreds()){ // (id,true,null)
                           //q'''���ɹ���q''��û���ɹ�
                           WaitState wq=new WaitState();
                           wq.setLevel(state.getLevel());
                           wq.getList().add(wt);
                           this.popFunction();//pop ��ʱ��˳��Ҳ clear �� list
                           try {
                               this.pushTaskDo(new ActorTask(id,wq,task.isInSelf()));
                           } catch (CloneNotSupportedException e) {
                               e.printStackTrace();
                           }
                       }
                   }
               }else if("pathResult".equals(subject)){ // actorTask,���� data ��һ��q''�ķ��ؽ����String��
                    // �� waitTask ���ҵ���Ӧ��ID��Ȼ�󽫺��� path ���Ľ�� ���¸�ֵ
                   ActorTask atask = (ActorTask) ss.peek();//ջ��task
                   State state = (State) atask.getObject();//ջ�� state
                   //��ջ��q.list�е� wt ��ֵ
                   List list=state.getList();
                   WaitTask wt = (WaitTask) (list.get(list.size()-1));
                   if(wt.getPathR()!=null){// �Ѿ��к���path���ɹ�����ͬ�㼶�Ľ��
                       list.add(wt);
                   }else{
                       wt.setPathR((String) (task.getObject()));
                   }
               }else{                              // actorTask,���� data ��һ��qName��String��
                    if (!ss.isEmpty()) {
                        // �ҵ���ǰ actor �ĵ�ǰջ���ĵ�ǰ state
                        currQ = (State) (((ActorTask) (ss.peek())).getObject());
                        String tag = (String)object;
                        int layer = task.getId();
                        if("startE".equals(subject)){
                            try {
                                currQ.startElementDo(tag, layer, this);
                            } catch (CloneNotSupportedException e) {
                                e.printStackTrace();
                            }
                        }else if("endE".equals(subject)){
                            currQ.endElementDo(tag, layer, this);
                        }
                    }
                }
            }
        }
        //ɾ�����͵���Ϣ-->ÿ��Actor���յ������Ϣ����=100
        //this.remove(message);
    }

    public void pushTaskDo(ActorTask actorTask) throws CloneNotSupportedException {
        Stack curstack=this.getMyStack();
        State state=(State)(actorTask.getObject());     // Ҫѹջ�� state

        if(state instanceof StateT1||state instanceof StateT2|| state instanceof WaitState){
            curstack.push(actorTask);
        }else {
            int id=actorTask.getId();
            boolean isInSelf=actorTask.isInSelf();
            int level=((State)(actorTask.getObject())).getLevel();// T3 Ҫƥ��Ĳ���

            if(state instanceof StateT3_1){
                State firstPred=((StateT3_1) state)._q2; // T2-1
                State remainPred=((StateT3_1) state)._q3;// q''
                firstPred.setLevel(level);
                remainPred.setLevel(level);
                //push(q''')
                curstack.push(new ActorTask(id, firstPred, isInSelf));
                //�� T3-1.q'''.list �����Ҫ�ȴ��� wt
                firstPred.getList().add(new WaitTask(id, null, null));

                String name=((Integer)(((StateT3_1) state)._predstack).hashCode()).toString().concat("T3-1.prActor");
                Actor actor=State.actors.get(name);
                //push(q'')-->�������ô˺����ж�ѹջ
                if(actor==null){
                    State.stacklist.add(((StateT3_1) state)._predstack);
                    actor=manager.createAndStartActor(MyStateActor.class, name);
                    State.actors.put(actor.getName(), actor);

                    getManager().send(new DefaultMessage("resActor", null), this, actor);

                    getManager().send(new DefaultMessage("setCategory", "T3PredsActor"), this, actor);

                    getManager().send(new DefaultMessage("pushTask",new ActorTask(id,remainPred,false)),this, actor);
                }else{
                    State currQ=(State)remainPred.copy();
                    currQ.setLevel(level);
                    getManager().send(new DefaultMessage("pushTask",new ActorTask(id,currQ,false)),this,actor);
                }
            }else if(state instanceof StateT3_2){
                State firPred=((StateT3_2) state)._q2;  //T2-2
                State remPred=((StateT3_2) state)._q3;  // q''
                firPred.setLevel(level);
                remPred.setLevel(level);
                curstack.push(new ActorTask(id, firPred, isInSelf));
                //�� T3-2.q'''.list �����Ҫ�ȴ��� wt
                firPred.getList().add(new WaitTask(id, null, null));
                String name=((Integer)(((StateT3_2) state)._predstack).hashCode()).toString().concat("T3-2.prActor");
                Actor actor=State.actors.get(name);
                if(actor==null){
                    State.stacklist.add(((StateT3_2) state)._predstack);
                    actor=manager.createAndStartActor(MyStateActor.class, name);
                    State.actors.put(actor.getName(), actor);

                    getManager().send(new DefaultMessage("resActor", null), this, actor);

                    getManager().send(new DefaultMessage("setCategory", "T3PredsActor"), this, actor);

                    getManager().send(new DefaultMessage("pushTask",new ActorTask(id,remPred,false)),this, actor);
                }else{
                    State currQ=(State)remPred.copy();
                    currQ.setLevel(level);
                    getManager().send(new DefaultMessage("pushTask",new ActorTask(id,currQ,false)),this,actor);
                }
            }else if(state instanceof StateT3_3){
                State firstPred=((StateT3_3) state)._q2;  //T2-3
                State remainPred=((StateT3_3) state)._q3;  // q''
                firstPred.setLevel(level);
                remainPred.setLevel(level);
                curstack.push(new ActorTask(id, firstPred, isInSelf));
                //�� T3-3.q'''.list �����Ҫ�ȴ��� wt
                firstPred.getList().add(new WaitTask(id, null, null));
                String name=((Integer)(((StateT3_3) state)._predstack).hashCode()).toString().concat("T3-3.prActor");
                Actor actor=State.actors.get(name);
                if(actor==null){
                    State.stacklist.add(((StateT3_3) state)._predstack);
                    actor=manager.createAndStartActor(MyStateActor.class, name);
                    State.actors.put(actor.getName(), actor);

                    getManager().send(new DefaultMessage("resActor", null), this, actor);

                    getManager().send(new DefaultMessage("setCategory", "T3PredsActor"), this, actor);

                    getManager().send(new DefaultMessage("pushTask",new ActorTask(id,remainPred,false)),this, actor);
                }else{
                    State currQ=(State)remainPred.copy();
                    currQ.setLevel(level);
                    getManager().send(new DefaultMessage("pushTask",new ActorTask(id,currQ,false)),this,actor);
                }
            }else if(state instanceof StateT3_4){
                State firstPred=((StateT3_4) state)._q2;  //T2-4
                State remainPred=((StateT3_4) state)._q3;  // q''
                firstPred.setLevel(level);
                remainPred.setLevel(level);
                curstack.push(new ActorTask(id, firstPred, isInSelf));
                //�� T3-4.q'''.list �����Ҫ�ȴ��� wt
                firstPred.getList().add(new WaitTask(id, null, null));
                String name=((Integer)(((StateT3_4) state)._predstack).hashCode()).toString().concat("T3-4.prActor");
                Actor actor=State.actors.get(name);
                if(actor==null){
                    State.stacklist.add(((StateT3_4) state)._predstack);
                    actor=manager.createAndStartActor(MyStateActor.class, name);
                    State.actors.put(actor.getName(), actor);

                    getManager().send(new DefaultMessage("resActor", null), this, actor);

                    getManager().send(new DefaultMessage("setCategory", "T3PredsActor"), this, actor);

                    getManager().send(new DefaultMessage("pushTask",new ActorTask(id,remainPred,false)),this, actor);
                }else{
                    State currQ=(State)remainPred.copy();
                    currQ.setLevel(level);
                    getManager().send(new DefaultMessage("pushTask",new ActorTask(id,currQ,false)),this,actor);
                }
            }
        }
    }

    public void popFunction(){
        Stack currStack = this.getMyStack();
        if(!currStack.isEmpty())
            currStack.pop();
    }

    public void sendPredsResult(ActorTask actorTask){// ν�ʼ��ɹ����ϴ������id��true������Ӧ�� wt
        if(actorTask.isInSelf()){
            getManager().send(new DefaultMessage("predResult",actorTask), this, this);
            this.peekNext("predResult");//���ȴ���ν�ʷ���
        }else{
            getManager().send(new DefaultMessage("predResult",actorTask), this, this.getResActor());
            this.getResActor().peekNext("predResult");//���ȴ���ν�ʷ���
        }
    }

    public boolean sendPathResult(ActorTask actorTask){// path���ɹ����ϴ������id��tag������Ӧ�� wt
        if(actorTask.isInSelf()){
            getManager().send(new DefaultMessage("pathResult",actorTask), this, this);
        }else{
            MyStateActor actor=(MyStateActor)this.getResActor();//�ϼ�actor
            State state = (State)((ActorTask)(actor.getMyStack()).peek()).getObject();//�ϼ�actor��ջ�� state
            if(state instanceof StateT1){
                getManager().send(new DefaultMessage("pathResult", actorTask), this, actor);
            }else return false;//ջ��Ҫ��ν��(T1-6.preds)�����ǩ�Ǵ�����ȥ��

        }
        //this.peekNext("pathResult");//���ȴ���path����
        return true;
    }

    public void doNext(WaitTask wtask){   //���/�ϴ�/remove/
        Stack currstack=this.getMyStack();
        ActorTask task=(ActorTask)currstack.peek();
        int id=task.getId(); // ��ǰջ�� taskmodel �� id
        boolean isInSelf=task.isInSelf();

        if (wtask.isSatisfiedOut()) {//��ǰ wt �����������
            if(this.getName().equals("stackActor") && (currstack.size()==1)){//��stack��
                this.output(wtask);
                ((State)task.getObject()).removeWTask(wtask);
            }else { //����stack�� && ��ΪT1-5�ĺ���path �� ����  ����Ϊ���� path ��һ������pathջ��
                ((State)task.getObject()).removeWTask(wtask);
                boolean isSent=this.sendPathResult(new ActorTask(id, wtask.getPathR(),isInSelf));
                //��ΪT1-6�ĺ���path�ķ��ؽ��������ǰʱ����preds��δ�����ɣ�����Ҫ�ȼĴ��ڵ�ǰstate.list�У�
                //ֱ������T1-6�Ľ�����ǩ,����ǰlist�����е������wt���ϴ�
                if(!isSent)
                    ((State)task.getObject()).addWTask(wtask);
            }
        }else{//���Լ��Ľ�����ǩ����ǰwt�������������
            ((State)task.getObject()).removeWTask(wtask);
        }
    }

    public void processSameADPred(){
        Stack currstack=this.getMyStack();

        while(!currstack.isEmpty()){
            ActorTask task=(ActorTask)currstack.peek();
            int id=task.getId(); // ��ǰջ�� taskmodel �� id
            boolean isInSelf=task.isInSelf();
            this.popFunction();
            sendPredsResult(new ActorTask(id,true,isInSelf));
        }
        //ջΪ��
        getManager().detachActor(this);
    }


    public void output(WaitTask wt){
        wt.output();
    }
}
