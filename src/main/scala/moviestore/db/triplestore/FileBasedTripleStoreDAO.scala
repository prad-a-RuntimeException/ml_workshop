package moviestore.db.triplestore

import java.io.{File, InputStream}
import java.lang.String.format

import moviestore.db.triplestore.rdfparsers.{CustomRDFDataMgr, LenientNquadParser}
import org.apache.commons.io.FileUtils
import org.apache.jena.query._
import org.apache.jena.rdf.model.{Model, RDFNode, Resource}
import org.apache.jena.riot.other.BatchedStreamRDF
import org.apache.jena.riot.system.StreamRDF
import org.apache.jena.riot.{Lang, RDFDataMgr}
import org.apache.jena.sparql.core.{DatasetGraph, Quad}
import org.apache.jena.tdb.TDBFactory
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._

/**
  * Uses Jena TDB for triplestore in the local filesystem.
  * Not very scalable, but can handle upto million quads in
  * a commodity dev hardware.
  */
object FileBasedTripleStoreDAO {
  val LOGGER: Logger = LoggerFactory.getLogger(classOf[FileBasedTripleStoreDAO])
  val BASE_LOCATION: String = "triple_store"
  val getFileLocation: (String) => String = (datasetName) => format("%s/%s", BASE_LOCATION, datasetName)
}

class FileBasedTripleStoreDAO(val datasetName: String) extends TripleStoreDAO {
  val quadConsumer = (quad: Quad) => {
    datasetGraph.add(quad)
  }
  private val fileLocation = FileBasedTripleStoreDAO.getFileLocation.apply(this.datasetName)

  var dataSet:Dataset = _
  init()

  def init(): Unit = {

    val file: File = new File(FileBasedTripleStoreDAO.BASE_LOCATION)
    if (!file.exists && !file.isDirectory) {
      val mkdir: Boolean = file.mkdir
      if (!mkdir) {
        throw new RuntimeException("Creation of directory failed ")
      }
    }

    this.dataSet = TDBFactory.createDataset(fileLocation)

  }

  lazy val datasetGraph: DatasetGraph = TDBFactory.createDatasetGraph(fileLocation)

  def populate(datasetStream: InputStream, lang: Lang) {
    try {
      val sink: StreamRDF = new BatchedStreamRDF(QuadsBatchHandler.apply(quadConsumer))
      RDFDataMgr.parse(sink, datasetStream, lang)
    }
    catch {
      case e: Exception => {
        FileBasedTripleStoreDAO.LOGGER.warn("Possible bad data in the input triple ", e)
        saveAndClose()
      }
    }
  }

  def populateNquad(datasetStream: InputStream) {
    try {
      val sink: StreamRDF = new BatchedStreamRDF(QuadsBatchHandler(quadConsumer))
      CustomRDFDataMgr.parse(sink, datasetStream, LenientNquadParser.LANG)
    }
    catch {
      case e: Exception => {
        FileBasedTripleStoreDAO.LOGGER.warn("Possible bad data in the input triple ", e)
        saveAndClose()
      }
    }
  }

  override def runQuery(sparqlQuery: String, variables: java.util.Set[String]): Iterator[Map[String, RDFNode]] = {
    val query: Query = QueryFactory.create(sparqlQuery)
    val queryExecution: QueryExecution = QueryExecutionFactory.create(query, dataSet)
    val resultSet: ResultSet = queryExecution.execSelect
    resultSet.asScala.map(querySolution => variables.asScala
      .map(v => (v -> querySolution.get(v))).toMap)
  }

  def saveAndClose() {
    if (dataSet != null) {
      try {
        dataSet.commit()
        dataSet.close()
      }
      catch {
        case e: Exception => {
          FileBasedTripleStoreDAO.LOGGER.warn("Failed cleaning up Triplestore file system")
        }
      }
    }
  }

  def delete(clearFileSystem: Boolean) {
    if (dataSet != null) {
      dataSet.close()
      saveAndClose()
    }
    if (clearFileSystem) FileUtils.deleteDirectory(new File(fileLocation))
    init()
  }

  def model: Model = {
    dataSet.getNamedModel("urn:x-arq:UnionGraph")
  }

  def resources(resourceUri: String): Iterator[Resource] = {
    model.listStatements(null, null, model.getResource(resourceUri))
      .asScala
      .map(stmt => stmt.getSubject)
  }
}