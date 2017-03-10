package moviestore.db.triplestore

import java.io.IOException

import com.google.common.io.Resources.getResource
import org.apache.jena.riot.Lang

object FileBasedTripleStoreDAOTest {
  private val SCHEMA_MOVIE_FILES: String = "movies.nq"
  private val MOVIE_LENS_FILES: String = "movielens.n3"
  private var tripleStoreDAO: FileBasedTripleStoreDAO = null
  try {
    val resourceStream = getResource(SCHEMA_MOVIE_FILES).openStream
    val movieLensStream = getResource(MOVIE_LENS_FILES).openStream
    tripleStoreDAO = new FileBasedTripleStoreDAO("test")
    tripleStoreDAO.populateNquad(resourceStream)
    tripleStoreDAO.populate(movieLensStream, Lang.N3)
  }
  catch {
    case e: IOException => {
      throw new RuntimeException("Failed initializing query ", e)
    }
  }
}

class FileBasedTripleStoreDAOTest extends AbstractTripleStoreDAOTest {
  def getTripleStoreDAO: TripleStoreDAO = {
    return FileBasedTripleStoreDAOTest.tripleStoreDAO
  }
}