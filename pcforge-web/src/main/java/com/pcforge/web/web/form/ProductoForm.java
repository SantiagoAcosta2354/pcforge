package com.pcforge.web.web.form;

import java.math.BigDecimal;

/**
 * Datos que se capturan en el formulario de producto.
 *
 * Se mantiene como clase con accesores en lugar de un registro porque Thymeleaf
 * necesita poder rellenar los campos uno a uno al mostrar el formulario y al
 * recibirlo. Los atributos tecnicos son opcionales a proposito: un componente
 * puede no declararlos, y en ese caso el validador de compatibilidad avisa en
 * lugar de dar por buena una compatibilidad que no puede comprobar.
 */
public class ProductoForm {

    private String id;
    private String sku;
    private String name;
    private String brand;
    private String categoryId;
    private String componentType;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Integer minimumStock;
    private boolean active = true;

    // --- atributos tecnicos que consume el motor de compatibilidad ---
    private String socket;
    private String ramTypeSupported;
    private String ramType;
    private Integer estimatedPowerW;
    private Integer capacityW;
    private String formFactor;

    /** Ficha informativa libre, una entrada por linea con el formato clave=valor. */
    private String extra;

    public boolean esNuevo() {
        return id == null || id.isBlank();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(Integer minimumStock) {
        this.minimumStock = minimumStock;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getSocket() {
        return socket;
    }

    public void setSocket(String socket) {
        this.socket = socket;
    }

    public String getRamTypeSupported() {
        return ramTypeSupported;
    }

    public void setRamTypeSupported(String ramTypeSupported) {
        this.ramTypeSupported = ramTypeSupported;
    }

    public String getRamType() {
        return ramType;
    }

    public void setRamType(String ramType) {
        this.ramType = ramType;
    }

    public Integer getEstimatedPowerW() {
        return estimatedPowerW;
    }

    public void setEstimatedPowerW(Integer estimatedPowerW) {
        this.estimatedPowerW = estimatedPowerW;
    }

    public Integer getCapacityW() {
        return capacityW;
    }

    public void setCapacityW(Integer capacityW) {
        this.capacityW = capacityW;
    }

    public String getFormFactor() {
        return formFactor;
    }

    public void setFormFactor(String formFactor) {
        this.formFactor = formFactor;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}
