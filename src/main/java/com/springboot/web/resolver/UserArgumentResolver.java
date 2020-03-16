package com.springboot.web.resolver;

import com.springboot.web.annotation.SocialUser;
import com.springboot.web.domain.User;
import com.springboot.web.domain.enums.SocialType;
import com.springboot.web.repository.UserRepository;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.springboot.web.domain.enums.SocialType.*;

@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    private UserRepository userRepository;

    public UserArgumentResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(SocialUser.class) != null && parameter.getParameterType().equals(User.class);   //파라미터에 @SocialUser 어노테이션이 있고 타입이 User인 파라미터만 true를 반환
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpSession session = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getSession();
        User user = (User) session.getAttribute("user");
        return getUser(user, session);
    }

    private User getUser(User user, HttpSession session) {  //인증된 User 객체를 만드는 메인 메서드
        if(user == null) {
            try {
                OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
                Map<String, String> map = (HashMap<String, String>) authentication.getUserAuthentication().getDetails();
                User convertUser = convertUser(String.valueOf(authentication.getAuthorities().toArray()[0]), map);

                user = userRepository.findByEmail(convertUser.getEmail());
                if(user == null) {
                    user = userRepository.save(convertUser);
                }

                setRoleIfNotSame(user, authentication, map);
                session.setAttribute("user", user);
            } catch (ClassCastException e) {
                return user;
            }
        }
        return user;
    }

    private User convertUser(String authority, Map<String, String> map) {   //사용자의 인증된 소셜 미디어 타입에 따라 빌더를 사용해 User 객체를 만드러 주는 가교 역할
        if(FACEBOOK.isEquals(authority)) return getModernUser(FACEBOOK, map);
        else if(GOOGLE.isEquals(authority)) return getModernUser(GOOGLE, map);
        else if(KAKAO.isEquals(authority)) return getKaKaoUser(map);
        return null;
    }
    private User getModernUser(SocialType socialType, Map<String, String> map) {    //페이스북이나 구글과 같이 공통되는 명명규칙을 가진 그룹을 User 객체로 매핑
        return User.builder()
            .name(map.get("name"))
            .email(map.get("email"))
            .pincipal(map.get("id"))
            .socialType(socialType)
            .createdDate(LocalDateTime.now())
            .build();
    }

    private User getKaKaoUser(Map<String, String> map) {    //(키의 네이밍값이 타 소셜 미디어와 다른)카카오 회원을 위한 메서드/getModernUser 메서드와 동일하게 User 객체로 매핑
        HashMap<String, String> propertyMap = (HashMap<String, String>)(Object)map.get("properties");
        return User.builder()
            .name(propertyMap.get("nickname"))
            .email(map.get("kaccount_email"))
            .pincipal(String.valueOf(map.get("id")))
            .socialType(KAKAO)
            .createdDate(LocalDateTime.now())
            .build();
    }


    private void setRoleIfNotSame(User user, OAuth2Authentication authentication, Map<String, String> map) {    //인증된 authentication이 권한을 갖고 있는지 체크하는 용도/관한이 없을 경우 SecurityContextHolder를 사용해 해당 소셜 미디어 타입으로 권한을 저장
        if(!authentication.getAuthorities().contains(new SimpleGrantedAuthority(user.getSocialType().getRoletype()))) {
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(map, "N/A", AuthorityUtils.createAuthorityList(user.getSocialType().getRoletype())));
        }
    }

}
