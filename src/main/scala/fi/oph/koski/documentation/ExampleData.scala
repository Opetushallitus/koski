package fi.oph.koski.documentation
import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.YleissivistavakoulutusExampleData._
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._

object ExampleData {
  lazy val longTimeAgo = date(2000, 1, 1)
  lazy val suomenKieli = Some(Koodistokoodiviite("FI", Some("suomi"), "kieli", None))
  lazy val ruotsinKieli = Some(Koodistokoodiviite("SV", Some("ruotsi"), "kieli", None))
  lazy val sloveeni = Some(Koodistokoodiviite("SL", Some("sloveeni"), "kieli", None))
  val opiskeluoikeusLäsnä = Koodistokoodiviite("lasna", Some("Läsnä"), "koskiopiskeluoikeudentila", Some(1))
  val opiskeluoikeusValmistunut = Koodistokoodiviite("valmistunut", Some("Valmistunut"), "koskiopiskeluoikeudentila", Some(1))
  val opiskeluoikeusEronnut = Koodistokoodiviite("eronnut", Some("Eronnut"), "koskiopiskeluoikeudentila", Some(1))
  lazy val tilaKesken = Koodistokoodiviite("KESKEN", "suorituksentila")
  lazy val tilaValmis: Koodistokoodiviite = Koodistokoodiviite(koodistoUri = "suorituksentila", koodiarvo = "VALMIS")
  lazy val tilaKeskeytynyt: Koodistokoodiviite = Koodistokoodiviite(koodistoUri = "suorituksentila", koodiarvo = "KESKEYTYNYT")
  val helsinki = Koodistokoodiviite(koodistoUri = "kunta", koodiarvo = "091", nimi = Some("Helsinki"))
  val jyväskylä = Koodistokoodiviite(koodistoUri = "kunta", koodiarvo = "179", nimi = Some("Jyväskylä"))
  val suomi = Koodistokoodiviite(koodistoUri = "maatjavaltiot2", koodiarvo = "246", nimi = Some("Suomi"))
  val ruotsi = Koodistokoodiviite(koodistoUri = "maatjavaltiot2", koodiarvo = "752", nimi = Some("Ruotsi"))
  def vahvistus(päivä: LocalDate = date(2016, 6, 4), org: OrganisaatioWithOid = jyväskylänNormaalikoulu) =
    Some(HenkilövahvistusIlmanPaikkakuntaa(päivä = päivä, myöntäjäOrganisaatio = org, myöntäjäHenkilöt = List(Organisaatiohenkilö("Reijo Reksi", "rehtori", org))))
  def vahvistusPaikkakunnalla(päivä: LocalDate = date(2016, 6, 4), org: OrganisaatioWithOid = jyväskylänNormaalikoulu, kunta: Koodistokoodiviite = jyväskylä) =
    Some(HenkilövahvistusPaikkakunnalla(päivä = päivä, kunta, myöntäjäOrganisaatio = org, myöntäjäHenkilöt = List(Organisaatiohenkilö("Reijo Reksi", "rehtori", org))))

  def vahvistusValinnaisellaTittelillä(päivä: LocalDate = date(2016, 6, 4), org: OrganisaatioWithOid = jyväskylänNormaalikoulu, titteli: Option[LocalizedString] = Some("rehtori")) =
    Some(
      HenkilövahvistusValinnaisellaTittelilläJaIlmanPaikkakuntaa(
        päivä = päivä,
        myöntäjäOrganisaatio = org,
        myöntäjäHenkilöt = List(OrganisaatiohenkilöValinnaisellaTittelillä("Reijo Reksi", titteli,  org))
      )
    )

  def vahvistusPaikkakunnallaJaValinnaisellaTittelillä(päivä: LocalDate = date(2016, 6, 4), org: OrganisaatioWithOid = jyväskylänNormaalikoulu, kunta: Koodistokoodiviite = jyväskylä,  titteli: Option[LocalizedString] = Some("rehtori")) =
    Some(
      HenkilövahvistusValinnaisellaTittelilläJaPaikkakunnalla(
        päivä = päivä,
        kunta,
        myöntäjäOrganisaatio = org,
        myöntäjäHenkilöt = List(OrganisaatiohenkilöValinnaisellaTittelillä("Reijo Reksi", titteli, org))
      )
    )
}
