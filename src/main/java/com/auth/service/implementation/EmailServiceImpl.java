package com.auth.service.implementation;

import com.auth.dto.request.EmailDetails;
import com.auth.exception.EmailServiceException;
import com.auth.service.interfaces.IEmailService;
import com.auth.utils.EmailMessageUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;

import static com.auth.utils.EmailMessageUtils.*;

@Service
public class EmailServiceImpl implements IEmailService {
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final AuthServiceImpl authService ;

    public EmailServiceImpl(JavaMailSender javaMailSender, TemplateEngine templateEngine, AuthServiceImpl authService) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
        this.authService  = authService ;
    }

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String emailSender;

    @Value("${backend.url}")
    private String backendUrl;

    /**
     * Sends an email for password recovery.
     *
     * @param toEmail          The recipient's email address.
     * @param resetPasswordLink The link to reset the password.
     * @throws EmailServiceException if an error occurs while sending the email.
     */
    @Override
    public void sendPasswordRecoveryEmail(String toEmail, String resetPasswordLink) {
        try {
            String userName = getUserNameByEmail(toEmail);
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(emailSender, SUPPORT_NAME);
            helper.setTo(toEmail);
            helper.setSubject(PASSWORD_RECOVERY_SUBJECT);

            Context context = new Context();
            context.setVariable("resetPasswordLink", resetPasswordLink);
            context.setVariable("userName", userName);
            String htmlContent = templateEngine.process(PASSWORD_RECOVERY_TEMPLATE, context);

            helper.setText(htmlContent, true);
            javaMailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new EmailServiceException("Failed to send password recovery email", e);
        }
    }

    /**
     * Sends a generic email with the provided subject and content.
     *
     * @param subject    The subject of the email.
     * @param emailDetails    The email details.
     * @param redirectLink   The link to redirect the user to.
     * @throws EmailServiceException if an error occurs while sending the email.
     */
    @Override
    public void sendEmail(String subject, EmailDetails emailDetails, String redirectLink) {
        validateEmailParameters(emailDetails.toEmail(), subject, emailDetails.userName(), emailDetails.senderName());
        String emailTitle, messageBody, extraMessage, callToActionMessage = null;
        switch (subject) {
            case REGISTRATION_CONFIRMATION_SUBJECT:
                emailTitle = "Confirmación de Registro";
                messageBody = "Gracias por registrarte en Financia.al. Estamos encantados  e que formes parte de nuestra comunidad.";
                callToActionMessage = "Para completar tu registro y comenzar a disfrutar de nuestros servicios, por favor confirma tu dirección de correo electrónico haciendo clic en el siguiente enlace:";
                extraMessage = "Este paso nos permite garantizar la seguridad de tu cuenta y mantener una comunicación confiable contigo.";
                break;

            case PASSWORD_CHANGE_CONFIRMATION_SUBJECT:
                emailTitle = "Confirmación de Cambio de Contraseña";
                messageBody = "Tu contraseña ha sido actualizada con éxito en Financial Al.";
                extraMessage = "Si no solicitaste este cambio, por favor, contacta con nuestro soporte de inmediato.";
                break;

            default:
                throw new IllegalArgumentException("Tipo de email no soportado: " + subject);
        }
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(emailSender, emailDetails.senderName());
            helper.setTo(emailDetails.toEmail());
            helper.setSubject(subject);

            Context context = new Context();
            context.setVariable("emailTitle", emailTitle);
            context.setVariable("userName", emailDetails.userName());
            context.setVariable("messageBody", messageBody);
            context.setVariable("extraMessage", extraMessage);
            context.setVariable("callToActionMessage", callToActionMessage);
            context.setVariable("redirectLink", redirectLink);
            String htmlContent = templateEngine.process(CONFIRMATION_TEMPLATE, context);

            helper.setText(htmlContent, true);
            javaMailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e ) {
            throw new EmailServiceException("Failed to send password change confirmation email", e);
        }
    }

    @Override
    public void sendPasswordChangeConfirmationEmail(String toEmail) {
        String userName = getUserNameByEmail(toEmail);
        EmailDetails emailDetails = new EmailDetails(toEmail, userName, SUPPORT_NAME);
        sendEmail(PASSWORD_CHANGE_CONFIRMATION_SUBJECT, emailDetails, null);
    }

    @Async
    @Override
    public void sendLoanStatusUpdateEmail(String toEmail, String userName, String subject) {
        validateEmailParameters(toEmail, subject, userName, ADMIN_NAME);
        String emailTitle, messageBody, extraMessage, callToActionMessage, buttonText = null;
        switch (subject) {
            case "INITIATED":
                emailTitle = "";
                messageBody = ".";
                extraMessage = ".";
                callToActionMessage = ":";
                buttonText = "Acceder a mi cuenta";
                break;

            case "PRE_APPROVED":
                emailTitle = "";
                messageBody = ".";
                extraMessage = ". ";
                callToActionMessage = ": ";
                buttonText = "";
                break;

            case "APPROVED":
                emailTitle = "";
                messageBody = ". ";
                extraMessage = ".";
                callToActionMessage = "Accede aquí para más información:";
                buttonText = "Acceder a mi cuenta";
                break;

            case "REFUSED":
                emailTitle = "";
                messageBody = ".";
                extraMessage = ".";
                callToActionMessage = ":";
                buttonText = "Acceder a mi cuenta";
                break;

            case "PENDING":
                emailTitle = "";
                messageBody = ".";
                extraMessage = ".";
                callToActionMessage = ": ";
                buttonText = "Acceder a mi cuenta";
                break;

            default:
                throw new IllegalArgumentException("Tipo de email no soportado: " + subject);
        }
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(emailSender, ADMIN_NAME);
            helper.setTo(toEmail);
            helper.setSubject(subject);

            Context context = new Context();
            context.setVariable("emailTitle", emailTitle);
            context.setVariable("userName", userName);
            context.setVariable("messageBody", messageBody);
            context.setVariable("extraMessage", extraMessage);
            context.setVariable("callToActionMessage", callToActionMessage);
            context.setVariable("redirectLink", frontendUrl + "auth/sign-in");
            context.setVariable("buttonText", buttonText);
            String htmlContent = templateEngine.process(STATUS_NOTIFICATION_TEMPLATE, context);

            helper.setText(htmlContent, true);
            javaMailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e ) {
            throw new EmailServiceException("Failed to send password change confirmation email", e);
        }
    }

    @Async
    @Override
    public void sendLoanRejectionEmail(String toEmail, String userName, String subject, String notification) {
        validateEmailParameters(toEmail, subject, userName, ADMIN_NAME);
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(emailSender, ADMIN_NAME);
            helper.setTo(toEmail);
            helper.setSubject(subject);

            Context context = new Context();
            context.setVariable("emailTitle", EmailMessageUtils.EMAIL_TITLE);
            context.setVariable("userName", userName);
            context.setVariable("messageBody", EmailMessageUtils.MESSAGE_BODY);
            context.setVariable("extraMessage", notification);
            context.setVariable("callToActionMessage", EmailMessageUtils.CALL_TO_ACTION_MESSAGE);
            String htmlContent = templateEngine.process(STATUS_NOTIFICATION_TEMPLATE, context);

            helper.setText(htmlContent, true);
            javaMailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e ) {
            throw new EmailServiceException("Failed to send password change confirmation email", e);
        }
    }

    @Override
    @Async
    public void sendAccountActivationEmail(String toEmail) {
        String userName = getUserNameByEmail(toEmail);
        String token = generateActivationToken(toEmail);
        String activationLink = String.format("%s/api/auth/activate?token=%s", backendUrl, token);
        EmailDetails emailDetails = new EmailDetails(toEmail, userName, WELCOME_TEAM_NAME);
        sendEmail(REGISTRATION_CONFIRMATION_SUBJECT, emailDetails, activationLink);
    }

    private String generateActivationToken(String email) {
        return authService.generateActivationToken(email);
    }

    private String getUserNameByEmail(String email) {
        return authService .getUserNameByEmail(email).orElse(DEFAULT_USER_NAME);
    }

    private void validateEmailParameters(String toEmail, String subject, String userName, String senderName) {
        if (toEmail == null || toEmail.isEmpty()) {
            throw new IllegalArgumentException("El correo del destinatario (toEmail) no debe ser nulo ni estar vacío.");
        }
        if (subject == null || subject.isEmpty()) {
            throw new IllegalArgumentException("El asunto del correo no debe ser nulo ni estar vacío.");
        }
        if (userName == null || userName.isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario no debe ser nulo ni estar vacío.");
        }
        if (senderName == null || senderName.isEmpty()) {
            throw new IllegalArgumentException("El nombre del remitente no debe ser nulo ni estar vacío.");
        }
    }
}
