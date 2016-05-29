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
    protected State _q1;//检查后续 path

    protected StateT1_7(ASTPath path, State q1) {
        super(path);
        _q1 = q1;
        _q1.setLevel(this.getLevel()+1);
        this._pathstack=new Stack();
    }

    public static State TranslateState(ASTPath path) {//重新创建T1-7
        State q1 = StateT1.TranslateStateT1(path.getRemainderPath());
        return new StateT1_7(path, q1);
    }

    public void startElementDo(String tag, int layer,MyStateActor curactor) throws CloneNotSupportedException{
        if ((layer >= getLevel()) && (tag.equals(_test))) {
            String name=((Integer)this._pathstack.hashCode()).toString().concat("T1-7.paActor");
            Actor actor=(actors.get(name));// path的 actor
            // 在 tlist 中添加需要等待匹配的任务模型
            curactor.addWTask(new WaitTask(layer,true,null));

            if(actor == null){  // 若pathActor 还没有创建 --> _pathstack 一定为空
                stacklist.add(this._pathstack);
                actor =actorManager.createAndStartActor(MyStateActor.class, name);
                actors.put(actor.getName(),actor);

                dmessage=new DefaultMessage("resActor", null);
                actorManager.send(dmessage, curactor, actor);
                //发送 q'' 给 paActor
                _q1.setLevel(layer + 1);
                dmessage=new DefaultMessage("push", new ActorTask(layer,_q1,false));
                actorManager.send(dmessage,curactor,actor);
            } else{  // 若path  actor 已经创建了,则发送 q'' 给 paActor即可
                State currQ=(State)_q1.copy();
                currQ.setLevel(layer + 1);
                dmessage=new DefaultMessage("push",new ActorTask(layer,currQ,false));
                actorManager.send(dmessage, curactor, actor);

            }
            // 还要设置一下path actor接收消息的subject
            //actor.willReceive("startE");
            //actor.peekNext("startE",isX1(getLevel()+1));// path actor接受的标签应该是当前标签的 child
        }
    }

    public void endElementDo(String tag,int layer,MyStateActor curactor){

        WaitTask wtask;
        ActorTask atask;
        Stack currstack=curactor.getMyStack();

        if (tag.equals(_test)) {  // 遇到自己的结束标签，检查
            int id=((ActorTask)currstack.peek()).getId(); // 当前栈顶 taskmodel 的 id
            String name=curactor.getName();
            List list=curactor.getTlist();

            for(int i=list.size()-1;i>=0;i--){
                wtask = (WaitTask)list.get(i);
                if (wtask.getId()==layer) {
                    if (wtask.isSatisfiedOut()) {
                        if(name.equals("stackActor")){//在stack中-==>输出/传结果
                            if(currstack.size()==1){//输出
                                curactor.output(wtask);
                                // 若 T1-7.pathStack 中弹栈之后还有 AD 轴的后续 path 的检查，
                                // 则需要把当前满足的 wt.paResult复制到之前的等待的 wt.paResult
                                //直接在T1-7所在的list中把wt.pathR设置为wtask.pathR.并且不用弹栈
                                if(list.size()>1){
                                    if (this._q1 instanceof StateT1_3 || this._q1 instanceof StateT1_4
                                            || this._q1 instanceof StateT1_7 || this._q1 instanceof StateT1_8){
                                        for(int j=i-1;j>=0;j--){
                                            ((WaitTask)list.get(j)).setPathR(wtask.getPathR());
                                        }
                                    }
                                }
                                curactor.removeWTask(wtask);
                            }else {//在stack中 && 作为T1-5的后续path
                                for(int j = i-1; j >=0; j--){
                                    WaitTask wtask1 = (WaitTask) curactor.tlist.get(j);
                                    if(wtask1.getId()==id){
                                        atask=new ActorTask(id,wtask.getPathR());
                                        dmessage=new DefaultMessage("pathResult",atask);
                                        actorManager.send(dmessage,curactor,curactor);
                                        curactor.removeWTask(wtask);
                                        return;//跳出这个方法（即跳出了小循环j，又跳出了大循环i）
                                    }
                                }
                            }
                        }else {//作为AD 轴后续path的一部分-->在paActor中
                            boolean inInThis=false;
                            atask = new ActorTask(id, wtask.getPathR());
                            for (int j = i-1; j >=0 && !inInThis; j--) {
                                WaitTask wtask1 = (WaitTask) curactor.tlist.get(j);
                                if (wtask1.getId() == id) {//在paActor中 && 作为T1-5的后续path
                                    inInThis=true;
                                    dmessage = new DefaultMessage("pathResult", atask);
                                    actorManager.send(dmessage, curactor, curactor);
                                }
                            }
                            if(!inInThis){
                                //在T1-6、T1-7、T1-8的 paActor 中
                                dmessage = new DefaultMessage("pathResult", atask);
                                actorManager.send(dmessage, curactor, curactor.getResActor());
                            }
                            curactor.removeWTask(wtask);
                            return;//跳出这个方法（即跳出了小循环j，又跳出了大循环i）
                        }
                    }
                    //到自己的结束标签，当前wt不满足输出条件-->应该检查后面的谓词所对应的Actor是否做完了工作，
                    // 若做完了，则删除不满足的wt；
                    // 若还没做完，则当前actro应该等谓词actor做完再判断；


                }
            }
        }else if (layer == getLevel() - 1) { // 遇到上层结束标签(T1-7作为一个后续的path)
            // (能遇到上层结束标签，即T1-7作为一个后续的path
            // （T1-5 的时候也会放在stackActor中），T1-6~T1-8会被放在paActor中)
            // T1-5 的后续的path时，与T1-5 放在同一个栈，T1-6~T1-8 放在pathstack中
            curactor.popFunction();   // T1-7弹栈
            currstack=curactor.getMyStack();
            if(currstack.isEmpty()) {   // 弹完之后当前actor 所在的stack 为空了，则删除当前 actor
                actorManager.detachActor(curactor);
            }else{                      // T1-7 作为 T1-5 的后续 path
                State state =(State)((ActorTask)(currstack.peek())).getObject();
                if(state instanceof StateT1_5){
                    state.endElementDo(tag,layer,curactor);
                }
            }
        }
    }
}
