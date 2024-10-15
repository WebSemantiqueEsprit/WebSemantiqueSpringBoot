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
import java.util.HashMap;
import java.util.Map;

@Component
public class CarbonFootprintService {

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

    // Method to add a new carbon footprint
    public void addCarbonFootprint(String footprintName, String type, double carbonValue) {
        if (model == null) {
            loadRDF();
        }

        // Create a new individual for the carbon footprint
        Resource footprintResource = model.createResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + footprintName);
        footprintResource.addProperty(RDF.type, model.getResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#CarbonFootprint"));
        footprintResource.addProperty(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasCarbonValue"), model.createTypedLiteral(carbonValue));
        footprintResource.addProperty(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasType"), type);

        // Save the updated model back to the RDF file
        saveRDF();
    }

    // Method to update an existing carbon footprint
    public void updateCarbonFootprint(String footprintName, String newType, double newCarbonValue) {
        if (model == null) {
            loadRDF();
        }

        // Find the existing carbon footprint resource by name
        Resource footprintResource = model.getResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + footprintName);

        if (footprintResource != null) {
            // Update the properties
            footprintResource.removeAll(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasType"));
            footprintResource.addProperty(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasType"), newType);

            footprintResource.removeAll(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasCarbonValue"));
            footprintResource.addProperty(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasCarbonValue"), model.createTypedLiteral(newCarbonValue));

            // Save changes
            saveRDF();
        }
    }

    // Method to delete a carbon footprint
    public void deleteCarbonFootprint(String footprintName) {
        if (model == null) {
            loadRDF();
        }

        // Find the existing carbon footprint resource by name
        Resource footprintResource = model.getResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + footprintName);

        if (footprintResource != null) {
            // Remove the resource from the model
            model.removeAll(footprintResource, null, null);
            model.removeAll(null, null, footprintResource);

            // Save changes
            saveRDF();
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

    // Method to query carbon footprints
    public String queryCarbonFootprints() {
        loadRDF();
        System.out.println("Model size: " + model.size());
    
        String queryString =
            "PREFIX ontology: <http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#> " +
            "SELECT ?carbonFootprint ?hasCarbonValue ?hasType " +
            "WHERE { " +
            "  ?carbonFootprint a ontology:CarbonFootprint . " +
            "  ?carbonFootprint ontology:hasCarbonValue ?hasCarbonValue . " +
            "  ?carbonFootprint ontology:hasType ?hasType . " +
            "}";
    
        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            JSONArray carbonFootprintsArray = new JSONArray();
    
            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                JSONObject carbonFootprintObject = new JSONObject();
    
                // Extract the footprint name
                String carbonFootprintUrl = solution.getResource("carbonFootprint").toString();
                String carbonFootprintName = carbonFootprintUrl.split("#")[1];
    
                // Extract hasCarbonValue and hasType
                String hasCarbonValue = solution.get("hasCarbonValue").toString().replaceAll("\\^\\^.*", "");
                String hasType = solution.get("hasType").toString();
    
                // Create a JSON object for this footprint
                carbonFootprintObject.put("footprintName", carbonFootprintName);
                carbonFootprintObject.put("hasCarbonValue", hasCarbonValue);
                carbonFootprintObject.put("hasType", hasType);
                
                carbonFootprintsArray.put(carbonFootprintObject);
            }
    
            JSONObject resultJson = new JSONObject();
            resultJson.put("carbonFootprints", carbonFootprintsArray);
            return resultJson.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error querying carbon footprints: " + e.getMessage());
        }
    }
    }
