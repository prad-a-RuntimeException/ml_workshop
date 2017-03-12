package moviestore.db.triplestore

import java.io.InputStream
import java.util

import org.apache.jena.rdf.model.{Model, RDFNode, Resource}
import org.apache.jena.riot.Lang

trait TripleStoreDAO {
  def populate(datasetStream: InputStream, lang: Lang)

  def populateNquad(fileInputStream: InputStream)

  def runQuery(sparqlQuery: String, variables: util.Set[String]): Iterator[Map[String, RDFNode]]

  def saveAndClose()

  def delete(clearFileSystem: Boolean)

  def init():Unit

  def model: Model

  def resources(resourceUri: String): Iterator[Resource]
}
