package tn.esprit.twin1.EducationSpringApp;

import org.apache.jena.rdf.model.*;
import org.apache.jena.query.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDF;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Component
public class Jena {

    private static final String RDF_FILE_PATH = "C:/Users/pc/Desktop/Ontology-WebSemantic.rdf";
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

    // Method to query contracts
    public String queryContracts() {
        loadRDF();
        System.out.println("Model size: " + model.size());

        String queryString =
            "PREFIX untitled-ontology-4: <http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#> " +
            "SELECT ?contract ?property ?value " +
            "WHERE { " +
            "  ?contract a untitled-ontology-4:Contract . " +
            "  ?contract ?property ?value . " +
            "}";

        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            Map<String, JSONObject> contractsMap = new HashMap<>();

            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                String contractUrl = solution.getResource("contract").toString();
                String contractName = contractUrl.split("#")[1];
                String property = solution.get("property").toString().split("#")[1];
                String value = solution.get("value").toString();

                if (property.equals("hasCost")) {
                    // Supprimez la notation de type
                    value = value.replaceAll("\\^\\^.*", "");
                }

                contractsMap.putIfAbsent(contractName, new JSONObject());
                contractsMap.get(contractName).put(property, value);
            }

            JSONObject resultJson = new JSONObject();
            JSONArray contractsArray = new JSONArray();

            for (Map.Entry<String, JSONObject> entry : contractsMap.entrySet()) {
                JSONObject contractObject = new JSONObject();
                contractObject.put(entry.getKey(), entry.getValue());
                contractsArray.put(contractObject);
            }

            resultJson.put("contracts", contractsArray);
            return resultJson.toString();
        }
    }

    // Method to add a new contract
    public void addContract(String contractName, String duration, double cost) {
        if (model == null) {
            loadRDF(); // Load the model if it's not already loaded
        }

        // Create a new individual for the contract
        Resource contractResource = model.createResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + contractName);
        contractResource.addProperty(RDF.type, model.getResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#Contract"));
        contractResource.addProperty(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasDuration"), duration);
        contractResource.addProperty(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasCost"), model.createTypedLiteral(cost));

        // Save the updated model back to the RDF file
        saveRDF();
    }

    // Method to update an existing contract
    public void updateContract(String contractName, String newDuration, double newCost) {
        if (model == null) {
            loadRDF(); // Load the model if it's not already loaded
        }

        Resource contractResource = model.getResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + contractName);
        if (contractResource != null) {
            // Remove existing properties
            contractResource.removeAll(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasDuration"));
            contractResource.removeAll(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasCost"));

            // Add updated properties
            contractResource.addProperty(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasDuration"), newDuration);
            contractResource.addProperty(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasCost"), model.createTypedLiteral(newCost));

            // Save the updated model back to the RDF file
            saveRDF();
        } else {
            System.out.println("Contract not found: " + contractName);
        }
    }

    // Method to delete a contract
    public void deleteContract(String contractName) {
        if (model == null) {
            loadRDF(); // Load the model if it's not already loaded
        }

        Resource contractResource = model.getResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + contractName);
        if (contractResource != null) {
            // Remove the contract resource from the model
            contractResource.removeProperties(); // Remove all properties of the resource

            // Save the updated model back to the RDF file
            saveRDF();
        } else {
            System.out.println("Contract not found: " + contractName);
        }
    }

    // Helper method to save the RDF model to file
    private void saveRDF() {
        try (FileOutputStream out = new FileOutputStream(RDF_FILE_PATH)) {
            model.write(out, "RDF/XML");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String searchContractsByCost(double minCost, double maxCost) {
        loadRDF();

        String queryString =
            "PREFIX untitled-ontology-4: <http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#> " +
            "SELECT ?contract ?duration ?cost " +
            "WHERE { " +
            "  ?contract a untitled-ontology-4:Contract . " +
            "  ?contract untitled-ontology-4:hasCost ?cost . " +
            "  ?contract untitled-ontology-4:hasDuration ?duration . " +
            "  FILTER(?cost >= " + minCost + " && ?cost <= " + maxCost + ") " +
            "}";

        return executeQuery(queryString);
    }

    // Method to search by duration
    public String searchContractsByDuration(String duration) {
        loadRDF();

        String queryString =
            "PREFIX untitled-ontology-4: <http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#> " +
            "SELECT ?contract ?cost " +
            "WHERE { " +
            "  ?contract a untitled-ontology-4:Contract . " +
            "  ?contract untitled-ontology-4:hasDuration \"" + duration + "\" . " +
            "  ?contract untitled-ontology-4:hasCost ?cost . " +
            "}";

        return executeQuery(queryString);
    }

    // Method to execute SPARQL query
    private String executeQuery(String queryString) {
        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            JSONArray contractsArray = new JSONArray();

            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                String contractUrl = solution.getResource("contract").toString();
                String contractName = contractUrl.split("#")[1];
                String cost = solution.get("cost").toString();

                // Supprimez la notation de type (^^http://www.w3.org/2001/XMLSchema#double)
                cost = cost.replaceAll("\\^\\^.*", "");

                JSONObject contractObject = new JSONObject();
                contractObject.put("contractName", contractName);
                contractObject.put("cost", cost);

                if (solution.contains("duration")) {
                    String duration = solution.get("duration").toString();
                    contractObject.put("duration", duration);
                }

                contractsArray.put(contractObject);
            }

            JSONObject resultJson = new JSONObject();
            resultJson.put("contracts", contractsArray);
            return resultJson.toString();
        }
    }
}
