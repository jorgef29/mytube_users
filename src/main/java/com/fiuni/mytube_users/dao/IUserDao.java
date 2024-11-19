package com.fiuni.mytube_users.dao;

import com.fiuni.mytube.domain.user.UserDomain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IUserDao extends JpaRepository<UserDomain, Integer> {
    Page<UserDomain> findAllByDeletedFalse(Pageable pageable);
    Optional<UserDomain> findByIdAndDeletedFalse(Integer id);
    boolean existsByEmailAndDeletedFalse(String email);
    boolean existsByUsernameAndDeletedFalse(String email);
    Optional <UserDomain> findByEmailAndDeletedFalse(String email);
}
