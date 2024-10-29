package tn.esprit.twin1.EducationSpringApp.servicesJena;

import org.apache.jena.rdf.model.*;
import org.apache.jena.query.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Component
public class CarbonFootprintService {

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

    // Method to add a new carbon footprint
    public void addCarbonFootprint(String footprintName, String type, double carbonValue) {
        if (model == null) {
            loadRDF();
        }

        // Create a new individual for the carbon footprint
        Resource footprintResource = model.createResource(
                "http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + footprintName);
        footprintResource.addProperty(RDF.type, model
                .getResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#CarbonFootprint"));
        footprintResource.addProperty(
                model.getProperty(
                        "http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasCarbonValue"),
                model.createTypedLiteral(carbonValue));
        footprintResource.addProperty(
                model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasType"),
                type);

        // Save the updated model back to the RDF file
        saveRDF();
    }

    // Method to update an existing carbon footprint
    public void updateCarbonFootprint(String footprintName, String newType, double newCarbonValue) {
        if (model == null) {
            loadRDF();
        }

        // Find the existing carbon footprint resource by name
        Resource footprintResource = model
                .getResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + footprintName);

        if (footprintResource != null) {
            // Update the properties
            footprintResource.removeAll(model
                    .getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasType"));
            footprintResource.addProperty(
                    model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasType"),
                    newType);

            footprintResource.removeAll(model.getProperty(
                    "http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasCarbonValue"));
            footprintResource.addProperty(
                    model.getProperty(
                            "http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasCarbonValue"),
                    model.createTypedLiteral(newCarbonValue));

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
        Resource footprintResource = model
                .getResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + footprintName);

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

    // Method to query carbon footprints with dynamic object properties
    public String queryCarbonFootprints() {
        loadRDF();
        System.out.println("Model size: " + model.size());

        String queryString = "PREFIX ontology: <http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#> "
                + "SELECT ?carbonFootprint ?hasCarbonValue ?hasType ?property ?relatedInstance "
                + "WHERE { "
                + "  ?carbonFootprint a ontology:CarbonFootprint . "
                + "  ?carbonFootprint ontology:hasCarbonValue ?hasCarbonValue . "
                + "  ?carbonFootprint ontology:hasType ?hasType . "
                + "  OPTIONAL { ?carbonFootprint ?property ?relatedInstance . } "
                + "}";

        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            JSONArray carbonFootprintsArray = new JSONArray();

            Map<String, JSONObject> footprintsMap = new HashMap<>();

            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();

                // Extract the footprint name
                String carbonFootprintUrl = solution.getResource("carbonFootprint").toString();
                String carbonFootprintName = carbonFootprintUrl.split("#")[1];

                // Extract hasCarbonValue and hasType
                String hasCarbonValue = solution.get("hasCarbonValue").toString().replaceAll("\\^\\^.*", "");
                String hasType = solution.get("hasType").toString();

                // Create or get the JSON object for this footprint
                JSONObject carbonFootprintObject = footprintsMap.getOrDefault(carbonFootprintName, new JSONObject());
                if (!carbonFootprintObject.has("footprintName")) {
                    carbonFootprintObject.put("footprintName", carbonFootprintName);
                    carbonFootprintObject.put("hasCarbonValue", hasCarbonValue);
                    carbonFootprintObject.put("hasType", hasType);
                    carbonFootprintObject.put("relations", new JSONArray()); // Initialize relations as empty
                    footprintsMap.put(carbonFootprintName, carbonFootprintObject);
                }

                // Extract the dynamic relationship if present
                if (solution.contains("property") && solution.contains("relatedInstance")) {
                    RDFNode propertyNode = solution.get("property");
                    RDFNode relatedInstanceNode = solution.get("relatedInstance");

                    if (propertyNode.isResource() && relatedInstanceNode.isResource()) {
                        String property = propertyNode.asResource().getLocalName();
                        String relatedInstanceUrl = relatedInstanceNode.asResource().toString();
                        String relatedInstanceName = relatedInstanceUrl.split("#")[1];

                        JSONObject relationObject = new JSONObject();
                        relationObject.put("relation", property);
                        relationObject.put("relatedInstance", relatedInstanceName);

                        carbonFootprintObject.getJSONArray("relations").put(relationObject);
                    }
                }
            }

            // Add all footprints to the main array
            for (JSONObject footprintObject : footprintsMap.values()) {
                // Only add relations if they exist
                if (footprintObject.getJSONArray("relations").length() == 0) {
                    footprintObject.put("relations", new JSONArray()); // Ensure relations are initialized to empty if
                                                                       // not added
                }
                carbonFootprintsArray.put(footprintObject);
            }

            JSONObject resultJson = new JSONObject();
            resultJson.put("carbonFootprints", carbonFootprintsArray);
            return resultJson.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error querying carbon footprints: " + e.getMessage());
        }
    }
    // ***** Recherche et filtrage ******/

    // Recherche par nom ou type, incluant les relations
    public String searchCarbonFootprints(String value) {
        if (model == null) {
            loadRDF();
        }

        // Construire la requête SPARQL pour rechercher `value` dans les deux champs
        String queryString = "PREFIX ontology: <http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#> "
                + "SELECT ?carbonFootprint ?hasCarbonValue ?hasType ?property ?relatedInstance "
                + "WHERE { "
                + "  ?carbonFootprint a ontology:CarbonFootprint . "
                + "  ?carbonFootprint ontology:hasCarbonValue ?hasCarbonValue . "
                + "  ?carbonFootprint ontology:hasType ?hasType . "
                + "  OPTIONAL { "
                + "    ?carbonFootprint ?property ?relatedInstance . "
                + "  } "
                + "  FILTER (STRENDS(STR(?carbonFootprint), \"" + value + "\") || ?hasType = \"" + value + "\") "
                + "}";

        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            JSONArray carbonFootprintsArray = new JSONArray();
            Map<String, JSONObject> footprintsMap = new HashMap<>();

            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();

                // Extraire le nom de l'empreinte
                String carbonFootprintUrl = solution.getResource("carbonFootprint").toString();
                String carbonFootprintName = carbonFootprintUrl.split("#")[1];

                // Extraire hasCarbonValue et hasType
                String hasCarbonValue = solution.get("hasCarbonValue").toString().replaceAll("\\^\\^.*", "");
                String hasType = solution.get("hasType").toString();

                // Créer ou récupérer l'objet JSON pour cette empreinte
                JSONObject carbonFootprintObject = footprintsMap.getOrDefault(carbonFootprintName, new JSONObject());
                if (!carbonFootprintObject.has("footprintName")) {
                    carbonFootprintObject.put("footprintName", carbonFootprintName);
                    carbonFootprintObject.put("hasCarbonValue", hasCarbonValue);
                    carbonFootprintObject.put("hasType", hasType);
                    carbonFootprintObject.put("relations", new JSONArray()); // Initialiser les relations comme vides
                    footprintsMap.put(carbonFootprintName, carbonFootprintObject);
                }

                // Extraire la relation dynamique si elle est présente
                if (solution.contains("property") && solution.contains("relatedInstance")) {
                    RDFNode propertyNode = solution.get("property");
                    RDFNode relatedInstanceNode = solution.get("relatedInstance");

                    if (propertyNode.isResource() && relatedInstanceNode.isResource()) {
                        String property = propertyNode.asResource().getLocalName();
                        String relatedInstanceUrl = relatedInstanceNode.asResource().toString();
                        String relatedInstanceName = relatedInstanceUrl.split("#")[1];

                        JSONObject relationObject = new JSONObject();
                        relationObject.put("relation", property);
                        relationObject.put("relatedInstance", relatedInstanceName);

                        carbonFootprintObject.getJSONArray("relations").put(relationObject);
                    }
                }
            }

            // Ajouter toutes les empreintes à la matrice principale
            for (JSONObject footprintObject : footprintsMap.values()) {
                // Assurez-vous que les relations sont initialisées à vides
                if (footprintObject.getJSONArray("relations").length() == 0) {
                    footprintObject.put("relations", new JSONArray());
                }
                carbonFootprintsArray.put(footprintObject);
            }

            JSONObject resultJson = new JSONObject();
            resultJson.put("carbonFootprints", carbonFootprintsArray);
            return resultJson.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error searching carbon footprints: " + e.getMessage());
        }
    }

    // Méthode pour rechercher des empreintes carbone entre deux valeurs de carbone
    public String searchCarbonFootprintsByRange(double minValue, double maxValue) {
        if (model == null) {
            loadRDF();
        }

        // Construire la requête SPARQL pour filtrer les empreintes carbone par valeur
        // de carbone
        String queryString = "PREFIX ontology: <http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#> "
                + "SELECT ?carbonFootprint ?hasCarbonValue ?hasType ?property ?relatedInstance "
                + "WHERE { "
                + "  ?carbonFootprint a ontology:CarbonFootprint . "
                + "  ?carbonFootprint ontology:hasCarbonValue ?hasCarbonValue . "
                + "  ?carbonFootprint ontology:hasType ?hasType . "
                + "  OPTIONAL { ?carbonFootprint ?property ?relatedInstance . } "
                + "  FILTER (?hasCarbonValue >= " + minValue + " && ?hasCarbonValue <= " + maxValue + ") "
                + "}";

        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            JSONArray carbonFootprintsArray = new JSONArray();

            // Map to hold footprints and their relations
            Map<String, JSONObject> footprintsMap = new HashMap<>();

            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                // Extraire les détails de chaque empreinte carbone
                String carbonFootprintUrl = solution.getResource("carbonFootprint").toString();
                String carbonFootprintName = carbonFootprintUrl.split("#")[1];

                String hasCarbonValue = solution.get("hasCarbonValue").toString().replaceAll("\\^\\^.*", "");
                String hasType = solution.get("hasType").toString();

                // Create or get the JSON object for this footprint
                JSONObject carbonFootprintObject = footprintsMap.getOrDefault(carbonFootprintName, new JSONObject());
                if (!carbonFootprintObject.has("footprintName")) {
                    carbonFootprintObject.put("footprintName", carbonFootprintName);
                    carbonFootprintObject.put("hasCarbonValue", hasCarbonValue);
                    carbonFootprintObject.put("hasType", hasType);
                    carbonFootprintObject.put("relations", new JSONArray()); // Initialize relations as empty
                    footprintsMap.put(carbonFootprintName, carbonFootprintObject);
                }

                // Extract the dynamic relationship if present
                if (solution.contains("property") && solution.contains("relatedInstance")) {
                    RDFNode propertyNode = solution.get("property");
                    RDFNode relatedInstanceNode = solution.get("relatedInstance");

                    if (propertyNode.isResource() && relatedInstanceNode.isResource()) {
                        String property = propertyNode.asResource().getLocalName();
                        String relatedInstanceUrl = relatedInstanceNode.asResource().toString();
                        String relatedInstanceName = relatedInstanceUrl.split("#")[1];

                        JSONObject relationObject = new JSONObject();
                        relationObject.put("relation", property);
                        relationObject.put("relatedInstance", relatedInstanceName);

                        carbonFootprintObject.getJSONArray("relations").put(relationObject);
                    }
                }
            }

            // Add all footprints to the main array
            for (JSONObject footprintObject : footprintsMap.values()) {
                // Ensure relations are initialized to empty if not added
                if (footprintObject.getJSONArray("relations").length() == 0) {
                    footprintObject.put("relations", new JSONArray());
                }
                carbonFootprintsArray.put(footprintObject);
            }

            JSONObject resultJson = new JSONObject();
            resultJson.put("carbonFootprints", carbonFootprintsArray);
            return resultJson.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error searching carbon footprints by range: " + e.getMessage());
        }
    }

    // *** fonctions lier a la relation ***/

    public String getRelationsBetweenCarbonFootprintAndReductionStrategy() {
        if (model == null) {
            loadRDF();
        }

        String queryString = "PREFIX ontology: <http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#> "
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                + "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
                + "SELECT ?property "
                + "WHERE { "
                + "  ?property a owl:ObjectProperty ; "
                + "           rdfs:domain ontology:CarbonFootprint ; "
                + "           rdfs:range ontology:CarbonReductionStrategy . "
                + "}";

        Query query = QueryFactory.create(queryString);
        JSONArray relationsArray = new JSONArray();

        synchronized (model) { // Synchronisation sur le modèle
            try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
                ResultSet results = qexec.execSelect();

                while (results.hasNext()) {
                    QuerySolution solution = results.nextSolution();
                    String property = solution.getResource("property").getLocalName();
                    relationsArray.put(property);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error retrieving relations: " + e.getMessage());
            }
        }

        JSONObject resultJson = new JSONObject();
        resultJson.put("relations", relationsArray);
        return resultJson.toString();
    }

    // Méthode pour ajouter une relation dynamique entre CarbonFootprint et
    // CarbonReductionStrategy
    public void addRelation(String relationName) {
        if (model == null) {
            loadRDF();
        }

        // On récupère les ressources pour CarbonFootprint et CarbonReductionStrategy
        Resource carbonFootprint = model
                .getResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#CarbonFootprint");
        Resource carbonReductionStrategy = model.getResource(
                "http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#CarbonReductionStrategy");

        // Créer une propriété avec le nom de relation dynamique
        Property relationProperty = model.createProperty(
                "http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + relationName);

        // Ajouter les propriétés de type, de domaine et de range
        model.add(relationProperty, RDF.type, OWL.ObjectProperty);
        model.add(relationProperty, RDFS.domain, carbonFootprint);
        model.add(relationProperty, RDFS.range, carbonReductionStrategy);

        // Sauvegarde du modèle RDF
        saveRDF();
    }

    public void addInstanceWithRelation(String footprintName, String reductionStrategyName, double carbonValue,
            String type, String relationName) {
        if (model == null) {
            loadRDF();
        }

        // Créer l'instance CarbonFootprint
        Resource footprintInstance = model.createResource(
                "http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + footprintName);
        footprintInstance.addProperty(RDF.type, model
                .getResource("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#CarbonFootprint"));
        footprintInstance.addProperty(
                model.getProperty(
                        "http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasCarbonValue"),
                model.createTypedLiteral(carbonValue));
        footprintInstance.addProperty(
                model.getProperty("http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#hasType"),
                type);

        // Créer l'instance CarbonReductionStrategy
        Resource reductionStrategyInstance = model.getResource(
                "http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + reductionStrategyName);

        // Définir la relation dynamique entre CarbonFootprint et
        // CarbonReductionStrategy
        Property relationProperty = model.createProperty(
                "http://www.semanticweb.org/ghazi/ontologies/2024/8/untitled-ontology-4#" + relationName);
        footprintInstance.addProperty(relationProperty, reductionStrategyInstance);

        // Sauvegarde du modèle RDF
        saveRDF();
    }

}
