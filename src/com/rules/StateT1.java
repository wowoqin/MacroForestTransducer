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
        List list=curactor.getTlist();//��ǰactor��list

        for(int i=0;i<list.size();i++){
            wtask = (WaitTask)list.get(i);
            if (wtask.getId()==layer) {//�ҵ�id==layer�� wt
                if (wtask.isSatisfied()) {//��ǰ wt �����������
                    if(curactor.getName().equals("stackActor")){//��stack��==>PC��
                        if(currstack.size()==1){//���
                            curactor.output(wtask);//Ҳ���ж���������ʱ��return��
                        }else {//��stack�� && ��ΪT1-5�ĺ���path
                            //���(wt.id��wt.getPathR())���Լ����list��id=wt.id�� wt1
                            for(int j=0;j<i;j++){
                                WaitTask wtask1 = (WaitTask) list.get(j);
                                if(wtask1.getId()==id){//�ҵ���ͬid�� wt���ѽ������ wt
                                    atask=new ActorTask(id,wtask.getPathR());
                                    dmessage=new DefaultMessage("paResult",atask);
                                    actorManager.send(dmessage,curactor,curactor);
                                    //��ʱ��Ҫ����ѭ��������ΪҲ���T1-5�����ж�����ϵ�T1-1
                                    // /a/b : ��a�����ж��b���ҵ����һ��ʱ��ǰ���Ѿ��кܶ����ͬid��(0,true,b)�ˣ�
                                    //       ���ǵ�id������0�����Դ�ʱ�Ͳ���Ҫ�ٰ�֮ǰ�Ѿ����õ�wt�ٴθ�ֵpathResult��
                                    curactor.removeWTask(wtask);
                                    return;//���������������������Сѭ��j���������˴�ѭ��i��
                                }
                            }
                        }
                    }else { //��ΪAD ����� path ��һ����
                        boolean isFind=false;
                        atask=new ActorTask(id,wtask.getPathR());
                        for(int j=0;j<i;j++){//���Լ���list������ͬid��wt
                            WaitTask wtask1 = (WaitTask) list.get(j);
                            if(wtask1.getId()==id){//�ҵ���==>����stack�� && ��ΪT1-5�ĺ���path
                                dmessage=new DefaultMessage("paResult",atask);
                                actorManager.send(dmessage, curactor, curactor);
                                isFind=true;
                                break;
                            }
                        }
                        if(!isFind){//��T1-6��T1-7��T1-8��pathջ��
                            dmessage=new DefaultMessage("paResult",atask);
                            actorManager.send(dmessage,curactor,curactor.getResActor());
                        }
                        curactor.removeWTask(wtask);
                        return;
                    }
                }
                //���Լ��Ľ�����ǩ�����ܵ�ǰwt�Ƿ����㣬��Ҫɾ��
                curactor.removeWTask(wtask);
            }
            //id!=layer,����һ��ѭ��
        }
    }
}
