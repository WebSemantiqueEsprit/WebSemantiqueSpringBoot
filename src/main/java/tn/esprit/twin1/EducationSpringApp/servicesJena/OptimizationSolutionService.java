package tn.esprit.twin1.EducationSpringApp.servicesJena;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDF;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class OptimizationSolutionService {

    private static final String RDF_FILE_PATH = "src/main/java/Ontology-WebSemantic.rdf";
    private static final String CLASS_TYPE = "http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#OptimizationSolution";

    private Model model;

    public Model loadRDF() {
        model = ModelFactory.createDefaultModel();
        InputStream in = FileManager.get().open(RDF_FILE_PATH);
        if (in == null) {
            throw new IllegalArgumentException("File not found: " + RDF_FILE_PATH);
        }
        model.read(in, null);
        return model;
    }

    public String queryOptimizationSolutions() {
        loadRDF();

        String queryString =
                "PREFIX untitled-ontology-4: <http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#> " +
                        "SELECT ?optimizationSolution ?property ?value " +
                        "WHERE { " +
                        "  ?optimizationSolution a untitled-ontology-4:OptimizationSolution . " +
                        "  ?optimizationSolution ?property ?value . " +
                        "}";

        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            Map<String, JSONObject> solutionsMap = new HashMap<>();

            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                String solutionUrl = solution.getResource("optimizationSolution").toString();
                String solutionName = solutionUrl.split("#")[1];
                String property = solution.get("property").toString().split("#")[1];
                String value = solution.get("value").toString();

                if (property.equals("hasImplementationCost") || property.equals("hasCostSavings")) {
                    value = value.replaceAll("\\^\\^.*", "");
                    try {
                        // Only parse as Float if it's a numeric value
                        solutionsMap.putIfAbsent(solutionName, new JSONObject());
                        solutionsMap.get(solutionName).put(property, Float.parseFloat(value));
                    } catch (NumberFormatException e) {
                        // If parsing fails, store value as a string
                        solutionsMap.get(solutionName).put(property, value);
                    }
                } else {
                    // For other properties, store the value as a string
                    solutionsMap.putIfAbsent(solutionName, new JSONObject());
                    solutionsMap.get(solutionName).put(property, value);
                }
            }


            JSONObject resultJson = new JSONObject();
            JSONArray solutionsArray = new JSONArray();

            for (Map.Entry<String, JSONObject> entry : solutionsMap.entrySet()) {
                JSONObject solutionObject = new JSONObject();
                solutionObject.put("solutionName", entry.getKey());
                solutionObject.put("properties", entry.getValue());
                solutionsArray.put(solutionObject);
            }

            resultJson.put("optimizationSolutions", solutionsArray);
            return resultJson.toString();
        }
    }

    public void addOptimizationSolution(String solutionName, float implementationCost, float costSavings) {
        if (model == null) {
            loadRDF();
        }

        Resource optimizationSolutionResource = model.createResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#"+ solutionName);
        optimizationSolutionResource.addProperty(RDF.type, model.getResource(CLASS_TYPE));
        optimizationSolutionResource.addProperty(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasImplementationCost"), model.createTypedLiteral(implementationCost));
        optimizationSolutionResource.addProperty(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasCostSavings"), model.createTypedLiteral(costSavings));

        saveRDF();
    }

    public void updateOptimizationSolution(String solutionName, double newImplementationCost, double newCostSavings) {
        if (model == null) {
            loadRDF();
        }

        Resource optimizationSolutionResource = model.getResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + solutionName);
        if (optimizationSolutionResource != null) {
            Property implementationCostProperty = model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasImplementationCost");
            Property costSavingsProperty = model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasCostSavings");

            // Debugging output
            System.out.println("Current properties:");
            optimizationSolutionResource.listProperties().forEachRemaining(stmt -> {
                System.out.println(stmt.getPredicate() + " : " + stmt.getObject());
            });

            // Remove existing properties if they exist
            optimizationSolutionResource.removeAll(implementationCostProperty);
            optimizationSolutionResource.removeAll(costSavingsProperty);

            // Add new properties
            optimizationSolutionResource.addProperty(implementationCostProperty, model.createTypedLiteral(newImplementationCost));
            optimizationSolutionResource.addProperty(costSavingsProperty, model.createTypedLiteral(newCostSavings));

            // Save the model
            saveRDF();

            System.out.println("Updated Implementation Cost to: " + newImplementationCost);
            System.out.println("Updated Cost Savings to: " + newCostSavings);
        } else {
            System.out.println("OptimizationSolution not found: " + solutionName);
        }
    }



    public void deleteOptimizationSolution(String solutionName) {
        if (model == null) {
            loadRDF();
        }

        Resource optimizationSolutionResource = model.getResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + solutionName);
        if (optimizationSolutionResource != null) {
            optimizationSolutionResource.removeProperties();
            saveRDF();
        } else {
            System.out.println("OptimizationSolution not found: " + solutionName);
        }
    }

    private void saveRDF() {
        try (FileOutputStream out = new FileOutputStream(RDF_FILE_PATH)) {
            model.write(out, "RDF/XML");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String searchOptimizationSolutionsByCost(float minCost, float maxCost) {
        loadRDF();

        String queryString =
                "PREFIX untitled-ontology-4: <http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#> " +
                        "SELECT ?optimizationSolution ?implementationCost ?costSavings " +
                        "WHERE { " +
                        "  ?optimizationSolution a untitled-ontology-4:OptimizationSolution . " +
                        "  ?optimizationSolution untitled-ontology-4:hasImplementationCost ?implementationCost . " +
                        "  ?optimizationSolution untitled-ontology-4:hasCostSavings ?costSavings . " +
                        "  FILTER(?implementationCost >= " + minCost + " && ?implementationCost <= " + maxCost + ") " +
                        "}";

        return executeQuery(queryString);
    }

    public String searchOptimizationSolutionsByCostSavings(float minSavings, float maxSavings) {
        loadRDF();

        String queryString =
                "PREFIX untitled-ontology-4: <http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#> " +
                        "SELECT ?optimizationSolution ?implementationCost ?costSavings " +
                        "WHERE { " +
                        "  ?optimizationSolution a untitled-ontology-4:OptimizationSolution . " +
                        "  ?optimizationSolution untitled-ontology-4:hasImplementationCost ?implementationCost . " +
                        "  ?optimizationSolution untitled-ontology-4:hasCostSavings ?costSavings . " +
                        "  FILTER(?costSavings >= " + minSavings + " && ?costSavings <= " + maxSavings + ") " +
                        "}";

        return executeQuery(queryString);
    }

    private String executeQuery(String queryString) {
        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            JSONArray solutionsArray = new JSONArray();

            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                String solutionUrl = solution.getResource("optimizationSolution").toString();
                String solutionName = solutionUrl.split("#")[1];
                String costSavings = solution.get("costSavings").toString().replaceAll("\\^\\^.*", "");
                String implementationCost = solution.get("implementationCost").toString().replaceAll("\\^\\^.*", "");

                JSONObject solutionObject = new JSONObject();
                solutionObject.put("solutionName", solutionName);
                solutionObject.put("implementationCost", Float.parseFloat(implementationCost));
                solutionObject.put("costSavings", Float.parseFloat(costSavings));

                solutionsArray.put(solutionObject);
            }

            JSONObject resultJson = new JSONObject();
            resultJson.put("optimizationSolutions", solutionsArray);
            return resultJson.toString();
        }
    }
}
