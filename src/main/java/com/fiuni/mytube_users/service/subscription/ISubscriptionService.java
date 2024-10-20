package com.fiuni.mytube_users.service.subscription;

import com.fiuni.mytube.dto.subscription.SubscriptionDTO;
import com.fiuni.mytube.dto.subscription.SubscriptionResult;
import com.fiuni.mytube_users.service.baseService.IBaseService;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ISubscriptionService extends IBaseService<SubscriptionDTO, SubscriptionResult> {
    List<SubscriptionDTO> getUserSubscriptions(Integer userId);
    void deleteSubscription(Integer id);
    SubscriptionResult getAll(Pageable pageable);
    SubscriptionDTO save(SubscriptionDTO subscriptionDTO);
}
