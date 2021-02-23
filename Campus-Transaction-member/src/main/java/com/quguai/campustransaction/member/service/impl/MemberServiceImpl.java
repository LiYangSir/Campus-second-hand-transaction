package com.quguai.campustransaction.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quguai.campustransaction.member.dao.MemberDao;
import com.quguai.campustransaction.member.entity.MemberEntity;
import com.quguai.campustransaction.member.entity.MemberLevelEntity;
import com.quguai.campustransaction.member.exception.PhoneNumberExistException;
import com.quguai.campustransaction.member.exception.UsernameExistException;
import com.quguai.campustransaction.member.service.MemberLevelService;
import com.quguai.campustransaction.member.service.MemberService;
import com.quguai.campustransaction.member.vo.SocialUser;
import com.quguai.campustransaction.member.vo.UserLoginVo;
import com.quguai.campustransaction.member.vo.UserRegister;
import com.quguai.common.utils.HttpUtils;
import com.quguai.common.utils.PageUtils;
import com.quguai.common.utils.Query;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.print.attribute.standard.MediaSize;
import java.util.HashMap;
import java.util.Map;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    private MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(UserRegister vo) {
        MemberEntity memberEntity = new MemberEntity();
        // 加载默认会员等级
        MemberLevelEntity levelEntity = memberLevelService.getDefaultLevel();
        // 设置默认等级Id
        memberEntity.setLevelId(levelEntity.getId());
        // 以异常的形式检查用户名和手机的唯一性
        checkUsernameUnique(vo.getUsername());
        checkPhoneUnique(vo.getPhone());
        // 设置用户基本信息
        memberEntity.setUsername(vo.getUsername());
        memberEntity.setNickname(vo.getUsername());
        memberEntity.setMobile(vo.getPhone());
        // 密码加密操作
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(vo.getPassword());
        memberEntity.setPassword(encode);
        // 保存用户信息
        this.save(memberEntity);
    }

    @Override
    public MemberEntity login(UserLoginVo userLoginVo) {
        // 对密码进行处理
        String loginAccount = userLoginVo.getLoginAccount();
        String loginPassword = userLoginVo.getLoginPassword();
        MemberEntity memberEntity = this.getOne(
                new QueryWrapper<MemberEntity>()
                        .eq("username", loginAccount)
                        .or()
                        .eq("mobile", loginAccount));
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        if (memberEntity != null && passwordEncoder.matches(loginPassword, memberEntity.getPassword())) {
            return memberEntity;
        }
        return null;
    }

    @Override
    public MemberEntity login(SocialUser socialUser) throws Exception {
        // 登陆注册两步走
        // 1. 判断是否注册过，如果是直接登录，否则先注册后登录
        String uid = socialUser.getUid();
        MemberEntity memberEntity = this.getOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
        if (memberEntity == null) {
            MemberEntity newMember = new MemberEntity();
            try {
                // 根据token获取的一些基本信息
                Map<String, String> request = new HashMap<>();
                request.put("access_token", socialUser.getAccess_token());
                request.put("uid", socialUser.getUid());
                // https://api.weibo.com/2/users/show.json?access_token=2.00x6yFQGNICBAC5917d5dedd2FSIMD&uid=5734642257
                HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new HashMap<>(), request);
                if (response.getStatusLine().getStatusCode() == 200) {
                    String s = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = JSON.parseObject(s);
                    String name = jsonObject.getString("name");
                    String gender = jsonObject.getString("gender");
                    newMember.setNickname(name);
                    newMember.setUsername(name);
                    newMember.setGender("m".equals(gender)? 1: 0);
                    // 其他一些基本信息:头像等等
                }
            } catch (Exception e) {
                // nothing
            }

            newMember.setSocialUid(socialUser.getUid());
            newMember.setAccessToken(socialUser.getAccess_token());
            newMember.setExpiresIn(socialUser.getExpires_in());
            this.save(newMember);
            return newMember;
        } else {
            // 进行登录操作, 更新令牌
            MemberEntity update = new MemberEntity();
            update.setId(memberEntity.getId());
            update.setAccessToken(socialUser.getAccess_token());
            update.setExpiresIn(socialUser.getExpires_in());
            this.updateById(update);
            memberEntity.setExpiresIn(socialUser.getExpires_in());
            memberEntity.setAccessToken(socialUser.getAccess_token());
            return memberEntity;
        }
    }

    private void checkPhoneUnique(String phone) throws PhoneNumberExistException {
        int count = this.count(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (count > 0) {
            throw new PhoneNumberExistException();
        }
    }

    private void checkUsernameUnique(String userName) throws UsernameExistException {
        int count = this.count(new QueryWrapper<MemberEntity>().eq("username", userName));
        if (count > 0) {
            throw new UsernameExistException();
        }
    }

}