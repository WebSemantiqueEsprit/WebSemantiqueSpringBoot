package tn.esprit.twin1.EducationSpringApp.ControllersJena;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.twin1.EducationSpringApp.servicesJena.EnergyEfficiencyService;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")  // Enable CORS for this controller
@RequestMapping("/energy-efficiencies") // Base URL for the controller
public class EnergyEfficiencyController {

    @Autowired
    private EnergyEfficiencyService energyEfficiencyService;

    // Endpoint to get energy efficiency data from RDF file in JSON format
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getEnergyEfficienciesData(
            @RequestParam(required = false) String rating,
            @RequestParam(required = false) Float minSavingsPotential,
            @RequestParam(required = false) Float maxSavingsPotential) {

        // Pass filter parameters to the service layer
        String result = energyEfficiencyService.queryEnergyEfficiencies(rating, minSavingsPotential, maxSavingsPotential);
        return ResponseEntity.ok(result);
    }

    // Endpoint to add a new energy efficiency record
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addEnergyEfficiency(@RequestBody Map<String, Object> newEnergyEfficiency) {
        String efficiencyName = (String) newEnergyEfficiency.get("efficiencyName");
        String category = (String) newEnergyEfficiency.get("category");
        double efficiencyValue = Double.parseDouble(newEnergyEfficiency.get("efficiencyValue").toString());

        energyEfficiencyService.addEnergyEfficiency(efficiencyName, category, (float) efficiencyValue);
        return ResponseEntity.ok("Energy efficiency added successfully!");
    }

    // Endpoint to update an existing energy efficiency record
    @PutMapping(value = "/{efficiencyName}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateEnergyEfficiency(@PathVariable String efficiencyName, @RequestBody Map<String, Object> updatedEnergyEfficiency) {
        String newCategory = (String) updatedEnergyEfficiency.get("category");
        double newEfficiencyValue = Double.parseDouble(updatedEnergyEfficiency.get("efficiencyValue").toString());

        energyEfficiencyService.updateEnergyEfficiency(efficiencyName, newCategory, (float) newEfficiencyValue);
        return ResponseEntity.ok("Energy efficiency updated successfully!");
    }

    // Endpoint to delete an energy efficiency record
    @DeleteMapping(value = "/{efficiencyName}")
    public ResponseEntity<String> deleteEnergyEfficiency(@PathVariable String efficiencyName) {
        energyEfficiencyService.deleteEnergyEfficiency(efficiencyName);
        return ResponseEntity.ok("Energy efficiency deleted successfully!");
    }
    // Endpoint to search energy efficiency by name
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> searchEnergyEfficiency(@RequestParam String efficiencyName) {
        String result = energyEfficiencyService.searchByEfficiencyName(efficiencyName);
        return ResponseEntity.ok(result);
    }

    // Endpoint to filter energy efficiencies by rating and minimum savings potential
    @GetMapping(value = "/filter", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> filterEnergyEfficiencies(@RequestParam String rating, @RequestParam Float minSavingsPotential) {
        String result = energyEfficiencyService.filterByRatingAndSavings(rating, minSavingsPotential);
        return ResponseEntity.ok(result);
    }

}
