package com.springboot.web;

import com.springboot.web.domain.Board;
import com.springboot.web.domain.User;
import com.springboot.web.domain.enums.BoardType;
import com.springboot.web.repository.BoardRepository;
import com.springboot.web.repository.UserRepository;
import com.springboot.web.resolver.UserArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

@SpringBootApplication
public class WebApplication extends WebMvcConfigurerAdapter {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }

    @Autowired
    private UserArgumentResolver userArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(userArgumentResolver);
    }

    @Bean
    public CommandLineRunner runner(UserRepository userRepository, BoardRepository
            boardRepository) { //스프링은 빈으로 생성된 메서드의 파라미터로 DI(Dependency Injection) 시키는 매커니즘이 존재, 생성자를 통해 의존성을 주업 시키는 방법가 유사
        return (args) -> {     //이를 이용해 CommandLineRunner를 빈으로 등록한 후 UserRepository와 BoardRepository를 주입받는다.
            User user = userRepository.save(User.builder()  //User 객체를 빌더 패턴(Builder Paatern)을 사용해 생성한 후 주입받은 UserRepository를 주입받는다.
                .name("havi")
                .password("test")
                .email("havi@gmail.com")
                .createdDate(LocalDateTime.now())
                .build());

            IntStream.rangeClosed(1, 200).forEach(index -> boardRepository.save(Board.builder() //IntStream의 rangeClosed를 사용해 index 순서대로 Board 객체 200개를 생성하여 저장
                    .title("게시글"+index)
                    .subTitle("순서"+index)
                    .content("콘텐츠")
                    .boardType(BoardType.free)
                    .createdDate(LocalDateTime.now())
                    .updatedDate(LocalDateTime.now())
                    .user(user).build())       
            );
        };
    }
}
