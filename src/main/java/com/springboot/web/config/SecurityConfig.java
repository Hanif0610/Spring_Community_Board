package com.springboot.web.config;

import com.springboot.web.domain.enums.SocialType;
import com.springboot.web.oauth.ClientResources;
import com.springboot.web.oauth.UserTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.CompositeFilter;

import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.List;

import static com.springboot.web.domain.enums.SocialType.*;

@Configuration
@EnableWebSecurity  //웹에서 시큐리티 기능을 사용하겠다는 어노테이션
@EnableOAuth2Client
public class SecurityConfig extends WebSecurityConfigurerAdapter {  //자동 설정 그대로 사용할 수도 있지만 요청, 권한, 기타 설정에 대해서는 필수적으로 최적화한 설정이 들어가야 한다.
                                                                    //최적화 설정을 위해 WebSecurityConfigurerAdapter를 상속받고 configure(HttpSecurity http) 오버라이드하여 원하는 형식의 시큐리티 설정
    @Autowired
    private OAuth2ClientContext oAuth2ClientContext;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        http.authorizeRequests().antMatchers("/", "/login/**", "/css/**", "/images/**", "/js/**", "/console/**").permitAll()
                .antMatchers("/facebook").hasAuthority(FACEBOOK.getRoletype())
                .antMatchers("/google").hasAuthority(GOOGLE.getRoletype())
                .antMatchers("/kakao").hasAuthority(KAKAO.getRoletype())
                .anyRequest().authenticated()
            .and()
                .headers().frameOptions().disable()
            .and()
                .exceptionHandling()
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))   //인증의 진입 지점(인증되지 않은 사용자가 허용되지 않은 경로로 리퀘스트 요청할 경우 /login으로 이동
            .and()
                .formLogin()
                .successForwardUrl("/board/list")
            .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
            .and()
                .addFilterBefore(filter, CsrfFilter.class)  //첫 번째 인자보다 먼저 시작될 필터 등록
                .addFilterBefore(oauth2Filter(), BasicAuthenticationFilter.class)
                .csrf().disable();
    }

    @Bean
    public FilterRegistrationBean ouath2ClientFilterRegistraction(OAuth2ClientContextFilter filter) {   //OAuth2 클라이언트용 시큐리티 필터인 매개변수를 불러와 올바른 순서로 필터가 작동하도록 설정
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(filter);
        registration.setOrder(-100);
        return registration;
    }

    private Filter oauth2Filter() {
        CompositeFilter filter = new CompositeFilter();
        List<Filter> filters = new ArrayList<>();
        filters.add(oauth2Filter(facebook(), "/login/facebook", FACEBOOK));
        filters.add(oauth2Filter(google(), "/login/google", GOOGLE));
        filters.add(oauth2Filter(kakao(), "/login/kakao", KAKAO));
        filter.setFilters(filters);
        return filter;
    }

    private Filter oauth2Filter(ClientResources client, String path, SocialType socialType) {
        OAuth2ClientAuthenticationProcessingFilter filter = new OAuth2ClientAuthenticationProcessingFilter(path);   //인증이 수행될 경로를 넣어 OAuth2 클라이언트용 인증 처리 필터를 생성
        OAuth2RestTemplate template = new OAuth2RestTemplate(client.getClient(), oAuth2ClientContext);  //권한 서버와의 통신을 위해 OAuth2RestTemplate생성 이를 생성하기 위해 client 프로퍼티 정보와 oAuth2ClientContext필요
        filter.setRestTemplate(template);
        filter.setTokenServices(new UserTokenService(client, socialType));
        filter.setAuthenticationSuccessHandler(((request, response, authentication) -> response.sendRedirect("/" + socialType.getValue() + "/complete")));  //인증이 성공적으로 이루어지면 필터에 리다이렉스될 URL을 설정
        filter.setAuthenticationFailureHandler(((request, response, exception) -> response.sendRedirect("/error")));    //인증이 실패하면 필터에 리다이렉트될 URL을 설정
        return filter;
    }

    @Bean
    @ConfigurationProperties("facebook")
    public ClientResources facebook() {
        return new ClientResources();
    }

    @Bean
    @ConfigurationProperties("google")
    public ClientResources google() {
        return new ClientResources();
    }

    @Bean
    @ConfigurationProperties("kakao")
    public ClientResources kakao() {
        return new ClientResources();
    }
}