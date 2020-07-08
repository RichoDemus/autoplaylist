import java.time.OffsetDateTime
import java.time.ZoneOffset

internal fun date(str: String): OffsetDateTime {
    val split = str.split("-")
    val year = split[0].toInt()
    val month = split[1].toInt()
    val day = split[2].toInt()
    return OffsetDateTime.of(year, month, day, 0, 0, 0, 0, ZoneOffset.UTC)
}