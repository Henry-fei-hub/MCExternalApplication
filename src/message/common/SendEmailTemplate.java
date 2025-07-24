package message.common;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.log4j.Logger;

/**
 *
 * @author cl
 */
public class SendEmailTemplate extends Thread {

    public static  String fromTitleCn = "ERP";
    public static  String hostName = "smtp.exmail.qq.com"; // 发送方smtp
    public static  String fromEmail = "hello@jaid.cn"; //发送方的邮箱用户名
    public static  String password = "JA884008";//密码

    private static final Logger __logger = Logger.getLogger(SendEmailTemplate.class);

    public static final int SEND_TYPE_ONE = 1;

    /**
     * 收件人邮箱
     */
    private final String toEmail;
    /**
     * 附带参数
     */
    private final String attrah;
    /**
     * 发送邮件内容
     */
    private final String htmlmsg;
    /**
     * 邮件主题
     */
    private final String subject;

    @Override
    public void run() {
        try{
            __logger.info(subject + "邮件发送线程   开启");
            HtmlEmail email = new HtmlEmail();
            email.setHostName(hostName);
            email.setAuthentication(fromEmail, password);
            email.addTo(toEmail);//接受方的邮箱地址，邮箱用户名
            email.setCharset("utf-8");
            email.setFrom(fromEmail, fromTitleCn);//发送方的邮箱地址，邮箱用户名
            email.setSubject(subject);
            email.setHtmlMsg(htmlmsg);
            email.send();
            __logger.info(subject + "邮件发送成功   关闭");
        }catch(EmailException e){
            __logger.error(subject + "邮件发送线程   失败" + e.getMessage());
        }
    }


    /**
     * 设置参数
     * @param toEmail
     * @param attrah
     * @param htmlmsg
     * @param subject
     */
    public SendEmailTemplate(String toEmail, String attrah, String htmlmsg, String subject){
        this.toEmail = toEmail;
        this.attrah = attrah;
        this.htmlmsg = htmlmsg;
        this.subject = subject;
    }
    /**
     * 邮件发送启动线程
     * @param toEmail
     * @param attrah
     * @param htmlmsg
     * @param subject
     */
    public static void sendEmail(String toEmail, String attrah, String htmlmsg, String subject){
        SendEmailTemplate th = new SendEmailTemplate(toEmail, attrah, htmlmsg, subject);
        th.start();
    }

}
