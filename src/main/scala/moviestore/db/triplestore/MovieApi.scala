package moviestore.db.triplestore

import java.io.InputStream
import javax.inject.{Inject, Named}

import org.apache.jena.rdf.model.Resource
import org.apache.jena.riot.Lang

class MovieApi @Inject()(tripleStoreDAO: TripleStoreDAO,
                         @Named("schemaMoviesStream") schemaMoviesStream: InputStream
                         , @Named("movieLensStream") movieLensStream: InputStream) {

  def load(forceCreate: Boolean) {
    if (forceCreate) {
      tripleStoreDAO.delete(true)
    }
    tripleStoreDAO.populateNquad(schemaMoviesStream)
    tripleStoreDAO.populate(movieLensStream, Lang.N3)
  }

  def getMovieData: Iterator[Resource] = {
    tripleStoreDAO.resources("http://schema.org/Movie")
  }

  def getMovieData(resourceFilter: (Resource) => Boolean = (res) => true): Iterator[Resource] = {
     getMovieData.filter(resourceFilter(_))
  }
}