package com.evaluationsys.taskevaluationsys.repository;

import com.evaluationsys.taskevaluationsys.entity.Supervisor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface SupervisorRepository extends JpaRepository<Supervisor, Long> {

    Optional<Supervisor> findTopBySupervisorCodeStartingWithOrderBySupervisorCodeDesc(String prefix);

    Optional<Supervisor> findBySupervisorCode(String supervisorCode);

}