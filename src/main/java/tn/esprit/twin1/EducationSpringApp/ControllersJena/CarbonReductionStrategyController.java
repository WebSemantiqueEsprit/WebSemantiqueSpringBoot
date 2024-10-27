package tn.esprit.twin1.EducationSpringApp.ControllersJena;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tn.esprit.twin1.EducationSpringApp.servicesJena.CarbonReductionStrategyService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200") // Enable CORS for this controller
@RequestMapping("/carbonreductionstrategies") // Base URL for the controller
public class CarbonReductionStrategyController {

    private static final Logger logger = LoggerFactory.getLogger(CarbonReductionStrategyController.class);

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
            logger.info("Received request to add carbon reduction strategy: {}", newStrategy);

            String strategyName = (String) newStrategy.get("reductionStrategyName");
            logger.debug("Parsed strategyName: {}", strategyName);

            // Encode the strategyName to make it a valid URI
            String encodedStrategyName = URLEncoder.encode(strategyName, StandardCharsets.UTF_8.toString());
            logger.debug("Encoded strategyName: {}", encodedStrategyName);

            double cost = Double.parseDouble(newStrategy.get("hasCost").toString());
            logger.debug("Parsed cost: {}", cost);

            double impactValue = Double.parseDouble(newStrategy.get("hasImpactValue").toString());
            logger.debug("Parsed impactValue: {}", impactValue);

            // Use encodedStrategyName when saving to RDF
            carbonReductionStrategyService.addCarbonReductionStrategy(encodedStrategyName, cost, impactValue);
            logger.info("Carbon reduction strategy added successfully: {}", strategyName);
            return ResponseEntity.ok("Carbon reduction strategy added successfully!");
        } catch (NumberFormatException e) {
            logger.error("Number format exception while parsing input: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid number format for cost or impact value.");
        } catch (Exception e) {
            logger.error("Error adding carbon reduction strategy: ", e);
            return ResponseEntity.status(500).body("Error adding carbon reduction strategy: " + e.getMessage());
        }
    }

    // Endpoint to update an existing carbon reduction strategy
    @PutMapping(value = "/{reductionStrategyName}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateCarbonReductionStrategy(@PathVariable String reductionStrategyName,
            @RequestBody Map<String, Object> updatedStrategy) {
        double newCost = Double.parseDouble(updatedStrategy.get("hasCost").toString());
        double newImpactValue = Double.parseDouble(updatedStrategy.get("hasImpactValue").toString());

        carbonReductionStrategyService.updateCarbonReductionStrategy(reductionStrategyName, newCost, newImpactValue);
        return ResponseEntity.ok("Carbon reduction strategy updated successfully!");
    }

    // Endpoint to delete a carbon reduction strategy
    @DeleteMapping(value = "/{strategyName}")
    public ResponseEntity<String> deleteCarbonReductionStrategy(@PathVariable String strategyName) {
        carbonReductionStrategyService.deleteCarbonReductionStrategy(strategyName);
        return ResponseEntity.ok("Carbon reduction strategy deleted successfully!");
    }

    // Endpoint to find a carbon reduction strategy by name
    @GetMapping(value = "/search/{strategyName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> findCarbonReductionStrategyByName(@PathVariable String strategyName) {
        String result = carbonReductionStrategyService.findCarbonReductionStrategyByName(strategyName);
        return ResponseEntity.ok(result);
    }

    // New endpoint to find carbon reduction strategies by cost range using path
    // variables
    @GetMapping(value = "/search/costRange/{minCost}/{maxCost}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> findCarbonReductionStrategiesByCostRange(
            @PathVariable double minCost, @PathVariable double maxCost) {
        logger.info("Received request to find strategies with cost between {} and {}", minCost, maxCost);
        String result = carbonReductionStrategyService.findCarbonReductionStrategiesByCostRange(minCost, maxCost);
        return ResponseEntity.ok(result);
    }

    // Endpoint to find carbon reduction strategies by impact value range
    @GetMapping(value = "/impact-value-range/{minImpactValue}/{maxImpactValue}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> findCarbonReductionStrategiesByImpactValueRange(
            @PathVariable double minImpactValue,
            @PathVariable double maxImpactValue) {
        String result = carbonReductionStrategyService.findCarbonReductionStrategiesByImpactValueRange(minImpactValue,
                maxImpactValue);
        return ResponseEntity.ok(result);
    }

}
