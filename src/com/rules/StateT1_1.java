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

    public static State TranslateState(ASTPath path) {//���´���T1-1
        return new StateT1_1(path);
    }

    @Override
    public void startElementDo(String tag, int layer, MyStateActor curactor) {
        WaitTask wtask;
        if ((getLevel() == layer) && (tag.equals(_test))) {//Ӧ��ƥ��Ĳ���-->getLayer������ ��ǰ��ǩ-->tag �Ĳ������
            // �� tlist �������Ҫ�ȴ�ƥ�������ģ��
            wtask=new WaitTask(layer,true,tag);
            curactor.addWTask(wtask);
        }
    }

    @Override
    public void endElementDo(String tag,int layer,MyStateActor curactor){
        if(tag.equals(_test)) {// �����Լ��Ľ�����ǩ�����
            this.processSelfEndTag(layer,curactor);
        }else if (layer == getLevel() - 1) { // �����ϲ������ǩ
            // (�������ϲ������ǩ����T1-1��Ϊһ��������path��T1-5 ��ʱ��Ҳ�����stackActor�У���T1-6~T1-8�ᱻ����paActor��)
            // T1-5 ʱ����T1-5 ����ͬһ��ջ��T1-6~T1-8 ����pathstack
            curactor.popFunction();   // T1-1��ջ
            Stack ss=curactor.getMyStack();
            if(!ss.isEmpty()){
                ((State)((ActorTask)ss.peek()).getObject()).endElementDo(tag,layer,curactor);
            }else{
                actorManager.detachActor(curactor);
            }

        }
    }



}

