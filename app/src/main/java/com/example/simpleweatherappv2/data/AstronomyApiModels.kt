package com.example.simpleweatherappv2.data

data class AstronomyMoonPhaseRequest(
    val format: String = "png",
    val style: MoonStyle,
    val observer: Observer,
    val view: View
)

data class MoonStyle(
    val moonStyle: String = "default",
    val backgroundStyle: String = "solid",
    val backgroundColor: String = "rgba(0,0,0,0)",
    val headingColor: String = "white",
    val textColor: String = "white"
)

data class Observer(
    val latitude: Double,
    val longitude: Double,
    val date: String // YYYY-MM-DD
)

data class View(
    val type: String = "landscape-simple",
    val orientation: String = "south-up"
)

data class AstronomyResponse(
    val data: AstronomyData
)

data class AstronomyData(
    val imageUrl: String
)

data class AstronomyStarChartRequest(
    val style: String = "default",
    val observer: Observer,
    val view: StarChartView
)

data class StarChartStyle(
    val starStyle: String = "default",
    val backgroundStyle: String = "solid",
    val backgroundColor: String = "rgba(0,0,0,0)",
    val headingColor: String = "white",
    val textColor: String = "white",
    val constellations: ConstellationStyle = ConstellationStyle()
)

data class ConstellationStyle(
    val lines: LineStyle = LineStyle(),
    val art: Boolean = false
)

data class LineStyle(
    val stroke: String = "white",
    val strokeWidth: Int = 1
)

data class StarChartView(
    val type: String = "constellation",
    val parameters: StarChartParameters
)

data class StarChartParameters(
    val constellation: String? = null,
    val position: StarChartPosition? = null,
    val zoom: Int? = null
)

data class StarChartPosition(
    val equatorial: EquatorialCoordinates
)

data class EquatorialCoordinates(
    val rightAscension: Double,
    val declination: Double
)

// Helper to map 3-letter IDs to Names
object Constellations {
    val map = mapOf(
        "and" to "Andromeda", "leo" to "Leo", "ant" to "Antlia", "lmi" to "Leo Minor",
        "aps" to "Apus", "lep" to "Lepus", "aqr" to "Aquarius", "lib" to "Libra",
        "aql" to "Aquila", "lup" to "Lupus", "ara" to "Ara", "lyn" to "Lynx",
        "ari" to "Aries", "lyr" to "Lyra", "aur" to "Auriga", "men" to "Mensa",
        "boo" to "Boötes", "mic" to "Microscopium", "cae" to "Caelum", "mon" to "Monoceros",
        "cam" to "Camelopardalis", "mus" to "Musca", "cnc" to "Cancer", "nor" to "Norma",
        "cvn" to "Canes Venatici", "oct" to "Octans", "cma" to "Canis Major", "oph" to "Ophiuchus",
        "cmi" to "Canis Minor", "ori" to "Orion", "cap" to "Capricornus", "pav" to "Pavo",
        "car" to "Carina", "peg" to "Pegasus", "cas" to "Cassiopeia", "per" to "Perseus",
        "cen" to "Centaurus", "phe" to "Phoenix", "cep" to "Cepheus", "pic" to "Pictor",
        "cet" to "Cetus", "psc" to "Pisces", "cha" to "Chamaeleon", "psa" to "Piscis Austrinus",
        "cir" to "Circinus", "pup" to "Puppis", "col" to "Columba", "pyx" to "Pyxis",
        "com" to "Coma Berenices", "ret" to "Reticulum", "cra" to "Corona Australis", "sge" to "Sagitta",
        "crb" to "Corona Borealis", "sgr" to "Sagittarius", "crv" to "Corvus", "sco" to "Scorpius",
        "crt" to "Crater", "scl" to "Sculptor", "cru" to "Crux", "sct" to "Scutum",
        "cyg" to "Cygnus", "ser" to "Serpens", "del" to "Delphinus", "dor" to "Dorado",
        "sex" to "Sextans", "dra" to "Draco", "tau" to "Taurus", "equ" to "Equuleus",
        "tel" to "Telescopium", "eri" to "Eridanus", "tri" to "Triangulum", "for" to "Fornax",
        "tra" to "TrA", "gem" to "Gemini", "tuc" to "Tucana", "gru" to "Grus",
        "uma" to "Ursa Major", "her" to "Hercules", "umi" to "Ursa Minor", "hor" to "Horologium",
        "vel" to "Vela", "hya" to "Hydra", "vir" to "Virgo", "hyi" to "Hydrus",
        "vol" to "Volans", "ind" to "Indus", "vul" to "Vulpecula", "lac" to "Lacerta"
    )
    
    fun getName(id: String): String {
        return map[id.lowercase()] ?: id.uppercase()
    }
}
