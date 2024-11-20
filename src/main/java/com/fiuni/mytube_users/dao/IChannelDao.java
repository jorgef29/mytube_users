package com.fiuni.mytube_users.dao;

import com.fiuni.mytube.domain.channel.ChannelDomain;
import com.fiuni.mytube.dto.channel.ChannelDTO;
import com.fiuni.mytube.dto.channel.ChannelResult;
import com.fiuni.mytube_users.service.baseService.IBaseService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IChannelDao extends JpaRepository<ChannelDomain, Integer> {
}
