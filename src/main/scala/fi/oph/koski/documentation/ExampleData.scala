package fi.oph.koski.documentation
import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.YleissivistavakoulutusExampleData._
import fi.oph.koski.koodisto.MockKoodistoViitePalvelu
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._

object ExampleData {
  lazy val longTimeAgo = date(2000, 1, 1)
  lazy val suomenKieli = Koodistokoodiviite("FI", Some("suomi"), "kieli", None)
  lazy val ruotsinKieli = Koodistokoodiviite("SV", Some("ruotsi"), "kieli", None)
  lazy val englanti = Koodistokoodiviite("EN", Some("englanti"), "kieli", None)
  lazy val sloveeni = Koodistokoodiviite("SL", Some("sloveeni"), "kieli", None)
  val opiskeluoikeusLäsnä = Koodistokoodiviite("lasna", Some("Läsnä"), "koskiopiskeluoikeudentila", Some(1))
  val opiskeluoikeusValmistunut = Koodistokoodiviite("valmistunut", Some("Valmistunut"), "koskiopiskeluoikeudentila", Some(1))
  val opiskeluoikeusEronnut = Koodistokoodiviite("eronnut", Some("Eronnut"), "koskiopiskeluoikeudentila", Some(1))
  val opiskeluoikeusKatsotaanEronneeksi = Koodistokoodiviite("katsotaaneronneeksi", Some("Katsotaan eronneekksi"), "koskiopiskeluoikeudentila", Some(1))
  val opiskeluoikeusPeruutettu = Koodistokoodiviite("peruutettu", Some("Peruutettu"), "koskiopiskeluoikeudentila", Some(1))
  val opiskeluoikeusValiaikaisestiKeskeytynyt = Koodistokoodiviite("valiaikaisestikeskeytynyt", Some("eronnut"), "koskiopiskeluoikeudentila", Some(1))
  val opiskeluoikeusMitätöity = Koodistokoodiviite("mitatoity", Some("Mitätöity"), "koskiopiskeluoikeudentila", Some(1))
  val opiskeluoikeusLoma = Koodistokoodiviite("loma", Some("Loma"), "koskiopiskeluoikeudentila", Some(1))
  val helsinki = Koodistokoodiviite(koodistoUri = "kunta", koodiarvo = "091", nimi = Some("Helsinki"))
  val jyväskylä = Koodistokoodiviite(koodistoUri = "kunta", koodiarvo = "179", nimi = Some("Jyväskylä"))
  val suomi = Koodistokoodiviite(koodistoUri = "maatjavaltiot2", koodiarvo = "246", nimi = Some("Suomi"))
  val ruotsi = Koodistokoodiviite(koodistoUri = "maatjavaltiot2", koodiarvo = "752", nimi = Some("Ruotsi"))
  def perusopetuksenLuokkaAste(aste: String) = Some(Koodistokoodiviite(aste, "perusopetuksenluokkaaste"))
  def vahvistus(päivä: LocalDate = date(2016, 6, 4), org: Organisaatio = jyväskylänNormaalikoulu, paikkakunta: Option[Koodistokoodiviite] = None) =
    Some(HenkilövahvistusValinnaisellaPaikkakunnalla(päivä = päivä, myöntäjäOrganisaatio = org, paikkakunta = paikkakunta, myöntäjäHenkilöt = List(Organisaatiohenkilö("Reijo Reksi", "rehtori", org))))
  def vahvistusPaikkakunnalla(päivä: LocalDate = date(2016, 6, 4), org: Organisaatio = jyväskylänNormaalikoulu, kunta: Koodistokoodiviite = jyväskylä) =
    Some(HenkilövahvistusPaikkakunnalla(päivä = päivä, kunta, myöntäjäOrganisaatio = org, myöntäjäHenkilöt = List(Organisaatiohenkilö("Reijo Reksi", "rehtori", org))))

  lazy val laajuusOpintoviikoissa: Koodistokoodiviite = MockKoodistoViitePalvelu.validateRequired(Koodistokoodiviite(koodiarvo = "1", koodistoUri = "opintojenlaajuusyksikko", nimi = Some("opintoviikkoa")))
  lazy val laajuusOpintopisteissä: Koodistokoodiviite = MockKoodistoViitePalvelu.validateRequired(Koodistokoodiviite(koodiarvo = "2", koodistoUri = "opintojenlaajuusyksikko", nimi = Some("opintopistettä")))
  lazy val laajuusVuosiviikkotunneissa: Koodistokoodiviite = MockKoodistoViitePalvelu.validateRequired(Koodistokoodiviite(koodiarvo = "3", koodistoUri = "opintojenlaajuusyksikko", nimi = Some("vuosiviikkotuntia")))
  lazy val laajuusKursseissa: Koodistokoodiviite = MockKoodistoViitePalvelu.validateRequired(Koodistokoodiviite(koodiarvo = "4", koodistoUri = "opintojenlaajuusyksikko", nimi = Some("kurssia")))
  lazy val laajuusTunneissa: Koodistokoodiviite = MockKoodistoViitePalvelu.validateRequired(Koodistokoodiviite(koodiarvo = "5", koodistoUri = "opintojenlaajuusyksikko", nimi = Some("tuntia")))
  lazy val laajuusOsaamispisteissä: Koodistokoodiviite = MockKoodistoViitePalvelu.validateRequired(Koodistokoodiviite(koodiarvo = "6", koodistoUri = "opintojenlaajuusyksikko", nimi = Some("osaamispistettä")))
  lazy val laajuusViikoissa: Koodistokoodiviite = MockKoodistoViitePalvelu.validateRequired(Koodistokoodiviite(koodiarvo = "8", koodistoUri = "opintojenlaajuusyksikko", nimi = Some("viikkoa"), lyhytNimi = Some("vk"), koodistoVersio = None))

  lazy val valtionosuusRahoitteinen: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "1", koodistoUri = "opintojenrahoitus")
  lazy val muutaKauttaRahoitettu: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "6", koodistoUri = "opintojenrahoitus")
  lazy val jatkuvanOppimisenRahoitus: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "14", koodistoUri = "opintojenrahoitus")

  def vahvistusValinnaisellaTittelillä(päivä: LocalDate = date(2016, 6, 4), org: Organisaatio = jyväskylänNormaalikoulu, titteli: Option[LocalizedString] = Some("rehtori")) =
    Some(
      HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla(
        päivä = päivä,
        myöntäjäOrganisaatio = org,
        myöntäjäHenkilöt = List(OrganisaatiohenkilöValinnaisellaTittelillä("Reijo Reksi", titteli,  org))
      )
    )

  def vahvistusPaikkakunnallaJaValinnaisellaTittelillä(päivä: LocalDate = date(2016, 6, 4), org: Organisaatio = jyväskylänNormaalikoulu, kunta: Koodistokoodiviite = jyväskylä,  titteli: Option[LocalizedString] = Some("rehtori")) =
    Some(
      HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla(
        päivä = päivä,
        Some(kunta),
        myöntäjäOrganisaatio = org,
        myöntäjäHenkilöt = List(OrganisaatiohenkilöValinnaisellaTittelillä("Reijo Reksi", titteli, org))
      )
    )
}
