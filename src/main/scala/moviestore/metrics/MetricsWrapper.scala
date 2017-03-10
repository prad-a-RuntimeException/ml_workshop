package moviestore.metrics

import org.slf4j.{Logger, LoggerFactory}

trait MetricsWrapper {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def getCount: Long

  def poke = {
    this match {
      case _: TimerWrapper => this.asInstanceOf[TimerWrapper].timer.time()
      case _: MeterWrapper => this.asInstanceOf[MeterWrapper].meter.mark()
      case _: CounterWrapper => this.asInstanceOf[CounterWrapper].counter.inc()
    }
  }

  def status: Long = {
    this.getCount
  }


  def close = {
    this match {
      case _: TimerWrapper => this.asInstanceOf[TimerWrapper].timer.time().stop()
      case _ => logger.warn(s"Close method not applicable for ${this.getClass}")
    }
  }
}
