package com.lingdonge.auth.entity;

import com.lingdonge.core.bean.base.BaseEntity;
import lombok.Data;

/**
 * auth 实体，用于承载Shiro里面装的数据
 */
@Data
public class ShiroInfo extends BaseEntity {

    /**
     * 用户ID信息
     */
    private Integer id;

    /**
     * 账号信息，可以是手机号，邮箱等，需要唯一性验证判断
     */
    private String account;

    /**
     * 密码信息
     */
    private String password;//密码

    /**
     * 用户状态信息，一般0未开通，1开通，2禁用
     */
    private boolean active;

    /**
     * 加密盐，用于加密密码
     */
    private String salt;

}
