package com.evaluationsys.taskevaluationsys;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class testone {

    @Id
    private Long id;
    private String userName;
    private String email;
}
