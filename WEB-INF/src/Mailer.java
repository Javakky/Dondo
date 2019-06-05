package server;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.RandomStringUtils;

@SuppressWarnings("deprecation")
public class Mailer {

    final static String from = "dondo.info@gmail.com";
    final static String password = "tadakachu";

    final static String charset = "UTF-8";
    final static String encoding = "base64";

    public static String createPass(){
		return RandomStringUtils.randomAlphabetic(8);

    }

    public static void sendResetPassMail(String password, String adress){

    	String subject = "【Dondo】パスワードの変更完了のお知らせ";
    	String content = "いつもDondoをご利用頂き、誠にありがとうございます。\n"
    			        + "パスワードの変更処理が完了しました。\n\n"
    			        + "パスワード：" + password
    			        + "\n\n本メールに心当たりがないという場合は、お手数ですが下記お問い合わせ先よりご連絡ください。\n"
    			        + "お問い合わせ：" + from;
    	try {
			sendMail(subject, content, adress);
		} catch (MessagingException e) {
			e.printStackTrace();
		}

    }

	public static void sendRegistrationMail(String password, String adress){

    	String subject = "【Dondo】アカウント登録完了のお知らせ";
    	String content = "この度はDondoにご登録頂き、誠にありがとうございます。\n"
    			        + "新規アカウントの正式登録が完了致しました。\n\n"
    			        + "パスワード：" + password
    			        + "\n\n本メールに心当たりがないという場合は、お手数ですが下記お問い合わせ先よりご連絡ください。\n"
    			        + "お問い合わせ：" + from;
    	try {
			sendMail(subject, content, adress);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	public static void sendMail(String subject, String content, String... adress) throws MessagingException{

		if(adress == null || adress.length == 0){
			throw new MessagingException("アドレスが指定されていません");
		}

    	// for gmail
        String host = "smtp.gmail.com";
        String port = "587";
        String starttls = "true";

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", starttls);

        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");

        props.put("mail.debug", "true");

        Session session = Session.getInstance(props,
        new javax.mail.Authenticator() {
           protected PasswordAuthentication getPasswordAuthentication() {
              return new PasswordAuthentication(from, Mailer.password);
           }
        });

        try {
          MimeMessage message = new MimeMessage(session);

          // Set From:
          message.setFrom(new InternetAddress(from, "Dondo公式アカウント"));
          // Set ReplyTo:
          message.setReplyTo(new Address[]{new InternetAddress(from)});

          if(adress.length == 1){
        	  // Set To:
        	  message.setRecipient(Message.RecipientType.TO, new InternetAddress(adress[0]));
          }else{
        	  message.setRecipients(Message.RecipientType.BCC, createInternetAddressArray(adress));
          }

          message.setSubject(subject, charset);
          message.setText(content, charset);

          message.setHeader("Content-Transfer-Encoding", encoding);

          Transport.send(message);

        } catch (Exception e) {
          e.printStackTrace();;
        }
	}
		private static InternetAddress[] createInternetAddressArray(String[] adress) throws AddressException{

			InternetAddress[] array = new InternetAddress[adress.length];

			for(int i = 0; i < adress.length; i++){
				array[i] = new InternetAddress(adress[i]);
			}

			return array;

		}

}
