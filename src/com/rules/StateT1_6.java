package com.rules;

import com.XPath.PathParser.ASTPath;
import com.XPath.PathParser.ASTPreds;
import com.ibm.actor.Actor;
import com.ibm.actor.DefaultMessage;

import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

/**
 * Created by qin on 2015/10/10.
 */
public class StateT1_6 extends StateT1{
    protected  State  _q3;//检查 preds
    protected  State  _q1;//检查后续 path
    protected StateT1_6(ASTPath path,State q3,State q1){
        super(path);
        _q3=q3;
        _q1=q1;
        _pathstack =new Stack();
    }

    public static State TranslateState(ASTPath path){//重新创建T1-6
        State q3=StateT3.TranslateStateT3(path.getFirstStep().getPreds());
        State q1=StateT1.TranslateStateT1(path.getRemainderPath());
        return new StateT1_6(path,q3,q1);//然后压入栈
    }
    @Override
    public void startElementDo(String tag,int layer,MyStateActor curactor) throws CloneNotSupportedException {// layer 是当前 tag 的层数
        if((getLevel() == layer)  && (tag.equals(_test))){//应该匹配的层数 getLevel（）和 当前层数相等
            Stack currStack=curactor.getMyStack();
            String name=((Integer)this._pathstack.hashCode()).toString().concat("T1-6.paActor");
            Actor actor=(actors.get(name));// path的 actor
            // 在 tlist 中添加需要等待匹配的任务模型
            curactor.addWTask(new WaitTask(layer,false,null));

            _q3.setLevel(getLevel() + 1);
            _q1.setLevel(getLevel() + 1);

            currStack.push(new ActorTask(layer, _q3));

            if(actor == null){  // 若pathActor 还没有创建 --> _pathstack 一定为空
                actor =actorManager.createAndStartActor(MyStateActor.class, name);
                actors.put(actor.getName(), actor);

                dmessage=new DefaultMessage("stack", new ActorTask(this._pathstack));
                actorManager.send(dmessage, curactor, actor);

                //发送 q'' 给 paActor
                dmessage=new DefaultMessage("pushTask", new ActorTask(layer,_q1));
                actorManager.send(dmessage,curactor,actor);
            } else{  // 若path  actor 已经创建了,则发送 q'' 给 paActor即可
                //发送 q'' 给 paActor
                State currQ=(State)_q1.copy();
                currQ.setLevel(layer + 1);
                dmessage=new DefaultMessage("pushTask", new ActorTask(layer,currQ));
                actorManager.send(dmessage, curactor, actor);
            }
        }
    }

    public void endElementDo(String tag,int layer,MyStateActor curactor){
        if (tag.equals(_test)) {// 遇到自己的结束标签，检查
            this.processSelfEndTag(layer,curactor);
        }else if (layer == getLevel() - 1) { // 遇到上层结束标签(肯定是作为后续path)
            // (能遇到上层结束标签，即T1-6作为一个后续的path（T1-5 的时候也会放在stackActor中），T1-6~T1-8会被放在paActor中)
            // T1-5 的后续的path时，与T1-5 放在同一个栈，T1-6~T1-8 放在pathstack中
            curactor.popFunction();   // T1-6弹栈
            Stack ss=curactor.getMyStack();
            if(ss.isEmpty()) {   // 弹完之后当前actor 所在的stack 为空了，则删除当前 actor
                actorManager.detachActor(curactor);
            }else{                      // T1-6 作为 T1-5 的后续 path
                State state =(State)((ActorTask)(ss.peek())).getObject();
                if(state instanceof StateT1_5){
                    state.endElementDo(tag,layer,curactor);
                }
            }
        }
    }
}
