    package com.fiuni.mytube_users.service.subscription;

    import com.fiuni.mytube.domain.channel.ChannelDomain;
    import com.fiuni.mytube.domain.subscription.SubscriptionDomain;
    import com.fiuni.mytube.domain.user.UserDomain;
    import com.fiuni.mytube.dto.subscription.SubscriptionDTO;
    import com.fiuni.mytube.dto.subscription.SubscriptionResult;
    import com.fiuni.mytube_users.dao.ISubscriptionDao;
    import com.fiuni.mytube_users.dao.IUserDao;
    import com.fiuni.mytube_users.exception.BadRequestException;
    import com.fiuni.mytube_users.exception.ResourceNotFoundException;
    import com.fiuni.mytube_users.service.baseService.BaseServiceImpl;
    import com.fiuni.mytube_users.service.utils.MyTransactionService;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.cache.annotation.CacheEvict;
    import org.springframework.cache.annotation.CachePut;
    import org.springframework.cache.annotation.Cacheable;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import org.springframework.data.redis.cache.RedisCacheManager;
    import org.springframework.http.HttpStatus;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Propagation;
    import org.springframework.transaction.annotation.Transactional;
    import org.springframework.web.server.ResponseStatusException;
    import com.fiuni.mytube_users.exception.ResourceNotFoundException;

    import java.util.Date;
    import java.util.List;
    import java.util.stream.Collectors;

    @Service
    @Slf4j
    public class SubscriptionServiceImpl extends BaseServiceImpl<SubscriptionDTO, SubscriptionDomain, SubscriptionResult> implements ISubscriptionService {
        @Autowired
        private IUserDao userDao;
        @Autowired
        private ISubscriptionDao subscriptionDao;
        @Autowired
        private RedisCacheManager redisCacheManager;
        @Autowired
        private MyTransactionService myTransactionService;

        @Override
        protected SubscriptionDTO convertDomainToDto(SubscriptionDomain domain) {
            // Convertir SubscriptionDomain a SubscriptionDTO
            SubscriptionDTO dto = new SubscriptionDTO();
            dto.set_id(domain.getId());
            dto.setUserId(domain.getUser().getId());
            dto.setChannelId(1);
            dto.setSubscriptionDate(domain.getSubscriptionDate());
            return dto;
        }

        @Override
        protected SubscriptionDomain convertDtoToDomain(SubscriptionDTO dto) {
            // Convertir SubscriptionDTO a SubscriptionDomain
            SubscriptionDomain domain = new SubscriptionDomain();

            // Buscar el usuario por ID
            UserDomain user = userDao.findById(dto.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

            // Hardcodear el canal
            ChannelDomain channel = new ChannelDomain();
            channel.setId(1); // ID del canal harcodeado
            channel.setChannelName("Canal predeterminado"); // Nombre del canal hardcodeado

            // Asignar los valores a la suscripción
            domain.setUser(user);
            domain.setChannel(channel);
            //domain.setSubscriptionDate(dto.getSubscriptionDate() != null ? dto.getSubscriptionDate() : new Date());
            domain.setSubscriptionDate(dto.getSubscriptionDate());

            return domain;
        }

        @Override
        //@CachePut(value = "my_tube_subscriptions",key = "'subscription_'+ #result.get_id()")
        @Transactional(propagation = Propagation.NEVER, rollbackFor = Exception.class)
        public SubscriptionDTO save(SubscriptionDTO dto) {
            SubscriptionDomain domain = convertDtoToDomain(dto);

            // Guardar la suscripción en la base de datos
            SubscriptionDomain savedDomain = subscriptionDao.save(domain);
            log.warn("Suscripción guardada: " + savedDomain);

            // Llamar al metodo con Propagation.NEVER
            myTransactionService.setDateToday(savedDomain);

            // Convertir el dominio guardado de nuevo a DTO y devolverlo
            SubscriptionDTO result = convertDomainToDto(savedDomain);
            log.warn("Resultado: {}", result);
            return result;
        }

        @Override
        //@Cacheable(value = "my_tube_subscription",key = "'subsctiption'+#id")
        @Transactional(readOnly = true)
        public SubscriptionDTO getById(Integer id) {
            SubscriptionDomain domain = subscriptionDao.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Subscription con id: "+ id +" no encontrado"));

            log.info("subscription guardado en cache: {}", domain);
            return convertDomainToDto(domain);
        }

        @Override
        public SubscriptionResult getAll() {
            List<SubscriptionDomain> domains = subscriptionDao.findAll();

            // Crear una lista de DTOs a partir de los dominios
            List<SubscriptionDTO> dtos = domains.stream()
                    .map(this::convertDomainToDto)
                    .collect(Collectors.toList());

            // Crear el resultado con la lista de DTOs
            SubscriptionResult result = new SubscriptionResult();
            result.setSubscriptions(dtos);
            return result;
        }

        @Override
        public List<SubscriptionDTO> getUserSubscriptions(Integer userId) {
            // Obtener las suscripciones del usuario
            List<SubscriptionDomain> subscriptions = subscriptionDao.findByUser_Id(userId);

            // Convertir las suscripciones a DTOs
            List<SubscriptionDTO> subscriptionDTOs = subscriptions.stream()
                    .map(this::convertDomainToDto)
                    .collect(Collectors.toList());

            // Iterar sobre cada DTO y guardarlo en el caché
            for (SubscriptionDTO dto : subscriptionDTOs) {
                String cacheKey = "my_tube_subscriptions_user_" + dto.get_id(); // Crear la clave para cada suscripción
                redisCacheManager.getCache("my_tube_subscriptions_user").put(cacheKey, dto);
                log.info("Suscripción guardada en cache: {}", dto);
            }

            return subscriptionDTOs;
        }

        @Override
        //@CacheEvict(value = "my_tube_subscriptions", key = "'subscription_'+#id")
        @Transactional(propagation = Propagation.SUPPORTS)
        public void deleteSubscription(Integer id) {
            // Verificar si la suscripción existe
            SubscriptionDomain subscription = subscriptionDao.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Subscription con id: "+ id +" no encontrado"));

            // Eliminar la suscripción
            subscriptionDao.deleteById(id);
            log.info("Suscripcion eliminada con id "+ id);
        }

        @Override
        @Transactional(readOnly = true)
        public SubscriptionResult getAll(Pageable pageable) {
            Page<SubscriptionDomain> page = subscriptionDao.findAll(pageable);
            SubscriptionResult result = new SubscriptionResult();
            List<SubscriptionDTO> dtos = convertDomainListToDtoList(page.getContent());
            /*for(SubscriptionDTO dto : dtos) {
                redisCacheManager.getCache("my_tube_subscriptions").put("my_tube_subscriptions" + dto.get_id(), dto);
                log.info("en cache" + dto);
            }*/
            result.setSubscriptions(dtos);
            return result;
        }

        //FUNCIONES PARA LLAMADAS INDIRECTAS
        /*@Transactional(propagation = Propagation.NEVER) // Fuera de la transacción principal
        public void setDateToday(SubscriptionDomain subscription) {
            log.info("parametro: "+subscription.getSubscriptionDate());
            subscription.setSubscriptionDate(new Date());
            subscriptionDao.save(subscription);
        }*/
    }
