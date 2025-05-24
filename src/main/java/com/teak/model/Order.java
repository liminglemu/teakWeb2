package com.teak.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created with: IntelliJ IDEA
 *
 * @Author: li zheng
 * @Date: 2025/5/23 09:36
 * @Project: teakWeb2
 * @File: Order.java
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long orderId;
    private String userId;
    private BigDecimal amount;
    private String status;
}
