package com.evaluationsys.taskevaluationsys.repository;

import com.evaluationsys.taskevaluationsys.entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    // Add to EvaluationRepository.java
    List<Evaluation> findByTask_TaskId(Long taskId);
    List<Evaluation> findByYear(Integer year);
    List<Evaluation> findByYearAndQuarter(Integer year, Integer quarter);
    long countByYear(Integer year);
    // Add these methods to EvaluationRepository
    List<Evaluation> findByTask_TaskIdAndYearAndQuarter(Long taskId, Integer year, Integer quarter);
    boolean existsByTask_TaskIdAndYearAndQuarter(Long taskId, Integer year, Integer quarter);
}