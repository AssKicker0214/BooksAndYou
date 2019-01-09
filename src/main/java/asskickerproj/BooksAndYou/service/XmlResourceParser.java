package asskickerproj.BooksAndYou.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class XmlResourceParser {
    public Element body;
    private StringBuilder sb = new StringBuilder();
    private Pattern ptn = Pattern.compile("h\\d|p");
    public XmlResourceParser(){
    }

    public XmlResourceParser append(String str){
        sb.append(str);
        return this;
    }

    public void parse(){
        Document doc = Jsoup.parse(sb.toString());
        body = doc.body() == null ? doc : doc.body();
    }

    public List<String[]> extractTagText(){
        return _extractTagText(body);
    }

    private List<String[]> _extractTagText(Element elem){
        LinkedList<String[]> list = new LinkedList<>();
        for(Element child:elem.children()){
            list.push(new String[]{child.tagName(), child.ownText()});
            List<String[]> txt = this._extractTagText(child);
            list.addAll(txt);
        }
        return list;
    }

    public List<Element> filter(){
        return this._filter(body);
    }

    public List<String> TextualElements(){
        List<Element> elems = this._filter(this._prune(body));
        LinkedList<String> texts = new LinkedList<>();
        for(Element elem : elems){
            texts.push(elem.outerHtml());
        }
        return texts;
    }

    private List<Element> _filter(Element elem){
        List<Element> texts = new LinkedList<>();
        for(Element child:elem.children()){
            if(ptn.matcher(child.tagName()).find()){
                texts.add(child);
            }else{
                texts.addAll(this._filter(child));
            }
        }
        return texts;
    }

    public Element prune(){
        return this._prune(body);
    }

    private Element _prune(Element elem){
        for(Attribute attr: elem.attributes().asList()){
            elem.removeAttr(attr.getKey());
        }
        for(Element child : elem.children()){
            this._prune(child);
        }
        return elem;
    }
}
