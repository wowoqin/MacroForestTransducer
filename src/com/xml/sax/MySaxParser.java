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

    protected DefaultActorManager manager;
    protected DefaultMessage message;

    public MySaxParser(String path_str) {
        super();
        qp = new QueryParser();
        path = qp.parseXPath(path_str);
        State currentQ = StateT1.TranslateStateT1(path);//��XPath����Ϊ����״̬
        Stack stack = new Stack();
        State.stacklist.add(stack);

        // SAX �ӿڴ�������
        manager  = State.actorManager;

        // ���� stack ��Ӧ�� actor--> stackActor
        Actor stackActor = manager.createAndStartActor(MyStateActor.class, "stackActor");
        State.actors.put(stackActor.getName(),stackActor);

        message = new DefaultMessage("resActor",null);
        manager.send(message, null, stackActor);

        message = new DefaultMessage("pushTask",new ActorTask(0,currentQ));
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
        message=new DefaultMessage("startE",new ActorTask(layer,qName));
        manager.broadcast(message, null);
        layer++; //layer �Ǳ�ʾ�� XML ���еı�ǩ�Ĳ���
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        layer--;
        //�ѽ�����ǩ�������е� stateActor
        message=new DefaultMessage("endE",new ActorTask(layer,qName));
        manager.broadcast(message,null);
    }


    @Override
    public void endDocument() throws SAXException{
        //State.actorManager.terminateAndWait();
        System.out.println("----------- End  Document ----------");
        super.endDocument();
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String content = new String(ch,start,length);
        super.characters(ch, start, length);
    }

}