package com.quguai.campustransaction.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quguai.common.utils.PageUtils;
import com.quguai.campustransaction.member.entity.MemberEntity;

import java.util.Map;

/**
 * ??Ա
 *
 * @author liyang
 * @email liyang@gmail.com
 * @date 2021-01-20 21:47:04
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

