package com.springboot.web.repository;

import com.springboot.web.domain.Board;
import com.springboot.web.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {
    Board findByUser(User user);
}
