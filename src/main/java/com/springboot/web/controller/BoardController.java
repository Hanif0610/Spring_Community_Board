package com.springboot.web.controller;

import com.springboot.web.service.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/board")       //API URI 경로를 '/board'로 정의한다.
public class BoardController {

    @Autowired
    BoardService boardService;  //boardService 의존성을 주입해야 하므로 Autowired 사용

    @GetMapping({"", "/"})      //매핑 경로를 중괄호를 사용하여 여러 개를 받을 수 있다.
    public String board(@RequestParam(value = "idx", defaultValue = "0") Long idx, Model model) {   //@RequestParam 어노테이션을 idx 파라미터를 필수로 받는다.
        model.addAttribute("board", boardService.findBoardByIdx(idx));                 //만약 바인딩할 값이 없으면 기본값 '0'으로 설정된다.
        return "/board/form";                                                                       //findByIdx(idx)로 조회 시 idx 값을 '0'으로 조회하면 board 값은 null 반환
    }

    @GetMapping("/list")
    public String list(@PageableDefault Pageable pageable, Model model) {   //@PageableDefault 어노테이션의 파라미터인 size, sort, direction 등을 사용하여 페이징 처리에 대한 규약 정의
        model.addAttribute("boardList", boardService.findBoardList(pageable));
        return "board/list";                                                //src/resources/templates를 기준으로 데이터를 바인딩할 타깃의 뷰 경로 지정
    }
}
