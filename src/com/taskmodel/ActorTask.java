package com.taskmodel;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by qin on 2016/3/28.
 *
 */
public class ActorTask {// actor 之间交互的数据
    /*
    * 返回的检查结果（ActorTask）的 id 与 tlist 中的等待任务模型（WaitTask）的id 相匹配
    * */
    protected int id;//id
    // 发送给 actor的数据，
    // 如：q（State）、qName（String）、q'的返回结果（True/False）、q''的返回结果（String）
    protected Object object;
    protected boolean isInSelf;//标识检查结果传给自己还是传给上级actor

    protected List list;

    public ActorTask(int id, Object object) {//actor之间传的消息(id,qName)
        this.id = id;
        this.object = object;
    }

    public ActorTask(int id, Object object, boolean flg) {//栈内元素(id,q,isInSelf)、（id,true/tag,isInself)
        this.id = id;
        this.object = object;
        this.isInSelf = flg;
    }

    public int getId() {
        return id;
    }

    public Object getObject() {
        return object;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List getList() {
        return list;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public boolean isInSelf() {
        return isInSelf;
    }

    public void setInSelf(boolean inSelf) {
        this.isInSelf = inSelf;
    }

    public void addWTask(WaitTask wtask){
        if(list==null){
            list=new LinkedList();
            getList().add(wtask);
        }else
            getList().add(wtask);
    }
}
