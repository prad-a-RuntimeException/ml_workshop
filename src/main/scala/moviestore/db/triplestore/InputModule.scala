package moviestore.db.triplestore

import java.io.InputStream
import java.nio.file.Files._
import java.nio.file.Paths.get
import java.nio.file.StandardOpenOption.READ

import com.google.inject.AbstractModule
import com.google.inject.name.Names
import moviestore.AppResource
import moviestore.misc.ResourceLoader
import net.codingwell.scalaguice.ScalaModule

object InputModule {
  private val schemaMovieFile: String = ResourceLoader(AppResource.TriplestoreResource, "schema_movie_file").get
  private val movieLensFile: String = ResourceLoader(AppResource.TriplestoreResource, "movielens_file").get


  private lazy val schemaMoviesStream = newInputStream(get(schemaMovieFile), READ)
  private lazy val movieLensStream = newInputStream(get(movieLensFile), READ)
}

class InputModule extends AbstractModule with ScalaModule {
  override protected def configure() {
    bind(classOf[TripleStoreDAO]).to(classOf[FileBasedTripleStoreDAO])
    bind(classOf[String]).annotatedWith(Names.named("datasetName")).toInstance("movies")
    bind(classOf[InputStream]).annotatedWith(Names.named("schemaMoviesStream")).toInstance(InputModule.schemaMoviesStream)
    bind(classOf[InputStream]).annotatedWith(Names.named("movieLensStream")).toInstance(InputModule.movieLensStream)
  }
}