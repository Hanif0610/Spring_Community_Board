package com.springboot.web.service;

import com.springboot.web.domain.Board;
import com.springboot.web.repository.BoardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class BoardService {

    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    public Page<Board> findBoardList(Pageable pageable) {
        pageable = new PageRequest(pageable.getPageNumber() <= 0 ? 0 :   //pageable로 넘어온 pageNumber객체가 0이하일 때 0으로 초기화
                pageable.getPageNumber() - 1, pageable.getPageSize());  //기본 페이지 크기인 10으로 새로운 PageRequest 객체를 만들어 페이징 처리된 게시글 리스트 변환
        return boardRepository.findAll(pageable);
    }

    public Board findBoardByIdx(Long idx) {
        return boardRepository.findOne(idx);       //board의 idx 값을 사용하여 board 객체 변환
    }

    public Board saveAndUpdateBoard(Board board) {
        return boardRepository.save(board);
    }
}