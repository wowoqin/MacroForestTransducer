package com.rules;

import com.ibm.actor.Actor;
import com.ibm.actor.DefaultActorManager;
import com.ibm.actor.DefaultMessage;

import java.util.*;

/**
 * Created by qin on 2015/10/10.
 */
public  abstract class State  implements Cloneable {
    public static List outlist=new ArrayList();            //�������Ҫ����ı�ǩ
    protected int level=0;                                 //��ǰӦ��ƥ��ı�ǩ�Ĳ���

    public static DefaultActorManager actorManager=DefaultActorManager.getDefaultInstance();
    public static Map<String,Actor> actors=new HashMap<String, Actor>();                   // ���е�actor�� map < actorName,actor >
    public DefaultMessage dmessage;                                            // �м����ɵ���Ϣ

    public  abstract void startElementDo(String tag,int layer,MyStateActor curactor) throws CloneNotSupportedException;
    public  abstract void endElementDo(String tag,int layer,MyStateActor curactor);
    public  abstract String getNodeTest();

    public int getLevel() { return level; }
    public void setLevel(int level) {
        this.level=level;
    }

    public boolean isX1(int layer){
        if(layer==this.getLevel()+1)
            return true;
        else
            return false;
    }
    public boolean isX2(int layer){
        if(layer==this.getLevel())
            return true;
        else
            return false;
    }
    public boolean isPreEnd(int layer){
        if(layer==this.getLevel()-1)
            return true;
        else
            return false;
    }
    public abstract Object copy() throws CloneNotSupportedException;
}