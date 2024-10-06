package com.fiuni.mytube_users.service.utils;

import com.fiuni.mytube.domain.subscription.SubscriptionDomain;
import com.fiuni.mytube.domain.user.UserDomain;
import com.fiuni.mytube_users.dao.ISubscriptionDao;
import com.fiuni.mytube_users.dao.IUserDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Service
public class MyTransactionService {
    @Autowired
    private IUserDao userDao;
    @Autowired
    private ISubscriptionDao subscriptionDao;

    //@Transactional(propagation = Propagation.MANDATORY, rollbackFor = Exception.class)
    //@Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void setAttributeUpdate(Integer id) {
        UserDomain userDomain = userDao.findByIdAndDeletedFalse(id).orElse(null);
        log.warn("setAttributeUpdate ejecutado SIN transacción: " + userDomain);

        // Actualización del avatarUrl dentro de una nueva transacción
        userDomain.setAvatarUrl("url-actualizado");
        log.warn("Actualizando avatarUrl a: " + userDomain.getAvatarUrl());

        // Simular un error si el nombre del usuario es 'errorTest'
        if (userDomain.getUsername().equals("errorTest")) {
            log.error("parametro: " + userDomain);
            throw new RuntimeException("error simulado para el rollback de update");
        }
        userDao.save(userDomain); // Guardar el cambio en la base de datos
        log.warn("Avatar URL guardado: " + userDomain.getAvatarUrl());
    }

    @Transactional(propagation = Propagation.NEVER)
    public void setDateToday(SubscriptionDomain subscription){
        subscription.setSubscriptionDate(new Date());
        subscriptionDao.save(subscription);
    }
}
