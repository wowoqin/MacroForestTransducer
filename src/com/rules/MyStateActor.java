package com.rules;

import com.ibm.actor.AbstractActor;
import com.ibm.actor.Actor;
import com.ibm.actor.DefaultMessage;
import com.ibm.actor.Message;
import com.taskmodel.ActorTask;
import com.taskmodel.WaitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by qin on 2016/3/22.
 */
public class MyStateActor extends AbstractActor {

    protected Stack myStack;//ÿ��actor ��Ӧ����һ�� stack ��Ҳ����һ�� stack ��Ӧ��һ�� actor
    protected Actor resActor;//�ϼ� Actor

    protected List tlist;// ��ŵȴ�ƥ������� list

    public MyStateActor(){
        tlist=new ArrayList();
    }

    public Stack getMyStack() {
        return myStack;
    }

    public List getTlist() {
        return tlist;
    }

    public void setMyStack(Stack myStack) {
        this.myStack = myStack;
    }

    @Override
    public void activate() {
        super.activate();
    }

    public void setResActor(Actor resActor) {
        this.resActor = resActor;
    }

    public Actor getResActor() {
        return resActor;
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public boolean willReceive(String subject) {
        return super.willReceive(subject);
    }

    @Override
    public int getMaxMessageCount() {
        return super.getMaxMessageCount();
    }

    @Override
    protected void runBody() {
        List list=State.stacklist;
        this.setMyStack((Stack) list.get(list.size() - 1));
    }

    /* stateActor �ܹ����յ�����Ϣ��
        * �� SAX �ӿ����� ��startE��endE
        * stateActor ֮�䣺stack: stack
        *                 pushTask��q'/ q''
        *                 predResult: True (false ���÷���)
        *                 pathResult: һ������ / null
        *                 setCategory��T3ν���� preds' ����Ӧ�� actor�����ͣ�String��
        *
        *  */
    @Override
    protected void loopBody(Message message) {
        sleep(1);
        Object data=message.getData();
        String subject = message.getSubject();
        if("resActor".equals(subject)){ // actorTask,���� data = null
            this.setResActor(message.getSource());
        }else{
            ActorTask task=(ActorTask)data;// task
            // ��һ��actorTask
            if(task!=null){
                // object ��q��State����qName��String����q'�ķ��ؽ����True/False����q''�ķ��ؽ����String��
                Object object = task.getObject();
                Stack  ss = this.getMyStack();
                State  currQ;

               if("push".equals(subject)){//������push
                   this.getMyStack().push(task);
               }else if("pushTask".equals(subject)){       // actorTask,���� data ��һ�� q3
                    this.pushFunction(task);
               }else if("setCategory".equals(subject)){    // actorTask,���� data ��һ�� string
                    this.setCategory((String)object);
               }else if("predResult".equals(subject)){     // actorTask,���� data ��һ��q'�ķ��ؽ����True��
                    //��ʱ��ν�ʵķ��ؽ�����������Լ���������������һ����actor��T2��ν��actor������T3��preds'��Ӧ��actor��
                    // �յ�ν�ʷ��ؽ������list���ҵ���Ӧ��wt��ID��Ȼ��ν�ʼ��Ľ�����¸�ֵ
                    for(int i=0;i<tlist.size();i++) {
                        WaitTask wt = (WaitTask) (tlist.get(i));
                        if (wt.getId() == task.getId()) {
                            if (message.getSource().getCategory().equals("T3PredsActor")) {  //ν�ʷ��ؽ������T3 preds'��actor
                                wt.setPathR((String) (task.getObject()));
                            } else {    //��Ϣ�����Լ�-->curactor ��������Ϣ����T2 �ĺ���ν��
                                wt.setPredR((Boolean)(task.getObject()));
                            }
                            //�����������֮�󿴵�ǰ�������wt�ǲ���ν�������-->˵��֮ǰ���ص�ν�ʽ��ֻ����Ϊν�ʵ�ν��
                            if (wt.isPredsSatisified()) {
                            /*ν������(id,true,true)����ֵ������
                            *   ��ԭʼ��ν��ΪT2-->��testƥ���list.add(id,false,true);
                            *          T2-2����ǰջ��ΪT2-2����ν�ʼ��ɹ�����T2-2����Ҳ���ɹ���
                            *          T2-4����ǰջ��ΪT2-4����ν�ʼ��ɹ�����T2-4����Ҳ���ɹ������ʱ
                            *                �Ϳ������Ƿ�������һ��AD ���test(����-->T2-4��һ������ջ)��
                            *                ���ǣ���ǰջ���������T2-4���ǳɹ��ģ�
                            *
                            *  ��ԭʼ��ν��ΪT3��
                            *          T3-1����ǰջ��:
                            *                1)(id,waitState)
                            *          T3-2����ǰջ����
                            *                1)(id,waitState)�� preds'����ɹ������ȼ��ɹ�T2-2��preds(q')����T2-2�滻ΪwaitState
                            *                2)(id,T2-2)��      preds'�ȼ��ɹ���wt=(id,false,true),Ȼ��T2-2��preds'(q')�ż��ɹ���pop(q')������T2-2
                            *          T3-3����ǰջ��:
                            *                1)(id,waitState)
                            *          T3-4����ǰջ����
                            *                1)T2-4:
                            *                  1. q'''�ȼ��ɹ���
                            *                     ��T2-4.test�ж��ƥ�䣬����T2-4.prActor���ж��q'����ʱ��Ҫpop(layer,T2-4)
                            *                      && taskmodel=(layer,true)��T3-4�� wtask-->(id,T,F)==>(id,T,T)ʱ��ջ��Ϊ(id,qw);
                            *                  2. q'''����ɹ����ڴ�֮ǰT3-4.wtask�Ѿ���(id,F,T)��
                            *                     ��(layer,T)��T3-4.wtask-->(layer,T,T),����ʱջ��(layer,T2-4),��������T3-4�Ѿ����ɹ��ˣ�
                            *                     ����Ҫpop��layer,T2-4��&& (qw.id,true)����ȥ��
                            *                2)(id,waitState)
                            *
                            * */
                                //ν�����㣬����ǰջ��
                                if (!ss.isEmpty()) {
                                    //����ν�ʽ�� && pop ��ǰջ��
                                    ActorTask atask = (ActorTask) ss.peek();
                                    int id = atask.getId();

                                    State state = (State) atask.getObject();
                                    if (state instanceof StateT2_4) {
                                        //��T2-4 ��ν��ջ���ﷵ�ؽ��-->T2-4���ɹ�
                                        //remove ��T2-4 ��ص�wt-->//d[/b]
                                        for (int j = i + 1; j < tlist.size(); j++) {
                                            wt = (WaitTask) tlist.get(j);
                                            this.tlist.remove(wt);
                                        }
                                    }
                                    this.sendPredsResult(new ActorTask(id, true));
                                }
                                this.removeWTask(wt);   //����wt֮��ɾ�����ν�������wt
                            }
                            // ν��ԭ����T3 && �յ����� T2 �ĺ���ν�ʵķ��ؽ��
                            //preds:child::test preds] ���� [desc_or_self::test preds]�е�preds�ķ��ؽ��
                            else if (wt.isWaitT3PPreds()) {// (id,true,"false")
                                //q'''���ɹ���q''��û���ɹ�
                                if (!ss.isEmpty()) {
                                    ActorTask atask = (ActorTask) ss.peek();
                                    State state = (State) atask.getObject();//ջ��state
                                    int id = atask.getId();
                                    if(state instanceof  StateT2_2){    // ԭ����ν��Ϊ T3-2 && preds'��û���ɹ�
                                        State waitState=new WaitState();
                                        waitState.setLevel(((State)atask.getObject()).getLevel());
                                        atask=new ActorTask(id,waitState);
                                        //pop ��ǰ(id,T2-2)
                                        this.popFunction();
                                        //push(id,qw)
                                        this.pushFunction(atask);
                                    }
                                    if (state instanceof StateT2_4) {   // ԭ����ν��Ϊ T3-4 && preds'��û���ɹ�
                                        //��T2-4 ��ν��ջ���ﷵ�ؽ��-->T2-4���ɹ�
                                        //remove ��T2-4 ��ص�wt-->//d[/b]
                                        for (int j = i + 1; j < tlist.size(); j++) {
                                            wt = (WaitTask) tlist.get(j);
                                            this.tlist.remove(wt);
                                        }
                                        //pop(T2-4)
                                        this.popFunction();
                                    }
                                }
                            }
                        }
                    }
               }else if("pathResult".equals(subject)){ // actorTask,���� data ��һ��q''�ķ��ؽ����String��
                    // �� waitTask ���ҵ���Ӧ��ID��Ȼ�󽫺��� path ���Ľ�� ���¸�ֵ
                    for(int i=0;i<tlist.size();i++){
                        WaitTask wt = (WaitTask)(tlist.get(i));
                        if(wt.getId() == task.getId()){
                            if(wt.getPathR()!=null){// �Ѿ��к���path���ɹ�����ͬ�㼶�Ľ��
                                tlist.add(i+1,wt);
                            }else{
                                wt.setPathR((String) (task.getObject()));
                            }
                            break;
                        }
                    }
               }else{                              // actorTask,���� data ��һ��qName��String��
                    if (!ss.isEmpty()) {
                        // �ҵ���ǰ actor �ĵ�ǰջ���ĵ�ǰ state
                        currQ = (State) (((ActorTask) (ss.peek())).getObject());
                        String tag = (String)object;
                        int layer = task.getId();
                        if("startE".equals(subject)){
                            try {
                                currQ.startElementDo(tag, layer, this);
                            } catch (CloneNotSupportedException e) {
                                e.printStackTrace();
                            }
                        }else if("endE".equals(subject)){
                            currQ.endElementDo(tag, layer, this);
                        }
                    }
                }
            }
        }
        //ɾ�����͵���Ϣ-->ÿ��Actor���յ������Ϣ����=100
        //this.remove(message);
    }

    public void pushFunction(ActorTask actorTask){
        //������������ĳ�����T3ν������ AD ��� path����preds && T3��test��ƥ��
        Stack currStack = this.getMyStack();
        int layer=((State)(actorTask.getObject())).getLevel();
        if(!currStack.isEmpty()){
            ActorTask at=(ActorTask)currStack.peek();
            if((((State)(at.getObject())).getLevel())>layer){
                //����ǰջ�� q1 ��layer ���ڽ�Ҫѹջ�� q2 ��layer����ѹջ˳��Ϊ q2��q1
                currStack.pop();
                currStack.push(actorTask);
                currStack.push(at);
            }else{
                currStack.push(actorTask);
            }
        }else{
            currStack.push(actorTask);
        }

    }

    public void popFunction(){
        Stack currStack = this.getMyStack();
        if(!currStack.isEmpty())
            currStack.pop();
    }

    public void sendPathResult(ActorTask actorTask){// path���ɹ����ϴ������id��tag������Ӧ�� wt
        boolean isFindInThis=false;
        if(!this.tlist.isEmpty()){        //����curactor.list�����Ƿ�����ͬ id ��wt
            for(int i=(tlist.size()-1);i>=0;i--){
                WaitTask wTask=(WaitTask)tlist.get(i);
                if(wTask.getId()==actorTask.getId()){// �ҵ���ͬ id �� wt
                    isFindInThis=true;
                    Message message = new DefaultMessage("predResult",actorTask);
                    getManager().send(message, this, this);
                }
            }
        }
        if(!isFindInThis){  //curactor.list ��û����ͬid��wt�����ϴ���resActor
            Message message = new DefaultMessage("predResult",actorTask);
            getManager().send(message, this, this.getResActor());
        }
    }

    public void sendPredsResult(ActorTask actorTask){// ν�ʼ��ɹ����ϴ������id��true������Ӧ�� wt
        if(actorTask.isInSelf()){
            getManager().send(new DefaultMessage("predResult",actorTask), this, this);
        }else{
            getManager().send(new DefaultMessage("predResult",actorTask), this, this.getResActor());
        }
    }

    //û�м��ɹ���ջʱ remove ��actorTask.id��ȵ� wtask
    public void FindAndRemoveFailedWTask(ActorTask actorTask){
        boolean isFindInThis = false;
        WaitTask wtask;
        List list=this.getTlist();
        //�Լ��� list ����������ͬ id �� wt(������ж��)
        if(!list.isEmpty()){
            for(int i=0;i<list.size();i++) {
                 wtask = (WaitTask) list.get(i);
                if (wtask.getId() == actorTask.getId()) {
                    isFindInThis = true;
                    list.remove(wtask);
                }
            }
        }
        //���Լ����ڵ�list��û���ҵ���ͬ id �� wt�������ϼ� actor ����
        if(!isFindInThis){
            MyStateActor resActor=(MyStateActor)this.getResActor();
            list= resActor.getTlist();
            for(int i=0;i<list.size();i++){
                wtask=(WaitTask)list.get(i);
                if(wtask.getId()==actorTask.getId())
                    resActor.removeWTask(wtask);
            }
        }
    }

    public void removeWTask(WaitTask wt){
        this.tlist.remove(wt);
    }

    public void output(WaitTask wt){
        wt.output();
        this.removeWTask(wt);
    }

    public void addWTask(WaitTask wt){
        this.tlist.add(wt);
    }
}
