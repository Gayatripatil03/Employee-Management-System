package com.ness.emps.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;


@Service
public class EmailService {

	@Autowired
	private JavaMailSender mailSender;
	
	@Value("$(Employee Management System)")
	private String fromEmailId;

	public void sendBirthdayEmailWithAttachments(String to,String subject,String fullName){
		
		try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmailId);
            helper.setTo(to);
            helper.setSubject(subject);
            
            
            String htmlTemplate = loadHtmlTemplate("templates/birthday_wishes.html");

            String htmlContent = htmlTemplate.replace("${fullName}", fullName);

            helper.setText(htmlContent, true); 
            
            Resource resource = new ClassPathResource("static/images/birthday.jpg");
            FileSystemResource attachmentJpg = new FileSystemResource(resource.getFile());
            
            helper.addAttachment("birthday.jpg", attachmentJpg);

            mailSender.send(message);
        } catch (MessagingException | IOException e) {
            e.printStackTrace();     
        }
	    	
	}
	
    public void sendOnboaringEmailWithAttachment(String to, String subject, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmailId);
            helper.setTo(to);
            helper.setSubject(subject);

            String htmlContent = loadHtmlTemplate("templates/onboarding_process.html");

            htmlContent = htmlContent.replace("${fullName}", fullName);

            helper.setText(htmlContent, true);

            Resource resource = new ClassPathResource("templates/onboarding_process.pdf");

            FileSystemResource attachmentPdf = new FileSystemResource(resource.getFile());
            helper.addAttachment("instructions.pdf", attachmentPdf);

            mailSender.send(message);
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }
    
    public void sendWorkAnniversaryEmailWithAttachment(String to, String subject, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmailId);
            helper.setTo(to);
            helper.setSubject(subject);

            String htmlContent = loadHtmlTemplate("templates/work_anniversary.html");
            htmlContent = htmlContent.replace("${fullName}", fullName);

            helper.setText(htmlContent, true);

            Resource resource = new ClassPathResource("static/images/work_anniversary.jpg");
            FileSystemResource attachment = new FileSystemResource(resource.getFile());

            helper.addAttachment("work_anniversary.jpg", attachment);

            mailSender.send(message);
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }
	
	 private String loadHtmlTemplate(String templatePath) {
	        try {
	            Resource resource = new ClassPathResource(templatePath);
	            return new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);
	        } catch (IOException e) {
	            e.printStackTrace();
	            return "";
	        }
	    }

}
