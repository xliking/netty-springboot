package xlike.top.nettydemo.controller;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    /**
     * 模拟登录接口
     * 在实际项目中，这里应该是校验用户名密码并登录的逻辑
     */
    @PostMapping("/login")
    public SaTokenInfo login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        if ("123456".equals(password) && username != null && username.startsWith("user")) {
            try {
                long loginId = Long.parseLong(username.substring(4));
                // Sa-Token 登录
                StpUtil.login(loginId);
                // 返回 Token 信息
                return new SaTokenInfo(StpUtil.getTokenInfo());
            } catch (NumberFormatException e) {
                 // 登录失败
            }
        }
        
        // 实际项目中应抛出认证异常
        return null;
    }
    
    // 用于封装 Sa-Token 信息的内部类
    public static class SaTokenInfo {
        public String tokenName;
        public String tokenValue;
        public SaTokenInfo(cn.dev33.satoken.stp.SaTokenInfo saTokenInfo) {
            this.tokenName = saTokenInfo.getTokenName();
            this.tokenValue = saTokenInfo.getTokenValue();
        }
    }
}