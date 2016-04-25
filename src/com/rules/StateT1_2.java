package com.rules;

import com.XPath.PathParser.ASTPath;
import com.ibm.actor.DefaultMessage;

import java.util.List;
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

    public void startElementDo(String tag, int layer,MyStateActor curactor) {// layer 表示当前标签 tag 的层数
        if((getLevel() == layer) && (tag.equals(_test))) {//应该匹配的层数 getLayer（）和 当前标签 tag 的层数相等
            WaitTask wtask = new WaitTask(layer,false,tag);
            curactor.addWTask(wtask);

            _q3.setLevel(getLevel() + 1);//检查的谓词的层数肯定是当前应该匹配层数所对应的标签的子孙的层数
            ActorTask atask=new ActorTask(layer,_q3);
            curactor.getMyStack().push(atask);

        }
    }

    @Override
    public void endElementDo(String tag,int layer,MyStateActor curactor){
        if (tag.equals(_test)) {// 遇到自己的结束标签，检查
            this.processSelfEndTag(layer,curactor);
        }else if (layer == getLevel() - 1) { // 遇到上层结束标签
            // (能遇到上层结束标签，即T1-1作为一个后续的path（T1-5 的时候也会放在stackActor中），T1-6~T1-8会被放在paActor中)
            // T1-5 时，与T1-5 放在同一个栈，T1-6~T1-8 放在pathstack中
            curactor.popFunction();   // T1-2弹栈
            Stack ss=curactor.getMyStack();
            if(!ss.isEmpty()){
                ((State)((ActorTask)ss.peek()).getObject()).endElementDo(tag,layer,curactor);
            }else{
                actorManager.detachActor(curactor);
            }
        }
    }
}
