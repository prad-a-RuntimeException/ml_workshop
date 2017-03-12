package moviestore

import com.google.inject.{Guice, Injector}
import moviestore.db.triplestore.{InputModule, MovieApi}
import org.apache.jena.rdf.model.Resource

object TripleStoreEntryPoint {
  var inputModule: Injector = Guice.createInjector(new InputModule)

  def main(args: Array[String]) {
    loadMovieData()
  }

  def loadMovieData() {
    inputModule.getInstance(classOf[MovieApi]).load(true)
  }

  def readData(): Seq[Resource] = {
    val movies: Seq[Resource] = inputModule.getInstance(classOf[MovieApi]).getMovieData()
    movies
  }

}
