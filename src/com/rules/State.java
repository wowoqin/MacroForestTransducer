package com.rules;

import com.ibm.actor.AbstractActor;
import com.ibm.actor.Actor;
import com.ibm.actor.DefaultActorManager;
import com.ibm.actor.DefaultMessage;
import com.taskmodel.WaitTask;

import java.util.*;

/**
 * Created by qin on 2015/10/10.
 */
public  abstract class State  implements Cloneable {
    protected int level=0;                                 //��ǰӦ��ƥ��ı�ǩ�Ĳ���
    public DefaultMessage dmessage;                       // �м����ɵ���Ϣ
    public static DefaultActorManager actorManager=DefaultActorManager.getDefaultInstance();
    public static Map<String,Actor> actors=new HashMap<String, Actor>();
            //new HashMap<String, Actor>();// ���е�actor�� map < actorName,actor >

    protected List list=new LinkedList();//ÿһ�� state ��һ�� list������� wt

    public  abstract void startElementDo(String tag,int layer,MyStateActor curactor) throws CloneNotSupportedException;
    public  abstract void endElementDo(String tag,int layer,MyStateActor curactor);

    public int getLevel() { return level; }
    public void setLevel(int level) {
        this.level=level;
    }

    public List getList() {
        return list;
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

    public void addWTask(WaitTask wtask){
        System.out.println("�� "+this+" �ĵȴ���������ӵȴ�����");
        this.getList().add(wtask);
    }

    public void removeWTask(WaitTask wtask){
        this.getList().remove(wtask);
    }

//    public static Map<String, Actor> getActors() {
//        AbstractActor[] mactors=actorManager.getActors();
//        for(int i=0;i<mactors.length;i++){
//            actors.put(mactors[i].getName(),mactors[i]);
//        }
//        return actors;
//    }

}
