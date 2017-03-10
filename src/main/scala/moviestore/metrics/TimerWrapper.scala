package moviestore.metrics

final case class TimerWrapper(timer: com.codahale.metrics.Timer) extends MetricsWrapper {
  override def getCount: Long = timer.getCount
}
