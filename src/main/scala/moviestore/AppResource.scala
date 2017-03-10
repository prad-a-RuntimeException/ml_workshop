package moviestore

object AppResource extends Enumeration {
  type ResourceName = Value
  val TinkerpopResource = Value("tinkerpop")
  val TriplestoreResource = Value("triplestore")
  val GraphResource = Value("graph")
  val LuceneResource = Value("lucene")
}
