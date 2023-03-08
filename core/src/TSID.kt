package klite

import java.lang.System.currentTimeMillis
import java.security.SecureRandom
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger

/** Time-Sorted unique ID, a more compact and index-friendly alternative to UUID */
typealias TSID = TypedTSID<Any>

@JvmInline value class TypedTSID<T>(private val value: Long) {
  companion object {
    const val RANDOM_BITS = 22
    const val RANDOM_MASK = 0x003fffff
    val EPOCH = Instant.parse(Config.optional("TSID_EPOCH", "2022-10-21T03:45:00.000Z")).toEpochMilli()
    val random = SecureRandom()
    val counter = AtomicInteger()
    @Volatile var lastTime = 0L

    private fun generate(): Long {
      val time = (currentTimeMillis() - EPOCH) shl RANDOM_BITS
      if (time != lastTime) {
        counter.set(random.nextInt())
        lastTime = time
      }
      val tail = counter.incrementAndGet() and RANDOM_MASK
      return time or tail.toLong()
    }

    init {
      Converter.use { TSID(it) }
      Converter.use { TypedTSID<Any>(it) }
    }
  }

  constructor(): this(generate())
  constructor(tsid: String): this(tsid.toLong(36))
  override fun toString() = value.toString(36)

  val createdAt: Instant get() = Instant.ofEpochMilli((value shr RANDOM_BITS) + EPOCH)
}
