package com.evaluationsys.taskevaluationsys.repository;

import com.evaluationsys.taskevaluationsys.entity.TaskReplacement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskReplacementRepository extends JpaRepository<TaskReplacement, Long> {
}
