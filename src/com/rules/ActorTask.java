package com.rules;

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
    // �磺q��State����qName��String����stack��Stack����q'�ķ��ؽ����True/False����q''�ķ��ؽ����String��
    protected Object object;


    public ActorTask(Object object) {// ��ֻ����һ�� ��stack�� ʱ�����ô˹��캯��
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
