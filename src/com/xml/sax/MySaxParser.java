package com.xml.sax;

import com.XPath.PathParser.ASTPath;
import com.XPath.PathParser.QueryParser;
import com.ibm.actor.Actor;
import com.ibm.actor.DefaultActorManager;
import com.ibm.actor.DefaultMessage;
import com.rules.*;
import com.taskmodel.ActorTask;
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

    // SAX 接口处的引用
    protected DefaultActorManager manager= State.actorManager;
    protected DefaultMessage message;
    protected Actor stackActor;
    public MySaxParser(String path_str) {
        super();
        qp = new QueryParser();
        path = qp.parseXPath(path_str);
        State currentQ = StateT1.TranslateStateT1(path);//将XPath翻译为各个状态
        Stack stack = new Stack();

        // 创建 stack 对应的 actor--> stackActor
        stackActor = manager.createAndStartActor(MyStateActor.class, "stackActor");
//        (MyStateActor)stackActor.getThread().join();
        message=new DefaultMessage("resActor&&pushTask",new Object[]{stack,new ActorTask(currentQ.getLevel(),currentQ,true)});
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
        message=new DefaultMessage("startE",new ActorTask(layer,qName));
        manager.send(message,null,stackActor);
        layer++; //layer 是表示在 XML 流中的标签的层数
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        layer--;
        message=new DefaultMessage("endE", new ActorTask(layer, qName));
        manager.send(message,null,stackActor);
    }


    @Override
    public void endDocument() throws SAXException{
        super.endDocument();
        System.out.println("----------- End  Document ----------");

    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String content = new String(ch,start,length);
        super.characters(ch, start, length);
    }

}