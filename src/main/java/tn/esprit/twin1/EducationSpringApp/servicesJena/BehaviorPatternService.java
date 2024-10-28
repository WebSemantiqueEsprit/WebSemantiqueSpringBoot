package tn.esprit.twin1.EducationSpringApp.servicesJena;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.update.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Service;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BehaviorPatternService {

    private static final String ONTOLOGY_NAMESPACE = "http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#";
    private static final String RDF_FILE_PATH = "/Users/macbookpro/Desktop/websementique/projet_Chouaib/Ontology-WebSemantic.rdf";

    private Model model;

    public BehaviorPatternService() {
        model = ModelFactory.createDefaultModel();
        model.read(RDF_FILE_PATH);
    }

    public Model loadRDF() {
        model = ModelFactory.createDefaultModel();
        InputStream in = FileManager.get().open(RDF_FILE_PATH);
        if (in == null) {
            throw new IllegalArgumentException("File not found: " + RDF_FILE_PATH);
        }
        model.read(in, null);
        return model;
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
    public void addBehaviorPattern(String name, String usagePattern, float reductionPotential) {
        if (model == null) {
            loadRDF();
        }

        Resource behaviorPatternResource = model.createResource(ONTOLOGY_NAMESPACE + name);
        behaviorPatternResource.addProperty(RDF.type, model.getResource(ONTOLOGY_NAMESPACE + "BehaviorPattern"));
        behaviorPatternResource.addProperty(model.getProperty(ONTOLOGY_NAMESPACE + "hasUsagePattern"), usagePattern);
        behaviorPatternResource.addProperty(model.getProperty(ONTOLOGY_NAMESPACE + "hasReductionPotential"), model.createTypedLiteral(reductionPotential));

        saveRDF();
    }





    // Update an existing Behavior Pattern
    public void updateBehaviorPattern(String name, String newUsagePattern, float newReductionPotential) {
        if (model == null) {
            loadRDF();
        }

        Resource behaviorPatternResource = model.getResource(ONTOLOGY_NAMESPACE + name);
        if (behaviorPatternResource != null) {
            behaviorPatternResource.removeAll(model.getProperty(ONTOLOGY_NAMESPACE + "hasUsagePattern"))
                    .addProperty(model.getProperty(ONTOLOGY_NAMESPACE + "hasUsagePattern"), newUsagePattern);
            behaviorPatternResource.removeAll(model.getProperty(ONTOLOGY_NAMESPACE + "hasReductionPotential"))
                    .addProperty(model.getProperty(ONTOLOGY_NAMESPACE + "hasReductionPotential"), model.createTypedLiteral(newReductionPotential));

            saveRDF();
        }
    }


    // Delete a Behavior Pattern
    public void deleteBehaviorPattern(String name) {
        if (model == null) {
            loadRDF();
        }

        Resource behaviorPatternResource = model.getResource(ONTOLOGY_NAMESPACE + name);
        if (behaviorPatternResource != null) {
            model.removeAll(behaviorPatternResource, null, null);
            model.removeAll(null, null, behaviorPatternResource);

            saveRDF();
        }
    }


    // Custom query to get Behavior Patterns enabling a Carbon Reduction Strategy
    public List<Map<String, Object>> getBehaviorPatternsWithCarbonReductionStrategy() {
        loadRDF();
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

    public String queryBehaviorPatterns() {
        loadRDF();
        System.out.println("Model size: " + model.size());

        String queryString =
                "PREFIX ontology: <http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#> " +
                        "SELECT ?behaviorPattern ?hasReductionPotential ?hasUsagePattern ?enablesCarbonReductionStrategy " +
                        "WHERE { " +
                        "  ?behaviorPattern a ontology:BehaviorPattern . " +
                        "  ?behaviorPattern ontology:hasReductionPotential ?hasReductionPotential . " +
                        "  ?behaviorPattern ontology:hasUsagePattern ?hasUsagePattern . " +
                        "  OPTIONAL { ?behaviorPattern ontology:enablesCarbonReductionStrategy ?enablesCarbonReductionStrategy . } " +
                        "}";

        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            JSONArray behaviorPatternsArray = new JSONArray();

            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                JSONObject behaviorPatternObject = new JSONObject();

                // Extract the BehaviorPattern name
                String behaviorPatternUrl = solution.getResource("behaviorPattern").toString();
                String behaviorPatternName = behaviorPatternUrl.split("#")[1];

                // Extract hasReductionPotential and hasUsagePattern
                String hasReductionPotential = solution.get("hasReductionPotential").toString().replaceAll("\\^\\^.*", "");
                String hasUsagePattern = solution.get("hasUsagePattern").toString();

                // Extract enablesCarbonReductionStrategy if it exists
                String enablesCarbonReductionStrategy = solution.contains("enablesCarbonReductionStrategy")
                        ? solution.getResource("enablesCarbonReductionStrategy").toString().split("#")[1]
                        : "N/A";

                // Populate JSON object for this BehaviorPattern
                behaviorPatternObject.put("behaviorPatternName", behaviorPatternName);
                behaviorPatternObject.put("hasReductionPotential", hasReductionPotential);
                behaviorPatternObject.put("hasUsagePattern", hasUsagePattern);
                behaviorPatternObject.put("enablesCarbonReductionStrategy", enablesCarbonReductionStrategy);

                behaviorPatternsArray.put(behaviorPatternObject);
            }

            JSONObject resultJson = new JSONObject();
            resultJson.put("behaviorPatterns", behaviorPatternsArray);
            return resultJson.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error querying behavior patterns: " + e.getMessage());
        }
    }

    private void saveRDF() {
        try (FileOutputStream out = new FileOutputStream(RDF_FILE_PATH)) {
            model.write(out, "RDF/XML");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}