package asskickerproj.BooksAndYou.controller;

import asskickerproj.BooksAndYou.service.Token;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TemplateController {
    @RequestMapping("/index")
    public String login(){
        return "login";
    }

    @GetMapping("desk")
    public String desk(@RequestParam("token") String token,
                       Model model){
        Token t = new Token(token);
        if(t.isValid()){
            model.addAttribute("token", token);
            System.out.printf("Get token: %s\n", token);

            return "desk";
        }else{
            return "error";
        }

    }
}
