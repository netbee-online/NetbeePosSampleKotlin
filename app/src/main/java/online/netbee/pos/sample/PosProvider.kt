package online.netbee.pos.sample

sealed class PosProvider(
    val displayName: String,
    val type: String,
) {
    object IranKish : PosProvider(
        displayName = "ایران کیش",
        type = "iran_kish",
    )

    object Sadad : PosProvider(
        displayName = "سداد",
        type = "sadad",
    )

    object BehPardakht : PosProvider(
        displayName = "به پرداخت",
        type = "beh_pardakht",
    )

    object AsanPardakht : PosProvider(
        displayName = "آسان پرداخت",
        type = "asan_pardakht",
    )

    object SamanKish : PosProvider(
        displayName = "سامان کیش",
        type = "saman_kish",
    )

    object Fanava : PosProvider(
        displayName = "فن آوا",
        type = "fanava",
    )

    object Damavand : PosProvider(
        displayName = "دماوند",
        type = "damavand",
    )

    object Parsian : PosProvider(
        displayName = "پارسیان",
        type = "parsian",
    )

    override fun toString(): String {
        return displayName
    }
}

val posProviders = listOf(
    PosProvider.SamanKish,
    PosProvider.Fanava,
    PosProvider.BehPardakht,
    PosProvider.AsanPardakht,
    PosProvider.Sadad,
    PosProvider.IranKish,
    PosProvider.Damavand,
    PosProvider.Parsian,
)
    .sortedBy { pos -> pos.displayName }