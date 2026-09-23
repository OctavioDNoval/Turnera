package com.turnera.config;

import com.turnera.entity.Teacher;
import com.turnera.repository.TeacherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;
    private final String adminName;

    public DataInitializer(TeacherRepository teacherRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${turnera.admin-username}") String adminUsername,
                           @Value("${turnera.admin-password}") String adminPassword,
                           @Value("${turnera.admin-name}") String adminName) {
        this.teacherRepository = teacherRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.adminName = adminName;
    }

    @Override
    public void run(String... args) {
        if (teacherRepository.count() > 0) {
            return;
        }
        Teacher teacher = new Teacher();
        teacher.setName(adminName);
        teacher.setUsername(adminUsername);
        teacher.setPasswordHash(passwordEncoder.encode(adminPassword));
        teacher.setEnabled(true);
        teacherRepository.save(teacher);
        log.warn("Profesor inicial creado: '{}' ({}). Guardar tus credenciales en .env.", adminUsername, adminName);
    }
}