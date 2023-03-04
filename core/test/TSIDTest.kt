import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import klite.Converter
import klite.TSID
import org.junit.jupiter.api.Test

class TSIDTest {
  val maxValue = TSID(Long.MAX_VALUE)

  @Test fun tsid() {
    expect(TSID(1234L).toString()).toEqual("ya")
    expect(maxValue.toString()).toEqual("1y2p0ij32e8e7")
    expect(TSID("1y2p0ij32e8e7")).toEqual(maxValue)
  }

  @Test fun converter() {
    expect(Converter.from<TSID>(maxValue.toString())).toEqual(maxValue)
  }

  @Test fun `no collisions`() {
    val ids = mutableSetOf<TSID>()
    for (i in 1..1000000) ids.add(TSID())
    expect(ids.size).toEqual(1000000)
  }
}
