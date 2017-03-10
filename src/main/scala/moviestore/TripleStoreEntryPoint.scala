package moviestore

import com.google.inject.{Guice, Injector}
import moviestore.input.{InputModule, MovieApi}

object TripleStoreEntryPoint {
  var inputModule: Injector = Guice.createInjector(new InputModule)

  def main(args: Array[String]) {
    loadMovieData()
  }

  def loadMovieData() {
    inputModule.getInstance(classOf[MovieApi]).load(true)
  }

}
