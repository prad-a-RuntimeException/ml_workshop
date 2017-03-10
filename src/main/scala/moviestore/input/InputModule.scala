package moviestore.input

import java.io.InputStream

import com.google.inject.name.Names
import moviestore.db.triplestore.{FileBasedTripleStoreDAO, TripleStoreDAO}
import moviestore.misc.{ResourceLoader, S3FileReader}
import moviestore.{AppResource, CommonModule}

object InputModule {
  private val bucketName: String = ResourceLoader(AppResource.TriplestoreResource, "s3_bucket").get
  private val schemaMovieFile: String = ResourceLoader(AppResource.TriplestoreResource, "schema_movie_file").get
  private val movieLensFile: String = ResourceLoader(AppResource.TriplestoreResource, "movielens_file").get


  private lazy val schemaMoviesStream = S3FileReader.readData(bucketName, schemaMovieFile)
  private lazy val movieLensStream = S3FileReader.readData(bucketName, movieLensFile)
}

class InputModule extends CommonModule {
  override protected def configure() {
    super.configure()
    bind(classOf[TripleStoreDAO]).to(classOf[FileBasedTripleStoreDAO])
    bind(classOf[String]).annotatedWith(Names.named("datasetName")).toInstance("movies")
    bind(classOf[InputStream]).annotatedWith(Names.named("schemaMoviesStream")).toInstance(InputModule.schemaMoviesStream)
    bind(classOf[InputStream]).annotatedWith(Names.named("movieLensStream")).toInstance(InputModule.movieLensStream)
  }
}