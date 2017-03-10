package moviestore.metrics

final case class MeterWrapper(meter: com.codahale.metrics.Meter) extends MetricsWrapper {
  override def getCount: Long = meter.getCount
}
