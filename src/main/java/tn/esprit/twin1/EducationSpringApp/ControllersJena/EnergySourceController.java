package tn.esprit.twin1.EducationSpringApp.ControllersJena;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tn.esprit.twin1.EducationSpringApp.servicesJena.EnergySourceService;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200") // Enable CORS for this controller
@RequestMapping("/energy-sources") // Base URL for the controller
public class EnergySourceController {

    @Autowired
    private EnergySourceService energySourceService;

    // Endpoint to get all energy sources
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getEnergySources() {
        String result = energySourceService.queryEnergySources();
        return ResponseEntity.ok(result);
    }
 @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE) 
 public ResponseEntity<Void> addEnergySource(@RequestBody Map<String, String> request) { 
    String energyType = request.get("energyType"); 
    String renewablePercentage = request.get("renewablePercentage");
     energySourceService.addEnergySource(energyType, renewablePercentage);
      return ResponseEntity.status(201).build();
 }
 
 // HTTP 201 Created }

// Endpoint to update an existing energy source
@PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<Void> updateEnergySource(@RequestBody Map<String, String> request) {
    String energyType = request.get("energyType");
    String newRenewablePercentage = request.get("renewablePercentage");
    energySourceService.updateEnergySource(energyType, newRenewablePercentage);
    return ResponseEntity.ok().build(); // HTTP 200 OK
}

// Endpoint to delete an energy source
@DeleteMapping("/{energyType}")
public ResponseEntity<Void> deleteEnergySource(@PathVariable String energyType) {
    energySourceService.deleteEnergySource(energyType);
    return ResponseEntity.noContent().build(); // HTTP 204 No Content
}

// Endpoint to get energy source by type
@GetMapping("/renewable-percentage/{renewablePercentage}")
public ResponseEntity<String> getEnergySourceByRenewablePercentage(@PathVariable String renewablePercentage) {
    return energySourceService.getEnergySourceByRenewablePercentage(renewablePercentage)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
}


// Endpoint to get energy sources by renewable percentage range
@GetMapping("/renewable-percentage")
public ResponseEntity<String> getEnergySourcesByRenewablePercentageRange(
        @RequestParam String minPercentage, @RequestParam String maxPercentage) {
    return energySourceService.getEnergySourcesByRenewablePercentageRange(minPercentage, maxPercentage)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
}
}