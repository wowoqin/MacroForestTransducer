package com.rules;

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
    // 如：q（State）、qName（String）、stack（Stack）、q'的返回结果（True/False）、q''的返回结果（String）
    protected Object object;


    public ActorTask(Object object) {// 在只发送一个 “stack” 时会运用此构造函数
        this.object = object;
    }

    public ActorTask(int id, Object object) {
        this.id = id;
        this.object = object;
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

    public void setObject(Object object) {
        this.object = object;
    }


}
