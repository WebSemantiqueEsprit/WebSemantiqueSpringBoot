package tn.esprit.twin1.EducationSpringApp.ControllersJena;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.twin1.EducationSpringApp.servicesJena.ContractService;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/contracts")
public class ContractController {

    @Autowired
    private ContractService contractService;

    // Endpoint to get contract data from RDF file in JSON format
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getContractsData() {
        String result = contractService.queryContracts();
        return ResponseEntity.ok(result);
    }

    // Endpoint to add a new contract
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addContract(@RequestBody Map<String, Object> newContract) {
        String contractName = (String) newContract.get("contractName");
        double cost = Double.parseDouble(newContract.get("hasCostContract").toString());
        String duration = (String) newContract.get("hasDuration");

        contractService.addContract(contractName, cost, duration);
        return ResponseEntity.ok("Contract added successfully");
    }

    // Endpoint to update an existing contract
    @PutMapping(value = "/{contractName}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateContract(@PathVariable String contractName, @RequestBody Map<String, Object> updatedContract) {
        // Extract values from the JSON request body
        double newCost = Double.parseDouble(updatedContract.get("hasCostContract").toString());
        String newDuration = (String) updatedContract.get("hasDuration");

        // Call the service method to perform the update
        contractService.updateContract(contractName, newCost, newDuration);

        // Return success response
        return ResponseEntity.ok("Contract updated successfully!");
    }


    // Endpoint to delete a contract
    @DeleteMapping("/{contractName}")
    public ResponseEntity<String> deleteContract(@PathVariable String contractName) {
        contractService.deleteContract(contractName);
        return ResponseEntity.ok("Contract deleted successfully");
    }
}
