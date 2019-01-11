package asskickerproj.BooksAndYou.controller;

import asskickerproj.BooksAndYou.service.Desk;
import asskickerproj.BooksAndYou.service.DeskManager;
import asskickerproj.BooksAndYou.service.Token;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class WebSocketController {

    @MessageMapping("/hello")
    @SendTo("/desk/greetings")
    public String greeting(String name){
        System.out.println(name);
        return String.format("Greetings, %s", name);
    }

    @MessageMapping("/turn-page")
    @SendTo("/desk/controller")
    public String checkTurnPage(PageAction action){
        String token = action.token;
        System.out.println("[ws get] -> " + action.toString());
        Token t = new Token(token);
        boolean ok = false;
        Desk desk;
        if(t.isValid() && (desk = DeskManager.getDesk(t.getDeskID())) != null){
            ok = desk.vote(t.getReaderID(), action.direction);
        }
        String response = String.format("{\"name\": \"turn-page\",\"token\":\"%s\", \"ok\": %b}", token, ok);
        System.out.println("[ws send] -> " + response);
        return response;
    }

    @MessageMapping("/skip-page")
    @SendTo("/desk/controller")
    public String skipPage(PageAction action){
        String token = action.token;
        System.out.println("[ws get] -> " + action.toString());
        Token t = new Token(token);
        Desk desk;
        if(t.isValid() && (desk = DeskManager.getDesk(t.getDeskID())) != null){
            desk.skip(action.skip, action.direction);
        }
        return String.format("{\"name\":\"skip-page\",\"token\":\"%s\",\"ok\":true}", token);
    }

    @MessageMapping("/highlight")
    @SendTo("/desk/controller")
    public String highlight(String json){
        System.out.println(json);
        return json;
    }

    @MessageMapping("/chat")
    @SendTo("/desk/controller")
    public String chat(String json){
        System.out.println("[chat] -> "+json);
        return json;
    }
}


class PageAction{
    public String token;
    public String direction;
    public int skip;


    public String toString(){
        return token + ":" + direction + "," + skip;
    }
}