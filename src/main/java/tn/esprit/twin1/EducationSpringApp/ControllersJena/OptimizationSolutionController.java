package tn.esprit.twin1.EducationSpringApp.ControllersJena;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.twin1.EducationSpringApp.dto.OptimizationSolutionRequest;
import tn.esprit.twin1.EducationSpringApp.dto.UpdateSolutionRequest;
import tn.esprit.twin1.EducationSpringApp.servicesJena.OptimizationSolutionService;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/optimizationSolutions")
public class OptimizationSolutionController {
    @Autowired
    private OptimizationSolutionService solutionService;

    @GetMapping
    public ResponseEntity<String> getAllOptimizationSolutions() {
        String result = solutionService.queryOptimizationSolutions();
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> addOptimizationSolution(@RequestBody OptimizationSolutionRequest request) {
        String solutionName = request.getSolutionName();
        float implementationCost = request.getImplementationCost();
        float costSavings = request.getCostSavings();

        solutionService.addOptimizationSolution(solutionName, implementationCost, costSavings);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Optimization Solution added successfully.");
        return ResponseEntity.ok(response);
    }


    @PutMapping("/update")
    public ResponseEntity<Map<String, String>> updateOptimizationSolution(@RequestBody UpdateSolutionRequest request) {
        String solutionName = request.getSolutionName();
        float newImplementationCost = request.getImplementationCost();
        float newCostSavings = request.getCostSavings();

        solutionService.updateOptimizationSolution(solutionName, newImplementationCost, newCostSavings);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Optimization Solution updated successfully.");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{solutionName}")
    public ResponseEntity<String> deleteOptimizationSolution(@PathVariable String solutionName) {
        solutionService.deleteOptimizationSolution(solutionName);
        return ResponseEntity.ok("Optimization Solution deleted successfully.");
    }
}
