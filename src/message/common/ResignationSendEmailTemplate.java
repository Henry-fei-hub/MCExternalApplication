package message.common;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

import com.sun.mail.util.MailSSLSocketFactory;

/**
 *
 * @author cl
 */
public class ResignationSendEmailTemplate extends Thread {
    
	public static  String fromTitleCn = "ERP";
    public static  String hostName = "smtp.exmail.qq.com"; // 发送方smtp
    public static  String fromEmail = "hello@jaid.cn"; //发送方的邮箱用户名
    public static  String password = "JA884008";//密码
    private static String host = "smtp.exmail.qq.com";		//服务器地址（邮件服务器）
    private static String port = "465";		//端口
    private static String protocol = "smtp"; //协议

    private static MimeMessage mimeMessage;

    private static final Logger __logger = Logger.getLogger(ResignationSendEmailTemplate.class);
    
//    public static final int SEND_TYPE_ONE = 1;
    
    /**
     * 收件人邮箱
     */
    private final String toEmail;
    /**
     * 附带参数
     */
//    private final String attrah;
    /**
     * 发送邮件内容
     */
    private final String htmlmsg;
    /**
     * 邮件主题
     */
    private final String subject;
    
    /**
 	* 用户名密码验证，需要实现抽象类Authenticator的抽象方法PasswordAuthentication，
    * SMTP验证类(内部类)，继承javax.mail.Authenticator
    */
    static class MyAuthenricator extends Authenticator {
    	String username = null;
        String password = null;
        public MyAuthenricator(String username,String password){
            this.username=username;
            this.password=password;
        }
        
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username,password);
        }
    }

    @Override
    public void run() {
    	
    	__logger.info(subject + "邮件发送线程   开启");
    	
    	Properties prop = new Properties();
        // 协议
        prop.setProperty("mail.transport.protocol", protocol);
        // 服务器
        prop.setProperty("mail.smtp.host", host);
        // 端口
        prop.setProperty("mail.smtp.port", port);
        
        prop.setProperty("mail.smtp.socketFactory.port", port);
//        prop.setProperty("mail.smtp.port", 587);
        // 使用smtp身份验证
        prop.setProperty("mail.smtp.auth", "true");
        
        /**
    	 * 背景
    	 * 在使用javamail进行邮件发送的时候，报错：
    	 * Could not connect to SMTP host: smtp.***.com, port: 465, response: -1
    	 * 原因：
    	 * 465端口是为SMTPS（SMTP-over-SSL）协议服务开放的，这是SMTP协议基于SSL安全协议之上的一种变种协议。
    	 * 解决：
    	 * 加上如下代码即可：
    	 */
        prop.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        
        prop.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");

        //使用SSL，企业邮箱必需！
        //开启安全协议
        MailSSLSocketFactory sf = null;
        try {
            sf = new MailSSLSocketFactory();
            sf.setTrustAllHosts(true);
        } catch (GeneralSecurityException e1) {
            e1.printStackTrace();
        }

        prop.put("mail.smtp.ssl.enable", "true");
        prop.put("mail.smtp.ssl.socketFactory", sf);

        Session session = Session.getDefaultInstance(prop, new MyAuthenricator(fromEmail, password));
        // 开启DEBUG模式,在控制台中或日志中有日志信息显示，也就是可以从控制台中看一下服务器的响应信息；
        //session.setDebug(true);
        mimeMessage = new MimeMessage(session);

        try {
            //发件人
			mimeMessage.setFrom(new InternetAddress(fromEmail,"ERP"));
            //收件人
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            //主题
            mimeMessage.setSubject(subject);
            //时间
            mimeMessage.setSentDate(new Date());

            //仅仅发送文本
           
           // mimeMessage.setContent(htmlmsg, "text/html");
            Multipart multipart = new MimeMultipart("related");

            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setText(htmlmsg, "utf-8", "html");
            multipart.addBodyPart(htmlPart);
            mimeMessage.setContent(multipart);
            //mimeMessage.setText(htmlmsg,"UTF-8");
            mimeMessage.saveChanges();
            Transport.send(mimeMessage);
            __logger.info(subject + "邮件发送成功   关闭");
        } catch (MessagingException e) {
        	 __logger.error(subject + "邮件发送线程   失败" + e.getMessage());
        }catch (UnsupportedEncodingException e) {
        	__logger.error(subject + "邮件发送线程   失败" + e.getMessage());
		}
//        try{
//            __logger.info(subject + "邮件发送线程   开启");
//            HtmlEmail email = new HtmlEmail();
//            email.setHostName(hostName);
//            email.setAuthentication(fromEmail, password);
//            email.setSmtpPort(587);
//            email.addTo(toEmail);//接受方的邮箱地址，邮箱用户名
//            email.setCharset("utf-8");
//            email.setFrom(fromEmail, fromTitleCn);//发送方的邮箱地址，邮箱用户名
//            email.setSubject(subject);
//            email.setHtmlMsg(htmlmsg);
//            email.send();
//            __logger.info(subject + "邮件发送成功   关闭");
//        }catch(EmailException e){
//            __logger.error(subject + "邮件发送线程   失败" + e.getMessage());
//        }
        
    }

    
    /**
     * 设置参数
     * @param toEmail 收件人
     * @param htmlmsg 发送邮箱的内容
     * @param subject  主题
     */
    public ResignationSendEmailTemplate(String toEmail, String htmlmsg, String subject){
        this.toEmail = toEmail;
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
        ResignationSendEmailTemplate th = new ResignationSendEmailTemplate(toEmail,  htmlmsg, subject);
        th.start();
    }
    
}
