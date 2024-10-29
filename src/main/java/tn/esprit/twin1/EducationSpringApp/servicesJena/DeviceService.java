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
import java.util.Optional;

@Component
public class DeviceService {

    private static final String RDF_FILE_PATH = "C:/Ontology-WebSemantic.rdf";
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

    public void addDevice(String deviceName, String powerRating, String usageFrequency) {
        ensureModelLoaded();

        Resource deviceResource = model.createResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + deviceName.replace(" ", "_"));
        deviceResource.addProperty(RDF.type, model.getResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#Device"));
        deviceResource.addProperty(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasPowerRating"), powerRating);
        deviceResource.addProperty(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasUsageFrequency"), usageFrequency);

        saveRDF();
    }

    public void updateDevice(String deviceName, String newPowerRating, String newUsageFrequency) {
        ensureModelLoaded();

        Resource deviceResource = model.getResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + deviceName.replace(" ", "_"));
        if (deviceResource != null) {
            deviceResource.removeAll(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasPowerRating"));
            deviceResource.removeAll(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasUsageFrequency"));
            deviceResource.addProperty(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasPowerRating"), newPowerRating);
            deviceResource.addProperty(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasUsageFrequency"), newUsageFrequency);
            saveRDF();
        }
    }

    public void deleteDevice(String deviceName) {
        ensureModelLoaded();

        Resource deviceResource = model.getResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + deviceName.replace(" ", "_"));
        if (deviceResource != null) {
            model.removeAll(deviceResource, null, null);
            model.removeAll(null, null, deviceResource);
            saveRDF();
        }
    }

    private void saveRDF() {
        try (FileOutputStream out = new FileOutputStream(RDF_FILE_PATH)) {
            model.write(out, "RDF/XML");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error saving RDF file: " + e.getMessage());
        }
    }

    public String queryDevices() {
        ensureModelLoaded();
        String queryString =
            "PREFIX ontology: <http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#> " +
            "SELECT ?device ?powerRating ?usageFrequency " +
            "WHERE { " +
            "  ?device a ontology:Device . " +
            "  ?device ontology:hasPowerRating ?powerRating . " +
            "  ?device ontology:hasUsageFrequency ?usageFrequency . " +
            "}";

        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            JSONArray devicesArray = new JSONArray();

            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                JSONObject deviceObject = new JSONObject();
                String deviceUrl = solution.getResource("device").toString();
                String deviceName = deviceUrl.split("#")[1];
                String powerRating = solution.get("powerRating").toString();
                String usageFrequency = solution.get("usageFrequency").toString();

                deviceObject.put("deviceName", deviceName);
                deviceObject.put("powerRating", powerRating);
                deviceObject.put("usageFrequency", usageFrequency);
                
                devicesArray.put(deviceObject);
            }

            JSONObject resultJson = new JSONObject();
            resultJson.put("devices", devicesArray);
            return resultJson.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error querying devices: " + e.getMessage());
        }
    }

    public Optional<String> getDeviceByPowerRating(String powerRating) {
        ensureModelLoaded();
    
        String queryString =
            "PREFIX ontology: <http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#> " +
            "SELECT ?device ?usageFrequency " +
            "WHERE { " +
            "  ?device a ontology:Device . " +
            "  ?device ontology:hasPowerRating ?devicePowerRating . " +
            "  ?device ontology:hasUsageFrequency ?usageFrequency . " +
            "  FILTER(str(?devicePowerRating) = \"" + powerRating + "\") " +
            "}";

        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            JSONArray devicesArray = new JSONArray();

            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                JSONObject deviceObject = new JSONObject();
                String deviceUri = solution.getResource("device").toString();
                String usageFrequency = solution.getLiteral("usageFrequency").getString();

                deviceObject.put("uri", deviceUri);
                deviceObject.put("usageFrequency", usageFrequency);
                deviceObject.put("powerRating", powerRating);
                
                devicesArray.put(deviceObject);
            }

            if (devicesArray.isEmpty()) {
                return Optional.empty();
            }

            JSONObject resultJson = new JSONObject();
            resultJson.put("devices", devicesArray);
            return Optional.of(resultJson.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error querying devices by power rating: " + e.getMessage());
        }
    }

    private void ensureModelLoaded() {
        if (model == null) {
            loadRDF();
        }
    }
    public Optional<String> getDevicesByPowerRatingRange(String minPowerRating, String maxPowerRating) {
        ensureModelLoaded();
    
        String queryString =
            "PREFIX ontology: <http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#> " +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +  // Ajout du préfixe xsd
            "SELECT ?device ?devicePowerRating ?usageFrequency " +
            "WHERE { " +
            "  ?device a ontology:Device . " +
            "  ?device ontology:hasPowerRating ?devicePowerRating . " +
            "  ?device ontology:hasUsageFrequency ?usageFrequency . " +
            "  FILTER(xsd:float(?devicePowerRating) >= xsd:float(\"" + minPowerRating + "\") && " +
            "         xsd:float(?devicePowerRating) <= xsd:float(\"" + maxPowerRating + "\")) " +
            "}";
    
        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            JSONArray devicesArray = new JSONArray();
    
            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                JSONObject deviceObject = new JSONObject();
                String deviceUri = solution.getResource("device").toString();
                String powerRating = solution.getLiteral("devicePowerRating").getString();
                String usageFrequency = solution.getLiteral("usageFrequency").getString();
    
                deviceObject.put("uri", deviceUri);
                deviceObject.put("powerRating", powerRating);
                deviceObject.put("usageFrequency", usageFrequency);
    
                devicesArray.put(deviceObject);
            }
    
            if (devicesArray.isEmpty()) {
                return Optional.empty();
            }
    
            JSONObject resultJson = new JSONObject();
            resultJson.put("devices", devicesArray);
            return Optional.of(resultJson.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error querying devices by power rating range: " + e.getMessage());
        }
    }
      
}
