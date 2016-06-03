package com.rules;

import com.ibm.actor.AbstractActor;
import com.ibm.actor.Actor;
import com.ibm.actor.DefaultMessage;
import com.ibm.actor.Message;
import com.taskmodel.ActorTask;
import com.taskmodel.WaitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by qin on 2016/3/22.
 */
public class MyStateActor extends AbstractActor {

    protected Stack myStack;//每个actor 中应该有一个 stack ，也就是一个 stack 对应于一个 actor
    protected Actor resActor;//上级 Actor

    protected List tlist;// 存放等待匹配的任务 list

    public MyStateActor(){
        tlist=new ArrayList();
    }

    public Stack getMyStack() {
        return myStack;
    }

    public List getTlist() {
        return tlist;
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
        if("resActor".equals(subject)){ //  data = null
            this.setResActor(message.getSource());
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
                   ActorTask atask = (ActorTask) ss.peek();//栈顶task
                   int id = atask.getId();
                   State state = (State) atask.getObject();//栈顶 state
                   List list=state.getList();

                   for(int i=list.size()-1;i>=0;i--){
                       WaitTask wt = (WaitTask) (list.get(i));
                       if(message.getSource()==this){   //消息来自自己
                           wt.setPredR((Boolean)(task.getObject()));
                       }else {//消息来自下级 actor
                           if(message.getSource().getCategory().equals("T3PredsActor")){//T3的谓词栈
                               wt.setPathR((String) (task.getObject()));
                           }else{  // T2
                               wt.setPredR((Boolean)(task.getObject()));
                           }
                       }
                       //重新设置完成之后看当前设置完的wt是不是谓词满足的-->说明之前返回的谓词结果只是作为谓词的谓词
                       if (wt.isPredsSatisified()){
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
                                   this.sendPredsResult(new ActorTask(id,true,atask.isInSelf()));
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
                                   this.popFunction();//pop 的时候顺带也 clear 了 list
                                   WaitState wq=new WaitState();
                                   wq.setLevel(state.getLevel());
                                   wq.getList().add(wts);
                                   try {
                                       this.pushTaskDo(new ActorTask(id,wq,task.isInSelf()));
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
                           }else if(state instanceof WaitState){
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
                       }else if(wt.isWaitT3ParallPreds()){ // (id,true,null)
                           //q'''检查成功，q''还没检查成功
                           this.popFunction();//pop 的时候顺带也 clear 了 list
                           WaitState wq=new WaitState();
                           wq.setLevel(state.getLevel());
                           wq.getList().add(wt);
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
                   int id = atask.getId();
                   State state = (State) atask.getObject();//栈顶 state
                   //给栈顶q.list中的 wt 赋值
                   List list=state.getList();
                   for(int i=list.size()-1;i>=0;i--){
                       WaitTask wt = (WaitTask) (list.get(i));
                       if(wt.getPathR()!=null){// 已经有后续path检查成功的相同层级的结果
                           list.add(i+1,wt);
                       }else{
                           wt.setPathR((String) (task.getObject()));
                       }
                   }
               }else{                              // actorTask,并且 data 是一个qName（String）
                    if (!ss.isEmpty()) {
                        // 找到当前 actor 的当前栈顶的当前 state
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
        //删除发送的消息-->每个Actor接收的最大消息数量=100
        //this.remove(message);
    }

    public void pushTaskDo(ActorTask actorTask) throws CloneNotSupportedException {
        Stack curstack=this.getMyStack();
        State state=(State)(actorTask.getObject());     // 要压栈的 state

        if(state instanceof StateT1||state instanceof StateT2|| state instanceof WaitState){
            curstack.push(actorTask);
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

                String name=((Integer)(((StateT3_1) state)._predstack).hashCode()).toString().concat("T3-1.prActor");
                Actor actor=State.actors.get(name);
                //push(q'')-->继续调用此函数判断压栈
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
                //在 T3-2.q'''.list 中添加要等待的 wt
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
                //在 T3-3.q'''.list 中添加要等待的 wt
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
                //在 T3-4.q'''.list 中添加要等待的 wt
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

    public void sendPredsResult(ActorTask actorTask){// 谓词检查成功，上传结果（id，true）给相应的 wt
        if(actorTask.isInSelf()){
            getManager().send(new DefaultMessage("predResult",actorTask), this, this);
        }else{
            getManager().send(new DefaultMessage("predResult",actorTask), this, this.getResActor());
        }
    }

    public void sendPathResult(ActorTask actorTask){// path检查成功，上传结果（id，tag）给相应的 wt
        if(actorTask.isInSelf()){
            getManager().send(new DefaultMessage("pathResult",actorTask), this, this);
        }else{
            getManager().send(new DefaultMessage("pathResult", actorTask), this, this.getResActor());
        }
    }

    public void doNext(WaitTask wtask){   //输出/上传/remove/
        Stack currstack=this.getMyStack();
        ActorTask task=(ActorTask)currstack.peek();
        int id=task.getId(); // 当前栈顶 taskmodel 的 id
        boolean isInSelf=task.isInSelf();

        if (wtask.isSatisfiedOut()) {//当前 wt 满足输出条件
            if(this.getName().equals("stackActor") && (currstack.size()==1)){//在stack中
                this.output(wtask);//也许有多个输出，此时不return；
            }else { //（在stack中 && 作为T1-5的后续path ） 或者  （作为AD 轴后续 path 的一部分）
                ((State)task.getObject()).removeWTask(wtask);
                this.sendPathResult(new ActorTask(id, wtask.getPathR(),isInSelf));
            }
        }else{//到自己的结束标签，当前wt不满足输出条件
            ((State)task.getObject()).removeWTask(wtask);
        }
    }

    public void processSameADPred(){
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

    public void removeWTask(WaitTask wt){
        this.tlist.remove(wt);
    }

    public void output(WaitTask wt){
        wt.output();
    }

    public void addWTask(WaitTask wt){
        this.tlist.add(wt);
    }
}
