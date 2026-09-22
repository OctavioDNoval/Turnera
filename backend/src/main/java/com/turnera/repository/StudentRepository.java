package com.turnera.repository;

import com.turnera.entity.Student;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findByTeacherId(Long teacherId);

    List<Student> findByTeacherIdAndActiveTrue(Long teacherId);

    Optional<Student> findByTeacherIdAndEmail(Long teacherId, String email);

    Optional<Student> findByTeacherIdAndPhone(Long teacherId, String phone);
}