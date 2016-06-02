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

    public static State TranslateState(ASTPath path) {//���´���T1-3
        return new StateT1_3(path);
    }

    public void startElementDo(String tag, int layer, MyStateActor curactor) throws CloneNotSupportedException {
        if ((layer >= getLevel()) && (tag.equals(_test))) {//��ǰ�������ڵ���Ӧ��ƥ��Ĳ��� getLayer�����Ϳ���
            addWTask(new WaitTask(layer,true,tag));
        }
    }


    @Override
    public void endElementDo(String tag, int layer, MyStateActor curactor) {
        if (tag.equals(_test)) {// �����Լ��Ľ�����ǩ�����
            this.processSelfEndTag(layer, curactor);
        } else if (layer == getLevel() - 1) { // �����ϲ������ǩ
            // (�������ϲ������ǩ����T1-3��Ϊһ��������path��T1-5 ��ʱ��Ҳ�����stackActor�У���T1-6~T1-8�ᱻ����paActor��)
            // T1-5 ʱ����T1-5 ����ͬһ��ջ��T1-6~T1-8 ����pathstack��
            curactor.popFunction();   // T1-3��ջ
            Stack ss = curactor.getMyStack();
            if (!ss.isEmpty()) {
                State state = (State) ((ActorTask) (ss.peek())).getObject();
                if (state instanceof StateT1_5) {
                    state.endElementDo(tag, layer, curactor);
                }
            } else {
                actorManager.detachActor(curactor);
            }
        }
    }
}