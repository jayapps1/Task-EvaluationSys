package com.evaluationsys.taskevaluationsys.repository;

import com.evaluationsys.taskevaluationsys.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Optional<Task> findByTaskCode(String taskCode);

    @Query("SELECT t FROM Task t WHERE t.year = :year ORDER BY t.task_id DESC")
    Optional<Task> findTopByYear(@Param("year") Integer year);
}