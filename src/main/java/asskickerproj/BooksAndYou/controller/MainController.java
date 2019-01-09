package asskickerproj.BooksAndYou.controller;

import asskickerproj.BooksAndYou.service.Desk;
import asskickerproj.BooksAndYou.service.DeskManager;
import asskickerproj.BooksAndYou.service.Token;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class MainController {
    @RequestMapping("/hello")
    public String hello() {
        return "Hello";
    }

    @GetMapping("dump")
    public String dump(@RequestParam("token") String token){
        Token t = new Token(token);
        if (t.isValid()){
            Desk desk = DeskManager.getDesk(t.getDeskID());
            if (desk != null)   DeskManager.dumpDesk(desk);
            return "ok";
        }
        return "failed";
    }

    @GetMapping("/turn-page")
    public List<String> next(@RequestParam("token") String token, @RequestParam("direction") String direction) {
        Token t = new Token(token);
        List<String> empty = new ArrayList<>();


        Desk desk;
        if (!t.isValid() || (desk = DeskManager.getDesk(t.getDeskID())) == null) {
            return empty;
        }

        switch (direction) {
            case "current":
                return desk.current();
            case "last":
                return desk.last();
            case "next":
                return desk.next();
            default:
                return desk.current();
        }
    }
}
