package tn.esprit.twin1.EducationSpringApp.servicesJena;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.update.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BehaviorPatternService {

    private static final String ONTOLOGY_NAMESPACE = "http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#";
    private static final String RDF_FILE_PATH = "path/to/your/ontology.rdf";

    private Model model;

    public BehaviorPatternService() {
        model = ModelFactory.createDefaultModel();
        model.read(RDF_FILE_PATH);
    }

    // Query all Behavior Patterns
    public List<Map<String, Object>> getAllBehaviorPatterns() {
        List<Map<String, Object>> behaviorPatterns = new ArrayList<>();

        String queryString = String.format(
                "PREFIX ont: <%s> " +
                        "SELECT ?behaviorPattern ?reductionPotential ?usagePattern " +
                        "WHERE { " +
                        "  ?behaviorPattern a ont:BehaviorPattern ; " +
                        "                   ont:hasReductionPotential ?reductionPotential ; " +
                        "                   ont:hasUsagePattern ?usagePattern . " +
                        "}", ONTOLOGY_NAMESPACE);

        try (QueryExecution queryExecution = QueryExecutionFactory.create(QueryFactory.create(queryString), model)) {
            ResultSet results = queryExecution.execSelect();

            while (results.hasNext()) {
                QuerySolution solution = results.next();
                // Extract and map results (populate the behaviorPatterns list)
                // Example: mapping can be done with solution.get("fieldName").toString()
            }
        }

        return behaviorPatterns;
    }

    // Add a new Behavior Pattern
    public void addBehaviorPattern(String behaviorPatternName, String usagePattern, float reductionPotential) {
        String insertQuery = String.format(
                "PREFIX ont: <%s> " +
                        "INSERT DATA { " +
                        "  ont:%s a ont:BehaviorPattern ; " +
                        "           ont:hasReductionPotential %f ; " +
                        "           ont:hasUsagePattern \"%s\" . " +
                        "}", ONTOLOGY_NAMESPACE, behaviorPatternName, reductionPotential, usagePattern);

        UpdateAction.parseExecute(insertQuery, model);
    }

    // Update an existing Behavior Pattern
    public void updateBehaviorPattern(String behaviorPatternName, String usagePattern, float reductionPotential) {
        String deleteInsertQuery = String.format(
                "PREFIX ont: <%s> " +
                        "DELETE { ?bp ont:hasReductionPotential ?oldPotential ; ont:hasUsagePattern ?oldUsage . } " +
                        "INSERT { ?bp ont:hasReductionPotential %f ; ont:hasUsagePattern \"%s\" . } " +
                        "WHERE { ?bp a ont:BehaviorPattern ; " +
                        "        ont:hasReductionPotential ?oldPotential ; " +
                        "        ont:hasUsagePattern ?oldUsage ; " +
                        "        FILTER (str(?bp) = \"%s\") " +
                        "}", ONTOLOGY_NAMESPACE, reductionPotential, usagePattern, ONTOLOGY_NAMESPACE + behaviorPatternName);

        UpdateAction.parseExecute(deleteInsertQuery, model);
    }

    // Delete a Behavior Pattern
    public void deleteBehaviorPattern(String behaviorPatternName) {
        String deleteQuery = String.format(
                "PREFIX ont: <%s> " +
                        "DELETE WHERE { ont:%s ?p ?o . }", ONTOLOGY_NAMESPACE, behaviorPatternName);

        UpdateAction.parseExecute(deleteQuery, model);
    }

    // Custom query to get Behavior Patterns enabling a Carbon Reduction Strategy
    public List<Map<String, Object>> getBehaviorPatternsWithCarbonReductionStrategy() {
        List<Map<String, Object>> behaviorPatterns = new ArrayList<>();

        String queryString = String.format(
                "PREFIX ont: <%s> " +
                        "SELECT ?behaviorPattern ?strategy " +
                        "WHERE { " +
                        "  ?behaviorPattern a ont:BehaviorPattern ; " +
                        "                   ont:enablesCarbonReductionStrategy ?strategy . " +
                        "}", ONTOLOGY_NAMESPACE);

        try (QueryExecution queryExecution = QueryExecutionFactory.create(QueryFactory.create(queryString), model)) {
            ResultSet results = queryExecution.execSelect();

            while (results.hasNext()) {
                QuerySolution solution = results.next();
                // Extract and map results (populate the behaviorPatterns list)
            }
        }

        return behaviorPatterns;
    }
}