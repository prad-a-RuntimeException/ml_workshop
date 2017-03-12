package moviestore.db.triplestore

import java.util

import org.apache.jena.graph.{Node, Triple}
import org.apache.jena.riot.other.StreamRDFBatchHandler
import org.apache.jena.sparql.core.Quad
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger

import scala.collection.JavaConverters._

object QuadsBatchHandler {
  def createStreamBatchHandler(quadFn: (Quad) => Unit): QuadsBatchHandler = {
    return new QuadsBatchHandler(quadFn)
  }

  val MOVIE_FILTER: List[String] = List("www.imdb.com")

  def filterByUrl(url: String): Boolean =
    MOVIE_FILTER.find(pattern => url.toLowerCase().contains(pattern)).isDefined
}

class QuadsBatchHandler(val quadConsumer: (Quad) => Unit) extends StreamRDFBatchHandler {
  private val LOGGER: Logger = getLogger(classOf[QuadsBatchHandler])

  def start() {
    LOGGER.info("Starting nquad batch processing")
  }

  def batchTriples(currentSubject: Node, triples: util.List[Triple]) {
  }

  def batchQuads(currentGraph: Node, currentSubject: Node, quads: util.List[Quad]) {
    val uri: String = currentGraph.getURI.toLowerCase
    if (QuadsBatchHandler.filterByUrl(uri)) {
//      LOGGER.trace("For graph {} and subject {}, found quads  {}", currentGraph, currentSubject, quads.size)
      quads.asScala.foreach(quadConsumer)
    }
  }

  def base(base: String) {
  }

  def prefix(prefix: String, iri: String) {
  }

  def finish() {
    LOGGER.info("Quad batch processing done")
  }
}