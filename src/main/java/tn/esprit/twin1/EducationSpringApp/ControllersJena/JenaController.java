package tn.esprit.twin1.EducationSpringApp.ControllersJena;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tn.esprit.twin1.EducationSpringApp.Jena;

import java.util.Map; // Import the Map interface

@RestController
public class JenaController {

    @Autowired
    private Jena jena;

    // Endpoint to get contract data from RDF file in JSON format
    @GetMapping(value = "/rdf/contracts", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getContractsData() {
        String result = jena.queryContracts();
        return ResponseEntity.ok(result);
    }

    // Endpoint to add a new contract
    @PostMapping(value = "/contracts", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addContract(@RequestBody Map<String, Object> newContract) {
        String contractName = (String) newContract.get("contractName");
        String duration = (String) newContract.get("duration");
        double cost = Double.parseDouble(newContract.get("cost").toString());

        jena.addContract(contractName, duration, cost);
        return ResponseEntity.ok("Contract added successfully!");
    }

    // Endpoint to update an existing contract
    @PutMapping(value = "/contracts/{contractName}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateContract(@PathVariable String contractName, @RequestBody Map<String, Object> updatedContract) {
        String newDuration = (String) updatedContract.get("duration");
        double newCost = Double.parseDouble(updatedContract.get("cost").toString());

        jena.updateContract(contractName, newDuration, newCost);
        return ResponseEntity.ok("Contract updated successfully!");
    }
     // Endpoint to delete a contract
     @DeleteMapping(value = "/contracts/{contractName}")
     public ResponseEntity<String> deleteContract(@PathVariable String contractName) {
         jena.deleteContract(contractName);
         return ResponseEntity.ok("Contract deleted successfully!");
     }

    // Endpoint pour rechercher des contrats par prix
     @GetMapping(value = "/contracts/searchByCost", produces = MediaType.APPLICATION_JSON_VALUE)
     public ResponseEntity<String> searchContractsByCost(@RequestParam double minCost, @RequestParam double maxCost) {
         String result = jena.searchContractsByCost(minCost, maxCost);
         return ResponseEntity.ok(result);
     }
 
     // Endpoint pour rechercher des contrats par durée
     @GetMapping(value = "/contracts/searchByDuration", produces = MediaType.APPLICATION_JSON_VALUE)
     public ResponseEntity<String> searchContractsByDuration(@RequestParam String duration) {
         String result = jena.searchContractsByDuration(duration);
         return ResponseEntity.ok(result);
     }


}
