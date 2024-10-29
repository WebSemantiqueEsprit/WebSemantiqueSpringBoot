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
public class EnergySourceService {

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

    public void addEnergySource(String energyType, String renewablePercentage) {
        ensureModelLoaded();

        Resource energyResource = model.createResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + energyType.replace(" ", "_"));
        energyResource.addProperty(RDF.type, model.getResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#EnergySource"));
        energyResource.addProperty(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasRenewablePercentage"), renewablePercentage);

        saveRDF();
    }

    public void updateEnergySource(String energyType, String newRenewablePercentage) {
        ensureModelLoaded();

        Resource energyResource = model.getResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + energyType.replace(" ", "_"));
        if (energyResource != null) {
            energyResource.removeAll(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasRenewablePercentage"));
            energyResource.addProperty(model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasRenewablePercentage"), newRenewablePercentage);
            saveRDF();
        }
    }

    public void deleteEnergySource(String energyType) {
        ensureModelLoaded();

        Resource energyResource = model.getResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + energyType.replace(" ", "_"));
        if (energyResource != null) {
            model.removeAll(energyResource, null, null);
            model.removeAll(null, null, energyResource);
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

    public String queryEnergySources() {
        ensureModelLoaded();
        String queryString =
            "PREFIX ontology: <http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#> " +
            "SELECT ?energySource ?renewablePercentage " +
            "WHERE { " +
            "  ?energySource a ontology:EnergySource . " +
            "  ?energySource ontology:hasRenewablePercentage ?renewablePercentage . " +
            "}";

        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            JSONArray energyArray = new JSONArray();

            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                JSONObject energyObject = new JSONObject();
                String energyUrl = solution.getResource("energySource").toString();
                String energyName = energyUrl.split("#")[1];
                String renewablePercentage = solution.get("renewablePercentage").toString();

                energyObject.put("energyType", energyName);
                energyObject.put("renewablePercentage", renewablePercentage);
                
                energyArray.put(energyObject);
            }

            JSONObject resultJson = new JSONObject();
            resultJson.put("energySources", energyArray);
            return resultJson.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error querying energy sources: " + e.getMessage());
        }
    }
    public Optional<String> getEnergySourceByRenewablePercentage(String renewablePercentage) {
        ensureModelLoaded();
    
        String queryString =
            "PREFIX ontology: <http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#> " +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +  // Ajoutez cette ligne pour définir xsd
            "SELECT ?energySource ?renewablePercentage " +
            "WHERE { " +
            "  ?energySource a ontology:EnergySource . " +
            "  ?energySource ontology:hasRenewablePercentage ?renewablePercentage . " +
            "  FILTER(xsd:float(?renewablePercentage) = xsd:float(\"" + renewablePercentage + "\")) " +
            "}";
    
        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            JSONArray energyArray = new JSONArray();
    
            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                JSONObject energyObject = new JSONObject();
                String energyUri = solution.getResource("energySource").toString();
                String renewableValue = solution.getLiteral("renewablePercentage").getString();
    
                energyObject.put("uri", energyUri);
                energyObject.put("renewablePercentage", renewableValue);
                energyArray.put(energyObject);
            }
    
            if (energyArray.isEmpty()) {
                return Optional.empty();
            }
    
            JSONObject resultJson = new JSONObject();
            resultJson.put("energySources", energyArray);
            return Optional.of(resultJson.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error querying energy sources by renewable percentage: " + e.getMessage());
        }
    }
    
    public Optional<String> getEnergySourcesByRenewablePercentageRange(String minPercentage, String maxPercentage) {
        ensureModelLoaded();

        String queryString =
            "PREFIX ontology: <http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#> " +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
            "SELECT ?energySource ?renewablePercentage " +
            "WHERE { " +
            "  ?energySource a ontology:EnergySource . " +
            "  ?energySource ontology:hasRenewablePercentage ?renewablePercentage . " +
            "  FILTER(xsd:float(?renewablePercentage) >= xsd:float(\"" + minPercentage + "\") && " +
            "         xsd:float(?renewablePercentage) <= xsd:float(\"" + maxPercentage + "\")) " +
            "}";

        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            JSONArray energyArray = new JSONArray();

            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                JSONObject energyObject = new JSONObject();
                String energyUri = solution.getResource("energySource").toString();
                String renewablePercentage = solution.getLiteral("renewablePercentage").getString();

                energyObject.put("uri", energyUri);
                energyObject.put("renewablePercentage", renewablePercentage);

                energyArray.put(energyObject);
            }

            if (energyArray.isEmpty()) {
                return Optional.empty();
            }

            JSONObject resultJson = new JSONObject();
            resultJson.put("energySources", energyArray);
            return Optional.of(resultJson.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error querying energy sources by renewable percentage range: " + e.getMessage());
        }
    }

    private void ensureModelLoaded() {
        if (model == null) {
            loadRDF();
        }
    }
}
