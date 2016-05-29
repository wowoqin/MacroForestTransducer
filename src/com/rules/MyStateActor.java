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
        * 从 SAX 接口那里 ：startE、endE
        * stateActor 之间：stack: stack
        *                 pushTask：q'/ q''
        *                 predResult: True (false 不用返回)
        *                 pathResult: 一颗子树 / null
        *                 setCategory：T3谓词中 preds' 所对应的 actor的类型（String）
        *
        *  */
    @Override
    protected void loopBody(Message message) {
        sleep(1);
        Object data=message.getData();
        String subject = message.getSubject();
        if("resActor".equals(subject)){ // actorTask,并且 data = null
            this.setResActor(message.getSource());
        }else{
            ActorTask task=(ActorTask)data;// task
            // 是一个actorTask
            if(task!=null){
                // object 是q（State）、qName（String）、q'的返回结果（True/False）、q''的返回结果（String）
                Object object = task.getObject();
                Stack  ss = this.getMyStack();
                State  currQ;

               if("push".equals(subject)){//单纯的push
                   this.getMyStack().push(task);
               }else if("pushTask".equals(subject)){       // actorTask,并且 data 是一个 q3
                    this.pushFunction(task);
               }else if("setCategory".equals(subject)){    // actorTask,并且 data 是一个 string
                    this.setCategory((String)object);
               }else if("predResult".equals(subject)){     // actorTask,并且 data 是一个q'的返回结果（True）
                    //此时，谓词的返回结果或者来自自己，或者是来自下一级的actor（T2的谓词actor或者是T3的preds'对应的actor）
                    // 收到谓词返回结果后，在list中找到相应的wt的ID，然后将谓词检查的结果重新赋值
                    for(int i=0;i<tlist.size();i++) {
                        WaitTask wt = (WaitTask) (tlist.get(i));
                        if (wt.getId() == task.getId()) {
                            if (message.getSource().getCategory().equals("T3PredsActor")) {  //谓词返回结果来自T3 preds'的actor
                                wt.setPathR((String) (task.getObject()));
                            } else {    //消息来自自己-->curactor 或者是消息来自T2 的后续谓词
                                wt.setPredR((Boolean)(task.getObject()));
                            }
                            //重新设置完成之后看当前设置完的wt是不是谓词满足的-->说明之前返回的谓词结果只是作为谓词的谓词
                            if (wt.isPredsSatisified()) {
                            /*谓词满足(id,true,true)会出现的情况：
                            *   最原始的谓词为T2-->在test匹配后list.add(id,false,true);
                            *          T2-2：则当前栈顶为T2-2，其谓词检查成功，则T2-2整体也检查成功；
                            *          T2-4：则当前栈顶为T2-4，其谓词检查成功，则T2-4整体也检查成功，则此时
                            *                就考虑它是否修饰了一个AD 轴的test(自身-->T2-4在一个独立栈)，
                            *                若是，则当前栈里面的所有T2-4都是成功的；
                            *
                            *  最原始的谓词为T3：
                            *          T3-1：当前栈顶:
                            *                1)(id,waitState)
                            *          T3-2：当前栈顶：
                            *                1)(id,waitState)： preds'后检查成功，即先检查成功T2-2的preds(q')，将T2-2替换为waitState
                            *                2)(id,T2-2)：      preds'先检查成功，wt=(id,false,true),然后T2-2的preds'(q')才检查成功，pop(q')后遇到T2-2
                            *          T3-3：当前栈顶:
                            *                1)(id,waitState)
                            *          T3-4：当前栈顶：
                            *                1)T2-4:
                            *                  1. q'''先检查成功：
                            *                     则T2-4.test有多个匹配，即在T2-4.prActor中有多个q'，此时需要pop(layer,T2-4)
                            *                      && taskmodel=(layer,true)给T3-4的 wtask-->(id,T,F)==>(id,T,T)时，栈顶为(id,qw);
                            *                  2. q'''后检查成功，在此之前T3-4.wtask已经是(id,F,T)：
                            *                     则(layer,T)给T3-4.wtask-->(layer,T,T),而此时栈顶(layer,T2-4),但是整个T3-4已经检查成功了，
                            *                     则需要pop（layer,T2-4）&& (qw.id,true)发出去；
                            *                2)(id,waitState)
                            *
                            * */
                                //谓词满足，看当前栈顶
                                if (!ss.isEmpty()) {
                                    //发送谓词结果 && pop 当前栈顶
                                    ActorTask atask = (ActorTask) ss.peek();
                                    int id = atask.getId();

                                    State state = (State) atask.getObject();
                                    if (state instanceof StateT2_4) {
                                        //从T2-4 的谓词栈那里返回结果-->T2-4检查成功
                                        //remove 与T2-4 相关的wt-->//d[/b]
                                        for (int j = i + 1; j < tlist.size(); j++) {
                                            wt = (WaitTask) tlist.get(j);
                                            this.tlist.remove(wt);
                                        }
                                    }
                                    this.sendPredsResult(new ActorTask(id, true));
                                }
                                this.removeWTask(wt);   //用完wt之后，删除这个谓词满足的wt
                            }
                            // 谓词原来是T3 && 收到的是 T2 的后续谓词的返回结果
                            //preds:child::test preds] 或者 [desc_or_self::test preds]中的preds的返回结果
                            else if (wt.isWaitT3PPreds()) {// (id,true,"false")
                                //q'''检查成功，q''还没检查成功
                                if (!ss.isEmpty()) {
                                    ActorTask atask = (ActorTask) ss.peek();
                                    State state = (State) atask.getObject();//栈顶state
                                    int id = atask.getId();
                                    if(state instanceof  StateT2_2){    // 原来的谓词为 T3-2 && preds'还没检查成功
                                        State waitState=new WaitState();
                                        waitState.setLevel(((State)atask.getObject()).getLevel());
                                        atask=new ActorTask(id,waitState);
                                        //pop 当前(id,T2-2)
                                        this.popFunction();
                                        //push(id,qw)
                                        this.pushFunction(atask);
                                    }
                                    if (state instanceof StateT2_4) {   // 原来的谓词为 T3-4 && preds'还没检查成功
                                        //从T2-4 的谓词栈那里返回结果-->T2-4检查成功
                                        //remove 与T2-4 相关的wt-->//d[/b]
                                        for (int j = i + 1; j < tlist.size(); j++) {
                                            wt = (WaitTask) tlist.get(j);
                                            this.tlist.remove(wt);
                                        }
                                        //pop(T2-4)
                                        this.popFunction();
                                    }
                                }
                            }
                        }
                    }
               }else if("pathResult".equals(subject)){ // actorTask,并且 data 是一个q''的返回结果（String）
                    // 在 waitTask 中找到相应的ID，然后将后续 path 检查的结果 重新赋值
                    for(int i=0;i<tlist.size();i++){
                        WaitTask wt = (WaitTask)(tlist.get(i));
                        if(wt.getId() == task.getId()){
                            if(wt.getPathR()!=null){// 已经有后续path检查成功的相同层级的结果
                                tlist.add(i+1,wt);
                            }else{
                                wt.setPathR((String) (task.getObject()));
                            }
                            break;
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

    public void pushFunction(ActorTask actorTask){
        //这中情况发生的场景：T3谓词修饰 AD 轴的 path或者preds && T3的test不匹配
        Stack currStack = this.getMyStack();
        int layer=((State)(actorTask.getObject())).getLevel();
        if(!currStack.isEmpty()){
            ActorTask at=(ActorTask)currStack.peek();
            if((((State)(at.getObject())).getLevel())>layer){
                //若当前栈顶 q1 的layer 大于将要压栈的 q2 的layer，则压栈顺序为 q2，q1
                currStack.pop();
                currStack.push(actorTask);
                currStack.push(at);
            }else{
                currStack.push(actorTask);
            }
        }else{
            currStack.push(actorTask);
        }

    }

    public void popFunction(){
        Stack currStack = this.getMyStack();
        if(!currStack.isEmpty())
            currStack.pop();
    }

    public void sendPathResult(ActorTask actorTask){// path检查成功，上传结果（id，tag）给相应的 wt
        boolean isFindInThis=false;
        if(!this.tlist.isEmpty()){        //先在curactor.list中找是否有相同 id 的wt
            for(int i=(tlist.size()-1);i>=0;i--){
                WaitTask wTask=(WaitTask)tlist.get(i);
                if(wTask.getId()==actorTask.getId()){// 找到相同 id 的 wt
                    isFindInThis=true;
                    Message message = new DefaultMessage("predResult",actorTask);
                    getManager().send(message, this, this);
                }
            }
        }
        if(!isFindInThis){  //curactor.list 中没有相同id的wt，则上传给resActor
            Message message = new DefaultMessage("predResult",actorTask);
            getManager().send(message, this, this.getResActor());
        }
    }

    public void sendPredsResult(ActorTask actorTask){// 谓词检查成功，上传结果（id，true）给相应的 wt
        if(actorTask.isInSelf()){
            getManager().send(new DefaultMessage("predResult",actorTask), this, this);
        }else{
            getManager().send(new DefaultMessage("predResult",actorTask), this, this.getResActor());
        }
    }

    //没有检查成功弹栈时 remove 与actorTask.id相等的 wtask
    public void FindAndRemoveFailedWTask(ActorTask actorTask){
        boolean isFindInThis = false;
        WaitTask wtask;
        List list=this.getTlist();
        //自己的 list 中找有无相同 id 的 wt(或许会有多个)
        if(!list.isEmpty()){
            for(int i=0;i<list.size();i++) {
                 wtask = (WaitTask) list.get(i);
                if (wtask.getId() == actorTask.getId()) {
                    isFindInThis = true;
                    list.remove(wtask);
                }
            }
        }
        //在自己所在的list中没有找到相同 id 的 wt，则在上级 actor 中找
        if(!isFindInThis){
            MyStateActor resActor=(MyStateActor)this.getResActor();
            list= resActor.getTlist();
            for(int i=0;i<list.size();i++){
                wtask=(WaitTask)list.get(i);
                if(wtask.getId()==actorTask.getId())
                    resActor.removeWTask(wtask);
            }
        }
    }

    public void removeWTask(WaitTask wt){
        this.tlist.remove(wt);
    }

    public void output(WaitTask wt){
        wt.output();
        this.removeWTask(wt);
    }

    public void addWTask(WaitTask wt){
        this.tlist.add(wt);
    }
}
