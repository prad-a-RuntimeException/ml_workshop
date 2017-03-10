package moviestore.db.triplestore;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public interface TripleStoreDAO {

    void populate(InputStream datasetStream, Lang lang);

    void populateNquad(InputStream fileInputStream);

    Stream<Map<String, RDFNode>> runQuery(String sparqlQuery, Set<String> variables);

    void saveAndClose();

    void delete(boolean clearFileSystem);

    Model getModel();

    Iterator<Resource> getResource(String resourceUri);


}
