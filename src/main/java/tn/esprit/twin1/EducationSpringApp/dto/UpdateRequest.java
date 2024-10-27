package tn.esprit.twin1.EducationSpringApp.dto;

public class UpdateRequest {
    private String solutionName;
    private float capacity;
    private float efficiency;

    // Getters and setters
    public String getSolutionName() {
        return solutionName;
    }

    public void setSolutionName(String solutionName) {
        this.solutionName = solutionName;
    }

    public float getCapacity() {
        return capacity;
    }

    public void setCapacity(float capacity) {
        this.capacity = capacity;
    }

    public float getEfficiency() {
        return efficiency;
    }

    public void setEfficiency(float efficiency) {
        this.efficiency = efficiency;
    }
}

