package tn.esprit.twin1.EducationSpringApp.dto;

public class OptimizationSolutionRequest {
    private String solutionName;
    private float implementationCost;
    private float costSavings;

    // Getters and setters
    public String getSolutionName() { return solutionName; }
    public void setSolutionName(String solutionName) { this.solutionName = solutionName; }

    public float getImplementationCost() { return implementationCost; }
    public void setImplementationCost(float implementationCost) { this.implementationCost = implementationCost; }

    public float getCostSavings() { return costSavings; }
    public void setCostSavings(float costSavings) { this.costSavings = costSavings; }
}

