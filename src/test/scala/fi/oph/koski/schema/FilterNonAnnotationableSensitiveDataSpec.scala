package fi.oph.koski.schema

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.AmmatillinenExampleData.{autoalanPerustutkinto, lisätietoOsaamistavoitteet, stadinAmmattiopisto, stadinToimipiste}
import fi.oph.koski.documentation.ExampleData.suomenKieli
import fi.oph.koski.json.SensitiveDataAllowed
import fi.oph.koski.koskiuser.Rooli.Role
import fi.oph.koski.localization.LocalizedStringImplicits._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

// Manner-Suomen ammatillinen suodatus testataan fikstuuridatalla SensitiveAndRedundantDataFilterSpecissä.
// Ahvenanmaan ammatillista ei voi vielä tallentaa, joten suodatin testataan tässä suoraan.
class FilterNonAnnotationableSensitiveDataSpec extends AnyFreeSpec with Matchers {
  private val muuLisätieto = AmmatillisenTutkinnonOsanLisätieto(Koodistokoodiviite("muu", "ammatillisentutkinnonosanlisatieto"), "Muu lisätieto")
  private val lisätiedot = Some(List(lisätietoOsaamistavoitteet, muuLisätieto))

  private val osaAlue = YhteisenTutkinnonOsanOsaAlueenSuoritus(
    koulutusmoduuli = ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue(Koodistokoodiviite("MA", "ammatillisenoppiaineet"), pakollinen = true, None),
    lisätiedot = lisätiedot
  )

  private val ahvenanmaanAmmatillinen = AhvenanmaanAmmatillinenOpiskeluoikeus(
    oppilaitos = Some(stadinAmmattiopisto),
    tila = AhvenanmaanAmmatillinenOpiskeluoikeudenTila(List(
      AhvenanmaanAmmatillinenOpiskeluoikeusjakso(date(2025, 8, 1), Koodistokoodiviite("lasna", "koskiopiskeluoikeudentila"))
    )),
    suoritukset = List(AhvenanmaanAmmatillisenTutkinnonSuoritus(
      koulutusmoduuli = autoalanPerustutkinto,
      toimipiste = stadinToimipiste,
      suorituskieli = suomenKieli,
      osasuoritukset = Some(List(
        MuunAmmatillisenTutkinnonOsanSuoritus(
          koulutusmoduuli = MuuValtakunnallinenTutkinnonOsa(Koodistokoodiviite("100832", "tutkinnonosat"), pakollinen = true, None),
          toimipiste = None,
          lisätiedot = lisätiedot,
          osasuoritukset = Some(List(AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus(
            koulutusmoduuli = AmmatillisenTutkinnonOsaaPienempiKokonaisuus(PaikallinenKoodi("P1", "Pienempi kokonaisuus"), "Kuvaus"),
            lisätiedot = lisätiedot
          )))
        ),
        YhteisenAmmatillisenTutkinnonOsanSuoritus(
          koulutusmoduuli = YhteinenTutkinnonOsa(Koodistokoodiviite("106727", "tutkinnonosat"), pakollinen = true, None),
          toimipiste = None,
          lisätiedot = lisätiedot,
          osasuoritukset = Some(List(osaAlue))
        ),
        AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus(
          koulutusmoduuli = JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa(),
          osasuoritukset = Some(List(osaAlue))
        )
      ))
    ))
  )

  private def käyttäjä(luottamuksellisetSallittu: Boolean): SensitiveDataAllowed = new SensitiveDataAllowed {
    def sensitiveDataAllowed(allowedRoles: Set[Role]): Boolean = luottamuksellisetSallittu
  }

  private def lisätietojenKoodiarvot(oo: KoskeenTallennettavaOpiskeluoikeus): List[List[String]] = {
    def osasuorituksesta(s: Suoritus): List[List[String]] = (s match {
      case l: AmmatillisenTutkinnonOsanLisätiedollinen => List(l.lisätiedot.toList.flatten.map(_.tunniste.koodiarvo))
      case _ => Nil
    }) ++ s.osasuoritusLista.flatMap(osasuorituksesta)
    oo.suoritukset.flatMap(_.osasuoritusLista).flatMap(osasuorituksesta)
  }

  "Ahvenanmaan ammatillinen" - {
    "Mukautetun arvioinnin lisätiedot piilotetaan kaikilta osasuoritustasoilta ilman luottamuksellisia oikeuksia" in {
      val suodatettu = FilterNonAnnotationableSensitiveData.filter(ahvenanmaanAmmatillinen)(käyttäjä(luottamuksellisetSallittu = false))
      lisätietojenKoodiarvot(suodatettu) should equal(List(
        List("muu"), // muun tutkinnon osan suoritus
        List("muu"), // tutkinnon osaa pienempi kokonaisuus
        List("muu"), // yhteisen tutkinnon osan suoritus
        List("muu"), // osa-alue
        Nil,         // jatko-opintovalmiuksia tukevat opinnot (ei lisätietoja)
        List("muu")  // osa-alue
      ))
    }

    "Luottamuksellisilla oikeuksilla lisätiedot säilyvät" in {
      FilterNonAnnotationableSensitiveData.filter(ahvenanmaanAmmatillinen)(käyttäjä(luottamuksellisetSallittu = true)) should equal(ahvenanmaanAmmatillinen)
    }
  }
}
