package com.lingdonge.push.configuration;


import com.lingdonge.push.bean.PushSenderAccount;
import com.lingdonge.push.configuration.properties.PushProperties;
import com.lingdonge.push.service.PushCodeSendFactory;
import com.lingdonge.push.service.PushCodeSender;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
@EnableConfigurationProperties(PushProperties.class)
public class MessagePushAutoConfiguration {

    @Resource
    private PushProperties pushProperties;

    /**
     * 创建发送器的Bean
     *
     * @return
     */
    @Bean("pushCodeSender")
    public PushCodeSender getDefaultSender() {
        return PushCodeSendFactory.getPushSender(pushProperties.getDefaultSender());
    }

    /**
     * 获取配置文件中默认的账号信息
     *
     * @return
     */
    @Bean("pushSenderAccount")
    public PushSenderAccount getDefaultSmsSendAccount() {
        return pushProperties.getPushSenderAccount();
    }
}


