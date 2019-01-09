package asskickerproj.BooksAndYou.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @MessageMapping("/hello")
    @SendTo("/desk/greetings")
    public String greeting(String name){
        System.out.println(name);
        return String.format("Greetings, %s", name);
    }
}
