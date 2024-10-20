package com.fiuni.mytube_users.service.baseService;

import com.fiuni.mytube.dto.base.BaseDTO;
import com.fiuni.mytube.dto.base.BaseResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IBaseService<DTO extends BaseDTO, RESULT extends BaseResult<DTO>> {
    DTO getById(Integer id);
    RESULT getAll(Pageable pageable);
}
