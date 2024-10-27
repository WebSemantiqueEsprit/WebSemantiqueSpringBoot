package tn.esprit.twin1.EducationSpringApp.ControllersJena;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tn.esprit.twin1.EducationSpringApp.servicesJena.CarbonFootprintService;

import java.util.Map; // Import the Map interface

@RestController
@CrossOrigin(origins = "http://localhost:4200") // Enable CORS for this controller
@RequestMapping("/carbonfootprints") // Base URL for the controller
public class CarbonFootprintController {

    @Autowired
    private CarbonFootprintService carbonFootprintService;

    // Endpoint to get carbon footprint data from RDF file in JSON format
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getCarbonFootprintsData() {
        String result = carbonFootprintService.queryCarbonFootprints();
        return ResponseEntity.ok(result);
    }

    // Endpoint to add a new carbon footprint
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addCarbonFootprint(@RequestBody Map<String, Object> newCarbonFootprint) {
        String footprintName = (String) newCarbonFootprint.get("footprintName");
        String type = (String) newCarbonFootprint.get("type");
        double carbonValue = Double.parseDouble(newCarbonFootprint.get("carbonValue").toString());

        carbonFootprintService.addCarbonFootprint(footprintName, type, carbonValue);
        return ResponseEntity.ok("Carbon footprint added successfully!");
    }

    // Endpoint to update an existing carbon footprint
    @PutMapping(value = "/{footprintName}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateCarbonFootprint(@PathVariable String footprintName,
            @RequestBody Map<String, Object> updatedCarbonFootprint) {
        String newType = (String) updatedCarbonFootprint.get("type");
        double newCarbonValue = Double.parseDouble(updatedCarbonFootprint.get("carbonValue").toString());

        carbonFootprintService.updateCarbonFootprint(footprintName, newType, newCarbonValue);
        return ResponseEntity.ok("Carbon footprint updated successfully!");
    }

    // Endpoint to delete a carbon footprint
    @DeleteMapping(value = "/{footprintName}")
    public ResponseEntity<String> deleteCarbonFootprint(@PathVariable String footprintName) {
        carbonFootprintService.deleteCarbonFootprint(footprintName);
        return ResponseEntity.ok("Carbon footprint deleted successfully!");
    }

    // Endpoint to search carbon footprints by name or type
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> searchCarbonFootprints(@RequestParam("value") String value) {
        String result = carbonFootprintService.searchCarbonFootprints(value);
        return ResponseEntity.ok(result);
    }

}
