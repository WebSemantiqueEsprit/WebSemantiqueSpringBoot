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
public class ProviderService {

    private static final String RDF_FILE_PATH = "C:/Users/muham/Downloads/Ontology-WebSemantic.rdf";
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

    // Add a new provider
    public void addProvider(String providerName, double greenEnergyPercentage) {
        if (model == null) {
            loadRDF();
        }

        Resource providerResource = model.createResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + providerName);
        providerResource.addProperty(RDF.type, model.getResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#Provider"));
        providerResource.addProperty(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasNameProvider"), providerName);
        providerResource.addProperty(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasGreenEnergyPercentage"), model.createTypedLiteral(greenEnergyPercentage));

        saveRDF();
    }

    // Update an existing provider
    public void updateProvider(String providerName, double newGreenEnergyPercentage) {
        if (model == null) {
            loadRDF();
        }

        Resource providerResource = model.getResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + providerName);
        if (providerResource != null) {
            // Update the green energy percentage property
            providerResource.removeAll(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasGreenEnergyPercentage"));
            providerResource.addProperty(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasGreenEnergyPercentage"), model.createTypedLiteral(newGreenEnergyPercentage));

            // Save changes to RDF
            saveRDF();
        }
    }


    // Delete a provider
    public void deleteProvider(String providerName) {
        if (model == null) {
            loadRDF();
        }

        Resource providerResource = model.getResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + providerName);
        if (providerResource != null) {
            model.removeAll(providerResource, null, null);
            model.removeAll(null, null, providerResource);

            saveRDF();
        }
    }

    // Query providers
    public String queryProviders() {
        loadRDF();

        String queryString =
                "PREFIX ontology: <http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#> " +
                        "SELECT ?provider ?hasGreenEnergyPercentage ?hasNameProvider " +
                        "WHERE { " +
                        "  ?provider a ontology:Provider . " +
                        "  ?provider ontology:hasGreenEnergyPercentage ?hasGreenEnergyPercentage . " +
                        "  ?provider ontology:hasNameProvider ?hasNameProvider . " +
                        "}";

        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            JSONArray providersArray = new JSONArray();

            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                JSONObject providerObject = new JSONObject();

                String providerUrl = solution.getResource("provider").toString();
                String providerName = providerUrl.split("#")[1];

                String hasGreenEnergyPercentage = solution.get("hasGreenEnergyPercentage").toString().replaceAll("\\^\\^.*", "");
                String hasNameProvider = solution.get("hasNameProvider").toString();

                providerObject.put("providerName", providerName);
                providerObject.put("hasGreenEnergyPercentage", hasGreenEnergyPercentage);
                providerObject.put("hasNameProvider", hasNameProvider);

                providersArray.put(providerObject);
            }

            JSONObject resultJson = new JSONObject();
            resultJson.put("providers", providersArray);
            return resultJson.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error querying providers: " + e.getMessage());
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
