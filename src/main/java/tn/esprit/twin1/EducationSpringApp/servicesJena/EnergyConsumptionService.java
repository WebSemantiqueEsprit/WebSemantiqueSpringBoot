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
public class EnergyConsumptionService {

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

    // Method to query energy consumption data based on the EnergyConsumption class
    public Map<String, String> getEnergyConsumptionDetails(String energyConsumptionId) {
        loadRDF();
        Map<String, String> energyConsumptionDetails = new HashMap<>();

        // SPARQL query to get details of a specific energy consumption instance
        String queryString = "PREFIX ont: <http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#> "
                + "SELECT ?device ?energySource ?timeFrame ?value "
                + "WHERE { "
                + "   ont:" + energyConsumptionId + " ont:isAssociatedWithDevice ?device . "
                + "   ont:" + energyConsumptionId + " ont:usesEnergySource ?energySource . "
                + "   ont:" + energyConsumptionId + " ont:hasTimeFrame ?timeFrame . "
                + "   ont:" + energyConsumptionId + " ont:hasValue ?value . "
                + "}";

        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            if (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                energyConsumptionDetails.put("Device", soln.getResource("device").getURI());
                energyConsumptionDetails.put("EnergySource", soln.getResource("energySource").getURI());
                energyConsumptionDetails.put("TimeFrame", soln.getLiteral("timeFrame").getString());
                energyConsumptionDetails.put("Value", String.valueOf(soln.getLiteral("value").getFloat()));
            }
        }
        return energyConsumptionDetails;
    }

    private void saveRDF() {
        try (OutputStream out = new FileOutputStream(RDF_FILE_PATH)) {
            model.write(out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Method to create an EnergyConsumption instance
    public Resource createEnergyConsumption(String uri, float value, String timeFrame) {
        loadRDF();

        Resource energyConsumption = model.createResource(uri)
                .addProperty(RDF.type, model.createResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#EnergyConsumption"))
                .addLiteral(model.createProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasValue"), value)
                .addLiteral(model.createProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasTimeFrame"), timeFrame);

        /*if (providerUri != null) {
            energyConsumption.addProperty(model.createProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#isSuppliedBy"),
                    model.createResource(providerUri));
        }
        if (deviceUri != null) {
            energyConsumption.addProperty(model.createProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#isAssociatedWithDevice"),
                    model.createResource(deviceUri));
        }*/

        saveRDF();
        return energyConsumption;
    }

    // Method to update an existing EnergyConsumption instance
    public Resource updateEnergyConsumption(String uri, Float newValue, String newTimeFrame) {
        loadRDF();
        Resource energyConsumption = model.getResource(uri);
        if (energyConsumption == null) {
            throw new IllegalArgumentException("EnergyConsumption with URI " + uri + " does not exist.");
        }

        if (newValue != null) {
            energyConsumption.removeAll(model.createProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasValue"))
                    .addLiteral(model.createProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasValue"), newValue);
        }
        if (newTimeFrame != null) {
            energyConsumption.removeAll(model.createProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasTimeFrame"))
                    .addLiteral(model.createProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasTimeFrame"), newTimeFrame);
        }

        saveRDF();
        return energyConsumption;
    }

    // Method to delete an EnergyConsumption instance
    public boolean deleteEnergyConsumption(String uri) {
        loadRDF();
        Resource energyConsumption = model.getResource(uri);
        if (energyConsumption == null) {
            return false;
        }

        model.removeAll(energyConsumption, null, null);
        model.removeAll(null, null, energyConsumption);

        saveRDF();
        return true;
    }

    // Method to retrieve an EnergyConsumption by URI
    public Resource getEnergyConsumption(String uri) {
        loadRDF();
        return model.getResource(uri);
    }

    // EnergyConsumptionService.java
    // EnergyConsumptionService.java
    public List<Map<String, String>> getAllEnergyConsumptions() {
        loadRDF();
        List<Map<String, String>> energyConsumptions = new ArrayList<>();

        // Define the SPARQL query to retrieve all EnergyConsumption instances
        String queryString = "PREFIX ont: <http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#> "
                + "SELECT ?consumption ?timeFrame ?value "
                + "WHERE { "
                + "   ?consumption a ont:EnergyConsumption . "
                + "   ?consumption ont:hasTimeFrame ?timeFrame . "
                + "   ?consumption ont:hasValue ?value . "
                + "}";

        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();

            // Process each result in the ResultSet
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Map<String, String> details = new HashMap<>();

                // Extract URI, get the last part after the '#'
                String fullUri = soln.getResource("consumption").getURI();
                String uriName = fullUri.substring(fullUri.lastIndexOf("#") + 1);

                // Add URI name, time frame, and value to the map
                details.put("URI", uriName);
                details.put("TimeFrame", soln.getLiteral("timeFrame").getString());
                details.put("Value", String.valueOf(soln.getLiteral("value").getFloat()));

                energyConsumptions.add(details);
            }
        }
        return energyConsumptions;
    }



}
