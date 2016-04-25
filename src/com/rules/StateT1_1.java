package com.rules;

import com.XPath.PathParser.ASTPath;
import com.ibm.actor.Actor;
import com.ibm.actor.DefaultMessage;

import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

/**
 * Created by qin on 2015/10/10.
 */
public class StateT1_1 extends StateT1 {

    protected StateT1_1(ASTPath path) {
        super(path);
    }

    public static State TranslateState(ASTPath path) {//重新创建T1-1
        return new StateT1_1(path);
    }

    @Override
    public void startElementDo(String tag, int layer, MyStateActor curactor) {
        WaitTask wtask;
        if ((getLevel() == layer) && (tag.equals(_test))) {//应该匹配的层数-->getLayer（）和 当前标签-->tag 的层数相等
            // 在 tlist 中添加需要等待匹配的任务模型
            wtask=new WaitTask(layer,true,tag);
            curactor.addWTask(wtask);
        }
    }

    @Override
    public void endElementDo(String tag,int layer,MyStateActor curactor){
        if(tag.equals(_test)) {// 遇到自己的结束标签，检查
            this.processSelfEndTag(layer,curactor);
        }else if (layer == getLevel() - 1) { // 遇到上层结束标签
            // (能遇到上层结束标签，即T1-1作为一个后续的path（T1-5 的时候也会放在stackActor中），T1-6~T1-8会被放在paActor中)
            // T1-5 时，与T1-5 放在同一个栈，T1-6~T1-8 放在pathstack
            curactor.popFunction();   // T1-1弹栈
            Stack ss=curactor.getMyStack();
            if(!ss.isEmpty()){
                ((State)((ActorTask)ss.peek()).getObject()).endElementDo(tag,layer,curactor);
            }else{
                actorManager.detachActor(curactor);
            }

        }
    }



}

