package com.teak.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teak.system.annotation.SnowflakeAlgorithm;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Created with: IntelliJ IDEA
 *
 * @Author: li zheng
 * @Date: 2026/4/20 10:40
 * @Project: teakWeb2
 * @File: WebUser.java
 * @Description:
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "web_user")
@Data
public class WebUser extends BaseModel implements Serializable {
    @SnowflakeAlgorithm
    private Long id;

    private String userName;

    private String userPassword;
}
