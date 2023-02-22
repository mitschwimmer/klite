package klite.json

import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import klite.Converter
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.util.*

class JsonRendererTest {
  val mapper = JsonMapper()
  val uuid = UUID.fromString("b8ca58ec-ab15-11ed-93cc-8fdb43988a14")

  @Test fun literals() {
    expect(mapper.render(null)).toEqual("null")
    expect(mapper.render(true)).toEqual("true")
    expect(mapper.render(false)).toEqual("false")
    expect(mapper.render(123.45)).toEqual("123.45")
  }

  @Test fun string() {
    expect(mapper.render("Hello")).toEqual("\"Hello\"")
    expect(mapper.render("Hello\n\"World\"")).toEqual("\"Hello\\n\\\"World\\\"\"")
  }

  @Test fun converter() {
    expect(mapper.render(uuid)).toEqual("\"$uuid\"")

    val date = Converter.from<LocalDate>("2022-10-21")
    expect(mapper.render(date)).toEqual("\"2022-10-21\"")
  }

  @Test fun array() {
    expect(mapper.render(emptyList<Any>())).toEqual("[]")
    expect(mapper.render(listOf("a", 1, 3))).toEqual("[\"a\",1,3]")
    expect(mapper.render(arrayOf(1, null))).toEqual("[1,null]")
  }

  @Test fun objects() {
    expect(mapper.render(emptyMap<Any, Any>())).toEqual("{}")
    expect(mapper.render(mapOf("x" to 123, "y" to "abc"))).toEqual("""{"x":123,"y":"abc"}""")
    expect(mapper.render(mapOf(1 to mapOf(2 to arrayOf(1, 2, 3))))).toEqual("""{"1":{"2":[1,2,3]}}""")
  }

  @Test fun inline() {
    expect(mapper.render(Inline(123))).toEqual("123")
  }

  @JvmInline value class Inline(val value: Int)

  @Test fun classes() {
    val o = Hello("", uuid, LocalDate.parse("2022-10-21"), Instant.parse("2022-10-21T10:55:00Z"), Nested(567.toBigDecimal()), listOf(Nested(), Nested()))
    expect(mapper.render(o)).toEqual(/*language=JSON*/ """{"array":[{"x":0,"y":123},{"x":0,"y":123}],"date":"2022-10-21","hello":"","id":"b8ca58ec-ab15-11ed-93cc-8fdb43988a14","instant":"2022-10-21T10:55:00Z","nested":{"x":567,"y":123}}""")
  }

  @Test fun snakeCase() {
    val mapper = JsonMapper(JsonOptions(keys = SnakeCase()))
    expect(mapper.render(mapOf("snakeCase" to 123))).toEqual("""{"snake_case":123}""")
  }

  @Test fun valueConverter() {
    val mapper = JsonMapper(JsonOptions(values = object: ValueConverter<Any?>() {
      override fun to(o: Any?) = when (o) {
        is LocalDate -> o.year
        is Nested -> o.x + o.y.toBigDecimal()
        else -> o
      }
    }))
    expect(mapper.render(mapOf("date" to LocalDate.of(2022, 10, 21), "custom" to Nested()))).toEqual("""{"date":2022,"custom":123}""")
  }
}
