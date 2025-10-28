package ca.uottawa.seg.otams.email;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailTask {
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final String senderAddress;
    private final String senderAppPassword;

    private EmailTask.Builder builder;

    public interface Callback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public static class Builder {
        private String to;
        private String subject;
        private String body;
        private Callback callback;

        public void setTo(String to) {
            this.to = to;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public void setCallback(Callback callback) {
            this.callback = callback;
        }

        public EmailTask build() {
            EmailTask et = new EmailTask("uottawaotams@gmail.com", "metljffwrucpefwn");
            et.builder = this;
            return et;
        }
    }

    private EmailTask(String senderAddress, String senderAppPassword) {
        this.senderAddress = senderAddress;
        this.senderAppPassword = senderAppPassword;
    }

    public static EmailTask.Builder builder() {
        return new EmailTask.Builder();
    }

    public void send() {
        executor.execute(() -> {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.trust", SMTP_HOST);

            // Create session with authentication
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EmailTask.this.senderAddress, EmailTask.this.senderAppPassword);
                }
            });

            // Create email message
            Message message = new MimeMessage(session);
            try {
                message.setFrom(new InternetAddress(this.senderAddress));
                message.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse(this.builder.to));
                message.setSubject(this.builder.subject);
                message.setText(this.builder.body);
                Transport.send(message);
                mainHandler.post(() -> {
                    if (this.builder.callback != null) {
                        this.builder.callback.onSuccess();
                    }
                });
            } catch (MessagingException e) {
                mainHandler.post(() -> {
                    if (this.builder.callback != null) {
                        this.builder.callback.onFailure(e);
                    }
                });
            }
        });
    }
}
