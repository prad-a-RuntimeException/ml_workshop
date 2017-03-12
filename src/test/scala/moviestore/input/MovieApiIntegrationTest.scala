package moviestore.input

import java.io.InputStream

import moviestore.AppResource
import moviestore.db.triplestore.{FileBasedTripleStoreDAO, GraphVisitor, MovieApi}
import moviestore.misc.{ResourceLoader, S3FileReader}
import org.apache.jena.ext.com.google.common.io.Resources
import org.apache.jena.rdf.model.Resource
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

class MovieApiIntegrationTest extends FunSuite with Matchers with BeforeAndAfter {

  private val bucketName: String = ResourceLoader(AppResource.TriplestoreResource, "s3_bucket").get
  private val schemaMovieFile: String = ResourceLoader(AppResource.TriplestoreResource, "schema_movie_file").get
  private val movieLensFile: String = ResourceLoader(AppResource.TriplestoreResource, "movielens_file").get
  val movieStream: InputStream = Resources.getResource("movies.nq").openStream
  private lazy val movieLensStream = S3FileReader.readData(bucketName, movieLensFile)
  val movieApi = new MovieApi(new FileBasedTripleStoreDAO("int-test"), movieStream, movieLensStream)


  test("Should load movies from nquad data") {
    movieApi.load(true)
    val movieData: Iterator[Resource] = movieApi.getMovieData()
    movieData.size should be > 1

    movieData
      .filter(r => r.getProperty(r.getModel.getProperty("http://schema.org/Movie/name")) != null)
      .foreach(r => {
        val movieName = r.getProperty(r.getModel.getProperty("http://schema.org/Movie/name")).getObject.asLiteral().getString
        println(movieName)
        GraphVisitor.traverse(r, (v) => println(v), (e) => println(e))

      })


  }
}
