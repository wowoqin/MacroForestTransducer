package com.rules;

import com.XPath.PathParser.ASTPath;
import com.ibm.actor.Actor;
import com.ibm.actor.DefaultMessage;
import com.taskmodel.ActorTask;
import com.taskmodel.WaitTask;

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

    public  void startElementDo(String tag,int layer,MyStateActor curactor) throws CloneNotSupportedException{
        if ((layer >= getLevel()) && (tag.equals(_test))) {
            // 在 list 中添加需要等待匹配的任务模型
            addWTask(new WaitTask(layer, null, tag));
            String name = ((Integer) this._predstack.hashCode()).toString().concat("T1-4.prActor");

            if(this._predstack.isEmpty()) {// 若predstack 为空
                Actor actor=actorManager.createAndStartActor(MyStateActor.class, name);
                actors.put(name,actor);
                _q3.setLevel(layer + 1);
                dmessage=new DefaultMessage("resActor&&pushTask",
                        new Object[]{this._predstack,new ActorTask(layer, _q3, false)});
                actorManager.send(dmessage, curactor, actor);
            }else{  // 若谓词 actor 已经创建了,则发送 q' 给 prActor即可
                Actor actor=actors.get(name);
                actor.peekNext("pushTask");
                State currQ=(State) _q3.copy();
                currQ.setLevel(layer + 1);
                dmessage=new DefaultMessage("pushTask",new ActorTask(layer,currQ,false));
                actorManager.send(dmessage,curactor,actor);
            }
        }
    }

    @Override
    public void endElementDo(String tag,int layer,MyStateActor curactor) {
        if (tag.equals(_test)) {//遇到自己的结束标签，检查自己的list中的最后一个 wt -->输出/remove
            //T1-6.path时，谓词未检查成功就传不过去，T1-4.list.size>=1;
            List list=getList();
            WaitTask wtask = (WaitTask) getList().get(list.size()-1);
            if(wtask.hasReturned()){
                curactor.doNext(wtask);
            }else{//等待--谓词检查的消息已经发出去了，但是或许还没接收到，或许接收到了还没设置完成
                do{
                    try {
                        Thread.currentThread().sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }while(!wtask.hasReturned());
                curactor.doNext(wtask);
            }
        }else if (layer == getLevel() - 1) { // 遇到上层结束标签
            // (能遇到上层结束标签，即T1-2作为一个后续的path（T1-5 的时候也会放在stackActor中），T1-6~T1-8会被放在paActor中)
            // T1-5 时，与T1-5 放在同一个栈，T1-6~T1-8 放在pathstack中
            Stack ss=curactor.getMyStack();
            List list=getList();
            ActorTask task=(ActorTask)ss.peek();//(id,T1-3,isInSelf)
            int id=task.getId(); // 当前栈顶 taskmodel 的 id
            boolean isInSelf=task.isInSelf();
            if(!list.isEmpty()){   //上传
                WaitTask wt=(WaitTask)getList().get(0);
                for(int i=0;i<list.size();i++){//多个满足的标签,
                    curactor.sendPathResult(new ActorTask(id,wt.getPathR(),isInSelf));
                }
            }
            //pop(T1-4)
            curactor.popFunction();   // T1-4弹栈
            if(ss.isEmpty()) {   // 弹完之后当前actor 所在的stack 为空了，则删除当前 actor
                actorManager.detachActor(curactor);
            }else{                      // T1-4 作为 T1-5 的后续 path
                task=(ActorTask)(ss.peek());
                State currstate =(State)task.getObject();
                if(currstate instanceof StateT1_5){
                    currstate.endElementDo(tag,layer,curactor);
                }else if(currstate instanceof StateT1_3){
                    //T1-4作为AD轴test的后续path，即T1-7/T1-8
                    curactor.processSameADPath(currstate,list);
                }
            }
        }
    }
}


