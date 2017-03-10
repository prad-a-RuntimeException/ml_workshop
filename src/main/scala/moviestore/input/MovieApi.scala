package moviestore.input

import java.io.{File, FileOutputStream, InputStream}
import javax.inject.{Inject, Named}

import moviestore.db.triplestore.TripleStoreDAO
import org.apache.jena.rdf.model.Resource
import org.apache.jena.riot.Lang

import scala.collection.JavaConverters._

class MovieApi @Inject()(tripleStoreDAO: TripleStoreDAO,
                         @Named("schemaMoviesStream") schemaMoviesStream: InputStream
                         , @Named("movieLensStream") movieLensStream: InputStream) {

  def load(forceCreate: Boolean) {
    if (forceCreate) tripleStoreDAO.delete(true)
    tripleStoreDAO.populateNquad(schemaMoviesStream)
    tripleStoreDAO.populate(movieLensStream, Lang.N3)
  }

  def getMovieData: Iterator[Resource] = {
    return tripleStoreDAO.getResource("http://schema.org/Movie").asScala
  }

  def getMovieData(resourceFilter: (Resource) => Boolean = (res) => true): Seq[Resource] = {
    return getMovieData.toSeq.filter(resourceFilter(_))
  }
}