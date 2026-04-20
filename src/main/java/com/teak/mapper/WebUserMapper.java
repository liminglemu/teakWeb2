package com.teak.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teak.model.WebUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * Web用户 Mapper
 */
@Mapper
public interface WebUserMapper extends BaseMapper<WebUser> {
}
