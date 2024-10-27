package tn.esprit.twin1.EducationSpringApp.ControllersJena;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.twin1.EducationSpringApp.dto.EnergyStorageRequest;
import tn.esprit.twin1.EducationSpringApp.dto.UpdateRequest;
import tn.esprit.twin1.EducationSpringApp.servicesJena.EnergyStorageService;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/storage")
public class EnergyStorageController {
    @Autowired
    private EnergyStorageService storageService;


    @GetMapping
    public ResponseEntity<String> getAllEnergyStorages() {
        String result = storageService.queryEnergyStorages();
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> addEnergyStorage(@RequestBody Map<String, Object> payload) {
        String solutionName = (String) payload.get("solutionName");
        float capacity = Float.parseFloat(payload.get("capacity").toString());
        float efficiency = Float.parseFloat(payload.get("efficiency").toString());

        storageService.addEnergyStorage(solutionName, capacity, efficiency);

        Map<String, String> response = new HashMap<>();
        response.put("message", "EnergyStorage added successfully.");
        return ResponseEntity.ok(response);
    }


    @PutMapping("/update")
    public ResponseEntity<Map<String, String>> updateEnergyStorage(@RequestBody UpdateRequest request) {
        // Extract parameters from the request object
        String solutionName = request.getSolutionName();
        float newCapacity = request.getCapacity();
        float newEfficiency = request.getEfficiency();

        // Call the service method to update the energy storage
        storageService.updateEnergyStorage(solutionName, newCapacity, newEfficiency);

        // Return a JSON response
        Map<String, String> response = new HashMap<>();
        response.put("message", "EnergyStorage updated successfully.");
        return ResponseEntity.ok(response);
    }




    @DeleteMapping("/{solutionName}")
    public ResponseEntity<String> deleteEnergyStorage(@PathVariable String solutionName) {
        storageService.deleteEnergyStorage(solutionName);
        return ResponseEntity.ok("EnergyStorage  deleted successfully.");
    }

    @GetMapping("/searchByCapacity")
    public ResponseEntity<String> searchEnergyStoragesByCapacity(
            @RequestParam float minCapacity,
            @RequestParam float maxCapacity) {
        String result = storageService.searchEnergyStoragesByCost(minCapacity, maxCapacity);
        return ResponseEntity.ok(result);  // Return JSON response
    }

    @GetMapping("/searchByEfficiency")
    public ResponseEntity<String> searchEnergyStoragesByEfficiency(
            @RequestParam float minEfficiency,
            @RequestParam float maxEfficiency) {
        String result = storageService.searchEnergyStoragesByEfficiency(minEfficiency, maxEfficiency);
        return ResponseEntity.ok(result);  // Return JSON response
    }
}
