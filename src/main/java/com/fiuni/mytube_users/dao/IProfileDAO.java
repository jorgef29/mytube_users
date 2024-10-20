package com.fiuni.mytube_users.dao;

import com.fiuni.mytube.domain.profile.ProfileDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IProfileDAO extends JpaRepository<ProfileDomain, Integer> {
    Optional<ProfileDomain> findByUserId(Integer userId);
}
