package com.teak.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Web用户 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebUserServiceImpl extends ServiceImpl<WebUserMapper, WebUser> implements IService<WebUser> {
}
