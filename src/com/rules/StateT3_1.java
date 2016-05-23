package com.rules;

import com.XPath.PathParser.ASTPreds;
import com.ibm.actor.Actor;
import com.ibm.actor.DefaultMessage;

import java.util.List;
import java.util.Stack;

/**
 * Created by qin on 2015/10/10.
 */
public class StateT3_1 extends StateT3{
    protected  State _q3;//检查preds'
    protected  State _q2;//检查【child::test】

    protected  StateT3_1(ASTPreds preds,State q3,State q2){
        super(preds);
        _q3=q3;
        _q2=q2;
        this._predstack=new Stack();
    }

    public static StateT3 TranslateState(ASTPreds preds){//重新创建T3-1
        State q3=StateT3.TranslateStateT3(preds.getRemainderPreds());
        State q2=StateT2.TranslateStateT2(StateT3.getSinglePred(preds));
        return new StateT3_1(preds,q3,q2);
    }
    public void startElementDo(String tag,int layer,MyStateActor curactor) throws CloneNotSupportedException {
        //进到 q3-1 中就应该对 preds' 同时进行检查
        if(getLevel() == layer) {//应该匹配的层数 getLayer（）和 当前标签 tag 的层数相等
            WaitTask wtask;
            boolean isFindInThis = false;

            _q2.setLevel(getLevel());//q2 检查【child::test】，应该匹配的标签的层数 不变
            _q3.setLevel(getLevel());// q3 检查preds'，应该匹配的标签的层数与当前 [test] 同一层

            Stack stack=curactor.getMyStack();
            ActorTask atask=(ActorTask)stack.peek();
            int id=atask.getId();//当前栈顶的 id

            String name=((Integer)this._predstack.hashCode()).toString().concat("T3-1.prActor");
            Actor actor=(actors.get(name));// preds'的 actor


            if (tag .equals(_test)){
                //要是test匹配，则直接检查preds'，preds' 的结果作为 T3-1 的结果
                curactor.popFunction();
                stack.push(new ActorTask(id,_q3));
            }
            else {// test不匹配，检查 q'' 和 q'''&& preds'.startElementDo(tag,layer)
                //1.(id,T3-1) 换为 (id,waitstate)
                State waitState=new WaitState();
                waitState.setLevel(((State) atask.getObject()).getLevel());
                curactor.popFunction();
                curactor.pushFunction(new ActorTask(id, waitState));
                // PC:
                List list=curactor.tlist;
                if(!list.isEmpty()){
                    for(int i=(list.size()-1);i>=0;i--) {
                        wtask = (WaitTask) list.get(i);
                        if (wtask.getId() == id) {//在自己的list中找到了id相同的wt-->则T3-1约束 PC 轴的test
                            isFindInThis=true;
                            //2.push（layer,q'''）
                            curactor.pushFunction(new ActorTask(layer,_q2));
                            //push(layer,q'')
                            if(actor==null){
                                actor=actorManager.createAndStartActor(MyStateActor.class, name);

                                dmessage=new DefaultMessage("stack",this._predstack);
                                actorManager.send(dmessage, curactor, actor);

                                dmessage=new DefaultMessage("setCategory","T3PredsActor");
                                actorManager.send(dmessage,curactor,actor);

                                dmessage=new DefaultMessage("pushTask",new ActorTask(layer,_q3));
                                actorManager.send(dmessage,curactor,actor);
                            }else {
                                State currQ=(State)_q3.copy();
                                currQ.setLevel(layer + 1);
                                dmessage=new DefaultMessage("pushTask",new ActorTask(layer,currQ));
                                actorManager.send(dmessage, curactor, actor);
                            }
                            //3. add（layer,false,false）
                            curactor.addWTask(new WaitTask(layer,false,"false"));
                            break;
                        }
                    }
                }
                if(!isFindInThis){//AD:
                    //2.push（id,q'''）
                    curactor.pushFunction(new ActorTask(layer,_q2));
                    //push(id,q'')
                    if(actor==null){
                        actor=actorManager.createAndStartActor(MyStateActor.class, name);

                        dmessage=new DefaultMessage("stack",this._predstack);
                        actorManager.send(dmessage, curactor, actor);

                        dmessage=new DefaultMessage("setCategory","T3PredsActor");
                        actorManager.send(dmessage,curactor,actor);

                        dmessage=new DefaultMessage("pushTask",new ActorTask(layer,_q3));
                        actorManager.send(dmessage,curactor,actor);
                    }else {
                        State currQ=(State)_q3.copy();
                        currQ.setLevel(layer + 1);
                        dmessage=new DefaultMessage("pushTask",new ActorTask(layer,currQ));
                        actorManager.send(dmessage, curactor, actor);
                    }
                    //3. add（id,false,false）
                    curactor.addWTask(new WaitTask(id,false,"false"));
                }
                //q''.startElementDo(tag,layer)
                dmessage=new DefaultMessage("startE",new ActorTask(layer,tag));
                actorManager.send(dmessage, curactor, actor);
            }
        }
    }

    //无论成败，q3-1 都被替换，所以 q3-1 不会遇到上层结束标签
}

