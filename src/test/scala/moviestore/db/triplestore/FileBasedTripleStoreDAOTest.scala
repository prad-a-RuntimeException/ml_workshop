package moviestore.db.triplestore

import com.google.common.io.Resources.getResource
import org.apache.jena.rdf.model.Resource
import org.apache.jena.riot.Lang
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}


class FileBasedTripleStoreDAOTest extends FunSuite with Matchers with BeforeAndAfterAll {
  private val SCHEMA_MOVIE_FILES: String = "movies.nq"
  private val MOVIE_LENS_FILES: String = "movielens.n3"
  val resourceStream = getResource(SCHEMA_MOVIE_FILES).openStream
  val movieLensStream = getResource(MOVIE_LENS_FILES).openStream
  val tripleStoreDAO = new FileBasedTripleStoreDAO("test")

  test("Should get all movie resources") {
    tripleStoreDAO.populateNquad(resourceStream)
    tripleStoreDAO.populate(movieLensStream, Lang.N3)

    val statements: List[Resource] = tripleStoreDAO.resources("http://schema.org/Movie").toList
    statements.size > 0 should be(true)
    statements.size should be > 10
  }

  override def afterAll(): Unit = {
    tripleStoreDAO.delete(true)
  }

}