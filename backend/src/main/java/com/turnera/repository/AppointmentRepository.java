package com.turnera.repository;

import com.turnera.entity.Appointment;
import com.turnera.entity.AppointmentStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByTeacherId(Long teacherId);

    List<Appointment> findByTeacherIdAndStatus(Long teacherId, AppointmentStatus status);

    List<Appointment> findByStudentId(Long studentId);

    @Query("""
            select a
            from Appointment a
            where a.teacher.id = :teacherId
              and a.status <> :excludedStatus
              and a.endsAt > :start
              and a.startsAt < :end
            """)
    List<Appointment> findOverlapping(
            @Param("teacherId") Long teacherId,
            @Param("excludedStatus") AppointmentStatus excludedStatus,
            @Param("start") Instant start,
            @Param("end") Instant end);
}