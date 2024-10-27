package tn.esprit.twin1.EducationSpringApp.ControllersJena;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.twin1.EducationSpringApp.servicesJena.BehaviorPatternService;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")  // Enable CORS for this controller
@RequestMapping("/behaviorpatterns") // Base URL for the controller
public class BehaviorPatternController {

    @Autowired
    private BehaviorPatternService behaviorPatternService;

    // Endpoint to get all Behavior Patterns
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, Object>>> getAllBehaviorPatterns() {
        List<Map<String, Object>> result = behaviorPatternService.getAllBehaviorPatterns();
        return ResponseEntity.ok(result);
    }

    // Endpoint to add a new Behavior Pattern
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addBehaviorPattern(@RequestBody Map<String, Object> newBehaviorPattern) {
        String name = (String) newBehaviorPattern.get("name");
        String usagePattern = (String) newBehaviorPattern.get("usagePattern");
        float reductionPotential = Float.parseFloat(newBehaviorPattern.get("reductionPotential").toString());

        behaviorPatternService.addBehaviorPattern(name, usagePattern, reductionPotential);
        return ResponseEntity.ok("Behavior pattern added successfully!");
    }

    // Endpoint to update an existing Behavior Pattern
    @PutMapping(value = "/{name}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateBehaviorPattern(@PathVariable String name, @RequestBody Map<String, Object> updatedBehaviorPattern) {
        String newUsagePattern = (String) updatedBehaviorPattern.get("usagePattern");
        float newReductionPotential = Float.parseFloat(updatedBehaviorPattern.get("reductionPotential").toString());

        behaviorPatternService.updateBehaviorPattern(name, newUsagePattern, newReductionPotential);
        return ResponseEntity.ok("Behavior pattern updated successfully!");
    }

    // Endpoint to delete a Behavior Pattern
    @DeleteMapping(value = "/{name}")
    public ResponseEntity<String> deleteBehaviorPattern(@PathVariable String name) {
        behaviorPatternService.deleteBehaviorPattern(name);
        return ResponseEntity.ok("Behavior pattern deleted successfully!");
    }

    // Endpoint to get Behavior Patterns that enable Carbon Reduction Strategy
    @GetMapping(value = "/carbon-reduction", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, Object>>> getBehaviorPatternsWithCarbonReductionStrategy() {
        List<Map<String, Object>> result = behaviorPatternService.getBehaviorPatternsWithCarbonReductionStrategy();
        return ResponseEntity.ok(result);
    }
}