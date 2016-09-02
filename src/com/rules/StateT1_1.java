package com.rules;

import com.XPath.PathParser.ASTPath;
import com.ibm.actor.DefaultMessage;
import com.taskmodel.ActorTask;
import com.taskmodel.WaitTask;

import java.util.List;
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

    public void startElementDo(String tag, int layer, MyStateActor curactor) {
        if ((getLevel() == layer) && (tag.equals(_test))) {//应该匹配的层数-->getLayer（）和 当前标签-->tag 的层数相等
            // 在 list 中添加需要检查成功的任务模型
            addWTask(new WaitTask(layer, true, tag));
        }
    }

    public void endElementDo(String tag,int layer,MyStateActor curactor){
        // T1-1 不需要等待
        if(tag.equals(_test)){//遇到自己的结束标签，检查自己的list中的 wt -->输出/remove
            System.out.println("T1-1遇到自己结束标签-->doNext");
            List list=getList();
            curactor.doNext((WaitTask) list.get(0));
        }else if (layer == getLevel() - 1) { // 遇到上层结束标签
            // (能遇到上层结束标签，即T1-1作为一个后续的path（T1-5 的时候也会放在stackActor中），T1-6~T1-8会被放在paActor中)
            // T1-5 时，与T1-5 放在同一个栈，T1-6~T1-8 放在pathstack
            Stack ss=curactor.getMyStack();
            ActorTask task=(ActorTask)ss.peek();//(id,T1-1,isInSelf)
            List list=getList();
            if(!list.isEmpty()){  //上传T1-1.test
                System.out.println("T1-1遇到上层结束标签-->传递结果");
                WaitTask wt=(WaitTask)list.get(0);
                for(int i=0;i<list.size();i++){//也许会有多个满足的标签, /a/b  或者 //a/b-->都代表a的所有b孩子
                    //都上传
                    boolean isInself=task.isInSelf();
                    if(isInself){//T1-5的后续path，则优先处理path的返回结果，而不是T1-5 的结束标签
                        System.out.println("T1-1作为 T1-5 的 path-->T1-5应该先处理 pathR");
                        curactor.peekNext("pathResult");
                    }
                    else{
                        State state =(State)((ActorTask)
                                (((MyStateActor)(curactor.getResActor())).getMyStack().peek())).getObject();
                        if (state instanceof StateT1_7){
                            System.out.println("T1-1作为 T1-7 的 path--> T1-7应该先处理 pathR");
                            curactor.peekNext("pathResult");
                        }
                    }
                    curactor.sendPathResults(new ActorTask(task.getId(), wt.getPathR(), isInself));
                }
            }
            //pop(T1-1)
            curactor.popFunction();   // T1-1弹栈
            if(ss.isEmpty()) {   // 弹完之后当前actor 所在的stack 为空了，则删除当前 actor
                actorManager.detachActor(curactor);
            }else{                      // T1-1 作为 T1-5 的后续 path
                State state =(State)((ActorTask)(ss.peek())).getObject();
                if(state instanceof StateT1_5){
                    state.endElementDo(tag,layer,curactor);
                }
            }
        }
    }



}

