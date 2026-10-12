package com.pcforge.web.web.form;

/** Datos que se capturan en el formulario de categoria. */
public class CategoriaForm {

    private String id;
    private String name;
    private String slug;
    private String description;
    private boolean active = true;

    public boolean esNueva() {
        return id == null || id.isBlank();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
