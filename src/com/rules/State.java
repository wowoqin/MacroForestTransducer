package com.rules;

import com.ibm.actor.Actor;
import com.ibm.actor.DefaultActorManager;
import com.ibm.actor.DefaultMessage;

import java.util.*;

/**
 * Created by qin on 2015/10/10.
 */
public  abstract class State  implements Cloneable {
    protected int level=0;                                 //当前应该匹配的标签的层数
    public DefaultMessage dmessage;                       // 中间生成的消息
    public static DefaultActorManager actorManager=DefaultActorManager.getDefaultInstance();
    public static Map<String,Actor> actors=new HashMap<String, Actor>();// 所有的actor的 map < actorName,actor >

    public  abstract void startElementDo(String tag,int layer,MyStateActor curactor) throws CloneNotSupportedException;
    public  abstract void endElementDo(String tag,int layer,MyStateActor curactor);
    public abstract String getNodeTest();

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
