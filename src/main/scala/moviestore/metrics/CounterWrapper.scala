package moviestore.metrics

final case class CounterWrapper(counter: com.codahale.metrics.Counter) extends MetricsWrapper {
  override def getCount: Long = counter.getCount
}
