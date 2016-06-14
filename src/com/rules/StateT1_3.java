package com.rules;

import com.XPath.PathParser.ASTPath;
import com.taskmodel.ActorTask;
import com.taskmodel.WaitTask;

import java.util.Stack;

/**
 * Created by qin on 2015/10/10.
 */
public class StateT1_3 extends StateT1 implements Cloneable {

    protected StateT1_3(ASTPath path) {
        super(path);
    }

    public static State TranslateState(ASTPath path) {//重新创建T1-3
        return new StateT1_3(path);
    }

    public void startElementDo(String tag, int layer, MyStateActor curactor) throws CloneNotSupportedException {
        if ((layer >= getLevel()) && (tag.equals(_test))) {//当前层数大于等于应该匹配的层数 getLayer（）就可以
            addWTask(new WaitTask(layer,true,tag));
        }
    }


    @Override
    public void endElementDo(String tag, int layer, MyStateActor curactor) {
        // T1-3 不需要等待 && T1-3.list.size>=1
        if(tag.equals(_test)){//遇到自己的结束标签，检查自己的list中的最后一个 wt -->输出/上传/remove
            //本来上传list中的最后一个 wt即可，但
            //T1-6.path时，谓词未检查成功就传不过去，T1-3.list.size>=1;
            for(int i=(getList().size()-1);i>=0;i--){
                WaitTask wtask=(WaitTask) getList().get(i);
                if(wtask.getId()>=layer){//只上传/输出当前layer及其layer下的福和的标签
                    curactor.doNext(wtask);
                }else return;
            }
        }else if (layer == getLevel() - 1) { // 遇到上层结束标签
            // (能遇到上层结束标签，即T1-3作为一个后续的path（T1-5 的时候也会放在stackActor中），T1-6~T1-8会被放在paActor中)
            // T1-5 时，与T1-5 放在同一个栈，T1-6~T1-8 放在pathstack
            Stack ss=curactor.getMyStack();
            if(!getList().isEmpty()){   //T1-3作为T1-6的后续path && T1-6的谓词后检查完成(弹栈)
                //则此时是遇到的T1-6的结束标签（先传，后pop）
                ActorTask task=(ActorTask)ss.peek();//(id,T1-3,isInSelf)
                int id=task.getId(); // 当前栈顶 taskmodel 的 id
                boolean isInSelf=task.isInSelf();
                WaitTask wt=(WaitTask)getList().get(0);
                for(int i=0;i<list.size();i++){//多个满足的标签,
                    curactor.sendPathResult(new ActorTask(id,wt.getPathR(),isInSelf));
                }
            }
            //pop(T1-3)
            curactor.popFunction();   // T1-3弹栈
            if(ss.isEmpty()) {   // 弹完之后当前actor 所在的stack 为空了，则删除当前 actor
                actorManager.detachActor(curactor);
            }else{                      // T1-3 作为 T1-5 的后续 path
                State state =(State)((ActorTask)(ss.peek())).getObject();
                if(state instanceof StateT1_5){
                    state.endElementDo(tag,layer,curactor);
                }
            }
        }
    }
}