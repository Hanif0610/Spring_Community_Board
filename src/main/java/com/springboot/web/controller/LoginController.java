package com.springboot.web.controller;

import com.springboot.web.annotation.SocialUser;
import com.springboot.web.domain.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping(value = "/{facebook|google|kakao}/complete")    //인증이 성공적으로 처리된 이후에 리다이렉트되는 경로
    public String loginComplete(@SocialUser User user) {
        return "redirect:/board/list";
    }
//    public String loginComplete(HttpSession session) {
//        OAuth2Authentication authentication = (OAuth2Authentication)SecurityContextHolder.getContext().getAuthentication(); //SecurotyContexyHolder에서 인증된 정보를 OAuth2Authentication형태로 받아옴
//        Map<String, String> map = (HashMap<String, String>)authentication.getUserAuthentication().getDetails(); //리소스 서버에서 받아온 개인정보를 getDetails()를 사용해 Map 타입으로 받을 수 있다.
//        session.setAttribute("uesr", User.builder() //세션에 빌더를 사용하여 인증된 User 정보를 User 객체로 변환하여 저장함
//            .name(map.get("name"))
//            .email(map.get("email"))
//            .principal(map.get("id"))
//            .socialType(SocialType.FACEBOOK)
//            .createdDate(LocalDateTime.now())
//            .build()
//        );
}
