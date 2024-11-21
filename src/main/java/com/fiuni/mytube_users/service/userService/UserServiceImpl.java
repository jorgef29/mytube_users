package com.fiuni.mytube_users.service.userService;

import com.fiuni.mytube.domain.profile.ProfileDomain;
import com.fiuni.mytube.dto.user.UserDTO;
import com.fiuni.mytube.domain.user.UserDomain;
import com.fiuni.mytube.dto.user.UserResult;
import com.fiuni.mytube_users.dao.IProfileDAO;
import com.fiuni.mytube_users.dao.IRoleDao;
import com.fiuni.mytube_users.dao.IUserDao;
import com.fiuni.mytube_users.dto.UserDTOComplete;
import com.fiuni.mytube_users.dto.UserDTOCreate;
import com.fiuni.mytube_users.service.baseService.BaseServiceImpl;
import com.fiuni.mytube_users.util.DateUtil;
import com.fiuni.mytube_users.util.exception.ResourceNotFoundException;
import com.fiuni.mytube_users.util.exception.UserAlreadyExistsException;
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

import java.util.ArrayList;
import java.util.Date;

import com.fiuni.mytube.domain.user.RoleDomain;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
@Slf4j
public class UserServiceImpl extends BaseServiceImpl<UserDTO, UserDomain, UserResult> implements IUserService {

    @Autowired
    private IUserDao userDao;
    @Autowired
    private IProfileDAO profileDAO;
    @Autowired
    private RedisCacheManager redisCacheManager;
    @Autowired
    private IRoleDao roleDao;


    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "my_tube_users",key = "'user_'+#id",unless = "#result == null" ) //unless para no poner objetos nulos en cache
    public UserDTO getById(Integer id) {
        UserDomain userDomain = userDao.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("User no encontrado: "+ id));
        log.info("usuario obtenido: " + userDomain);
        return convertDomainToDto(userDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResult getAll(Pageable pageable) {
        // Obtener todos los usuarios no eliminados con paginación
        Page<UserDomain> page = userDao.findAllByDeletedFalse(pageable);
        UserResult result = new UserResult();
        List<UserDTO> userList = convertDomainListToDtoList(page.getContent());
        for(UserDTO u:userList){
            redisCacheManager.getCache("my_tube_users").put("user_"+u.get_id(),u);
            log.info("usuario en cache: " + u);
        }
        result.setUsers(userList);
        return result;
    }

    @Override
    protected UserDTO convertDomainToDto(UserDomain domain) {
        UserDTO dto = new UserDTO();
        dto.set_id(domain.getId());
        dto.setUsername(domain.getUsername());
        dto.setEmail(domain.getEmail());
        dto.setRoleName(String.valueOf(domain.getRole().getName()));
        return dto;
    }
    private UserDTOComplete convertDomainToDtoComplete(UserDomain user, ProfileDomain profile) {
        log.info("perfil recibido: " + profile);
        UserDTOComplete dto = new UserDTOComplete();
        dto.set_id(profile.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setAvatarUrl(profile.getAvatarUrl());
        dto.setBio(profile.getBio() != null ? profile.getBio().toString() : "");
        dto.setBirthday(DateUtil.formatDate(profile.getBirthday()));
        dto.setRegistrationDate(DateUtil.formatDate(profile.getRegistrationDate()));
        log.info("usuario final: ",dto);
        return dto;
    }

    @Override
    protected UserDomain convertDtoToDomain(UserDTO dto) {
        UserDomain domain = new UserDomain();
        domain.setId(dto.get_id());
        domain.setUsername(dto.getUsername());
        domain.setEmail(dto.getEmail());
        RoleDomain roleDomain = roleDao.findByName(dto.getRoleName()).orElse(null);
        domain.setRole(roleDomain);
        domain.setDeleted(false);
        return domain;
    }



    @Override // cacheable no se puede porque pone en cache antes de la ejecucion
    @CachePut(value = "mytube_users", key = "'user_'+ #result._id")
    public UserDTO createUser (UserDTOCreate dto) {
        if(userDao.existsByEmailAndDeletedFalse(dto.getEmail()) ||
        userDao.existsByUsernameAndDeletedFalse(dto.getUsername())){
            throw new UserAlreadyExistsException("El usuario ya existe");
        }
        UserDomain userDomain = new UserDomain();

        userDomain.setUsername(dto.getUsername());
        userDomain.setEmail(dto.getEmail());
        userDomain.setPassword(dto.getPassword());
        //por defecto role regular
        userDomain.setRole((roleDao.findByName("regular").orElse(null)));
        userDomain.setDeleted(false);
        UserDomain result = userDao.save(userDomain);

        //creacion de perfil
        ProfileDomain profile = new ProfileDomain();
        profile.setUser(userDomain);
        profile.setRegistrationDate(new Date());
        profileDAO.save(profile);

        return convertDomainToDto(result);
    }

    @Override
    public void changePassword(Integer id, UserDTOCreate dto){
        UserDomain userDomain = userDao.findByIdAndDeletedFalse(id).orElse(null);
        userDomain.setPassword(dto.getPassword());
        userDao.save(userDomain);
    }

    @Override
    @Transactional
    @CacheEvict(value = "my_tube_users", key = "'user_'+#id")
    public void deleteUser(Integer id) {
        UserDomain userDomain = userDao.findByIdAndDeletedFalse(id).orElse(null);
        if (userDomain != null) {
            userDomain.setDeleted(true);
            userDao.save(userDomain);
        } else {
            throw new ResourceNotFoundException("Usuario con id: "+id+" no encontrado");
        }
    }

    //@Override
    @Transactional
    //@CachePut(value = "my_tube_users_complete", key = "'user_complete_'+#id")
    public UserDTOComplete updateUser(Integer id, UserDTOComplete dto) {
        // Buscar el usuario existente en la base de datos
        UserDomain userDomain = userDao.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado con el ID: " + id));

        ProfileDomain profileDomain = profileDAO.findByUserId(id)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil no encontrado"));
        // Actualizar los campos del usuario con los valores del DTO
        userDomain.setUsername(dto.getUsername());
        userDomain.setEmail(dto.getEmail());

        // Guardar los cambios en la base de datos
        UserDomain updatedDomain = userDao.save(userDomain);

        profileDomain.setBio(dto.getBio());
        profileDomain.setAvatarUrl(dto.getAvatarUrl());
        profileDomain.setBirthday(DateUtil.parseToDate(dto.getBirthday()));
        //profileDomain.setRegistrationDate(dto.getRegistrationDate());
        ProfileDomain updateProfileDomain= profileDAO.save(profileDomain);

        // Convertir el dominio actualizado a DTO y devolverlo
        //return convertDomainToDto(updatedDomain);
        return convertDomainToDtoComplete(updatedDomain, updateProfileDomain);
    }

    @Transactional(readOnly = true)
    //@Cacheable(value = "my_tube_users_complete", key = "'user_complete_'+#id", unless = "#result == null")
    public UserDTOComplete getUserByIdComplete(Integer id) {
        // Primero buscamos el perfil utilizando el ID del usuario
        ProfileDomain profileDomain = profileDAO.findByUserId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil no encontrado para el usuario: " + id));

        // Luego, obtenemos el usuario a través del perfil encontrado
        UserDomain userDomain = userDao.findByIdAndDeletedFalse(profileDomain.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User no encontrado: " + id));

        // Convertimos ambos dominios en un UserDTOComplete
        UserDTOComplete complete = convertDomainToDtoComplete(userDomain, profileDomain);
        return complete;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "my_tube_users_complete", key = "'user_complete_all'", unless = "#result.isEmpty()")
    public List<UserDTOComplete> getAllUserDTOComplete() {
        List<ProfileDomain> profiles = profileDAO.findAll();
        List<UserDTOComplete> dtoCompleteList = new ArrayList<>();

        for (ProfileDomain profile : profiles) {
            UserDomain user = profile.getUser();
            UserDTOComplete dtoComplete = convertDomainToDtoComplete(user, profile);
            dtoCompleteList.add(dtoComplete);
        }

        return dtoCompleteList;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getIdByEmail(String email) {
        UserDomain userDomain = userDao.findByEmailAndDeletedFalse(email).orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return userDomain.getId();
    }

}
