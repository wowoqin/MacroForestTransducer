package com.rules;

import com.XPath.PathParser.ASTPath;
import com.XPath.PathParser.AxisType;
import com.ibm.actor.DefaultMessage;
import com.taskmodel.ActorTask;
import com.taskmodel.WaitTask;

import java.util.LinkedList;
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
    public String getNodeTest() {//�õ���ǰ XPath �Ĳ��Խڵ�
        return _path.getFirstStep().getNodeTest().toString();
    }

    public static State TranslateStateT1(ASTPath path) {
        //���������͡�ʣ��pathѡ���Եĵ�����Ӧ��T1����
        if (path.getFirstStep().getAxisType() == AxisType.PC) {//PC ��
            if (path.getRemainderPath().toString().equals("")){ //�޺���·��
                if (path.getFirstStep().getPreds().toString().equals("")){//��ν��
                    return StateT1_1.TranslateState(path);     //T1_1
                }
                else{
                    return StateT1_2.TranslateState(path);//��ν��
                }
            }
            else {
                if (path.getFirstStep().getPreds().toString().equals("")){//�к���·������ν��
                    return StateT1_5.TranslateState(path);
                }
                else{
                    return StateT1_6.TranslateState(path);//��ν��
                }
            }
        }
        //AD ��
        else{
            if (path.getRemainderPath().toString().equals("")){//�޺���·��
                if (path.getFirstStep().getPreds().toString().equals("")) //��ν��
                    return StateT1_3.TranslateState(path);
                else return StateT1_4.TranslateState(path);//��ν��
            }
            else {
                if (path.getFirstStep().getPreds().toString().equals(""))//�к���·������ν��
                    return StateT1_7.TranslateState(path);
                else return StateT1_8.TranslateState(path);//��ν��
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
        List list=curactor.getTlist();//��ǰactor��list
        for(int i=list.size()-1;i>=0;i--){
            WaitTask wtask = (WaitTask)list.get(i);
            if (wtask.getId()==layer){//�ҵ�id==layer�� wt
                if(wtask.hasReturned()){
                    curactor.doNext(wtask);
                }else{//���Լ��Ľ�����ǩ,����actor��û���꣬��ǰactroӦ�õ�����actor�������жϣ�
                    actorManager.awaitMessage(curactor);
                    while (wtask.hasReturned()) {
                        curactor.doNext(wtask);
                    }
                }
            }else if(wtask.getId()<layer){
                return;
            }
            //id>layer,����һ��ѭ��
        }
    }

}
