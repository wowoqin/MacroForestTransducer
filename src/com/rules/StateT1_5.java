package com.rules;

import com.XPath.PathParser.ASTPath;
import com.taskmodel.ActorTask;
import com.taskmodel.WaitTask;

import java.util.Stack;

/**
 * Created by qin on 2015/10/10.
 */
public class StateT1_5 extends StateT1{
    protected  State  _q1;//检查 后续 path

    protected StateT1_5(ASTPath path,State q1){
        super(path);
        _q1=q1;
    }

    public static State TranslateState(ASTPath path){//重新创建T1-5
        State q1=StateT1.TranslateStateT1(path.getRemainderPath());
        return new StateT1_5(path,q1);
    }

    public void startElementDo(String tag,int layer,MyStateActor curactor) throws CloneNotSupportedException {
        if((getLevel() == layer) && (tag.equals(_test))) {//应该匹配的层数 getLevel（）和 当前层数相等
            addWTask(new WaitTask(layer,true,null));
            _q1.setLevel(layer + 1); //q1 检查后续 path，肯定是当前标签的子孙中检查
            curactor.pushTaskDo(new ActorTask(layer,_q1,true));
        }
    }

    public void endElementDo(String tag,int layer,MyStateActor curactor){
        // T1-5能遇到自己的结束标签，则T1-5.q1 已经弹栈了，而T1-5.q1 弹栈则说明 q1 已经检查完了
        if (tag.equals(_test)) { //遇到自己的结束标签，检查自己的list中的每个wt -->输出/上传/remove
            for(int i=0;i<getList().size();i++){
                WaitTask wtask=(WaitTask) getList().get(i);
                curactor.doNext(wtask);
            }
        }else if (layer == getLevel() - 1) { // 遇到上层结束标签
            // (能遇到上层结束标签，即T1-5作为一个后续的path（T1-5 的时候也会放在stackActor中），T1-6~T1-8会被放在paActor中)
            // T1-5 时，与T1-5 放在同一个栈，T1-6~T1-8 放在pathstack中
            curactor.popFunction();   // T1-5弹栈
            Stack ss=curactor.getMyStack();
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
