package com.quguai.auth.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class MemberResponseVo {

    private Long id;
    private String socialUid;
    private String accessToken;
    private Long expiresIn;
    private Long levelId;
    private String username;
    private String password;
    private String nickname;
    private String mobile;
    private String email;
    private String header;
    private Integer gender;
    private Date birth;
    private String city;
    private String job;
    private String sign;
    private Integer sourceType;
    private Integer integration;
    private Integer growth;
    private Integer status;
    private Date createTime;
}
