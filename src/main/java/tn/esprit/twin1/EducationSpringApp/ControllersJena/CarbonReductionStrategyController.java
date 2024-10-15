package tn.esprit.twin1.EducationSpringApp.ControllersJena;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import tn.esprit.twin1.EducationSpringApp.servicesJena.CarbonReductionStrategyService;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")  // Enable CORS for this controller
@RequestMapping("/carbonreductionstrategies") // Base URL for the controller
public class CarbonReductionStrategyController {

    @Autowired
    private CarbonReductionStrategyService carbonReductionStrategyService;

    // Endpoint to get all carbon reduction strategies in JSON format
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAllCarbonReductionStrategies() {
        String result = carbonReductionStrategyService.queryCarbonReductionStrategies();
        return ResponseEntity.ok(result);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addCarbonReductionStrategy(@RequestBody Map<String, Object> newStrategy) {
        try {
            String strategyName = (String) newStrategy.get("reductionStrategyName");
            double cost = Double.parseDouble(newStrategy.get("hasCost").toString());
            double impactValue = Double.parseDouble(newStrategy.get("hasImpactValue").toString());

            carbonReductionStrategyService.addCarbonReductionStrategy(strategyName, cost, impactValue);
            return ResponseEntity.ok("Carbon reduction strategy added successfully!");
        } catch (Exception e) {
            // Log the exception for debugging
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error adding carbon reduction strategy: " + e.getMessage());
        }
    }

    // Endpoint to update an existing carbon reduction strategy
    @PutMapping(value = "/{strategyName}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateCarbonReductionStrategy(@PathVariable String strategyName,
            @RequestBody Map<String, Object> updatedStrategy) {
        double newCost = Double.parseDouble(updatedStrategy.get("cost").toString());
        double newImpactValue = Double.parseDouble(updatedStrategy.get("impactValue").toString());

        carbonReductionStrategyService.updateCarbonReductionStrategy(strategyName, newCost, newImpactValue);
        return ResponseEntity.ok("Carbon reduction strategy updated successfully!");
    }

    // Endpoint to delete a carbon reduction strategy
    @DeleteMapping(value = "/{strategyName}")
    public ResponseEntity<String> deleteCarbonReductionStrategy(@PathVariable String strategyName) {
        carbonReductionStrategyService.deleteCarbonReductionStrategy(strategyName);
        return ResponseEntity.ok("Carbon reduction strategy deleted successfully!");
    }
}
