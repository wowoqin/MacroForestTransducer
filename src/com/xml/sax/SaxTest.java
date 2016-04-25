package com.xml.sax;

import com.XPath.PathParser.ASTPath;
import com.XPath.PathParser.QueryParser;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by qin on 15-4-29.
 */
public class SaxTest {
    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        //File f = new File("test3.xml");
        //MySaxParser dh = new MySaxParser("/a//d[//c[/d]][/a]");
        //MySaxParser dh = new MySaxParser("/a/c[/b][/d]");
        //MySaxParser dh = new MySaxParser("/a/c/d[/a]");
        File f = new File("test8.xml");
        MySaxParser dh = new MySaxParser("//a");
        //MySaxParser dh = new MySaxParser("//a[/b]//d");
        //MySaxParser dh = new MySaxParser("/a[/b[/c]][/d]/c[/d]");
        //MySaxParser dh = new MySaxParser("//a[/d]/c[/b[//g]]");
        parser.parse(f, dh);
    }
}
