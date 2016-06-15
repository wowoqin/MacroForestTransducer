package com.rules;

import com.XPath.PathParser.ASTPath;
import com.ibm.actor.DefaultMessage;
import com.taskmodel.ActorTask;
import com.taskmodel.WaitTask;

import java.util.Stack;

/**
 * Created by qin on 2015/10/10.
 */
public class StateT1_2 extends StateT1 {
    protected State _q3;//检查 preds

    protected StateT1_2(ASTPath path, State q3) {
        super(path);
        _q3 = q3;
    }

    public static State TranslateState(ASTPath path) {//重新创建T1-2
        State q3 = StateT3.TranslateStateT3(path.getFirstStep().getPreds());
        return new StateT1_2(path, q3);
    }

    public void startElementDo(String tag, int layer,MyStateActor curactor) throws CloneNotSupportedException {// layer 表示当前标签 tag 的层数
        if((getLevel() == layer) && (tag.equals(_test))) {//应该匹配的层数 getLayer（）和 当前标签 tag 的层数相等
            addWTask(new WaitTask(layer, null, tag));
            _q3.setLevel(layer + 1);//检查的谓词的层数肯定是当前应该匹配层数所对应的标签的子孙的层数
            curactor.pushTaskDo(new ActorTask(layer, _q3, true));
        }
    }

    public void endElementDo(String tag,int layer,MyStateActor curactor){
        if (tag.equals(_test)) {// 遇到自己的结束标签，检查
            //T1-6.path时，谓词未检查成功就传不过去，T1-2.list.size>=1;
            for(int i=0;i<getList().size();i++){
                WaitTask wtask=(WaitTask) getList().get(i);
                if(wtask.hasReturned()){
                    curactor.doNext(wtask);
                }else{//等待--谓词是弹栈了，但谓词检查的消息已经发出去了，但是或许还没接收到，或许接收到了还没设置完成
                    //当前结束标签先不处理
                    curactor.addMessage(new DefaultMessage("endE", new ActorTask(layer,tag)));
                    curactor.peekNext("predR");//优先处理谓词返回结果的消息
                    while(wtask.hasReturned())
                        curactor.doNext(wtask);
                }
            }
        }else if (layer == getLevel() - 1) { // 遇到上层结束标签
            // (能遇到上层结束标签，即T1-2作为一个后续的path（T1-5 的时候也会放在stackActor中），T1-6~T1-8会被放在paActor中)
            // T1-5 时，与T1-5 放在同一个栈，T1-6~T1-8 放在pathstack中
            Stack ss=curactor.getMyStack();
            if(!getList().isEmpty()){   //T1-2作为T1-6的后续path && T1-6的谓词后检查完成(弹栈)
                //则此时是遇到的T1-6的结束标签（先传，后pop）
                ActorTask task=(ActorTask)ss.peek();//(id,T1-1,isInSelf)
                int id=task.getId(); // 当前栈顶 taskmodel 的 id
                boolean isInSelf=task.isInSelf();
                WaitTask wt=(WaitTask)getList().get(0);
                for(int i=0;i<list.size();i++){//多个满足的标签,
                    curactor.sendPathResult(new ActorTask(id,wt.getPathR(),isInSelf));
                }
            }
            //pop(T1-2)
            curactor.popFunction();   // T1-2弹栈
            if(ss.isEmpty()) {   // 弹完之后当前actor 所在的stack 为空了，则删除当前 actor
                actorManager.detachActor(curactor);
            }else{                      // T1-2 作为 T1-5 的后续 path
                State state =(State)((ActorTask)(ss.peek())).getObject();
                if(state instanceof StateT1_5){
                    state.endElementDo(tag,layer,curactor);
                }
            }
        }
    }
}
