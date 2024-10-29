package tn.esprit.twin1.EducationSpringApp.servicesJena;

import org.apache.jena.rdf.model.*;
import org.apache.jena.query.*;
import org.apache.jena.util.FileManager;
import org.springframework.stereotype.Component;
import org.apache.jena.vocabulary.RDF;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserService {

    private static final String RDF_FILE_PATH = "E:/Work/4 twin/Web Semantique/projet/Ontology-WebSemantic.rdf";
    private Model model;

    // Method to load the RDF file
    public Model loadRDF() {
        model = ModelFactory.createDefaultModel();
        InputStream in = FileManager.get().open(RDF_FILE_PATH);
        if (in == null) {
            throw new IllegalArgumentException("File not found: " + RDF_FILE_PATH);
        }
        model.read(in, null);
        return model;
    }

    // Method to query user details based on the User class
    public Map<String, String> getUserDetails(String userId) {
        loadRDF();
        Map<String, String> userDetails = new HashMap<>();

        // SPARQL query to get details of a specific user
        String queryString = "PREFIX ont: <http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#> "
                + "SELECT ?email ?name ?carbonFootprintGoal "
                + "WHERE { "
                + "   ont:" + userId + " ont:hasEmail ?email . "
                + "   ont:" + userId + " ont:hasName ?name . "
                + "   ont:" + userId + " ont:hasCarbonFootprintGoal ?carbonFootprintGoal . "
                + "}";

        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            if (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                userDetails.put("Email", soln.getLiteral("email").getString());
                userDetails.put("Name", soln.getLiteral("name").getString());
                userDetails.put("CarbonFootprintGoal", String.valueOf(soln.getLiteral("carbonFootprintGoal").getFloat()));
            }
        }
        return userDetails;
    }

    private void saveRDF() {
        try (OutputStream out = new FileOutputStream(RDF_FILE_PATH)) {
            model.write(out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to create a User instance
    public Resource createUser(String uri, String name, String email, float carbonFootprintGoal) {
        loadRDF();

        Resource user = model.createResource(uri)
                .addProperty(RDF.type, model.createResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#User"))
                .addLiteral(model.createProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasName"), name)
                .addLiteral(model.createProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasEmail"), email)
                .addLiteral(model.createProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasCarbonFootprintGoal"), carbonFootprintGoal);

        saveRDF();
        return user;
    }

    // Method to update an existing User instance
    public Resource updateUser(String uri, String newName, String newEmail, Float newCarbonFootprintGoal) {
        loadRDF();
        Resource user = model.getResource(uri);
        if (user == null) {
            throw new IllegalArgumentException("User with URI " + uri + " does not exist.");
        }

        if (newName != null) {
            user.removeAll(model.createProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasName"))
                    .addLiteral(model.createProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasName"), newName);
        }

        if (newEmail != null) {
            user.removeAll(model.createProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasEmail"))
                    .addLiteral(model.createProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasEmail"), newEmail);
        }

        if (newCarbonFootprintGoal != null) {
            user.removeAll(model.createProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasCarbonFootprintGoal"))
                    .addLiteral(model.createProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasCarbonFootprintGoal"), newCarbonFootprintGoal);
        }

        saveRDF();
        return user;
    }


    public boolean deleteUser(String uri) {
        loadRDF();
        Resource user = model.getResource(uri);
        if (user == null) {
            return false; // User not found
        }

        // Remove all triples associated with this user
        model.removeAll(user, null, null);
        model.removeAll(null, null, user);

        saveRDF(); // Save changes to RDF model
        return true; // User successfully deleted
    }


    public List<Map<String, String>> getAllUsers() {
        loadRDF();
        List<Map<String, String>> users = new ArrayList<>();

        // Define the SPARQL query to retrieve all User instances
        String queryString = "PREFIX ont: <http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#> "
                + "SELECT ?user ?name ?email ?carbonFootprintGoal "
                + "WHERE { "
                + "   ?user a ont:User . "
                + "   ?user ont:hasName ?name . "
                + "   ?user ont:hasEmail ?email . "
                + "   ?user ont:hasCarbonFootprintGoal ?carbonFootprintGoal . " // Correct the predicate
                + "}";

        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();

            // Process each result in the ResultSet
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Map<String, String> details = new HashMap<>();

                // Extract URI, get the last part after the '#'
                String fullUri = soln.getResource("user").getURI();
                String uriName = fullUri.substring(fullUri.lastIndexOf("#") + 1);

                // Add URI name, name, email, and carbon footprint goal to the map
                details.put("URI", uriName);
                details.put("Name", soln.getLiteral("name").getString());
                details.put("Email", soln.getLiteral("email").getString());
                details.put("CarbonFootprintGoal", String.valueOf(soln.getLiteral("carbonFootprintGoal").getFloat())); // Ensure to convert to String

                users.add(details);
            }
        }
        return users; // Return the list of users
    }

    public List<Map<String, String>> searchUserByName(String userName) {
        // Check if the model is initialized
        if (model == null) {
            loadRDF();
        }

        List<Map<String, String>> userDetailsList = new ArrayList<>();

        // Define the SPARQL query to search for users by name
        String queryString =
                "PREFIX ont: <http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#> " +
                        "SELECT ?user ?name ?email ?carbonFootprintGoal " +
                        "WHERE { " +
                        "  ?user a ont:User . " +
                        "  ?user ont:hasName ?name . " +
                        "  FILTER(CONTAINS(lcase(str(?name)), lcase(\"" + userName + "\"))) . " + // Case-insensitive search
                        "  OPTIONAL { ?user ont:hasEmail ?email . } " +
                        "  OPTIONAL { ?user ont:hasCarbonFootprintGoal ?carbonFootprintGoal . } " +
                        "}";

        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();

            // Process each result in the ResultSet
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Map<String, String> details = new HashMap<>();

                // Extract URI, get the last part after the '#'
                String fullUri = soln.getResource("user").getURI();
                String uriName = fullUri.substring(fullUri.lastIndexOf("#") + 1);

                // Add URI name, name, email, and carbon footprint goal to the map
                details.put("URI", uriName);
                details.put("Name", soln.getLiteral("name").getString());
                details.put("Email", soln.getLiteral("email") != null ? soln.getLiteral("email").getString() : "N/A");
                details.put("CarbonFootprintGoal", soln.getLiteral("carbonFootprintGoal") != null ?
                        String.valueOf(soln.getLiteral("carbonFootprintGoal").getFloat()) : "N/A");

                userDetailsList.add(details);
            }
        }
        return userDetailsList; // Return the list of user details
    }


    public List<Map<String, String>> filterUsersByCarbonFootprintGoal(float carbonFootprintGoal) {
        // Check if the model is initialized
        if (model == null) {
            loadRDF();
        }

        List<Map<String, String>> users = new ArrayList<>();

        // Define the SPARQL query to filter users by carbon footprint goal
        String queryString =
                "PREFIX ont: <http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#> " +
                        "SELECT ?user ?name ?email ?carbonFootprintGoal " +
                        "WHERE { " +
                        "  ?user a ont:User . " +
                        "  ?user ont:hasName ?name . " +
                        "  ?user ont:hasEmail ?email . " +
                        "  ?user ont:hasCarbonFootprintGoal ?carbonFootprintGoal . " +
                        "  FILTER(?carbonFootprintGoal <= " + carbonFootprintGoal + ") " + // Filter by carbon footprint goal
                        "}";

        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();

            // Process each result in the ResultSet
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Map<String, String> details = new HashMap<>();

                // Extract URI, get the last part after the '#'
                String fullUri = soln.getResource("user").getURI();
                String uriName = fullUri.substring(fullUri.lastIndexOf("#") + 1);

                // Add URI name, name, email, and carbon footprint goal to the map
                details.put("URI", uriName);
                details.put("Name", soln.getLiteral("name").getString());
                details.put("Email", soln.getLiteral("email").getString());
                details.put("CarbonFootprintGoal", String.valueOf(soln.getLiteral("carbonFootprintGoal").getFloat())); // Ensure to convert to String

                users.add(details);
            }
        }
        return users; // Return the filtered list of users
    }

}
