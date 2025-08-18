package xlike.top.nettydemo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 负责处理页面视图导航的控制器
 * @author Administrator
 */
@Controller
public class ViewController {


    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/2")
    public String index2() {
        return "index2";
    }

}