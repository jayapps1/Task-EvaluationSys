package com.evaluationsys.taskevaluationsys.service;

import com.evaluationsys.taskevaluationsys.entity.Resource;
import com.evaluationsys.taskevaluationsys.repository.ResourceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    // =========================
    // GET ALL RESOURCES
    // =========================
    public List<Resource> getAllResources() {
        return resourceRepository.findAll();
    }

    // =========================
    // GET RESOURCE BY ID
    // =========================
    public Optional<Resource> getResourceById(Long resourceId) {
        return resourceRepository.findById(resourceId);
    }

    // =========================
    // CREATE RESOURCE
    // =========================
    public Resource createResource(Resource resource) {
        return resourceRepository.save(resource);
    }

    // =========================
    // UPDATE RESOURCE
    // =========================
    public Optional<Resource> updateResource(Long resourceId, Resource resourceDetails) {
        return resourceRepository.findById(resourceId).map(resource -> {
            resource.setResourceItem(resourceDetails.getResourceItem());
            resource.setDescription(resourceDetails.getDescription());
            resource.setAvailabilityStatus(resourceDetails.isAvailabilityStatus());
            return resourceRepository.save(resource);
        });
    }

    // =========================
    // DELETE RESOURCE
    // =========================
    public void deleteResource(Long resourceId) {
        resourceRepository.findById(resourceId).ifPresent(resourceRepository::delete);
    }
}