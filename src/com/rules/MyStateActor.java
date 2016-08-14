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

    protected Stack myStack;    //每个actor 中应该有一个 stack ，也就是一个 stack 对应于一个 actor
    protected Actor resActor;  //上级 Actor


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

    /* stateActor 能够接收到的消息：
        * stateActor 之间：resActor:    设置上级 actor
        *                 pushTask：    q'/ q''
        *                 predResult:   True/false
        *                 pathResult:   一颗子树 / null
        *                 setCategory：  T3谓词中 preds' 所对应的 actor的类型（String）
        *                 qName：        startE、endE
        *  */
    @Override
    protected void loopBody(Message message) {
        sleep(1);
        String subject = message.getSubject();
        Object data=message.getData();
        if("resActor".equals(subject)){ //  data = stack
            System.out.println("创建了新的actor：" + this.getName() + " ，然后设置其 resActor && 与栈相关联");
            this.setResActor(message.getSource());
            this.setMyStack((Stack) data);
            this.peekNext("pushTask");//新创建了actor后接下来的操作肯定是push操作
        }else if("setCategory".equals(subject)){    // actorTask,并且 data 是一个 string
            this.setCategory((String) data);
        }else{
            ActorTask task=(ActorTask)data;// task
            // 是一个actorTask
            if(task!=null){
                // object 是 q（State）、qName（String）、q'的返回结果（True/False）、q''的返回结果（String）
                Object object = task.getObject();
                Stack  ss = this.getMyStack();
                State  currQ;

                if("pushTask".equals(subject)){       // actorTask,并且 data 是一个 q3
                    try {
                        this.pushTaskDo(task);
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                }else if("predResult".equals(subject)){     // actorTask,并且 data 是一个q'的返回结果（True）
                    // 收到谓词返回结果后，在当前栈顶的state.list中找到相应的wt的ID，然后将谓词检查的结果重新赋值
                    System.out.println(this.getName()+" 开始处理接收到的 predResult");
                    ActorTask atask = (ActorTask) ss.peek();//栈顶task
                    int id = atask.getId();
                    State state = (State) atask.getObject();//栈顶 state
                    List list=state.getList();

                    for(int i=list.size()-1;i>=0;i--){//要是有多个等待同一谓词检查结果的wt(T1-6、T1-8)，需要把每一个的结果都设置了
                        WaitTask wt = (WaitTask) (list.get(i));
                        if(message.getSource()==this){   //消息来自自己
                            wt.setPredR((Boolean)(task.getObject()));
                        }else {//消息来自下级 actor
                            if(message.getSource().getCategory().equals("T3PredsActor")){//T3的谓词栈
                                System.out.println(this.getName()+" loopBody 处理T3并列谓词返回结果");
                                wt.setPathR((String) (task.getObject()));
                            }else{  // T2
                                System.out.println(this.getName()+" loopBody 处理T2谓词的返回结果");
                                wt.setPredR((Boolean)(task.getObject()));
                            }
                        }
                        //重新设置完成之后看当前设置完的wt是不是谓词满足的-->说明之前返回的谓词结果只是作为谓词的谓词
                        if (wt.isPredsSatisified()){//谓词成功
                            if(state instanceof StateT2_1){       //原来是T3-1
                                //pop栈顶；发送 true
                                this.popFunction();
                                this.sendPredsResult(new ActorTask(id,true,atask.isInSelf()));
                            }else if(state instanceof StateT2_2){
                                if(list.size()==1){ //T2-2
                                    //pop栈顶；发送 true
                                    this.popFunction();
                                    this.sendPredsResult(new ActorTask(id,true,atask.isInSelf()));
                                }else{//原来是T3-2,现在只是T2-2检查成功
                                    list.remove(wt);
                                    if(i==0){//T3-2检查成功(q''之前就检查成功了)
                                        this.sendPredsResult(new ActorTask(id,true,atask.isInSelf()));
                                    }else{//i==1，T2-2检查成功(无论之前q''是否检查成功，都先给自己setPredR=true)
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
                            }else if(state instanceof StateT2_3){ //原来是T3-3 && q''先检查成功--(id,null,"true")
                                //pop栈顶；发送 true
                                this.popFunction();
                                this.sendPredsResult(new ActorTask(id, true, atask.isInSelf()));
                                atask = (ActorTask) ss.peek();//栈顶task
                                state = (State) atask.getObject();//栈顶 state
                                if(!ss.isEmpty()){
                                    if((state instanceof StateT2_3) && (!atask.isInSelf())){// T2-3 作为 AD 轴test的谓词
                                        this.processSameADPred();
                                    }
                                }
                            }else if(state instanceof StateT2_4){
                                WaitTask wts=(WaitTask)list.get(0);
                                if(wts.isWaitT3ParallPreds()){ //(id,true,null)--原来是T3-4 && q''还未检查成功
                                    //T2-4 换为 qw
                                    WaitState wq=new WaitState();
                                    wq.setLevel(state.getLevel());
                                    wq.getList().add(wts);
                                    this.popFunction();//pop 的时候顺带也 clear 了 list
                                    try {
                                        this.pushTaskDo(new ActorTask(id,wq,atask.isInSelf()));
                                    } catch (CloneNotSupportedException e) {
                                        e.printStackTrace();
                                    }
                                }else{//(id,null,"true")---》原来是T2-4；
                                    // 或者 （原来是T3-4 && q''先检查成功-->现在整个T3-4都检查成功）
                                    //pop栈顶；发送 true
                                    this.popFunction();
                                    this.sendPredsResult(new ActorTask(id,true,atask.isInSelf()));
                                    if(!ss.isEmpty()){
                                        atask = (ActorTask) ss.peek();//栈顶task
                                        state = (State) atask.getObject();//栈顶 state
                                        if((state instanceof StateT2_4) && (!atask.isInSelf())){// T2-4 作为 AD 轴test的谓词
                                            this.processSameADPred();
                                        }
                                    }
                                }
                            }else if(state instanceof WaitState){//q'''已经先检查成功，现在q''也检查成功了
                                //pop栈顶；发送 true
                                this.popFunction();
                                this.sendPredsResult(new ActorTask(id,true,atask.isInSelf()));
                                if(!ss.isEmpty()){
                                    atask = (ActorTask) ss.peek();//栈顶task
                                    state = (State) atask.getObject();//栈顶 state
                                    if(((state instanceof StateT2_3) || (state instanceof StateT2_4)) && (!atask.isInSelf())){// T2-3/T2-4 作为 AD 轴test的谓词
                                        this.processSameADPred();
                                    }
                                }
                            }
                        }
                        else if(wt.isWaitT3ParallPreds()){ // (id,true,null)
                            //q'''检查成功，q''还没检查成功
                            WaitState wq=new WaitState();
                            wq.setLevel(state.getLevel());
                            wq.getList().add(wt);
                            this.popFunction();//pop 的时候顺带也 clear 了 list
                            try {
                                this.pushTaskDo(new ActorTask(id,wq,task.isInSelf()));
                            } catch (CloneNotSupportedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }else if("pathResult".equals(subject)){ // actorTask,并且 data 是一个q''的返回结果（String）
                    // 在 waitTask 中找到相应的ID，然后将后续 path 检查的结果 重新赋值
                    ActorTask atask = (ActorTask) ss.peek();//栈顶task
                    State state = (State) atask.getObject();//栈顶 state
                    //给栈顶q.list中的 wt 赋值
                    List list=state.getList();
                    WaitTask wt = (WaitTask) (list.get(list.size()-1));
                    System.out.println(state+" 在 loopbody 中处理传回的 pathResult");
                    if(wt.getPathR()!=null){// 已经有后续path检查成功的相同层级的结果
                        System.out.println(state+" 的list 中已经有传回的 pathResults");
                        list.add(wt);
                        System.out.println(state+" 的 list 中已有的wt 的数量是："+list.size());
                    }else{
                        System.out.println(state+" 的list 中还没有传回的 pathResults");
                        wt.setPathR((String) (task.getObject()));
                    }
                }else{   // actorTask,并且 data 是一个qName（String）
                    if (!ss.isEmpty()) {
                        // 找到当前 actor 的当前栈顶的当前 state
                        currQ = (State) (((ActorTask) (ss.peek())).getObject());
                        String tag = (String)object;
                        int layer = task.getId();
                        System.out.println(this.getName()+" 接收到标签 "+tag +"，当前actor的数量："+manager.getActors().length);
                        if((!State.actors.isEmpty()) && (this.getName().equals("stackActor"))){
                            System.out.println("由 "+this.getName()+" 向其它actor发送标签 "+tag);
                            for(String key:State.actors.keySet()){
                                Actor to=State.actors.get(key);
                                getManager().send(message,null,to);
                            }
                        }

                        if("startE".equals(subject)){
                            System.out.println(this.getName()+" 在 loopBody 中开始处理接收到 startE："+tag);
                            try {
                                currQ.startElementDo(tag, layer, this);
                            } catch (CloneNotSupportedException e) {
                                e.printStackTrace();
                            }
                        }else if("endE".equals(subject)){
                            System.out.println(this.getName() + " 在 loopBody 中开始处理接收到 endE：" + tag);
                            currQ.endElementDo(tag, layer, this);
                        }
                    }
                }
            }
        }


        //删除发送的消息-->每个Actor接收的最大消息数量=100
        //this.remove(message);
    }

    public void pushTaskDo(ActorTask actorTask) throws CloneNotSupportedException {
        Stack curstack=this.getMyStack();
        State state=(State)(actorTask.getObject());     // 要压栈的 state

        if(state instanceof StateT1||state instanceof StateT2|| state instanceof WaitState){
            if(curstack!=null){
                curstack.push(actorTask);
                System.out.println(this.getName()+" 的栈中压入了"+ state);
            }
        }else {
            int id=actorTask.getId();
            boolean isInSelf=actorTask.isInSelf();
            int level=((State)(actorTask.getObject())).getLevel();// T3 要匹配的层数

            if(state instanceof StateT3_1){
                State firstPred=((StateT3_1) state)._q2; // T2-1
                State remainPred=((StateT3_1) state)._q3;// q''
                firstPred.setLevel(level);
                remainPred.setLevel(level);
                //push(q''')
                curstack.push(new ActorTask(id, firstPred, isInSelf));
                //在 T3-1.q'''.list 中添加要等待的 wt
                firstPred.getList().add(new WaitTask(id, null, null));

                Stack stack=((StateT3_1) state)._predstack;
                String name=((Integer)(stack).hashCode()).toString().concat("T3-1.prActor");
                //push(q'')-->继续调用此函数判断压栈
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
                //在 T3-2.q'''.list 中添加要等待的 wt
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
                //在 T3-3.q'''.list 中添加要等待的 wt
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
                //在 T3-4.q'''.list 中添加要等待的 wt
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
            System.out.println(this.getName() + " 执行 popFunction");
            currStack.pop();
        }
    }

    public void sendPredsResult(ActorTask actorTask){// 谓词检查成功，上传结果（id，true）给相应的 wt
        DefaultMessage message=new DefaultMessage("predResult",actorTask);
        if(actorTask.isInSelf()){
            System.out.println(this.getName() + " sendPredResults  To 自己");
            this.peekNext("predResult");//优先处理谓词返回
            getManager().send(message, this, this);
        } else {
            System.out.println(this.getName() + " sendPredResults  To 上级");
            MyStateActor  res = (MyStateActor)this.getResActor();
            res.peekNext("predResult");//优先处理谓词返回
            getManager().send(message, this, res);
        }
    }

    public boolean sendPathResult(ActorTask actorTask){// path检查成功，上传结果（id，tag）给相应的 wt
        if(actorTask.isInSelf()){
            System.out.println(this.getName()+" sendPathResults  To self");
            DefaultMessage message=new DefaultMessage("pathResult", actorTask);
            getManager().send(message, this, this);
        }else{
            MyStateActor actor=(MyStateActor)this.getResActor();//上级actor
            State state = (State)((ActorTask)(actor.getMyStack()).peek()).getObject();//上级actor的栈顶 state
            if(state instanceof StateT1){
                getManager().send(new DefaultMessage("pathResult", actorTask), this, actor);
            }else return false;//栈顶要是谓词(T1-6.preds)，则标签是传不过去的-->此时选择不传，等到下一结束标签或者是上级结束标签时再传

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

    public void doNext(WaitTask wtask){   //输出/remove
        Stack currstack=this.getMyStack();
        ActorTask task=(ActorTask)currstack.peek();

        if (wtask.isSatisfiedOut()) {//当前 wt 满足输出条件
            if(this.getName().equals("stackActor") && (currstack.size()==1)){//在stack中
                System.out.println( this.getName()+" doNext && 成功输出");
                this.output(wtask);
                ((State)task.getObject()).removeWTask(wtask);
                System.out.println(((State)task.getObject()).getList().size());
            }
//            else { //（在stack中 && 作为T1-5的后续path ） 或者  （作为后续 path 的一部分在path栈）
//                ((State)task.getObject()).removeWTask(wtask);
//                boolean isSent=this.sendPathResult(new ActorTask(id, wtask.getPathR(),isInSelf));
//                //作为T1-6的后续path的返回结果，若当前时刻其preds还未检查完成，则需要先寄存在当前state.list中，
//                //直到遇到T1-6的结束标签,将当前list中所有的满足的wt都上传
//                if(!isSent)
//                    ((State)task.getObject()).addWTask(wtask);
//            }
        }else{//到自己的结束标签，当前wt不满足输出条件
            System.out.println( this.getName()+" doNext && 失败移除");
            ((State)task.getObject()).removeWTask(wtask);
        }
    }

    public void processSameADPred(){
        System.out.println(this.getName()+" processSameADPred");
        Stack currstack=this.getMyStack();

        while(!currstack.isEmpty()){
            ActorTask task=(ActorTask)currstack.peek();
            int id=task.getId(); // 当前栈顶 taskmodel 的 id
            boolean isInSelf=task.isInSelf();
            this.popFunction();
            sendPredsResult(new ActorTask(id,true,isInSelf));
        }
        //栈为空
        getManager().detachActor(this);
    }

    public void processSameADPath(State state,List list) {
        System.out.println(this.getName() + " processSameADPath");
        for(int i=0;i<list.size();i++)
            state.getList().add(list.get(i));
    }

    public void  createAnotherActor(String name,Stack stack,ActorTask task){
        System.out.println(Thread.currentThread().getName());
        System.out.println(this.getName() + " 创建下级 Actor");
        Actor actor=getManager().createAndStartActor(this.getClass(), name);
        System.out.println(actor.getName()+" 与栈相关联");
        ((MyStateActor) actor).setMyStack(stack);
        System.out.println("设置 "+this.getName()+" 的上级 Actor");
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
