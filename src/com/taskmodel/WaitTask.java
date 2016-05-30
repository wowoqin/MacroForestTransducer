package com.taskmodel;

import com.ibm.actor.DefaultMessage;

/**
 * Created by qin on 2016/3/28.
 */
public class WaitTask {   // �� actor ��list �����ӵ�����
    protected  int id;          // id
    protected  Boolean predR;   // ν�ʵķ��ؽ��
    protected  String  pathR;   // ���� path �ķ��ؽ��������preds'�ķ��ؽ��

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

    public Boolean getPredR() {
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

    public void setPredR(Boolean predR) {
        this.predR = predR;
    }

    public void setPathR(String pathR) {
        this.pathR = pathR;
    }

    public boolean isPredsTrue(){
        if (getPredR()==true)
            return true;
        return false;
    }

    public boolean hasReturned(){
        return (getPredR()!=null && getPathR()!=null);
    }

    public boolean isSatisfiedOut() { // ��鵱ǰ waitTask �ǲ����Ѿ�����������������Խ�����������ˣ�
        return (isPredsTrue() && (getPathR()!=null) && (!getPathR().equals("NF")));
    }

    public  boolean isPredsSatisified(){ //wt��Ϊһ��ν�ʣ����ɹ� (id,true,"true")
        return (isPredsTrue() && getPathR().equals("true"));
    }

    public  boolean isWaitT3FirstPreds(){ //wt��Ϊһ��ν��T3��q''�ɹ���q'''��û���ɹ� //(id,false,"true")
        return (!isPredsTrue() && getPathR().equals("true"));
    }

    public  boolean isWaitT3ParallPreds(){ //wt��Ϊһ��ν��T3��q'''�ɹ���q''��û���ɹ� //(id,true,"false")
        return (isPredsTrue() && getPathR().equals("false"));
    }

    public void output(){ //������յļ����
        System.out.println(this.getPathR());
    }

//    public void doNext(){
//        if (this.isSatisfiedOut()) {//��ǰ wt �����������
//            if(curactor.getName().equals("stackActor")){//��stack��
//                if(currstack.size()==1){//���
//                    curactor.output(wtask);//Ҳ���ж���������ʱ��return��
//                    break;
//                }else {//��stack�� && ��ΪT1-5�ĺ���path
//                    //���(wt.id��wt.getValue())���Լ����list��id=wt.id�� wt1
//                    for(int j=i-1;j>=0;j--){
//                        if(((WaitTask) list.get(j)).getId()==id){//�ҵ���ͬid�� wt���ѽ������ wt
//                            dmessage=new DefaultMessage("pathResult",new ActorTask(id,wtask.getPathR()));
//                            actorManager.send(dmessage,curactor,curactor);
//                            //��ʱ��Ҫ����ѭ��������ΪҲ����T1-5�����ж�����ϵ�T1-1
//                            // /a/b : ��a�����ж��b������/bʱ�����ҵ����һ��bʱ��ǰ���Ѿ��кܶ����ͬid��(0,true,b)�ˣ�
//                            //       ���ǵ�id������0�����Դ�ʱ�Ͳ���Ҫ�ٰ�֮ǰ�Ѿ����õ�wt�ٴθ�ֵpathResult��
//                            curactor.removeWTask(wtask);
//                            return;//���������������������Сѭ��j���������˴�ѭ��i��
//                        }
//                    }
//                }
//            }else { //��ΪAD ����� path ��һ����
//                if(isInSelf){
//                    dmessage=new DefaultMessage("pathResult",new ActorTask(id,wtask.getPathR()));
//                    actorManager.send(dmessage, curactor, curactor);
//                }else{
//                    dmessage=new DefaultMessage("paResult",new ActorTask(id,wtask.getPathR()));
//                    actorManager.send(dmessage,curactor,curactor.getResActor());
//                }
//                curactor.removeWTask(wtask);
//                return;//������ѭ��
//            }
//        }else{//���Լ��Ľ�����ǩ����ǰwt�������������
//            curactor.removeWTask(wtask);
//            return;//������ѭ��
//        }
//    }
}