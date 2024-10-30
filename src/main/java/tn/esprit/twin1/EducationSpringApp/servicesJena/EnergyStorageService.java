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
public class EnergyStorageService {

    private static final String RDF_FILE_PATH = "src/main/java/Ontology-WebSemantic.rdf";
    private static final String CLASS_TYPE = "http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#EnergyStorage";

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

    public String queryEnergyStorages() {
        loadRDF();

        String queryString =
                "PREFIX untitled-ontology-4: <http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#> " +
                        "SELECT ?energyStorage ?property ?value " +
                        "WHERE { " +
                        "  ?energyStorage a untitled-ontology-4:EnergyStorage . " +
                        "  ?energyStorage ?property ?value . " +
                        "}";

        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            Map<String, JSONObject> storagesMap = new HashMap<>();

            while (results.hasNext()) {
                QuerySolution storage = results.nextSolution();
                String storageUrl = storage.getResource("energyStorage").toString();
                String storageName = storageUrl.split("#")[1];
                String property = storage.get("property").toString().split("#")[1];
                String value = storage.get("value").toString();

                if (property.equals("hasCapacity") || property.equals("hasEfficiency")) {
                    value = value.replaceAll("\\^\\^.*", "");
                    try {
                        // Only parse as Float if it's a numeric value
                        storagesMap.putIfAbsent(storageName, new JSONObject());
                        storagesMap.get(storageName).put(property, Float.parseFloat(value));
                    } catch (NumberFormatException e) {
                        // If parsing fails, store value as a string
                        storagesMap.get(storageName).put(property, value);
                    }
                } else {
                    // For other properties, store the value as a string
                    storagesMap.putIfAbsent(storageName, new JSONObject());
                    storagesMap.get(storageName).put(property, value);
                }
            }


            JSONObject resultJson = new JSONObject();
            JSONArray storagesArray = new JSONArray();

            for (Map.Entry<String, JSONObject> entry : storagesMap.entrySet()) {
                JSONObject storageObject = new JSONObject();
                storageObject.put("storageName", entry.getKey());
                storageObject.put("properties", entry.getValue());
                storagesArray.put(storageObject);
            }

            resultJson.put("energyStorages", storagesArray);
            return resultJson.toString();
        }
    }

    public void addEnergyStorage(String storageName, float capacity, float efficiency) {
        if (model == null) {
            loadRDF();
        }

        Resource energyStorageResource = model.createResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#"+ storageName);
        energyStorageResource.addProperty(RDF.type, model.getResource(CLASS_TYPE));
        energyStorageResource.addProperty(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasCapacity"), model.createTypedLiteral(capacity));
        energyStorageResource.addProperty(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasEfficiency"), model.createTypedLiteral(efficiency));

        saveRDF();
    }

    public void updateEnergyStorage(String storageName, double newCapacity, double newEfficiency) {
        if (model == null) {
            loadRDF();
        }

        Resource energyStorageResource = model.getResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + storageName);
        if (energyStorageResource != null) {
            Property capacityProperty = model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasCapacity");
            Property efficiencyProperty = model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasEfficiency");

            // Debugging output
            System.out.println("Current properties:");
            energyStorageResource.listProperties().forEachRemaining(stmt -> {
                System.out.println(stmt.getPredicate() + " : " + stmt.getObject());
            });

            // Remove existing properties if they exist
            energyStorageResource.removeAll(capacityProperty);
            energyStorageResource.removeAll(efficiencyProperty);

            // Add new properties
            energyStorageResource.addProperty(capacityProperty, model.createTypedLiteral(newCapacity));
            energyStorageResource.addProperty(efficiencyProperty, model.createTypedLiteral(newEfficiency));

            // Save the model
            saveRDF();

            System.out.println("Updated Implementation Cost to: " + newCapacity);
            System.out.println("Updated Cost Savings to: " + newEfficiency);
        } else {
            System.out.println("EnergyStorage not found: " + storageName);
        }
    }



    public void deleteEnergyStorage(String storageName) {
        if (model == null) {
            loadRDF();
        }

        Resource energyStorageResource = model.getResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + storageName);
        if (energyStorageResource != null) {
            energyStorageResource.removeProperties();
            saveRDF();
        } else {
            System.out.println("EnergyStorage not found: " + storageName);
        }
    }

    private void saveRDF() {
        try (FileOutputStream out = new FileOutputStream(RDF_FILE_PATH)) {
            model.write(out, "RDF/XML");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String searchEnergyStoragesByCost(float minCost, float maxCost) {
        loadRDF();

        String queryString =
                "PREFIX untitled-ontology-4: <http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#> " +
                        "SELECT ?energyStorage ?capacity ?efficiency " +
                        "WHERE { " +
                        "  ?energyStorage a untitled-ontology-4:EnergyStorage . " +
                        "  ?energyStorage untitled-ontology-4:hasCapacity ?capacity . " +
                        "  ?energyStorage untitled-ontology-4:hasEfficiency ?efficiency . " +
                        "  FILTER(?capacity >= " + minCost + " && ?capacity <= " + maxCost + ") " +
                        "}";

        return executeQuery(queryString);
    }

    public String searchEnergyStoragesByEfficiency(float minSavings, float maxSavings) {
        loadRDF();

        String queryString =
                "PREFIX untitled-ontology-4: <http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#> " +
                        "SELECT ?energyStorage ?capacity ?efficiency " +
                        "WHERE { " +
                        "  ?energyStorage a untitled-ontology-4:EnergyStorage . " +
                        "  ?energyStorage untitled-ontology-4:hasCapacity ?capacity . " +
                        "  ?energyStorage untitled-ontology-4:hasEfficiency ?efficiency . " +
                        "  FILTER(?efficiency >= " + minSavings + " && ?efficiency <= " + maxSavings + ") " +
                        "}";

        return executeQuery(queryString);
    }

    private String executeQuery(String queryString) {
        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            JSONArray storagesArray = new JSONArray();

            while (results.hasNext()) {
                QuerySolution storage = results.nextSolution();
                String storageUrl = storage.getResource("energyStorage").toString();
                String storageName = storageUrl.split("#")[1];
                String efficiency = storage.get("efficiency").toString().replaceAll("\\^\\^.*", "");
                String capacity = storage.get("capacity").toString().replaceAll("\\^\\^.*", "");

                JSONObject storageObject = new JSONObject();
                storageObject.put("storageName", storageName);
                storageObject.put("capacity", Float.parseFloat(capacity));
                storageObject.put("efficiency", Float.parseFloat(efficiency));

                storagesArray.put(storageObject);
            }

            JSONObject resultJson = new JSONObject();
            resultJson.put("energyStorages", storagesArray);
            return resultJson.toString();
        }
    }
}
