package com.xml.sax;

import com.ibm.actor.Actor;
import com.rules.State;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;

/**
 * Created by qin on 15-4-29.
 */
public class SaxTest {
    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
        System.out.println(Thread.currentThread().getName()+" 线程开始运行");
//        Thread.currentThread().setPriority(1);
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        //File f = new File("test3.xml");
        //MySaxParser dh = new MySaxParser("/a//d[//c[/d]][/a]");
        //MySaxParser dh = new MySaxParser("/a/c[/b][/d]");
        //MySaxParser dh = new MySaxParser("/a/c/d[/a]");
        File f = new File("test8.xml");
        MySaxParser dh = new MySaxParser("//a[/b]");
        //MySaxParser dh = new MySaxParser("//a[/b]//d");
        //MySaxParser dh = new MySaxParser("/a[/b[/c]]/e[/f]");
        //MySaxParser dh = new MySaxParser("//a[/d]/c[/b[//g]]");
        parser.parse(f, dh);
        //主线程等待所有子线程结束才结束
        //System.out.println(State.actorManager.getActiveRunnableCount());
        //if(State.actorManager.getActiveRunnableCount()>=1){
        //把所有子线程join到main中--》得到子线程的名称
        //State.actorManager.terminateAndWait();
        //State.actors=State.actorManager.getActors();
//        for(String key:State.actorManager.getActors().keySet()){
//            Actor actor=State.actors.get(key);
//            if(!actor.isShutdown()){
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }

        //State.actorManager.terminateAndWait();

        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        for(int i=0;i<State.actorManager.getActors().length;i++){
//            Actor actor=State.actorManager.getActors()[i];
//            if(!actor.isShutdown()){
//
//            }
//        }
        System.out.println(Thread.currentThread().getName() + " 线程结束运行");
    }
}
