package com.quguai.auth.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class UserRegister {

    @NotBlank(message = "用户名必须提交")
    @Length(min = 4, max = 8, message = "用户名必须大于4位，小于8位")
    private String username;

    @NotBlank(message = "密码必须填写")
    @Length(min = 6, max = 8, message = "密码必须是6-8位字符")
    private String password;

    @NotBlank(message = "手机号必须填写")
    @Pattern(regexp = "^[1]([3-9])[0-9]{9}$", message = "手机号格式错误")
    private String phone;

    @NotBlank(message = "验证码必须填写")
    private String code;
}
