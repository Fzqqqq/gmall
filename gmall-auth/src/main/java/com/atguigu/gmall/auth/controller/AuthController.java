package com.atguigu.gmall.auth.controller;

import com.atguigu.gmall.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author fzqqq
 * @create 2020-10-19 14:05
 */
@Controller
public class AuthController {

    @Autowired
    private AuthService authService;


    @GetMapping("toLogin.html")
    public String login(@RequestParam("returnUrl") String returnUrl, Model model) {
        // 把登录前的页面地址，记录到登录页面，以备将来登录成功，回到登录前的页面
        model.addAttribute("returnUrl", returnUrl);
        return "login";
    }

    @PostMapping("login")
    public String login(
            @RequestParam("loginName")String loginName,
            @RequestParam("password")String password,
            @RequestParam("returnUrl")String returnUrl,
            HttpServletRequest request, HttpServletResponse response
            ){
        this.authService.accredit(loginName, password, request, response);
        return "redirect:" + returnUrl;
    }

}
