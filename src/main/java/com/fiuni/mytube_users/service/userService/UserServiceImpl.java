package com.fiuni.mytube_users.service.userService;

import com.fiuni.mytube.dto.user.UserDTO;
import com.fiuni.mytube.domain.user.UserDomain;
import com.fiuni.mytube.dto.user.UserResult;
import com.fiuni.mytube.dto.video.VideoDTO;
import com.fiuni.mytube_users.dao.IRoleDao;
import com.fiuni.mytube_users.dao.IUserDao;
import com.fiuni.mytube_users.dto.UserDTOCreate;
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

import java.util.Date;
import java.util.Optional;
import com.fiuni.mytube.domain.user.RoleDomain;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class UserServiceImpl extends BaseServiceImpl<UserDTO, UserDomain, UserResult> implements IUserService {

    @Autowired
    private IUserDao userDao;
    @Autowired
    private RedisCacheManager redisCacheManager;
    @Autowired
    private IRoleDao roleDao;

    @Autowired
    private MyTransactionService myTransactionService;

    @Override
    @Transactional
    @CachePut(value="my_tube_users", key="'user_'+#result.get_id()")
    public UserDTO save(UserDTO dto) {
        UserDomain userDomain = convertDtoToDomain(dto);
        // Guardar en la base de datos usando el repositorio
        userDomain = userDao.save(userDomain);
        log.info("usuario creado: " + userDomain);

        // Convertir de dominio a DTO y devolver el DTO guardado
        UserDTO result = convertDomainToDto(userDomain);

        log.info("Guardando en cache con clave: user_" + result.get_id());

        return result;
    }


    @Override
    @Transactional(readOnly = true)
    //@Cacheable(value = "my_tube_users",key = "'user_'+#id",unless = "#result == null" ) //unless para no poner objetos nulos en cache
    public UserDTO getById(Integer id) {
        UserDomain userDomain = userDao.findById(id)
                .orElse(null);
        log.info("usuario obtenido: " + userDomain);
        // Convertir de dominio a DTO y devolver
        return convertDomainToDto(userDomain);
    }

    @Override //VER DONDE ESTA LA CABECERA DE ESTE METODO
    public UserResult getAll() {
        return null;
    }

    @Override
    @Transactional(readOnly = true) //not supported
    public UserResult getAll(Pageable pageable) {
        // Obtener todos los usuarios no eliminados con paginación
        Page<UserDomain> page = userDao.findAllByDeletedFalse(pageable);
        UserResult result = new UserResult();
        List<UserDTO> userList = convertDomainListToDtoList(page.getContent());
        /*for(UserDTO u:userList){
            redisCacheManager.getCache("my_tube_users").put("user_"+u.get_id(),u);
            log.info("usuario en cache: " + u);
        }*/
        result.setUsers(userList);
        return result;
    }

    @Override
    protected UserDTO convertDomainToDto(UserDomain domain) {
        UserDTO dto = new UserDTO();
        dto.set_id(domain.getId());
        dto.setUsername(domain.getUsername());
        dto.setEmail(domain.getEmail());
        dto.setRegistrationDate(domain.getRegistrationDate());
        dto.setAvatarUrl(domain.getAvatarUrl());
        dto.setBio(domain.getBio());
        dto.setRoleName(String.valueOf(domain.getRole().getName()));
        return dto;
    }

    @Override
    protected UserDomain convertDtoToDomain(UserDTO dto) {
        UserDomain domain = new UserDomain();
        domain.setId(dto.get_id());
        domain.setUsername(dto.getUsername());
        domain.setEmail(dto.getEmail());
        domain.setRegistrationDate(dto.getRegistrationDate());
        domain.setAvatarUrl(dto.getAvatarUrl());
        domain.setBio(dto.getBio());
        RoleDomain roleDomain = roleDao.findByName(dto.getRoleName()).orElse(null);
        domain.setRole(roleDomain);
        domain.setDeleted(false);
        return domain;
    }


    @Override // cacheable no se puede porque pone en cache antes de la ejecucion
    //@CachePut(value = "mytube_users", key = "'user_'+ #result._id")
    @Transactional(rollbackFor = Exception.class) //rollback para todas las excepciones
    public UserDTO createUser (UserDTOCreate dto) {
        log.info("Iniciando creacion de usuario: "+dto);
        UserDomain userDomain = new UserDomain();

        userDomain.setUsername(dto.getUsername());
        userDomain.setEmail(dto.getEmail());
        userDomain.setPassword(dto.getPassword());
        //datos por defectos | NN
        userDomain.setBio("");
        userDomain.setRole(roleDao.findByName("new").orElse(null));
        userDomain.setDeleted(false);
        //guardar
        try{
            UserDomain result = userDao.save(userDomain);
            log.info("usuario creado: " + result);
            //asignar demas atributos por defecto
            assignDefaultAttribute(result);
            log.info("Creacion completada");
            return convertDomainToDto(result);
        }catch (Exception e){
            log.error("error durante la creacion, realizando rollback",e.getMessage());
            throw e;
        }
    }

    @Override
    //@Transactional(propagation = Propagation.REQUIRES_NEW)
    @Transactional(propagation = Propagation.NEVER)
    public void changePassword(Integer id, UserDTOCreate dto){
        UserDomain userDomain = userDao.findByIdAndDeletedFalse(id).orElse(null);
        userDomain.setPassword(dto.getPassword());
        setAttribute(userDomain);
        userDao.save(userDomain);

    }

    @Override
    //@Transactional(rollbackFor = Exception.class)
    @Transactional(propagation = Propagation.SUPPORTS)
    //@CacheEvict(value = "my_tube_users", key = "'user_'+#id")
    public void deleteUser(Integer id) {
        UserDomain userDomain = userDao.findByIdAndDeletedFalse(id).orElse(null);
        if (userDomain != null) {
            userDomain.setDeleted(true);
            userDao.save(userDomain);
            log.info("eliminando usuario: " + userDomain);
            log.info("");
            setAttributeDelete(userDomain);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado con el ID: " + id);
        }
    }

    //@Override
    //@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    //@CachePut(value = "my_tube_users", key = "'user_' + #id")
    @Override
    //@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    @Transactional(propagation = Propagation.NOT_SUPPORTED, rollbackFor = Exception.class)
    public UserDTO updateUser(Integer id, UserDTO dto) {
        // Buscar el usuario existente en la base de datos
        UserDomain userDomain = userDao.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado con el ID: " + id));

        // Actualizar los campos del usuario con los valores del DTO
        log.info("Actualizando datos de: "+userDomain);

        userDomain.setUsername(dto.getUsername());
        userDomain.setEmail(dto.getEmail());
        userDomain.setAvatarUrl(dto.getAvatarUrl()); // Actualización en la transacción principal
        userDomain.setBio(dto.getBio());
        userDomain.setRole(roleDao.findByName(dto.getRoleName()).orElse(null));
        userDomain.setRegistrationDate(dto.getRegistrationDate());

        // Guardar los cambios iniciales en la transacción principal
        userDao.save(userDomain);
        log.info("datos principales actualizados");
        try {
            // Llamar al metodo que requiere una nueva transacción para actualizar el avatarUrl
            log.info("llamando a setAttribute");
            myTransactionService.setAttributeUpdate(id);

        } catch (Exception e) {
            log.error("Error en setAttributeUpdate", e);
            throw e;
        }
        // Convertir el dominio actualizado a DTO y devolverlo
        return convertDomainToDto(userDomain);
    }



    //LLAMADAS INDIRECTAS
    @Transactional(propagation = Propagation.REQUIRED)
    public void assignDefaultAttribute(UserDomain userDomain) {
        log.info("Asignando valores por defecto a: "+userDomain);
        userDomain.setRegistrationDate(new Date());
        userDomain.setAvatarUrl("foto-generica.jpg");
        userDomain.setBio("nuevo usuario");
        userDomain.setRole(roleDao.findByName("regular").orElse(null));
        if(userDomain.getUsername().equals("errorTest")){
            log.error("Error simulado para el rollback en assignDefaultAttribute");
            throw new RuntimeException("error simulado para el rollback");
        }
        userDao.save(userDomain);
    }
   /* @Transactional(propagation = Propagation.REQUIRES_NEW)
    //@CachePut(value = "my_tube_users", key = "'user_' + #userDomain.getId()")
    public void setAttributeUpdate(Integer id) {
        UserDomain userDomain = userDao.findByIdAndDeletedFalse(id).orElse(null);
        log.warn("setAttributeUpdate ejecutado SIN transacción"+userDomain);
        userDomain.setAvatarUrl("url-final-ahora-si");
        if (userDomain.getUsername().equals("errorTest")) {
            log.error("parametro: "+userDomain);
            throw new RuntimeException("error simulado para el rollback de update");
        }
        userDao.save(userDomain);
        log.warn("llega a guardar despues de la excepcion");
    }*/
    @Transactional(propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
    public void setAttributeDelete(UserDomain userDomain) {
        userDomain.setBio("cuenta eliminada");
        userDomain.setAvatarUrl("url-de-perfil-eliminado");
        userDao.save(userDomain);
        if (userDomain.getUsername().equals("admin")) {
            throw new RuntimeException("error simulado para el rollback de delete");
        }
    }
    @Transactional(propagation = Propagation.MANDATORY)
    public void setAttribute(UserDomain userDomain) {
        userDomain.setBio("actualizacion de seguridad");
        if (userDomain.getUsername().equals("admin")) {
            throw new RuntimeException("error simulado para el rollback de delete");
        }
        userDao.save(userDomain);
    }
}
