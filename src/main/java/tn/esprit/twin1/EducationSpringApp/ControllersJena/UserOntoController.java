package tn.esprit.twin1.EducationSpringApp.ControllersJena;

import org.apache.jena.rdf.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.twin1.EducationSpringApp.servicesJena.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/users") // Base URL for user-related endpoints
public class UserOntoController {

    @Autowired
    private UserService userService; // Assuming you have a UserService for user operations

    // Endpoint to retrieve user details
    @GetMapping(value = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> getUserDetails(@PathVariable String userId) {
        Map<String, String> details = userService.getUserDetails(userId);
        return ResponseEntity.ok(details);
    }

    // Endpoint to create a new user
    @PostMapping(value = "/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createUser(@RequestBody Map<String, Object> newUser) {
        String uri = "http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + newUser.get("uri");
        String name = (String) newUser.get("name");
        String email = (String) newUser.get("email");
        Object carbonFootprintGoalObject = newUser.get("carbonFootprintGoal");
        Float carbonFootprintGoal = null;

        if (carbonFootprintGoalObject instanceof Number) {
            carbonFootprintGoal = ((Number) carbonFootprintGoalObject).floatValue();
        } else if (carbonFootprintGoalObject instanceof String) {
            try {
                carbonFootprintGoal = Float.parseFloat((String) carbonFootprintGoalObject);
            } catch (NumberFormatException e) {
                System.err.println("Invalid format for carbonFootprintGoal: " + carbonFootprintGoalObject);
            }
        }

        Resource user = userService.createUser(uri, name, email, carbonFootprintGoal);
        return ResponseEntity.ok("User created successfully with URI: " + user.getURI());
    }

    // Endpoint to update an existing user
    @PutMapping(value = "/{uri}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateUser(@PathVariable String uri, @RequestBody Map<String, Object> updatedData) {
        String userUri = "http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + uri;
        String newName = updatedData.containsKey("name") ? (String) updatedData.get("name") : null;
        String newEmail = updatedData.containsKey("email") ? (String) updatedData.get("email") : null;
        Object newCarbonFootprintGoalObject = updatedData.containsKey("carbonFootprintGoal") ? updatedData.get("carbonFootprintGoal") : null;
        Float newCarbonFootprintGoal = null;

        if (newCarbonFootprintGoalObject instanceof Number) {
            newCarbonFootprintGoal = ((Number) newCarbonFootprintGoalObject).floatValue();
        } else if (newCarbonFootprintGoalObject instanceof String) {
            try {
                newCarbonFootprintGoal = Float.parseFloat((String) newCarbonFootprintGoalObject);
            } catch (NumberFormatException e) {
                // Handle the exception (e.g., log it or set a default value)
                System.err.println("Invalid format for carbonFootprintGoal: " + newCarbonFootprintGoalObject);
            }
        }

        Resource updatedUser = userService.updateUser(userUri, newName, newEmail, newCarbonFootprintGoal);
        return ResponseEntity.ok("User updated successfully for URI: " + updatedUser.getURI());
    }


    // Endpoint to delete a user
    @DeleteMapping(value = "/{uri}")
    public ResponseEntity<String> deleteUser(@PathVariable String uri) {
        String userUri = "http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + uri;
        boolean deleted = userService.deleteUser(userUri);
        if (deleted) {
            return ResponseEntity.ok("User deleted successfully for URI: " + uri);
        } else {
            return ResponseEntity.status(404).body("User with URI " + uri + " not found.");
        }
    }

    // Endpoint to get all users
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, List<Map<String, String>>>> getAllUsers() {
        List<Map<String, String>> users = userService.getAllUsers();

        // Wrap the list in a map with the "Users" key
        Map<String, List<Map<String, String>>> response = new HashMap<>();
        response.put("Users", users);

        return ResponseEntity.ok(response);
    }


    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, String>>> searchUserByName(@RequestParam String UserName) {
        List<Map<String, String>> result = userService.searchUserByName(UserName);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Map<String, String>>> filterUsersByCarbonFootprintGoal(@RequestParam float carbonFootprintGoal) {
        List<Map<String, String>> filteredUsers = userService.filterUsersByCarbonFootprintGoal(carbonFootprintGoal);
        return ResponseEntity.ok(filteredUsers);
    }
}
