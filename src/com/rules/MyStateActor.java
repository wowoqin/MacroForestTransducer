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

    protected Stack myStack;    //ÿ��actor ��Ӧ����һ�� stack ��Ҳ����һ�� stack ��Ӧ��һ�� actor
    protected Actor resActor;  //�ϼ� Actor


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

    //@Override
//    public void addMessage(DefaultMessage message){
//        DefaultMessage[] messages=getMessages();
//        synchronized (messages){
//            for(int i=0;i<messages.length;i++){
//                messages[i+1]=messages[i];
//            }
//            messages[0]=message;
//        }
//    }

    @Override
    protected void runBody() {}

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
        if("resActor".equals(subject)){ //  data = stack
            System.out.println("�������µ�actor��" + this.getName() + " ��Ȼ�������� resActor && ��ջ�����");
            this.setResActor(message.getSource());
            this.setMyStack((Stack) data);
            this.peekNext("pushTask");//�´�����actor��������Ĳ����϶���push����
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
                    System.out.println(this.getName()+" ��ʼ������յ��� predResult");
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
                                System.out.println(this.getName()+" loopBody ����T3����ν�ʷ��ؽ��");
                                wt.setPathR((String) (task.getObject()));
                            }else{  // T2
                                System.out.println(this.getName()+" loopBody ����T2ν�ʵķ��ؽ��");
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
                        }
                        else if(wt.isWaitT3ParallPreds()){ // (id,true,null)
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
                    System.out.println(state+" �� loopbody �д����ص� pathResult");
                    if(wt.getPathR()!=null){// �Ѿ��к���path���ɹ�����ͬ�㼶�Ľ��
                        System.out.println(state+" ��list ���Ѿ��д��ص� pathResults");
                        list.add(wt);
                        System.out.println(state+" �� list �����е�wt �������ǣ�"+list.size());
                    }else{
                        System.out.println(state+" ��list �л�û�д��ص� pathResults");
                        wt.setPathR((String) (task.getObject()));
                    }
                }else{   // actorTask,���� data ��һ��qName��String��
                    if (!ss.isEmpty()) {
                        // �ҵ���ǰ actor �ĵ�ǰջ���ĵ�ǰ state
                        currQ = (State) (((ActorTask) (ss.peek())).getObject());
                        String tag = (String)object;
                        int layer = task.getId();
                        System.out.println(this.getName()+" ���յ���ǩ "+tag +"����ǰactor��������"+manager.getActors().length);
                        if((!State.actors.isEmpty()) && (this.getName().equals("stackActor"))){
                            System.out.println("�� "+this.getName()+" ������actor���ͱ�ǩ "+tag);
                            for(String key:State.actors.keySet()){
                                Actor to=State.actors.get(key);
                                getManager().send(message,null,to);
                            }
                        }

                        if("startE".equals(subject)){
                            System.out.println(this.getName()+" �� loopBody �п�ʼ������յ� startE��"+tag);
                            try {
                                currQ.startElementDo(tag, layer, this);
                            } catch (CloneNotSupportedException e) {
                                e.printStackTrace();
                            }
                        }else if("endE".equals(subject)){
                            System.out.println(this.getName() + " �� loopBody �п�ʼ������յ� endE��" + tag);
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
            if(curstack!=null){
                curstack.push(actorTask);
                System.out.println(this.getName()+" ��ջ��ѹ����"+ state);
            }
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

                Stack stack=((StateT3_1) state)._predstack;
                String name=((Integer)(stack).hashCode()).toString().concat("T3-1.prActor");
                //push(q'')-->�������ô˺����ж�ѹջ
                if(stack.isEmpty()){
                    this.createAnotherActor(name,stack,new ActorTask(id,remainPred,false));
                }else{
                    State currQ=(State)remainPred.copy();
                    currQ.setLevel(level);
                    DefaultMessage message=new DefaultMessage("pushTask",new ActorTask(id,currQ,false));
                    Actor actor=State.actors.get(name);
                    getManager().send(message, this, actor);
//                    for(int i=0;i<State.actors.length;i++){
//                        if(State.actors[i].getName().equals(name)){
//                            getManager().send(message, this, State.actors[i]);
//                            return;
//                        }
//                    }
                }
            }else if(state instanceof StateT3_2){
                State firPred=((StateT3_2) state)._q2;  //T2-2
                State remainPred=((StateT3_2) state)._q3;  // q''
                firPred.setLevel(level);
                remainPred.setLevel(level);
                curstack.push(new ActorTask(id, firPred, isInSelf));
                //�� T3-2.q'''.list �����Ҫ�ȴ��� wt
                firPred.getList().add(new WaitTask(id, null, null));
                Stack stack=((StateT3_2) state)._predstack;
                String name=((Integer)(stack).hashCode()).toString().concat("T3-2.prActor");
                if(stack.isEmpty()){
                    this.createAnotherActor(name, ((StateT3_2) state)._predstack, new ActorTask(id, remainPred, false));
                }else{
                    State currQ=(State)remainPred.copy();
                    currQ.setLevel(level);
                    DefaultMessage message=new DefaultMessage("pushTask",new ActorTask(id,currQ,false));
                    Actor actor=State.actors.get(name);
                    getManager().send(message, this, actor);
//                    for(int i=0;i<State.actors.length;i++){
//                        if(State.actors[i].getName().equals(name)){
//                            getManager().send(message, this, State.actors[i]);
//                            return;
//                        }
//                    }
                }
            }else if(state instanceof StateT3_3){
                State firstPred=((StateT3_3) state)._q2;  //T2-3
                State remainPred=((StateT3_3) state)._q3;  // q''
                firstPred.setLevel(level);
                remainPred.setLevel(level);
                curstack.push(new ActorTask(id, firstPred, isInSelf));
                //�� T3-3.q'''.list �����Ҫ�ȴ��� wt
                firstPred.getList().add(new WaitTask(id, null, null));
                Stack stack=((StateT3_3) state)._predstack;
                String name=((Integer)(stack).hashCode()).toString().concat("T3-3.prActor");
                if(stack.isEmpty()){
                    this.createAnotherActor(name, ((StateT3_3) state)._predstack, new ActorTask(id, remainPred, false));
                }else{
                    State currQ=(State)remainPred.copy();
                    currQ.setLevel(level);
                    DefaultMessage message=new DefaultMessage("pushTask",new ActorTask(id,currQ,false));
                    Actor actor=State.actors.get(name);
                    getManager().send(message, this, actor);

//                    for(int i=0;i<State.actors.size();i++){
//                        if(State.actors.getName().equals(name)){
//                            getManager().send(message, this, State.actors[i]);
//                            return;
//                        }
//                    }
               }
            }else if(state instanceof StateT3_4){
                State firstPred=((StateT3_4) state)._q2;  //T2-4
                State remainPred=((StateT3_4) state)._q3;  // q''
                firstPred.setLevel(level);
                remainPred.setLevel(level);
                curstack.push(new ActorTask(id, firstPred, isInSelf));
                //�� T3-4.q'''.list �����Ҫ�ȴ��� wt
                firstPred.getList().add(new WaitTask(id, null, null));
                Stack stack=((StateT3_4) state)._predstack;
                String name=((Integer)(stack).hashCode()).toString().concat("T3-4.prActor");
                if(stack.isEmpty()){
                    this.createAnotherActor(name, ((StateT3_4) state)._predstack, new ActorTask(id, remainPred, false));
                }else{
                    State currQ=(State)remainPred.copy();
                    currQ.setLevel(level);
                    DefaultMessage message=new DefaultMessage("pushTask",new ActorTask(id,currQ,false));
                    Actor actor=State.actors.get(name);
                    getManager().send(message, this, actor);
//                    for(int i=0;i<State.actors.length;i++){
//                        if(State.actors[i].getName().equals(name)){
//                            getManager().send(message, this, State.actors[i]);
//                            return;
//                        }
//                    }
                }
            }
        }
    }

    public void popFunction(){
        Stack currStack = this.getMyStack();
        if(!currStack.isEmpty()){
            System.out.println(this.getName() + " ִ�� popFunction");
            currStack.pop();
        }
    }

    public void sendPredsResult(ActorTask actorTask){// ν�ʼ��ɹ����ϴ������id��true������Ӧ�� wt
        DefaultMessage message=new DefaultMessage("predResult",actorTask);
        if(actorTask.isInSelf()){
            System.out.println(this.getName() + " sendPredResults  To �Լ�");
            this.peekNext("predResult");//���ȴ���ν�ʷ���
            getManager().send(message, this, this);
        } else {
            System.out.println(this.getName() + " sendPredResults  To �ϼ�");
            MyStateActor  res = (MyStateActor)this.getResActor();
            res.peekNext("predResult");//���ȴ���ν�ʷ���
            getManager().send(message, this, res);
        }
    }

    public boolean sendPathResult(ActorTask actorTask){// path���ɹ����ϴ������id��tag������Ӧ�� wt
        if(actorTask.isInSelf()){
            System.out.println(this.getName()+" sendPathResults  To self");
            DefaultMessage message=new DefaultMessage("pathResult", actorTask);
            getManager().send(message, this, this);
        }else{
            MyStateActor actor=(MyStateActor)this.getResActor();//�ϼ�actor
            State state = (State)((ActorTask)(actor.getMyStack()).peek()).getObject();//�ϼ�actor��ջ�� state
            if(state instanceof StateT1){
                getManager().send(new DefaultMessage("pathResult", actorTask), this, actor);
            }else return false;//ջ��Ҫ��ν��(T1-6.preds)�����ǩ�Ǵ�����ȥ��-->��ʱѡ�񲻴����ȵ���һ������ǩ�������ϼ�������ǩʱ�ٴ�

        }
        return true;
    }

    public void sendPathResults(ActorTask actorTask){
        DefaultMessage message=new DefaultMessage("pathResult", actorTask);
        if(actorTask.isInSelf()){
            System.out.println(this.getName()+" sendPathResults  To self");
            getManager().send(message, this, this);
        }else{
            System.out.println(this.getName()+" sendPathResults  To res");
            getManager().send(message, this, this.getResActor());
            }
    }

    public void doNext(WaitTask wtask){   //���/remove
        Stack currstack=this.getMyStack();
        ActorTask task=(ActorTask)currstack.peek();

        if (wtask.isSatisfiedOut()) {//��ǰ wt �����������
            if(this.getName().equals("stackActor") && (currstack.size()==1)){//��stack��
                System.out.println( this.getName()+" doNext && �ɹ����");
                this.output(wtask);
                ((State)task.getObject()).removeWTask(wtask);
                System.out.println(((State)task.getObject()).getList().size());
            }
//            else { //����stack�� && ��ΪT1-5�ĺ���path �� ����  ����Ϊ���� path ��һ������pathջ��
//                ((State)task.getObject()).removeWTask(wtask);
//                boolean isSent=this.sendPathResult(new ActorTask(id, wtask.getPathR(),isInSelf));
//                //��ΪT1-6�ĺ���path�ķ��ؽ��������ǰʱ����preds��δ�����ɣ�����Ҫ�ȼĴ��ڵ�ǰstate.list�У�
//                //ֱ������T1-6�Ľ�����ǩ,����ǰlist�����е������wt���ϴ�
//                if(!isSent)
//                    ((State)task.getObject()).addWTask(wtask);
//            }
        }else{//���Լ��Ľ�����ǩ����ǰwt�������������
            System.out.println( this.getName()+" doNext && ʧ���Ƴ�");
            ((State)task.getObject()).removeWTask(wtask);
        }
    }

    public void processSameADPred(){
        System.out.println(this.getName()+" processSameADPred");
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

    public void processSameADPath(State state,List list) {
        System.out.println(this.getName() + " processSameADPath");
        for(int i=0;i<list.size();i++)
            state.getList().add(list.get(i));
    }

    public void  createAnotherActor(String name,Stack stack,ActorTask task){
        System.out.println(Thread.currentThread().getName());
        System.out.println(this.getName() + " �����¼� Actor");
        Actor actor=getManager().createAndStartActor(this.getClass(), name);
        System.out.println(actor.getName()+" ��ջ�����");
        ((MyStateActor) actor).setMyStack(stack);
        System.out.println("���� "+this.getName()+" ���ϼ� Actor");
        ((MyStateActor)actor).setResActor(this);
        try {
            ((MyStateActor)actor).pushTaskDo(task);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public void output(WaitTask wt){
        wt.output();
    }


}
