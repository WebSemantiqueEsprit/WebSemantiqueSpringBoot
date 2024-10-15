package tn.esprit.twin1.EducationSpringApp.servicesJena;

import org.apache.jena.rdf.model.*;
import org.apache.jena.query.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.InputStream;

@Component
public class CarbonReductionStrategyService {

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

    public void addCarbonReductionStrategy(String strategyName, double cost, double impactValue) {
        if (model == null) {
            loadRDF();
        }

        // Create a new individual for the carbon reduction strategy
        Resource strategyResource = model.createResource(
                "http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + strategyName);
        strategyResource.addProperty(RDF.type, model.getResource(
                "http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#CarbonReductionStrategy"));
        strategyResource.addProperty(
                model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasCost"),
                model.createTypedLiteral(cost));
        strategyResource.addProperty(
                model.getProperty(
                        "http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasImpactValue"),
                model.createTypedLiteral(impactValue));

        // Save the updated model back to the RDF file
        saveRDF();
    }

    // Method to update an existing carbon reduction strategy
    public void updateCarbonReductionStrategy(String strategyName, double newCost, double newImpactValue) {
        if (model == null) {
            loadRDF();
        }

        // Find the existing carbon reduction strategy resource by name
        Resource strategyResource = model
                .getResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + strategyName);

        if (strategyResource != null) {
            // Update the properties
            strategyResource.removeAll(model
                    .getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasCost"));
            strategyResource.addProperty(
                    model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasCost"),
                    model.createTypedLiteral(newCost));

            strategyResource.removeAll(model.getProperty(
                    "http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasImpactValue"));
            strategyResource.addProperty(
                    model.getProperty(
                            "http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasImpactValue"),
                    model.createTypedLiteral(newImpactValue));

            // Save changes
            saveRDF();
        }
    }

    // Method to delete a carbon reduction strategy
    public void deleteCarbonReductionStrategy(String strategyName) {
        if (model == null) {
            loadRDF();
        }

        // Find the existing carbon reduction strategy resource by name
        Resource strategyResource = model
                .getResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + strategyName);

        if (strategyResource != null) {
            // Remove the resource from the model
            model.removeAll(strategyResource, null, null);
            model.removeAll(null, null, strategyResource);

            // Save changes
            saveRDF();
        }
    }

    private void saveRDF() {
        try (FileOutputStream out = new FileOutputStream(RDF_FILE_PATH)) {
            model.write(out, "RDF/XML");
        } catch (Exception e) {
            e.printStackTrace(); // Log the error for debugging
        }
    }

    // Method to query carbon reduction strategies
    public String queryCarbonReductionStrategies() {
        loadRDF();
        String queryString = "PREFIX ontology: <http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#> "
                +
                "SELECT ?strategy ?cost ?impactValue " +
                "WHERE { " +
                "  ?strategy a ontology:CarbonReductionStrategy . " +
                "  ?strategy ontology:hasCost ?cost . " +
                "  ?strategy ontology:hasImpactValue ?impactValue . " +
                "}";

        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();

            // Start building the JSON result
            StringBuilder jsonResult = new StringBuilder();
            jsonResult.append("{\n");
            jsonResult.append("    \"CarbonReductionStrategy\": [\n");

            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Resource strategy = soln.getResource("strategy");
                Literal cost = soln.getLiteral("cost");
                Literal impactValue = soln.getLiteral("impactValue");

                jsonResult.append("        {\n")
                        .append("            \"reductionStrategyName\": \"").append(strategy.getLocalName())
                        .append("\",\n") // Use getLocalName() for just the name
                        .append("            \"hasCost\": ").append(cost.getFloat()).append(",\n")
                        .append("            \"hasImpactValue\": ").append(impactValue.getFloat()).append("\n")
                        .append("        }");
                if (results.hasNext()) {
                    jsonResult.append(","); // Add a comma if there are more results
                }
                jsonResult.append("\n");
            }

            // Close the JSON structure
            jsonResult.append("    ]\n");
            jsonResult.append("}");

            return jsonResult.toString();
        }
    }

}
