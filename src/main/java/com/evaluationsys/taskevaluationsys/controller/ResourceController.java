package com.evaluationsys.taskevaluationsys.controller;

import com.evaluationsys.taskevaluationsys.entity.Resource;
import com.evaluationsys.taskevaluationsys.service.ResourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    // =========================
    // GET ALL RESOURCES
    // =========================
    @GetMapping("/")
    public ResponseEntity<List<Resource>> getAllResources() {
        return ResponseEntity.ok(resourceService.getAllResources());
    }

    // =========================
    // GET RESOURCE BY ID
    // =========================
    @GetMapping("/{resourceId}")
    public ResponseEntity<Resource> getResourceById(@PathVariable Long resourceId) {
        Optional<Resource> resource = resourceService.getResourceById(resourceId);
        return resource.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // =========================
    // CREATE RESOURCE
    // =========================
    @PostMapping("/")
    public ResponseEntity<Resource> createResource(@RequestBody Resource resource) {
        Resource savedResource = resourceService.createResource(resource);
        return ResponseEntity.created(URI.create("/resources/" + savedResource.getResource_id())).body(savedResource);
    }

    // =========================
    // UPDATE RESOURCE
    // =========================
    @PutMapping("/{resourceId}")
    public ResponseEntity<Resource> updateResource(@PathVariable Long resourceId, @RequestBody Resource resourceDetails) {
        Optional<Resource> updatedResource = resourceService.updateResource(resourceId, resourceDetails);
        return updatedResource.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // =========================
    // DELETE RESOURCE
    // =========================
    @DeleteMapping("/{resourceId}")
    public ResponseEntity<Void> deleteResource(@PathVariable Long resourceId) {
        resourceService.deleteResource(resourceId);
        return ResponseEntity.noContent().build();
    }
}