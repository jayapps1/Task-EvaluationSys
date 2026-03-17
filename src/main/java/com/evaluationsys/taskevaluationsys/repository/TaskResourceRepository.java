package com.evaluationsys.taskevaluationsys.repository;

import com.evaluationsys.taskevaluationsys.entity.TaskResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskResourceRepository extends JpaRepository<TaskResource, Long> {
}
