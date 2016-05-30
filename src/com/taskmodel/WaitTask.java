package com.taskmodel;

/**
 * Created by qin on 2016/3/28.
 */
public class WaitTask {   // �� actor ��list ����ӵ�����
    protected  int id;          // id
    protected  Boolean predR;   // ν�ʵķ��ؽ��
    protected  String pathR;    // ���� path �ķ��ؽ��������preds'�ķ��ؽ��

    /*
    * �ڴˣ�T1-1 ~ T1-4 : pathR �д�ŵ�һ����ƥ���� --> test
    *      T1-5 ~ T1-8 : pathR �д�ź��� path �ļ����
    *      T2-1 ~ T2-4 : pathR ��ֱ�Ӵ�š�True��
    *      T3-1 ~ T3-4 : pathR �д�� preds'�ļ����
    *
    *      ���صļ������ActorTask���� id �� tlist �еĵȴ�����ģ�ͣ�WaitTask����id ��ƥ��
    * */

    public WaitTask(int id, Boolean predR, String pathR) {
        this.id = id;
        this.predR = predR;
        this.pathR = pathR;
    }

    public boolean getPredR() {
        return predR;
    }

    public int getId() {
        return id;
    }

    public String getPathR() {
        return pathR;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPredR(boolean predR) {
        this.predR = predR;
    }

    public void setPathR(String pathR) {
        this.pathR = pathR;
    }


    public boolean isSatisfiedOut() { // ��鵱ǰ waitTask �ǲ����Ѿ�����������������Խ�����������ˣ�
        if(this.getPathR()!=null){
            if((this.getPredR()) && (!this.getPathR().equals("true")))
                return true;
        }
        return false;
    }

    public  boolean isPredsSatisified(){ //ν�ʼ��ɹ�
        if(this.getPathR()!=null){
            if((this.getPredR()) && (this.getPathR().equals("true")))
                return true;
        }
        return false;
    }

    public  boolean isWaitT3ParallPreds(){ //ν��T3��q'''�ɹ���q''��û���ɹ�
        if(this.getPathR()!=null){
            if((this.getPredR()) && (this.getPathR().equals("false")))//(id,true,"false")
                return true;
        }
        return false;
    }

    public  boolean isWaitT3FirstPreds(){ //ν��T3��q''�ɹ���q'''��û���ɹ�
        if(this.getPathR()!=null){
            if((!this.getPredR()) && (this.getPathR().equals("true")))//(id,false,"true")
                return true;
        }
        return false;
    }

    public void output(){ //������յļ����
        System.out.println(this.getPathR());
    }
}
