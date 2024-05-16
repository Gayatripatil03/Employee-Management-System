package com.ness.emps.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ness.emps.model.UserDtls;
import com.ness.emps.repository.UserRepository;

@Service
public class MailNotificationService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepo; 
    
	Logger log = LoggerFactory.getLogger(MailNotificationService.class);


    public boolean sendBirthdayEmails() {
        LocalDate currentDate = LocalDate.now();
        log.info("Current date is: "+currentDate);
        List<UserDtls> users = userRepo.findByBirthDateMonthAndDay(currentDate.getMonthValue(), currentDate.getDayOfMonth());
        log.info("Month value is: "+currentDate.getMonthValue());
        log.info("Day value is: "+currentDate.getDayOfMonth());
        log.info("Users with Birthday are: "+users);

        if (users.isEmpty()) {
            log.info("No users have a birthday today.");
            return false;
        } else {
            for (UserDtls user : users) {
                String subject = "Happy Birthday, " + user.getFullName() + "!";
                emailService.sendBirthdayEmailWithAttachments(user.getEmail(), subject, user.getFullName());
            }
            return true;
        }
    }
    
    public boolean sendOnboardingEmails() {
        LocalDate currentDate = LocalDate.now();
        log.info("Current date is: " + currentDate);

        String currentJoiningDate = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        List<UserDtls> newEmployees = userRepo.findByJoiningDate(currentJoiningDate);
        log.info("New employees joined today are: " + newEmployees);

        if (newEmployees.isEmpty()) {
            log.info("No new employees joined today.");
            return false;
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (UserDtls employee : newEmployees) {
                try {
                    String subject = "Welcome to the Team, " + employee.getFullName() + "!";
                    emailService.sendOnboaringEmailWithAttachment(employee.getEmail(), subject, employee.getFullName());
                } catch (DateTimeParseException e) {
                    log.error("Error parsing joining date for employee: " + employee.getId(), e);
                }
            }
            return true;
        }
    }





    public boolean sendWorkAnniversaryEmails() {
        LocalDate currentDate = LocalDate.now();
        log.info("Current date is: " + currentDate);
        List<UserDtls> employees = userRepo.findByJoiningDateMonthAndDay(currentDate.getMonthValue(), currentDate.getDayOfMonth());
        log.info("Employees with Work Anniversary today are: " + employees);

        if(employees.isEmpty()) {
            log.info("No employees have a work anniversary today.");
            return false;
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (UserDtls employee : employees) {
                try {
                    LocalDate joiningDate = LocalDate.parse(employee.getJoiningDate(), formatter);
                    if (joiningDate.getMonth() == currentDate.getMonth() && 
                        joiningDate.getDayOfMonth() == currentDate.getDayOfMonth() &&
                        joiningDate.getYear() != currentDate.getYear()) {
                        String subject = "Congratulations on your Work Anniversary, " + employee.getFullName() + "!";
                        emailService.sendWorkAnniversaryEmailWithAttachment(employee.getEmail(), subject, employee.getFullName());
                    }
                } catch (DateTimeParseException e) {
                    log.error("Error parsing joining date for employee: " + employee.getId(), e);
                }
            }
            return true;
        }
    }
}
