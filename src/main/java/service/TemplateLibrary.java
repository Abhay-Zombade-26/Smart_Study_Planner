package service;

import model.FeatureTemplate;
import model.FeatureTemplate.TaskTemplate;
import java.util.*;

public class TemplateLibrary {

    private Map<String, List<FeatureTemplate>> templatesByType;

    public TemplateLibrary() {
        templatesByType = new HashMap<>();
        initializeTemplates();
    }

    private void initializeTemplates() {
        // Web Frontend Templates
        List<FeatureTemplate> webTemplates = new ArrayList<>();

        // Login Feature Template
        FeatureTemplate loginFeature = new FeatureTemplate("Login System", 8);
        loginFeature.addSubTask(new TaskTemplate(
                "Create login form UI",
                new String[]{"Login.jsx", "Login.tsx", "login.html", "Login.vue"},
                0.15, "UI"
        ));
        loginFeature.addSubTask(new TaskTemplate(
                "Implement authentication logic",
                new String[]{"auth.js", "AuthService.js", "authentication.js"},
                0.25, "BACKEND"
        ));
        loginFeature.addSubTask(new TaskTemplate(
                "Add validation and error handling",
                new String[]{"validation.js", "validators.js"},
                0.15, "BACKEND"
        ));
        loginFeature.addSubTask(new TaskTemplate(
                "Create user session management",
                new String[]{"session.js", "SessionManager.js"},
                0.20, "BACKEND"
        ));
        loginFeature.addSubTask(new TaskTemplate(
                "Design and implement database schema",
                new String[]{"User.java", "User.js", "schema.sql"},
                0.15, "DATABASE"
        ));
        loginFeature.addSubTask(new TaskTemplate(
                "Write tests",
                new String[]{"auth.test.js", "Login.test.jsx"},
                0.10, "TESTING"
        ));
        webTemplates.add(loginFeature);

        // CRUD Feature Template
        FeatureTemplate crudFeature = new FeatureTemplate("CRUD Operations", 12);
        crudFeature.addSubTask(new TaskTemplate(
                "Design data model",
                new String[]{"model.js", "schema.sql", "entity.java"},
                0.15, "BACKEND"
        ));
        crudFeature.addSubTask(new TaskTemplate(
                "Create API endpoints",
                new String[]{"routes.js", "controller.java", "api.js"},
                0.25, "BACKEND"
        ));
        crudFeature.addSubTask(new TaskTemplate(
                "Implement database queries",
                new String[]{"repository.java", "dao.js", "queries.sql"},
                0.20, "DATABASE"
        ));
        crudFeature.addSubTask(new TaskTemplate(
                "Create UI components",
                new String[]{"List.jsx", "Form.jsx", "table.html"},
                0.25, "UI"
        ));
        crudFeature.addSubTask(new TaskTemplate(
                "Add validation",
                new String[]{"validation.js", "validators.js"},
                0.10, "BACKEND"
        ));
        crudFeature.addSubTask(new TaskTemplate(
                "Write integration tests",
                new String[]{"api.test.js", "crud.test.js"},
                0.05, "TESTING"
        ));
        webTemplates.add(crudFeature);

        templatesByType.put("REACT_FRONTEND", webTemplates);
        templatesByType.put("NODE_BACKEND", webTemplates);

        // Java Backend Templates
        List<FeatureTemplate> javaTemplates = new ArrayList<>();

        // REST API Feature Template
        FeatureTemplate restFeature = new FeatureTemplate("REST API", 10);
        restFeature.addSubTask(new TaskTemplate(
                "Create Controller class",
                new String[]{"Controller.java", "Resource.java", "Endpoint.java"},
                0.20, "BACKEND"
        ));
        restFeature.addSubTask(new TaskTemplate(
                "Implement Service layer",
                new String[]{"Service.java", "ServiceImpl.java"},
                0.25, "BACKEND"
        ));
        restFeature.addSubTask(new TaskTemplate(
                "Create Repository/DAO",
                new String[]{"Repository.java", "DAO.java"},
                0.20, "DATABASE"
        ));
        restFeature.addSubTask(new TaskTemplate(
                "Define DTOs/Models",
                new String[]{"DTO.java", "Model.java", "Entity.java"},
                0.15, "BACKEND"
        ));
        restFeature.addSubTask(new TaskTemplate(
                "Add exception handling",
                new String[]{"ExceptionHandler.java", "GlobalException.java"},
                0.10, "BACKEND"
        ));
        restFeature.addSubTask(new TaskTemplate(
                "Write unit tests",
                new String[]{"Test.java", "ControllerTest.java"},
                0.10, "TESTING"
        ));
        javaTemplates.add(restFeature);

        templatesByType.put("SPRING_BOOT", javaTemplates);
        templatesByType.put("JAVA_BACKEND", javaTemplates);
    }

    public List<FeatureTemplate> getTemplatesForProject(String projectType) {
        return templatesByType.getOrDefault(projectType, new ArrayList<>());
    }

    public FeatureTemplate findTemplate(String featureName, String projectType) {
        List<FeatureTemplate> templates = getTemplatesForProject(projectType);
        for (FeatureTemplate template : templates) {
            if (featureName.toLowerCase().contains(template.getFeatureName().toLowerCase())) {
                return template;
            }
        }
        return null;
    }
}