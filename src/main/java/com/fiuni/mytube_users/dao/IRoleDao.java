package com.fiuni.mytube_users.dao;

import com.fiuni.mytube.domain.user.RoleDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IRoleDao extends JpaRepository<RoleDomain,Integer> {
    //findByName
    Optional<RoleDomain> findByName(String name);
}
