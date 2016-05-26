package com.rules;

import com.XPath.PathParser.ASTPath;
import com.ibm.actor.Actor;
import com.ibm.actor.DefaultMessage;

import java.util.List;
import java.util.Stack;

/**
 * Created by qin on 2015/10/10.
 */
public class StateT1_4 extends StateT1 implements Cloneable {
    protected State _q3;//检查 preds

    protected StateT1_4(ASTPath path, State q3) {
        super(path);
        _q3 = q3;
        _q3.setLevel(this.getLevel() + 1);
        this._predstack = new Stack();
    }

    public static State TranslateState(ASTPath path) {//创建T1-4
        State q3 = StateT3.TranslateStateT3(path.getFirstStep().getPreds());
        return new StateT1_4(path, q3);
    }

    public void startElementDo(String tag,int layer,MyStateActor curactor) throws CloneNotSupportedException{
        if ((layer >= getLevel()) && (tag.equals(_test))) {
            String name=((Integer)this._predstack.hashCode()).toString().concat("T1-4.prActor");
            Actor actor=(actors.get(name));// preds 的 actor
            // 在 tlist 中添加需要等待匹配的任务模型
            curactor.addWTask(new WaitTask(layer,false,tag));

            if(actor == null){// 若谓词 actor 还没有创建 --> _predstack 一定为空
                stacklist.add(this._predstack);
                actor =actorManager.createAndStartActor(MyStateActor.class, name);
                actors.put(actor.getName(),actor);

                dmessage=new DefaultMessage("resActor", null);
                actorManager.send(dmessage, curactor, actor);
                //发送 q' 给 prActor
                _q3.setLevel(layer + 1);
                dmessage=new DefaultMessage("push", new ActorTask(layer,_q3));
                actorManager.send(dmessage,curactor,actor);
            }
            else{  // 若谓词 actor 已经创建了,则发送 q' 给 prActor即可
                State currQ=(State)_q3.copy();
                currQ.setLevel(layer + 1);
                dmessage=new DefaultMessage("pushTask",new ActorTask(layer,currQ));
                actorManager.send(dmessage, curactor, actor);
            }
        }
    }

    @Override
    public void endElementDo(String tag,int layer,MyStateActor curactor) {
        WaitTask wtask;
        ActorTask atask;
        Stack currstack = curactor.getMyStack();

        if (tag.equals(_test)) {  // 遇到自己的结束标签，检查
            int id = ((ActorTask) currstack.peek()).getId(); // 当前栈顶 task 的 id
            String name = curactor.getName();
            List list = curactor.getTlist();

            for (int i =list.size()-1; i >=0; i--) {
                wtask = (WaitTask) list.get(i);
                if (wtask.getId() == layer) {
                    if (wtask.isSatisfied()) {
                        if (name.equals("stackActor")) {
                            if (currstack.size() == 1) {//q0==T1-4-->输出
                                curactor.output(wtask);
                                curactor.removeWTask(wtask);
                                //此时需要判断T1-4 的谓词是否是AD轴，若是：把T1-4.predstack中相应的task对应的wt.predR设为True
                                if (list.size() > 1) {
                                    name = ((Integer) this._predstack.hashCode()).toString().concat("T1-4.prActor");
                                    Actor actor = (actors.get(name));// preds的 actor
                                    if (this._q3 instanceof StateT2_3){
                                        for(int j=i-1;j>=0;j--){
                                            ((WaitTask) list.get(j)).setPredR(true);
                                        }
                                        ((MyStateActor)actor).getMyStack().clear();
                                        actorManager.detachActor(actor);
                                    }else if (this._q3 instanceof StateT2_4) {
                                        for(int j=i-1;j>=0;j--){
                                            ((WaitTask) list.get(j)).setPredR(true);
                                        }
                                        ((MyStateActor)actor).getMyStack().clear();
                                        if(!((MyStateActor)actor).getTlist().isEmpty())
                                            ((MyStateActor)actor).getTlist().clear();
                                        actorManager.detachActor(actor);
                                    }else if(this._q3 instanceof StateT3_3){
                                        //设置q3所在的actor的list.wt.predR=true

                                        //查看preds'的性质

                                    }else if(this._q3 instanceof StateT3_4){

                                    }
                                } else {//在stack中 && T1-4作为T1-5的后续path-->上传结果-->只传一遍，相同id的不管了
                                    atask = new ActorTask(id, wtask.getPathR());
                                    for (int j = i-1; j >=0; j--) {
                                        WaitTask wtask1 = (WaitTask) curactor.tlist.get(j);
                                        if (wtask1.getId() == id) {
                                            dmessage = new DefaultMessage("pathResult", atask);
                                            actorManager.send(dmessage, curactor, curactor);
                                            curactor.removeWTask(wtask);
                                            return;//跳出这个方法（即跳出了小循环j，又跳出了大循环i）
                                        }
                                    }
                                }
                            } else {//作为AD 轴后续path的一部分-->在paActor中
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
                    }
                    //到自己的结束标签，当前wt不满足输出条件-->应该检查后面的谓词所对应的Actor是否做完了工作，
                    // 若做完了，则删除不满足的wt；
                    // 若还没做完，则当前actro应该等谓词actor做完再判断；

                }
            }
        } else if (layer == getLevel() - 1) { // 遇到上层结束标签(T1-7作为一个后续的path)
            // (能遇到上层结束标签，即T1-7作为一个后续的path
            // （T1-5 的时候也会放在stackActor中），T1-6~T1-8会被放在paActor中)
            // T1-5 的后续的path时，与T1-5 放在同一个栈，T1-6~T1-8 放在pathstack中
            curactor.popFunction();   // T1-4弹栈
            currstack = curactor.getMyStack();
            if (currstack.isEmpty()) {   // 弹完之后当前actor 所在的stack 为空了，则删除当前 actor
                actorManager.detachActor(curactor);
            } else {                      // T1-7 作为 T1-5 的后续 path
                State state = (State) ((ActorTask) (currstack.peek())).getObject();
                if (state instanceof StateT1_5) {
                    state.endElementDo(tag, layer, curactor);
                }
            }
        }
    }
}


