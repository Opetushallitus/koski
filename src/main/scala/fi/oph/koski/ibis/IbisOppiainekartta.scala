package fi.oph.koski.ibis

sealed trait IbisOppiaine {
  def tunniste: String
}

case class IbisMuuOppiaine(tunniste: String) extends IbisOppiaine

case class IbisKieliOppiaine(tunniste: String, kieli: String) extends IbisOppiaine

// IBIS-tulostiedoston oppiainekoodi (SUBJECT CODE) -> Kosken oppiaineetib-koodi ja kielioppiaineilla kielivalikoima-koodi.
// Koodit ja kommenttien nimet ovat IBO:n oppiaineluettelosta.
object IbisOppiainekartta {
  def get(koodi: String): Option[IbisOppiaine] = oppiaineet.get(koodi)

  // Näille IBO:n oppiaineluettelon oppiaineille ei vielä ole vastinetta Koskessa:
  // - Kieli puuttuu kielivalikoimasta: AKAN (112861), BELARUSIAN (112875, 133711), BEMBA (112897, 129712),
  //   BURMESE (112814, 112930), CHICHEWA (112910), DHIVEHI (112927), DZONGKHA (112879, 112921), GUARANI (175711, 175712),
  //   IRISH (112834), KAZAKH (112804, 141712), LAO (112881), LUGANDA (112841), MALAGASY (155711), NDEBELE (112819, 112852),
  //   SHONA (112832, 112909), TAJIK (112878, 141711), TIBETAN (112835, 112931), WELSH (112715, 112783) ja ZULU (112829, 112925).
  // - CLASS.GREEK (100129), koska kielivalikoiman EL on nykykreikka.
  // - Oppiaine puuttuu oppiaineetib-koodistosta: ART HISTORY (100058), ASTRONOMY (100068), BRAZILIAN SOCIAL STUDIES (101711),
  //   FOOD SCIENCE AND TECHNOLOGY (158711), LANGUAGE AND CULTURE (184711), LITERARY ARTS (188711), MARINE SCIENCE (100671),
  //   SYSTEMS TRANSFORMATION 1/2/A/B DUAL AWARD (189711, 189712, 193711, 193712), TURKEY IN THE 20TH CENTURY (107711)
  //   ja WORLD ARTS AND CULTURES (108714).
  // THEORY KNOWL. (100577) tunnistetaan tasosta TK, ja REF PROJECT (100657) kuuluu Career-related Programmeen, joten niitä ei
  // haeta koodilla.
  val oppiaineet: Map[String, IbisOppiaine] = Map(
    // Language A: literature
    "112815" -> IbisKieliOppiaine("A", "SQ"), // ALBANIAN A: Literature
    "112766" -> IbisKieliOppiaine("A", "AM"), // AMHARIC A: Literature
    "112721" -> IbisKieliOppiaine("A", "AR"), // ARABIC A: Literature
    "112813" -> IbisKieliOppiaine("A", "HY"), // ARMENIAN A: Literature
    "112903" -> IbisKieliOppiaine("A", "AZ"), // AZERBAIJANI A: Literature
    "112831" -> IbisKieliOppiaine("A", "BN"), // BENGALI A: Literature
    "112758" -> IbisKieliOppiaine("A", "BS"), // BOSNIAN A: Literature
    "112775" -> IbisKieliOppiaine("A", "BG"), // BULGARIAN A: Literature
    "112764" -> IbisKieliOppiaine("A", "CA"), // CATALAN A: Literature
    "112724" -> IbisKieliOppiaine("A", "ZH"), // CHINESE A: Literature
    "112761" -> IbisKieliOppiaine("A", "HR"), // CROATIAN A: Literature
    "112725" -> IbisKieliOppiaine("A", "CS"), // CZECH A: Literature
    "112755" -> IbisKieliOppiaine("A", "DA"), // DANISH A: Literature
    "112720" -> IbisKieliOppiaine("A", "NL"), // DUTCH A: Literature
    "112711" -> IbisKieliOppiaine("A", "EN"), // ENGLISH A: Literature
    "112859" -> IbisKieliOppiaine("A", "ET"), // ESTONIAN A: Literature
    "116711" -> IbisKieliOppiaine("A", "FIL"), // FILIPINO A: Literature
    "112726" -> IbisKieliOppiaine("A", "FI"), // FINNISH A: Literature
    "112750" -> IbisKieliOppiaine("A", "FR"), // FRENCH A: Literature
    "112877" -> IbisKieliOppiaine("A", "KA"), // GEORGIAN A: Literature
    "112751" -> IbisKieliOppiaine("A", "DE"), // GERMAN A: Literature
    "112722" -> IbisKieliOppiaine("A", "HE"), // HEBREW A: Literature
    "112747" -> IbisKieliOppiaine("A", "HI"), // HINDI A: Literature
    "112748" -> IbisKieliOppiaine("A", "HU"), // HUNGARIAN A: Literature
    "112883" -> IbisKieliOppiaine("A", "IS"), // ICELANDIC A: Literature
    "112749" -> IbisKieliOppiaine("A", "ID"), // INDONESIAN A: Literature
    "112753" -> IbisKieliOppiaine("A", "IT"), // ITALIAN A: Literature
    "112743" -> IbisKieliOppiaine("A", "JA"), // JAPANESE A: Literature
    "112896" -> IbisKieliOppiaine("A", "KM"), // KHMER A: Literature
    "115711" -> IbisKieliOppiaine("A", "RW"), // KINYARWANDA A: Literature
    "178713" -> IbisKieliOppiaine("A", "SW"), // KISWAHILI A: Literature
    "112744" -> IbisKieliOppiaine("A", "KO"), // KOREAN A: Literature
    "112807" -> IbisKieliOppiaine("A", "KU"), // KURDISH A: Literature
    "112723" -> IbisKieliOppiaine("A", "LV"), // LATVIAN A: Literature
    "112765" -> IbisKieliOppiaine("A", "LT"), // LITHUANIAN A: Literature
    "112712" -> IbisKieliOppiaine("A", "MK"), // MACEDONIAN A: Literature
    "112745" -> IbisKieliOppiaine("A", "MS"), // MALAY A: Literature
    "112867" -> IbisKieliOppiaine("A", "ML"), // MALAYALAM A: Literature
    "112820" -> IbisKieliOppiaine("A", "MR"), // MARATHI A: Literature
    "112727" -> IbisKieliOppiaine("A", "EL"), // MODERN GREEK A: Literature.
    "112882" -> IbisKieliOppiaine("A", "MN"), // MONGOLIAN A: Literature
    "112763" -> IbisKieliOppiaine("A", "NE"), // NEPALI A: Literature
    "112756" -> IbisKieliOppiaine("A", "NO"), // NORWEGIAN A: Literature
    "112822" -> IbisKieliOppiaine("A", "PS"), // PASHTO A: Literature
    "112746" -> IbisKieliOppiaine("A", "FA"), // PERSIAN A: Literature
    "112717" -> IbisKieliOppiaine("A", "PL"), // POLISH A: Literature
    "112718" -> IbisKieliOppiaine("A", "PT"), // PORTUGUESE A: Literature
    "112801" -> IbisKieliOppiaine("A", "PA"), // PUNJABI A: Literature
    "112886" -> IbisKieliOppiaine("A", "RO"), // ROMANIAN A: Literature
    "112754" -> IbisKieliOppiaine("A", "RU"), // RUSSIAN A: Literature
    "112759" -> IbisKieliOppiaine("A", "SR"), // SERBIAN A: Literature
    "112762" -> IbisKieliOppiaine("A", "ST"), // SESOTHO A: Literature
    "177711" -> IbisKieliOppiaine("A", "SI"), // SINHALA A: Literature
    "112760" -> IbisKieliOppiaine("A", "SK"), // SLOVAK A: Literature
    "112741" -> IbisKieliOppiaine("A", "SL"), // SLOVENE A: Literature
    "112800" -> IbisKieliOppiaine("A", "SO"), // SOMALI A: Literature
    "112752" -> IbisKieliOppiaine("A", "ES"), // SPANISH A: Literature
    "112757" -> IbisKieliOppiaine("A", "SV"), // SWEDISH A: Literature
    "112810" -> IbisKieliOppiaine("A", "TA"), // TAMIL A: Literature
    "112901" -> IbisKieliOppiaine("A", "TE"), // TELUGU A: Literature
    "112713" -> IbisKieliOppiaine("A", "TH"), // THAI A: Literature
    "112714" -> IbisKieliOppiaine("A", "TR"), // TURKISH A: Literature
    "112802" -> IbisKieliOppiaine("A", "UK"), // UKRAINIAN A: Literature
    "112885" -> IbisKieliOppiaine("A", "UR"), // URDU A: Literature
    "180711" -> IbisKieliOppiaine("A", "UG"), // UYGHUR A: Literature
    "112803" -> IbisKieliOppiaine("A", "UZ"), // UZBEK A: Literature
    "112884" -> IbisKieliOppiaine("A", "VI"), // VIETNAMESE A: Literature
    "112843" -> IbisKieliOppiaine("A", "YO"), // YORUBA A: Literature

    // Language A: language and literature
    "112739" -> IbisKieliOppiaine("A2", "AR"), // ARABIC A Language and Literature
    "112740" -> IbisKieliOppiaine("A2", "ZH"), // CHINESE A Language and Literature
    "112738" -> IbisKieliOppiaine("A2", "NL"), // DUTCH A Language and Literature
    "112733" -> IbisKieliOppiaine("A2", "EN"), // ENGLISH A Language and Literature
    "112734" -> IbisKieliOppiaine("A2", "FR"), // FRENCH A Language and Literature
    "112735" -> IbisKieliOppiaine("A2", "DE"), // GERMAN A Language and Literature
    "125711" -> IbisKieliOppiaine("A2", "ID"), // INDONESIAN A: Lang and Literature
    "112838" -> IbisKieliOppiaine("A2", "IT"), // ITALIAN A Language and Literature
    "128711" -> IbisKieliOppiaine("A2", "JA"), // JAPANESE A: Lang and Literatur
    "115715" -> IbisKieliOppiaine("A2", "KO"), // KOREAN A: Lang and Literature
    "128712" -> IbisKieliOppiaine("A2", "EL"), // MOD. GREEK A: Lang and Literat
    "128715" -> IbisKieliOppiaine("A2", "NO"), // NORWEGIAN A: Lang and Literatu
    "128713" -> IbisKieliOppiaine("A2", "PT"), // PORTUGUESE A: Lang and Literat
    "112839" -> IbisKieliOppiaine("A2", "RU"), // RUSSIAN A Language and Literature
    "112736" -> IbisKieliOppiaine("A2", "ES"), // SPANISH A Language and Literature
    "112737" -> IbisKieliOppiaine("A2", "SV"), // SWEDISH A Language and Literature
    "112731" -> IbisKieliOppiaine("A2", "TH"), // THAI A Language and Literature
    "174711" -> IbisKieliOppiaine("A2", "VI"), // VIETNAMESE A: Lang and Literature

    // Language A, extended essay
    "112914" -> IbisKieliOppiaine("A", "SQ"), // ALBANIAN A
    "112782" -> IbisKieliOppiaine("A", "AM"), // AMHARIC A
    "100049" -> IbisKieliOppiaine("A", "AR"), // ARABIC A
    "112905" -> IbisKieliOppiaine("A", "HY"), // ARMENIAN A
    "100069" -> IbisKieliOppiaine("A", "AZ"), // AZERBAIJANI A
    "112908" -> IbisKieliOppiaine("A", "BN"), // BENGALI A
    "112793" -> IbisKieliOppiaine("A", "BS"), // BOSNIAN A
    "112790" -> IbisKieliOppiaine("A", "BG"), // BULGARIAN A
    "100108" -> IbisKieliOppiaine("A", "CA"), // CATALAN A
    "100121" -> IbisKieliOppiaine("A", "ZH"), // CHINESE A
    "112796" -> IbisKieliOppiaine("A", "HR"), // CROATIAN A
    "112792" -> IbisKieliOppiaine("A", "CS"), // CZECH A
    "100142" -> IbisKieliOppiaine("A", "DA"), // DANISH A
    "100154" -> IbisKieliOppiaine("A", "NL"), // DUTCH A
    "100176" -> IbisKieliOppiaine("A", "EN"), // ENGLISH A
    "112929" -> IbisKieliOppiaine("A", "ET"), // ESTONIAN A
    "116713" -> IbisKieliOppiaine("A", "FIL"), // FILIPINO A
    "100202" -> IbisKieliOppiaine("A", "FI"), // FINNISH A
    "100210" -> IbisKieliOppiaine("A", "FR"), // FRENCH A
    "112933" -> IbisKieliOppiaine("A", "KA"), // GEORGIAN A
    "100227" -> IbisKieliOppiaine("A", "DE"), // GERMAN A
    "100244" -> IbisKieliOppiaine("A", "HE"), // HEBREW A
    "100249" -> IbisKieliOppiaine("A", "HI"), // HINDI A
    "112788" -> IbisKieliOppiaine("A", "HU"), // HUNGARIAN A
    "100272" -> IbisKieliOppiaine("A", "IS"), // ICELANDIC A
    "100281" -> IbisKieliOppiaine("A", "ID"), // INDONESIAN A
    "100295" -> IbisKieliOppiaine("A", "IT"), // ITALIAN A
    "100304" -> IbisKieliOppiaine("A", "JA"), // JAPANESE A
    "100322" -> IbisKieliOppiaine("A", "KM"), // KHMER A
    "129714" -> IbisKieliOppiaine("A", "RW"), // KINYARWANDA A
    "178711" -> IbisKieliOppiaine("A", "SW"), // KISWAHILI A
    "100332" -> IbisKieliOppiaine("A", "KO"), // KOREAN A
    "129715" -> IbisKieliOppiaine("A", "KU"), // KURDISH A
    "112850" -> IbisKieliOppiaine("A", "KY"), // KYRGYZ A
    "112794" -> IbisKieliOppiaine("A", "LV"), // LATVIAN A
    "112797" -> IbisKieliOppiaine("A", "LT"), // LITHUANIAN A
    "112798" -> IbisKieliOppiaine("A", "MK"), // MACEDONIAN A
    "112786" -> IbisKieliOppiaine("A", "MS"), // MALAY A
    "112791" -> IbisKieliOppiaine("A", "EL"), // MOD. GREEK A
    "112913" -> IbisKieliOppiaine("A", "MN"), // MONGOLIAN A
    "112780" -> IbisKieliOppiaine("A", "NE"), // NEPALI A
    "100421" -> IbisKieliOppiaine("A", "NO"), // NORWEGIAN A
    "112923" -> IbisKieliOppiaine("A", "PS"), // PASHTO A
    "100446" -> IbisKieliOppiaine("A", "FA"), // PERSIAN A
    "100459" -> IbisKieliOppiaine("A", "PL"), // POLISH A
    "100469" -> IbisKieliOppiaine("A", "PT"), // PORTUGUESE A
    "112919" -> IbisKieliOppiaine("A", "PA"), // PUNJABI A
    "100481" -> IbisKieliOppiaine("A", "RO"), // ROMANIAN A
    "100489" -> IbisKieliOppiaine("A", "RU"), // RUSSIAN A
    "112789" -> IbisKieliOppiaine("A", "SR"), // SERBIAN A
    "112781" -> IbisKieliOppiaine("A", "ST"), // SESOTHO A
    "177712" -> IbisKieliOppiaine("A", "SI"), // SINHALA A
    "112795" -> IbisKieliOppiaine("A", "SK"), // SLOVAK A
    "112785" -> IbisKieliOppiaine("A", "SL"), // SLOVENE A
    "112849" -> IbisKieliOppiaine("A", "SO"), // SOMALI A
    "100539" -> IbisKieliOppiaine("A", "ES"), // SPANISH A
    "100556" -> IbisKieliOppiaine("A", "SV"), // SWEDISH A
    "112926" -> IbisKieliOppiaine("A", "TA"), // TAMIL A
    "134714" -> IbisKieliOppiaine("A", "TE"), // TELUGU A
    "100570" -> IbisKieliOppiaine("A", "TH"), // THAI A
    "103711" -> IbisKieliOppiaine("A", "TR"), // TURKISH A
    "112928" -> IbisKieliOppiaine("A", "UK"), // UKRAINIAN A
    "112906" -> IbisKieliOppiaine("A", "UR"), // URDU A
    "100605" -> IbisKieliOppiaine("A", "VI"), // VIETNAMESE A

    // Language B
    "100053" -> IbisKieliOppiaine("B", "AR"), // ARABIC B
    "100124" -> IbisKieliOppiaine("B", "ZH"), // CHINESE B
    "117712" -> IbisKieliOppiaine("B", "YUE"), // CHINESE B - CANTONESE
    "117711" -> IbisKieliOppiaine("B", "CMN"), // CHINESE B - MANDARIN
    "100144" -> IbisKieliOppiaine("B", "DA"), // DANISH B
    "100157" -> IbisKieliOppiaine("B", "NL"), // DUTCH B
    "100179" -> IbisKieliOppiaine("B", "EN"), // ENGLISH B
    "100204" -> IbisKieliOppiaine("B", "FI"), // FINNISH B
    "100214" -> IbisKieliOppiaine("B", "FR"), // FRENCH B
    "100231" -> IbisKieliOppiaine("B", "DE"), // GERMAN B
    "100246" -> IbisKieliOppiaine("B", "HE"), // HEBREW B
    "100252" -> IbisKieliOppiaine("B", "HI"), // HINDI B
    "100284" -> IbisKieliOppiaine("B", "ID"), // INDONESIAN B
    "100299" -> IbisKieliOppiaine("B", "IT"), // ITALIAN B
    "100308" -> IbisKieliOppiaine("B", "JA"), // JAPANESE B
    "178712" -> IbisKieliOppiaine("B", "SW"), // KISWAHILI B
    "100334" -> IbisKieliOppiaine("B", "KO"), // KOREAN B
    "100424" -> IbisKieliOppiaine("B", "NO"), // NORWEGIAN B
    "100472" -> IbisKieliOppiaine("B", "PT"), // PORTUGUESE B
    "100493" -> IbisKieliOppiaine("B", "RU"), // RUSSIAN B
    "100543" -> IbisKieliOppiaine("B", "ES"), // SPANISH B
    "100559" -> IbisKieliOppiaine("B", "SV"), // SWEDISH B

    // Language ab initio
    "100052" -> IbisKieliOppiaine("AB", "AR"), // ARABIC AB.
    "162711" -> IbisKieliOppiaine("AB", "DA"), // DANISH AB.
    "167711" -> IbisKieliOppiaine("AB", "NL"), // DUTCH AB.
    "100704" -> IbisKieliOppiaine("AB", "EN"), // ENGLISH AB.
    "100213" -> IbisKieliOppiaine("AB", "FR"), // FRENCH AB.
    "100230" -> IbisKieliOppiaine("AB", "DE"), // GERMAN AB.
    "100298" -> IbisKieliOppiaine("AB", "IT"), // ITALIAN AB.
    "100307" -> IbisKieliOppiaine("AB", "JA"), // JAPANESE AB.
    "178714" -> IbisKieliOppiaine("AB", "SW"), // KISWAHILI AB.
    "100375" -> IbisKieliOppiaine("AB", "CMN"), // MANDARIN AB.
    "100492" -> IbisKieliOppiaine("AB", "RU"), // RUSSIAN AB.
    "100542" -> IbisKieliOppiaine("AB", "ES"), // SPANISH AB.
    "162712" -> IbisKieliOppiaine("AB", "SV"), // SWEDISH AB.

    // Classical languages
    "100340" -> IbisKieliOppiaine("CLA", "LA"), // LATIN

    // Muut oppiaineet
    "100088" -> IbisMuuOppiaine("BIO"), // BIOLOGY
    "147712" -> IbisMuuOppiaine("BU"), // BUSINESS MANAGEMENT
    "100113" -> IbisMuuOppiaine("CHE"), // CHEMISTRY
    "100132" -> IbisMuuOppiaine("CS"), // COMPUTER SC.
    "100140" -> IbisMuuOppiaine("DAN"), // DANCE
    "100146" -> IbisMuuOppiaine("DES"), // DESIGN TECH.
    "179711" -> IbisMuuOppiaine("DIS"), // DIGITAL SOC.
    "100164" -> IbisMuuOppiaine("ECO"), // ECONOMICS
    "100673" -> IbisMuuOppiaine("ESS"), // ENVIRONMENTAL SYSTEMS AND SOCIETIES
    "100200" -> IbisMuuOppiaine("FIL"), // FILM
    "100222" -> IbisMuuOppiaine("GEO"), // GEOGRAPHY
    "123711" -> IbisMuuOppiaine("POL"), // GLOBAL POLITICS
    "165711" -> IbisMuuOppiaine("HIS"), // HIST. AFRICA AND MID. EAST
    "100680" -> IbisMuuOppiaine("HIS"), // HISTORY
    "149713" -> IbisMuuOppiaine("HIS"), // HISTORY AFRICA AND MIDDLE EAST
    "149711" -> IbisMuuOppiaine("HIS"), // HISTORY AMERICAS
    "149714" -> IbisMuuOppiaine("HIS"), // HISTORY ASIA AND OCEANIA
    "149712" -> IbisMuuOppiaine("HIS"), // HISTORY EUROPE
    "112845" -> IbisMuuOppiaine("LIT"), // LITERATURE AND PERFORMANCE
    "100386" -> IbisMuuOppiaine("MAT"), // MATHEMATICS
    "166711" -> IbisMuuOppiaine("MATAA"), // MATHEMATICS ANALYSIS AND APPROACHES
    "166712" -> IbisMuuOppiaine("MATAI"), // MATHEMATICS APPLICATIONS AND INTERP
    "100402" -> IbisMuuOppiaine("MUS"), // MUSIC
    "100449" -> IbisMuuOppiaine("PHI"), // PHILOSOPHY
    "100452" -> IbisMuuOppiaine("PHY"), // PHYSICS
    "100474" -> IbisMuuOppiaine("PSY"), // PSYCHOLOGY
    "100532" -> IbisMuuOppiaine("SOC"), // SOC.CUL.ANTH.
    "100546" -> IbisMuuOppiaine("SPO"), // SPORTS EX SCI
    "100575" -> IbisMuuOppiaine("THE"), // THEATRE
    "100608" -> IbisMuuOppiaine("VA"), // VISUAL ARTS
    "126711" -> IbisMuuOppiaine("WS"), // WLD. STUDIES CONF PEACE AND SECURE
    "126712" -> IbisMuuOppiaine("WS"), // WLD. STUDIES CULT LANG AND IDENT
    "159711" -> IbisMuuOppiaine("WS"), // WLD. STUDIES ENV/ECO SUSTAINABILTY
    "126714" -> IbisMuuOppiaine("WS"), // WLD. STUDIES EQUALITY AND INEQUALITY
    "126715" -> IbisMuuOppiaine("WS"), // WLD. STUDIES HEALTH AND DEV
    "126716" -> IbisMuuOppiaine("WS"), // WLD. STUDIES SCI TECH AND SOC
    "100619" -> IbisMuuOppiaine("REL"), // WORLD RELIG.
  )
}
