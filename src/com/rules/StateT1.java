package com.rules;

import com.XPath.PathParser.ASTPath;
import com.XPath.PathParser.AxisType;
import com.ibm.actor.AbstractActor;
import com.ibm.actor.Actor;
import com.ibm.actor.DefaultMessage;
import com.ibm.actor.Message;

import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

/**
 * Created by qin on 2015/10/10.
 */
public class StateT1 extends State implements Cloneable {

    protected  ASTPath _path;
    protected  String  _test;

    protected Stack _predstack;
    protected Stack _pathstack;



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
        WaitTask wtask;
        ActorTask atask;
        Stack currstack=curactor.getMyStack();
        int id=((ActorTask)currstack.peek()).getId(); // ��ǰջ�� task �� id
        String name=curactor.getName();
        List list=curactor.getTlist();

        for(int i=0;i<list.size();i++){
            wtask = (WaitTask)list.get(i);
            if (wtask.getId()==layer) {
                if (wtask.isSatisfied()) {
                    if(name.equals("stackActor")){//��stack��-==>PC��
                        if(currstack.size()==1){//���
                            curactor.output(wtask);
                        }else {//��stack�� && ��ΪT1-5�ĺ���path
                            for(int j=0;j<i;j++){
                                wtask = (WaitTask) list.get(j);
                                if(wtask.getId()==id){
                                    atask=new ActorTask(id,wtask.getPathR());
                                    dmessage=new DefaultMessage("paResult",atask);
                                    actorManager.send(dmessage,curactor,curactor);
                                }
                            }
                        }
                    }else {//��ΪAD �����path��һ����
                        for(int j=0;j<i;j++){
                            wtask = (WaitTask) list.get(j);
                            atask=new ActorTask(id,wtask.getPathR());
                            if(wtask.getId()==id){//����stack�� && ��ΪT1-5�ĺ���path
                                dmessage=new DefaultMessage("paResult",atask);
                                actorManager.send(dmessage,curactor,curactor);
                            }else{//��T1-6��T1-7��T1-8��pathջ��
                                dmessage=new DefaultMessage("paResult",atask);
                                actorManager.send(dmessage,curactor,curactor.getResActor());
                            }
                        }
                    }
                }
                //���Լ��Ľ�����ǩ�����ܵ�ǰwt�Ƿ����㣬��Ҫɾ��
                curactor.removeWTask(wtask);
            }
        }
    }
}
