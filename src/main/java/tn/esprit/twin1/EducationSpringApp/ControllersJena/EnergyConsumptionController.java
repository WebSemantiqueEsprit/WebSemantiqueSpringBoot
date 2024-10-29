package tn.esprit.twin1.EducationSpringApp.ControllersJena;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.twin1.EducationSpringApp.servicesJena.EnergyConsumptionService;
import org.apache.jena.rdf.model.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/energy-consumptions")
public class EnergyConsumptionController {

    @Autowired
    private EnergyConsumptionService energyConsumptionService;

    // Endpoint to retrieve energy consumption details
    @GetMapping(value = "/{energyConsumptionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> getEnergyConsumptionDetails(@PathVariable String energyConsumptionId) {
        Map<String, String> details = energyConsumptionService.getEnergyConsumptionDetails(energyConsumptionId);
        return ResponseEntity.ok(details);
    }

    // Endpoint to create a new energy consumption record
    @PostMapping(value={"/add"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createEnergyConsumption(@RequestBody Map<String, Object> newEnergyConsumption) {
        String uri = "http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#"+(String) newEnergyConsumption.get("uri");
        float value = Float.parseFloat(newEnergyConsumption.get("value").toString());
        String timeFrame = (String) newEnergyConsumption.get("timeFrame");
        //String providerUri = (String) newEnergyConsumption.get("providerUri");
        //String deviceUri = (String) newEnergyConsumption.get("deviceUri");

        //Resource energyConsumption = energyConsumptionService.createEnergyConsumption(uri, value, timeFrame, providerUri, deviceUri);
        Resource energyConsumption = energyConsumptionService.createEnergyConsumption(uri, value, timeFrame);
        return ResponseEntity.ok("Energy consumption created successfully with URI: " + energyConsumption.getURI());
    }

    // Endpoint to update an existing energy consumption record
    @PutMapping(value = "/{uri}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateEnergyConsumption(@PathVariable String uri, @RequestBody Map<String, Object> updatedData) {
        Float newValue = updatedData.containsKey("value") ? Float.parseFloat(updatedData.get("value").toString()) : null;
        String newTimeFrame = (String) updatedData.get("timeFrame");


        System.out.println("Updating URI: " + uri);
        System.out.println("Data received: " + updatedData);
        String urio ="http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#"+uri ;
        Resource updatedEnergyConsumption = energyConsumptionService.updateEnergyConsumption(urio, newValue, newTimeFrame);
        return ResponseEntity.ok("Energy consumption updated successfully for URI: " + updatedEnergyConsumption.getURI());
    }

    // Endpoint to delete an energy consumption record
    @DeleteMapping(value = "/{uri}")
    public ResponseEntity<String> deleteEnergyConsumption(@PathVariable String uri) {
        String urio ="http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#"+uri ;
        boolean deleted = energyConsumptionService.deleteEnergyConsumption(urio);
        if (deleted) {
            return ResponseEntity.ok("Energy consumption deleted successfully for URI: " + uri);
        } else {
            return ResponseEntity.status(404).body("Energy consumption with URI " + uri + " not found.");
        }
    }
    // EnergyConsumptionController.java
    // EnergyConsumptionController.java
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, List<Map<String, String>>>> getAllEnergyConsumptions() {
        List<Map<String, String>> energyConsumptions = energyConsumptionService.getAllEnergyConsumptions();

        // Wrap the list in a map with the "EnergyConsumption" key
        Map<String, List<Map<String, String>>> response = new HashMap<>();
        response.put("EnergyConsumption", energyConsumptions);

        return ResponseEntity.ok(response);
    }


    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, String>>> searchEnergyEfficiency(@RequestParam String efficiencyName) {
        List<Map<String, String>> result = energyConsumptionService.searchByEfficiencyName(efficiencyName);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/filter")
    public List<Map<String, String>> getEnergyConsumptionsInRange(
            @RequestParam float minValue,
            @RequestParam float maxValue) {
        // Call the service method to get energy consumptions in the specified range
        return energyConsumptionService.getEnergyConsumptionsInRange(minValue, maxValue);
    }

}
