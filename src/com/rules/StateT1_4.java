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
        this._predstack = new Stack();
    }

    public static State TranslateState(ASTPath path) {//创建T1-4
        State q3 = StateT3.TranslateStateT3(path.getFirstStep().getPreds());
        return new StateT1_4(path, q3);
    }

    public void startElementDo(String tag,int layer,MyStateActor curactor) throws CloneNotSupportedException{
        if ((layer >= getLevel()) && (tag.equals(_test))) {
            WaitTask wtask;
            ActorTask atask;
            String name=((Integer)this._predstack.hashCode()).toString().concat("T1-4.prActor");
            Actor actor=(actors.get(name));// preds的 actor
            // 在 tlist 中添加需要等待匹配的任务模型
            wtask=new WaitTask(layer,false,tag);
            curactor.addWTask(wtask);

            if(actor == null){// 若谓词 actor 还没有创建 --> _predstack 一定为空
                _q3.setLevel(layer + 1);
                actor =actorManager.createAndStartActor(MyStateActor.class, name);
                actors.put(actor.getName(), actor);

                atask=new ActorTask(this._predstack);
                dmessage=new DefaultMessage("stack", atask);
                actorManager.send(dmessage, curactor, actor);
                //发送 q' 给 prActor
                atask=new ActorTask(layer,_q3);
                dmessage=new DefaultMessage("pushTask", atask);
                actorManager.send(dmessage,curactor,actor);
            }
            else{  // 若谓词 actor 已经创建了,则发送 q' 给 prActor即可
                State currQ=(State)_q3.copy();
                currQ.setLevel(layer + 1);
                atask=new ActorTask(layer,_q3);
                dmessage=new DefaultMessage("pushTask",atask);
                actorManager.send(dmessage, curactor, actor);
            }
            // 还要设置一下谓词actor接收消息的subject
            //actor.willReceive("startE");
            //actor.peekNext("startE",isX1(getLevel()+1));// 谓词actor接受的标签应该是当前标签的 child
        }
    }

    @Override
    public void endElementDo(String tag,int layer,MyStateActor curactor) {
        if (tag.equals(_test)) {// 遇到自己的结束标签，检查
            this.processSelfEndTag(layer,curactor);
        }else if (layer == getLevel() - 1) { // 遇到上层结束标签(肯定作为后续path)
            // (能遇到上层结束标签，即T1-4作为一个后续的path（T1-5 的时候也会放在stackActor中），T1-6~T1-8会被放在paActor中)
            // T1-5 时，与T1-5 放在同一个栈，T1-6~T1-8 放在pathstack中
            curactor.popFunction();   // T1-4弹栈
            Stack currstack=curactor.getMyStack();
            if(currstack.isEmpty()) {   // 弹完之后当前actor 所在的stack 为空了，则删除当前 actor
                actorManager.detachActor(curactor); // remove( T1-4.prActor )
            }else{                      // T1-4 作为 T1-5 的后续 path
                State state =(State)((ActorTask)(currstack.peek())).getObject();
                if(state instanceof StateT1_5){
                    state.endElementDo(tag,layer,curactor);
                }
            }
        }
    }
}


