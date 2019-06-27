package com.debug.steadyjack.service;

import com.debug.steadyjack.config.MailProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * 邮件发送工具
 * SMTP(Simple Mail Transfer Protocol)即简单邮件传输协议
 */
@Service
public class MailService {

    @Autowired
    private MailProperties mailProperties;

    @Autowired
    private Environment env;

    /**
     * SMTP服务器配置及邮件发送
     * @throws Exception
     */
    public void sendEmail() throws Exception{

        //========================》连接邮件的参数配置《==============================
        Properties properties = new Properties();
        // 设置发件人的SMTP服务器地址
        properties.setProperty("mail.stmp.host", mailProperties.getHost());
        // 设置传输协议(JavaMail规范要求)
        properties.setProperty("mail.transport.protocol", mailProperties.getProtocol());
        //设置用户认证方式
        properties.setProperty("mail.smtp.auth", mailProperties.getNeedAuth());
        properties.setProperty("mail.smtp.socketFactory.class", mailProperties.getSslClass());
        //  设置SMTP服务器端口 一般填写：25
        properties.setProperty("mail.smtp.port", mailProperties.getPort()+"");

        // 创建定义整个应用程序所需的环境信息的 Session 对象
        /*Session session = Session.getDefaultInstance(properties);
        // 设置调试信息在控制台打印出来
        session.setDebug(true);*/ //第一种写法

        Authenticator auth=new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailProperties.getUserName(),mailProperties.getPassword());
            }
        };
        Session session = Session.getInstance(properties,auth); //第二种写法

        // ===================》创建邮件的实例对象《==========================
        MimeMessage mimeMessage = new MimeMessage(session);
        //设置发件人地址
        mimeMessage.setFrom(env.getProperty("mail.from"));
        //设置邮件主题
        mimeMessage.setSubject(env.getProperty("mail.subject"));
        //设置邮件正文
        mimeMessage.setContent(env.getProperty("mail.content"), "text/html;charset=utf-8");

        //TODO：批量发送多个收件人
        String arr[]=env.getProperty("mail.to").split(",");
        Address[] addresses=new Address[arr.length];
        for(int i=0;i<arr.length;i++){
            addresses[i]=new InternetAddress(arr[i]);
        }
        //设置多个收件人地址(并抄送给自己，发件人)
        mimeMessage.addRecipients(Message.RecipientType.TO, addresses);
        //抄送自己，不抄送自己会报错，发送失败
        mimeMessage.addRecipients(Message.RecipientType.CC, "17661977890@163.com");

        //TODO：只发送一个收件人
        //mimeMessage.addRecipients(Message.RecipientType.TO, "1974544863@qq.com");
        //mimeMessage.addRecipients(Message.RecipientType.CC, "linsenzhong@126.com");

        //===============================》发送《============================================
        // 根据session对象获取邮件传输对象Transport
        Transport transport = session.getTransport();
        // 设置发件人的账户名和密码并连接
        transport.connect(mailProperties.getHost(), mailProperties.getUserName(), mailProperties.getPassword());
        // 发送邮件，并发送到所有收件人地址，message.getAllRecipients() 获取到的是在创建邮件对象时添加的所有收件人, 抄送人, 密送人
        transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
        // 关闭邮件连接
        transport.close();


    }


}