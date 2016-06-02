package com.taskmodel;

/**
 * Created by qin on 2016/3/28.
 *
 */
public class ActorTask {// actor ֮�佻��������
    /*
    * ���صļ������ActorTask���� id �� tlist �еĵȴ�����ģ�ͣ�WaitTask����id ��ƥ��
    * */

    protected int id;//id
    // ���͸� actor�����ݣ�
    // �磺q��State����qName��String����q'�ķ��ؽ����True/False����q''�ķ��ؽ����String��
    protected Object object;
    protected boolean isInSelf;//��ʶ����������Լ����Ǵ����ϼ�actor

    public ActorTask(int id, Object object) {//actor֮�䴫����Ϣ(id,qName)
        this.id = id;
        this.object = object;
    }

    public ActorTask(int id, Object object, boolean flg) {//ջ��Ԫ��(id,q,isInSelf)����id,true/tag,isInself)
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

    public void setObject(Object object) {
        this.object = object;
    }

    public boolean isInSelf() {
        return isInSelf;
    }

    public void setInSelf(boolean inSelf) {
        this.isInSelf = inSelf;
    }
}
