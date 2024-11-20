package com.fiuni.mytube_users.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.fiuni.mytube.domain.subscription.*;

import java.util.List;

public interface ISubscriptionDao extends JpaRepository<SubscriptionDomain, Integer> {
    List<SubscriptionDomain> findByUser_Id(Integer userId);
    List<SubscriptionDomain> findByChannel_Id(Integer channelId);
}
