package tn.esprit.twin1.EducationSpringApp.ControllersJena;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.twin1.EducationSpringApp.servicesJena.ProviderService;

import java.util.Map;

@RestController
@RequestMapping("/providers")
@CrossOrigin(origins = "http://localhost:4200")  // Enable CORS for this controller
public class ProviderController {

    @Autowired
    private ProviderService providerService;

    // Endpoint to add a new provider
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addProvider(@RequestBody Map<String, Object> newProvider) {
        // Check if providerName exists and is not null
        if (newProvider.get("providerName") == null) {
            return ResponseEntity.badRequest().body("providerName is required.");
        }
        String providerName = newProvider.get("providerName").toString();

        // Check if greenEnergyPercentage exists and is not null
        if (newProvider.get("greenEnergyPercentage") == null) {
            return ResponseEntity.badRequest().body("greenEnergyPercentage is required.");
        }
        double greenEnergyPercentage;
        try {
            greenEnergyPercentage = Double.parseDouble(newProvider.get("greenEnergyPercentage").toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("greenEnergyPercentage must be a valid number.");
        }

        providerService.addProvider(providerName, greenEnergyPercentage);
        return ResponseEntity.ok("Provider added successfully");
    }

    // Endpoint to update an existing provider
    @PutMapping(value = "/{providerName}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateProvider(@PathVariable String providerName, @RequestBody Map<String, Object> updatedProvider) {
        // Extract the new green energy percentage from the JSON request body
        double newGreenEnergyPercentage = Double.parseDouble(updatedProvider.get("greenEnergyPercentage").toString());

        // Call the service method to perform the update
        providerService.updateProvider(providerName, newGreenEnergyPercentage);

        // Return success response
        return ResponseEntity.ok("Provider updated successfully!");
    }


    // Endpoint to delete a provider
    @DeleteMapping("/{providerName}")
    public ResponseEntity<String> deleteProvider(@PathVariable String providerName) {
        providerService.deleteProvider(providerName);
        return ResponseEntity.ok("Provider deleted successfully");
    }

    // Endpoint to get provider data
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getProviders() {
        String result = providerService.queryProviders();
        return ResponseEntity.ok(result);
    }

    // Endpoint to search providers
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> searchProviders(@RequestParam("searchTerm") String searchTerm) {
        String result = providerService.searchProviders(searchTerm);
        return ResponseEntity.ok(result);
    }

}
