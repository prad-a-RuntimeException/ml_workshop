package moviestore.metrics

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

import com.codahale.metrics.{ConsoleReporter, MetricRegistry}
import com.twitter.storehaus.cache.MapCache.empty
import com.twitter.storehaus.cache.{Memoize, MutableCache}

object MetricsFactory {

  private val metricsCache: MutableCache[(String, Class[_]), MetricsWrapper] =
    empty[(String, Class[_]), MetricsWrapper].toMutable()

  val get = Memoize(metricsCache)(_get)

  val reporterStatus: AtomicBoolean = new AtomicBoolean(false)

  val metricRegistry = new MetricRegistry

  var reporter: ConsoleReporter = null


  private def createReporter: ConsoleReporter = {
    ConsoleReporter.forRegistry(metricRegistry).
      convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS).build
  }

  def initReporter(duration: Int = 10): Unit = {
    reporterStatus.set(true)
    reporter = createReporter
    reporter.start(duration, TimeUnit.SECONDS)
  }

  def stopReporter() = {
    if (reporter != null) {
      reporterStatus.set(false)
      reporter.stop()
      reporter = null
    }
  }

  def _get[T <: MetricsWrapper](name: String, c: Class[_]): MetricsWrapper = {
    val metrics: MetricsWrapper = if (classOf[MeterWrapper] isAssignableFrom c) new MeterWrapper(metricRegistry.meter(name))
    else if (classOf[CounterWrapper] isAssignableFrom c) new CounterWrapper(metricRegistry.counter(name))
    else if (classOf[TimerWrapper] isAssignableFrom c) new TimerWrapper(metricRegistry.timer(name))
    else throw new IllegalArgumentException("Cannot initiate metrics for class " + c)
    if (!reporterStatus.get()) {
      initReporter()
    }
    metrics
  }

  def remove[T <: MetricsWrapper](name: String, c: Class[_]): Unit = {
    metricsCache.evict(name, c)
    if (!metricsCache.iterator.hasNext) {
      stopReporter()
    }
  }


}
