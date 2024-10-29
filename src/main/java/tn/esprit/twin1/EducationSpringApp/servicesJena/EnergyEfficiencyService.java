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
@Component
public class EnergyEfficiencyService {

    private static final String RDF_FILE_PATH = "/Users/macbookpro/Desktop/websementique/projet_Chouaib/Ontology-WebSemantic.rdf";
    private Model model;

    // Load the RDF file
    public Model loadRDF() {
        model = ModelFactory.createDefaultModel();
        InputStream in = FileManager.get().open(RDF_FILE_PATH);
        if (in == null) {
            throw new IllegalArgumentException("File not found: " + RDF_FILE_PATH);
        }
        model.read(in, null);
        return model;
    }

    // Add a new EnergyEfficiency entry
    public void addEnergyEfficiency(String efficiencyName, String efficiencyRating, float savingsPotential) {
        if (model == null) {
            loadRDF();
        }
        Resource efficiencyResource = model.createResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + efficiencyName);
        efficiencyResource.addProperty(RDF.type, model.getResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#EnergyEfficiency"));
        efficiencyResource.addProperty(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasEfficiencyRating"), efficiencyRating);
        efficiencyResource.addProperty(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasSavingsPotential"), model.createTypedLiteral(savingsPotential));

        saveRDF();
    }

    // Update an existing EnergyEfficiency entry
    public void updateEnergyEfficiency(String efficiencyName, String newRating, float newSavingsPotential) {
        if (model == null) {
            loadRDF();
        }

        Resource efficiencyResource = model.getResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + efficiencyName);
        if (efficiencyResource != null) {
            efficiencyResource.removeAll(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasEfficiencyRating"))
                    .addProperty(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasEfficiencyRating"), newRating);
            efficiencyResource.removeAll(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasSavingsPotential"))
                    .addProperty(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasSavingsPotential"), model.createTypedLiteral(newSavingsPotential));

            saveRDF();
        }
    }

    // Delete an EnergyEfficiency entry
    public void deleteEnergyEfficiency(String efficiencyName) {
        if (model == null) {
            loadRDF();
        }

        Resource efficiencyResource = model.getResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + efficiencyName);
        if (efficiencyResource != null) {
            model.removeAll(efficiencyResource, null, null);
            model.removeAll(null, null, efficiencyResource);

            saveRDF();
        }
    }

    // Query all EnergyEfficiency entries
    public String queryEnergyEfficiencies(String rating, Float minSavingsPotential, Float maxSavingsPotential) {
        if (model == null) {
            loadRDF();
        }

        // Build SPARQL query with optional filters
        StringBuilder queryString = new StringBuilder(
                "PREFIX ontology: <http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#> " +
                        "SELECT ?efficiency ?rating ?savingsPotential " +
                        "WHERE { " +
                        "  ?efficiency a ontology:EnergyEfficiency . " +
                        "  ?efficiency ontology:hasEfficiencyRating ?rating . " +
                        "  ?efficiency ontology:hasSavingsPotential ?savingsPotential ."
        );

        if (rating != null) {
            queryString.append(" FILTER (?rating = '").append(rating).append("') ");
        }
        if (minSavingsPotential != null) {
            queryString.append(" FILTER (?savingsPotential >= ").append(minSavingsPotential).append(") ");
        }
        if (maxSavingsPotential != null) {
            queryString.append(" FILTER (?savingsPotential <= ").append(maxSavingsPotential).append(") ");
        }

        queryString.append("}");

        Query query = QueryFactory.create(queryString.toString());
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            JSONArray efficienciesArray = new JSONArray();

            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                JSONObject efficiencyObject = new JSONObject();

                String efficiencyUrl = solution.getResource("efficiency").toString();
                String efficiencyName = efficiencyUrl.split("#")[1];

                String efficiencyRating = solution.get("rating").toString();
                float efficiencySavingsPotential = solution.getLiteral("savingsPotential").getFloat();

                efficiencyObject.put("efficiencyName", efficiencyName);
                efficiencyObject.put("rating", efficiencyRating);
                efficiencyObject.put("savingsPotential", efficiencySavingsPotential);

                efficienciesArray.put(efficiencyObject);
            }

            JSONObject resultJson = new JSONObject();
            resultJson.put("energyEfficiencies", efficienciesArray);
            return resultJson.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error querying energy efficiencies: " + e.getMessage());
        }
    }


    // Search EnergyEfficiency by name
    public String searchByEfficiencyName(String efficiencyName) {
        if (model == null) {
            loadRDF();
        }
        String queryString =
                "PREFIX ontology: <http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#> " +
                        "SELECT ?rating ?savingsPotential " +
                        "WHERE { " +
                        "  ontology:" + efficiencyName + " a ontology:EnergyEfficiency . " +
                        "  ontology:" + efficiencyName + " ontology:hasEfficiencyRating ?rating . " +
                        "  ontology:" + efficiencyName + " ontology:hasSavingsPotential ?savingsPotential . " +
                        "}";

        return executeQuery(queryString);
    }

    // Filter EnergyEfficiencies by rating and minimum savings potential
    public String filterByRatingAndSavings(String rating, float minSavingsPotential) {
        if (model == null) {
            loadRDF();
        }
        String queryString =
                "PREFIX ontology: <http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#> " +
                        "SELECT ?efficiency ?savingsPotential " +
                        "WHERE { " +
                        "  ?efficiency a ontology:EnergyEfficiency . " +
                        "  ?efficiency ontology:hasEfficiencyRating \"" + rating + "\" . " +
                        "  ?efficiency ontology:hasSavingsPotential ?savingsPotential . " +
                        "  FILTER(?savingsPotential >= " + minSavingsPotential + ") " +
                        "}";

        return executeQuery(queryString);
    }

    // Execute the SPARQL query and return results as JSON
    private String executeQuery(String queryString) {
        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            JSONArray efficienciesArray = new JSONArray();

            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                JSONObject efficiencyObject = new JSONObject();

                if (solution.contains("efficiency")) {
                    String efficiencyUrl = solution.getResource("efficiency").toString();
                    String efficiencyName = efficiencyUrl.split("#")[1];
                    efficiencyObject.put("efficiencyName", efficiencyName);
                }

                if (solution.contains("rating")) {
                    efficiencyObject.put("rating", solution.get("rating").toString());
                }

                if (solution.contains("savingsPotential")) {
                    efficiencyObject.put("savingsPotential", solution.getLiteral("savingsPotential").getFloat());
                }

                efficienciesArray.put(efficiencyObject);
            }

            JSONObject resultJson = new JSONObject();
            resultJson.put("energyEfficiencies", efficienciesArray);
            return resultJson.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error querying energy efficiencies: " + e.getMessage());
        }
    }

    // Save the RDF model
    private void saveRDF() {
        try (FileOutputStream out = new FileOutputStream(RDF_FILE_PATH)) {
            model.write(out, "RDF/XML");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}