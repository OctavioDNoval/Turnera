package com.turnera.repository;

import com.turnera.entity.AvailabilitySlot;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {

    List<AvailabilitySlot> findByTeacherId(Long teacherId);

    List<AvailabilitySlot> findByTeacherIdAndActiveTrue(Long teacherId);

    void deleteByTeacherId(Long teacherId);
}