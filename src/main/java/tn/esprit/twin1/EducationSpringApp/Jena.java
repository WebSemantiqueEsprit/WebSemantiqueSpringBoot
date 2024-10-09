package tn.esprit.twin1.EducationSpringApp;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.VCARD;
import org.springframework.stereotype.Component;

@Component
public class Jena {

    public void execute() {
        // Create an empty model
        Model model = ModelFactory.createDefaultModel();

        // Define the URI for the resource (in this case, an animal)
        String URIanimal = "http://www.exemple.com/animal";

        // Create the resource and add a property
        Resource animal = model.createResource(URIanimal)
                               .addProperty(VCARD.FN, "elephant");

        // Write the model in RDF/XML format to the console
        model.write(System.out, "RDF/XML");
    }
}
