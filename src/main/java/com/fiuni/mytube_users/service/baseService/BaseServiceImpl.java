package com.fiuni.mytube_users.service.baseService;

import com.fiuni.mytube.domain.base.BaseDomain;
import com.fiuni.mytube.dto.base.BaseDTO;
import com.fiuni.mytube.dto.base.BaseResult;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseServiceImpl<DTO extends BaseDTO, DOMAIN extends BaseDomain, RESULT extends BaseResult<DTO>>
        implements IBaseService<DTO, RESULT> {
    protected abstract DTO convertDomainToDto(DOMAIN domain);
    protected abstract DOMAIN convertDtoToDomain(DTO dto);

    protected List<DTO> convertDomainListToDtoList(List<DOMAIN> domainList) {
        return domainList.stream()
                .map(this::convertDomainToDto)
                .collect(Collectors.toList());
    }

    protected List<DOMAIN> convertDtoListToDomainList(List<DTO> dtoList) {
        return dtoList.stream()
                .map(this::convertDtoToDomain)
                .collect(Collectors.toList());
    }
}
