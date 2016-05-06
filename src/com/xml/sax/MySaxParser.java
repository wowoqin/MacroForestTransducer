package com.xml.sax;

import com.XPath.PathParser.ASTPath;
import com.XPath.PathParser.QueryParser;
import com.ibm.actor.Actor;
import com.ibm.actor.DefaultActorManager;
import com.ibm.actor.DefaultMessage;
import com.rules.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.*;

/**
 * Created by qin on 15-4-27.
 */
public class MySaxParser<T> extends DefaultHandler {

    protected QueryParser qp ;
    protected ASTPath path;
    protected int layer;
    protected Stack stack;

    protected DefaultActorManager manager;
    protected DefaultMessage message;
    protected Actor stackActor;

    protected Map<String,Actor> myActors;

    public MySaxParser(String path_str) {
        super();
        qp = new QueryParser();
        path = qp.parseXPath(path_str);
        State currentQ = StateT1.TranslateStateT1(path);//��XPath����Ϊ����״̬
        stack=new Stack();



        // SAX �ӿڴ�������
        myActors = State.actors;
        manager  = State.actorManager;

        // ���� stack ��Ӧ�� actor--> stackActor
        stackActor = manager.createAndStartActor(MyStateActor.class, "stackActor");
        myActors.put(stackActor.getName(), stackActor);

        // �� stack �� stackActor ��ϵ����
        ActorTask atask = new ActorTask(stack);
        message = new DefaultMessage("stack",atask);
        manager.send(message, null, stackActor);

        atask = new ActorTask(layer,currentQ);
        message = new DefaultMessage("pushTask",atask);
        manager.send(message, null, stackActor);


    }

    @Override
    public void startDocument() throws SAXException {
        System.out.println("----------- Start Document ----------");
        layer = 0;
        super.startDocument();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //�ѿ�ʼ��ǩ�������е� stateActor
        ActorTask atask=new ActorTask(layer,qName);
        message=new DefaultMessage("startE",atask);
        manager.broadcast(message, null);
        layer++; //layer �Ǳ�ʾ�� XML ���еı�ǩ�Ĳ���
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        layer--;
        //�ѽ�����ǩ�������е� stateActor
        ActorTask atask=new ActorTask(layer,qName);
        message=new DefaultMessage("endE",atask);
        manager.broadcast(message,null);
        super.endElement(uri, localName, qName);
    }



    @Override
    public void endDocument() throws SAXException{
        System.out.println("----------- End  Document ----------");
        super.endDocument();
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String content = new String(ch,start,length);
        super.characters(ch, start, length);
    }

}