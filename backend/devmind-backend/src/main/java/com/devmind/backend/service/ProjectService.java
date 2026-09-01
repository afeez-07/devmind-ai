package com.devmind.backend.service;

import com.devmind.backend.entity.Project;
import com.devmind.backend.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import com.devmind.backend.exception.ProjectNotFoundException;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));
    }

    public Project createProject(Project project) {
        return projectRepository.save(project);
    }

    public Project updateProject(Long id, Project project) {
        Project existingProject = getProjectById(id);

        existingProject.setName(project.getName());
        existingProject.setDescription(project.getDescription());
        existingProject.setLanguage(project.getLanguage());

        return projectRepository.save(existingProject);
    }

    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }
}