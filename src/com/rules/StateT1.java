package com.rules;

import com.XPath.PathParser.ASTPath;
import com.XPath.PathParser.AxisType;
import com.ibm.actor.DefaultMessage;
import com.taskmodel.ActorTask;
import com.taskmodel.WaitTask;

import java.util.List;
import java.util.Stack;

/**
 * Created by qin on 2015/10/10.
 */
public class StateT1 extends State implements Cloneable {

    protected  ASTPath _path;
    protected  String  _test;
    protected  Stack   _predstack;
    protected  Stack   _pathstack;

    protected StateT1(ASTPath path) {
        _path = path;
        _test=_path.getFirstStep().getNodeTest().toString();
    }

    @Override
    public String getNodeTest() {//得到当前 XPath 的测试节点
        return _path.getFirstStep().getNodeTest().toString();
    }

    public static State TranslateStateT1(ASTPath path) {
        //根据轴类型、剩余path选择性的调用相应的T1规则
        if (path.getFirstStep().getAxisType() == AxisType.PC) {//PC 轴
            if (path.getRemainderPath().toString().equals("")){ //无后续路径
                if (path.getFirstStep().getPreds().toString().equals("")){//无谓词
                    return StateT1_1.TranslateState(path);     //T1_1
                }
                else{
                    return StateT1_2.TranslateState(path);//有谓词
                }
            }
            else {
                if (path.getFirstStep().getPreds().toString().equals("")){//有后续路径，无谓词
                    return StateT1_5.TranslateState(path);
                }
                else{
                    return StateT1_6.TranslateState(path);//有谓词
                }
            }
        }
        //AD 轴
        else{
            if (path.getRemainderPath().toString().equals("")){//无后续路径
                if (path.getFirstStep().getPreds().toString().equals("")) //无谓词
                    return StateT1_3.TranslateState(path);
                else return StateT1_4.TranslateState(path);//有谓词
            }
            else {
                if (path.getFirstStep().getPreds().toString().equals(""))//有后续路径，无谓词
                    return StateT1_7.TranslateState(path);
                else return StateT1_8.TranslateState(path);//有谓词
            }
        }
    }


    public State copy() throws CloneNotSupportedException {
        return (State)this.clone();
    }

    @Override
    public void startElementDo(String tag, int layer,MyStateActor curactor) throws CloneNotSupportedException {}

    @Override
    public void endElementDo(String tag,int layer,MyStateActor curactor){}

    public void processSelfEndTag(int layer,MyStateActor curactor){
        Stack currstack=curactor.getMyStack();
        ActorTask task=(ActorTask)currstack.peek();
        int id=task.getId(); // 当前栈顶 taskmodel 的 id
        boolean isInSelf=task.isInSelf();
        List list=curactor.getTlist();//当前actor的list

        for(int i=list.size()-1;i>=0;i--){
            WaitTask wtask = (WaitTask)list.get(i);
            if (wtask.getId()==layer) {//找到id==layer的 wt
                if (wtask.isSatisfiedOut()) {//当前 wt 满足输出条件
                    if(curactor.getName().equals("stackActor")){//在stack中
                        if(currstack.size()==1){//输出
                            curactor.output(wtask);//也许有多个输出，此时不return；
                            break;
                        }else {//在stack中 && 作为T1-5的后续path
                            //则把(wt.id，wt.getPathR())给自己这个list中id=wt.id的 wt1
                            for(int j=i-1;j>=0;j--){
                                if(((WaitTask) list.get(j)).getId()==id){//找到相同id的 wt，把结果传给 wt
                                    dmessage=new DefaultMessage("pathResult",new ActorTask(id,wtask.getPathR()));
                                    actorManager.send(dmessage,curactor,curactor);
                                    //此时需要跳出循环，是因为也许会T1-5下面有多个符合的T1-1
                                    // /a/b : 若a下面有多个b，遇到/b时，即找到最后一个b时，前面已经有很多的相同id的(0,true,b)了，
                                    //       它们的id都等于0，所以此时就不需要再把之前已经检查好的wt再次赋值pathResult了
                                    curactor.removeWTask(wtask);
                                    return;//跳出这个方法（即跳出了小循环j，又跳出了大循环i）
                                }
                            }
                        }
                    }else { //作为AD 轴后续 path 的一部分
                        if(isInSelf){
                            dmessage=new DefaultMessage("pathResult",new ActorTask(id,wtask.getPathR()));
                            actorManager.send(dmessage, curactor, curactor);
                        }else{
                            dmessage=new DefaultMessage("paResult",new ActorTask(id,wtask.getPathR()));
                            actorManager.send(dmessage,curactor,curactor.getResActor());
                        }
                        curactor.removeWTask(wtask);
                        return;//结束大循环
                    }
                }
                //到自己的结束标签，当前wt不满足输出条件-->应该检查后面的谓词所对应的Actor是否做完了工作，
                // 若做完了，则删除不满足的wt；
                // 若还没做完，则当前actro应该等谓词actor做完再判断；
                curactor.removeWTask(wtask);

            }
            //id!=layer,则下一次循环
        }
    }

}
