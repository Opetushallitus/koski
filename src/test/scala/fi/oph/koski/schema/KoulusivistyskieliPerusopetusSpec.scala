package fi.oph.koski.schema

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.misc.OpiskeluoikeusTestMethodsPerusopetus
import fi.oph.koski.documentation.PerusopetusExampleData._
import org.scalatest.freespec.AnyFreeSpec

class KoulusivistyskieliPerusopetusSpec extends AnyFreeSpec with KoskiHttpSpec with OpiskeluoikeusTestMethodsPerusopetus {

  "Koulusivistyskieli - Nuorten perusopetuksen oppimäärän suoritus" - {

    "Koulusivistyskieliä voi olla kaksi jos äidinkieli ja kirjallisuus suoritettu suomen ja ruotsin keilellä" in {
      val opiskeluoikeus = oppimääränOpiskeluoikeusOppiaineilla(
        äidinkieliJaKirjallisuus("AI1", "5"),
        äidinkieliJaKirjallisuus("AI2", "5")
      )

      verifyKoulusivistyskielet(opiskeluoikeus, Some(List(
        Koodistokoodiviite("FI", "kieli"),
        Koodistokoodiviite("SV", "kieli")
      )))
    }

    "Hyväksytty suoritus valitaan koulusivistyskieleksi" in {
      val opiskeluoikeus = oppimääränOpiskeluoikeusOppiaineilla(
        äidinkieliJaKirjallisuus("AI1", "10")
      )
      verifyKoulusivistyskielet(opiskeluoikeus, Some(List(Koodistokoodiviite("FI", "kieli"))))
    }

    "Hylättyä suoritusta ei valita koulusivistuskieleksi" in {
      val opiskeluoikeus: PerusopetuksenOpiskeluoikeus = oppimääränOpiskeluoikeusOppiaineilla(
        äidinkieliJaKirjallisuus("AI1", "4")
      )
      verifyKoulusivistyskielet(opiskeluoikeus, None)
    }

    "Valinnaista suoritusta ei valita koulusivistyskieleksi" in {
      val opiskeluoikeus = oppimääränOpiskeluoikeusOppiaineilla(
        äidinkieliJaKirjallisuus("AI1", "7", pakollinen = false)
      )
      verifyKoulusivistyskielet(opiskeluoikeus, None)
    }
  }

  def verifyKoulusivistyskielet(opiskeluoikeus: PerusopetuksenOpiskeluoikeus, expected: Option[List[Koodistokoodiviite]]) = {
    setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
      verifyResponseStatusOk()
      val koulusivistyskielet = lastOpiskeluoikeusByHetu(defaultHenkilö).suoritukset.collect {
        case x: NuortenPerusopetuksenOppimääränSuoritus => x.koulusivistyskieli
      }
      koulusivistyskielet.length should equal(1)
      koulusivistyskielet.head should equal(expected)
    }
  }

  def äidinkieliJaKirjallisuus(kieli: String, arvosana: String, pakollinen: Boolean = true) = suoritus(äidinkieli(kieli).copy(pakollinen = pakollinen)).copy(arviointi = arviointi(arvosana, kuvaus = None))

  def oppimääränOpiskeluoikeusOppiaineilla(oppiaineet: OppiaineenTaiToiminta_AlueenSuoritus*) = defaultOpiskeluoikeus.copy(
    suoritukset = List(
      yhdeksännenLuokanSuoritus,
      perusopetuksenOppimääränSuoritus.copy(
      osasuoritukset = Some(oppiaineet.toList)
    ))
  )
}
