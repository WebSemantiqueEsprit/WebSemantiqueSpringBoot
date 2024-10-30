package tn.esprit.twin1.EducationSpringApp.servicesJena;

import org.apache.jena.rdf.model.*;
import org.apache.jena.query.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDF;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Map;

@Component
public class ContractService {

    private static final String RDF_FILE_PATH = "src/main/java/Ontology-WebSemantic.rdf";
    private Model model;

    // Load RDF file
    public Model loadRDF() {
        model = ModelFactory.createDefaultModel();
        InputStream in = FileManager.get().open(RDF_FILE_PATH);
        if (in == null) {
            throw new IllegalArgumentException("File not found: " + RDF_FILE_PATH);
        }
        model.read(in, null);
        return model;
    }

    // Add a new contract
    public void addContract(String contractName, double cost, String duration) {
        if (model == null) {
            loadRDF();
        }

        Resource contractResource = model.createResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + contractName);
        contractResource.addProperty(RDF.type, model.getResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#Contract"));
        contractResource.addProperty(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasCostContract"), model.createTypedLiteral(cost));
        contractResource.addProperty(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasDuration"), duration);

        saveRDF();
    }

    // Update an existing contract
    public void updateContract(String contractName, double newCost, String newDuration) {
        if (model == null) {
            loadRDF();
        }

        Resource contractResource = model.getResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + contractName);
        if (contractResource != null) {
            // Update the cost property
            contractResource.removeAll(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasCostContract"));
            contractResource.addProperty(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasCostContract"), model.createTypedLiteral(newCost));

            // Update the duration property
            contractResource.removeAll(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasDuration"));
            contractResource.addProperty(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasDuration"), newDuration);

            // Save changes to RDF
            saveRDF();
        }
    }


    // Delete a contract
    public void deleteContract(String contractName) {
        if (model == null) {
            loadRDF();
        }

        Resource contractResource = model.getResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + contractName);
        if (contractResource != null) {
            model.removeAll(contractResource, null, null);
            model.removeAll(null, null, contractResource);

            saveRDF();
        }
    }

    // Query contracts
    public String queryContracts() {
        loadRDF();

        String queryString =
                "PREFIX ontology: <http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#> " +
                        "SELECT ?contract ?hasCostContract ?hasDuration " +
                        "WHERE { " +
                        "  ?contract a ontology:Contract . " +
                        "  ?contract ontology:hasCostContract ?hasCostContract . " +
                        "  ?contract ontology:hasDuration ?hasDuration . " +
                        "}";

        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            JSONArray contractsArray = new JSONArray();

            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                JSONObject contractObject = new JSONObject();

                String contractUrl = solution.getResource("contract").toString();
                String contractName = contractUrl.split("#")[1];

                String hasCostContract = solution.get("hasCostContract").toString().replaceAll("\\^\\^.*", "");
                String hasDuration = solution.get("hasDuration").toString();

                contractObject.put("contractName", contractName);
                contractObject.put("hasCostContract", hasCostContract);
                contractObject.put("hasDuration", hasDuration);

                contractsArray.put(contractObject);
            }

            JSONObject resultJson = new JSONObject();
            resultJson.put("contracts", contractsArray);
            return resultJson.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error querying contracts: " + e.getMessage());
        }
    }

    private void saveRDF() {
        try (FileOutputStream out = new FileOutputStream(RDF_FILE_PATH)) {
            model.write(out, "RDF/XML");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String searchContracts(String contractName, Double minCost, Double maxCost, String duration) {
        // Ensure the RDF model is loaded
        if (model == null) {
            loadRDF();
        }

        // Start constructing the SPARQL query
        StringBuilder queryString = new StringBuilder(
                "PREFIX ontology: <http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#> " +
                        "SELECT ?contract ?hasCostContract ?hasDuration WHERE { ?contract a ontology:Contract ."
        );

        // Apply filters based on provided criteria
        if (contractName != null && !contractName.isEmpty()) {
            queryString.append(" FILTER regex(str(?contract), \"").append(contractName).append("\", \"i\") ");
        }
        if (minCost != null) {
            queryString.append(" ?contract ontology:hasCostContract ?hasCostContract . FILTER(?hasCostContract >= ").append(minCost).append(") ");
        }
        if (maxCost != null) {
            queryString.append(" ?contract ontology:hasCostContract ?hasCostContract . FILTER(?hasCostContract <= ").append(maxCost).append(") ");
        }
        if (duration != null && !duration.isEmpty()) {
            queryString.append(" ?contract ontology:hasDuration \"").append(duration).append("\" . ");
        }

        queryString.append("}");

        // Create and execute the SPARQL query
        Query query = QueryFactory.create(queryString.toString());
        JSONArray contractsArray = new JSONArray();

        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();

            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                JSONObject contractObject = new JSONObject();

                String contractUrl = solution.getResource("contract").toString();
                String contractNameResult = contractUrl.substring(contractUrl.indexOf('#') + 1); // Get the name after the hash

                // Extract cost and duration, handling cases where values might be null
                String hasCostContract = solution.get("hasCostContract") != null ?
                        solution.get("hasCostContract").toString().replaceAll("\\^\\^.*", "") : "N/A";
                String hasDurationResult = solution.get("hasDuration") != null ?
                        solution.get("hasDuration").toString() : "N/A";

                contractObject.put("contractName", contractNameResult);
                contractObject.put("hasCostContract", hasCostContract);
                contractObject.put("hasDuration", hasDurationResult);

                contractsArray.put(contractObject);
            }

            // Construct and return the final result JSON
            JSONObject resultJson = new JSONObject();
            resultJson.put("contracts", contractsArray);
            return resultJson.toString();
        } catch (Exception e) {
            // Log and rethrow a runtime exception for error handling
            e.printStackTrace();
            throw new RuntimeException("Error querying contracts: " + e.getMessage());
        }
    }

}
