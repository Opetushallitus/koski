package fi.oph.koski.editor

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.schema._
import fi.oph.koski.servlet.InvalidRequestException

case class NuortenPerusopetusPakollisetOppiaineet(koodistoViitePalvelu: KoodistoViitePalvelu) {
  lazy val toimintaAlueidenSuoritukset = {
    (1 to 5).map(n => PerusopetuksenToiminta_Alue(koodi("perusopetuksentoimintaalue", n.toString))).map(ta => PerusopetuksenToiminta_AlueenSuoritus(ta)).toList
  }
  private def koodi(koodisto: String, arvo: String) = koodistoViitePalvelu.validateRequired(koodisto, arvo)
  private def aine(koodiarvo: String) = koodi("koskioppiaineetyleissivistava", koodiarvo)
  private def nuortenSuoritus(aine: NuortenPerusopetuksenOppiaine) = NuortenPerusopetuksenOppiaineenSuoritus(koulutusmoduuli = aine)
  private def aikuistenSuoritus(aine: AikuistenPerusopetuksenOppiaine) = AikuistenPerusopetuksenOppiaineenSuoritus(koulutusmoduuli = aine)
  def päättötodistuksenSuoritukset(suorituksenTyyppi: String, toimintaAlueittain: Boolean) = {
    suorituksenTyyppi match {
      case "perusopetuksenoppimaara" =>
        if (toimintaAlueittain) {
          toimintaAlueidenSuoritukset
        } else {
          päättötodistuksenOppiaineet.map(nuortenSuoritus)
        }
      case "aikuistenperusopetuksenoppimaara" =>
        aikuistenPäättötodistuksenOppiaineet.map(aikuistenSuoritus)
      case _ => Nil
    }
  }
  def pakollistenOppiaineidenTaiToimintaAlueidenSuoritukset(luokkaAste: Int, toimintaAlueittain: Boolean) = {
    if (toimintaAlueittain) {
      toimintaAlueidenSuoritukset
    } else if (luokkaAste >= 1 && luokkaAste <= 2) {
      luokkaAsteiden1_2Oppiaineet.map(nuortenSuoritus)
    } else if (luokkaAste >= 3 && luokkaAste <= 6) {
      luokkaAsteiden3_6Oppiaineet.map(nuortenSuoritus)
    } else if (luokkaAste <= 9) {
      päättötodistuksenSuoritukset("perusopetuksenoppimaara", toimintaAlueittain)
    } else {
      throw new InvalidRequestException(KoskiErrorCategory.badRequest.queryParam("Tuntematon luokka-aste: " + luokkaAste))
    }
  }

  private lazy val luokkaAsteiden3_6Oppiaineet = List(
        NuortenPerusopetuksenÄidinkieliJaKirjallisuus(tunniste = aine("AI"), kieli = koodi("oppiaineaidinkielijakirjallisuus", "AI1")),
        NuortenPerusopetuksenVierasTaiToinenKotimainenKieli(tunniste = aine("A1"), kieli = koodi("kielivalikoima", "EN")),
        MuuNuortenPerusopetuksenOppiaine(aine("MA")),
    MuuNuortenPerusopetuksenOppiaine(aine("YL")),
        MuuNuortenPerusopetuksenOppiaine(aine("KT")),
        MuuNuortenPerusopetuksenOppiaine(aine("HI")),
        MuuNuortenPerusopetuksenOppiaine(aine("YH")),
        MuuNuortenPerusopetuksenOppiaine(aine("MU")),
        MuuNuortenPerusopetuksenOppiaine(aine("KU")),
        MuuNuortenPerusopetuksenOppiaine(aine("KS")),
        MuuNuortenPerusopetuksenOppiaine(aine("LI")),
        MuuNuortenPerusopetuksenOppiaine(aine("OP"))
  )

  private lazy val päättötodistuksenOppiaineet = List(
        NuortenPerusopetuksenÄidinkieliJaKirjallisuus(tunniste = aine("AI"), kieli = koodi("oppiaineaidinkielijakirjallisuus", "AI1")),
    NuortenPerusopetuksenVierasTaiToinenKotimainenKieli(tunniste = aine("A1"), kieli = koodi("kielivalikoima", "EN")),
    NuortenPerusopetuksenVierasTaiToinenKotimainenKieli(tunniste = aine("B1"), kieli = koodi("kielivalikoima", "SV")),
        MuuNuortenPerusopetuksenOppiaine(aine("MA")),
    MuuNuortenPerusopetuksenOppiaine(aine("BI")),
    MuuNuortenPerusopetuksenOppiaine(aine("GE")),
    MuuNuortenPerusopetuksenOppiaine(aine("FY")),
    MuuNuortenPerusopetuksenOppiaine(aine("KE")),
    MuuNuortenPerusopetuksenOppiaine(aine("TE")),
        MuuNuortenPerusopetuksenOppiaine(aine("KT")),
    MuuNuortenPerusopetuksenOppiaine(aine("HI")),
    MuuNuortenPerusopetuksenOppiaine(aine("YH")),
        MuuNuortenPerusopetuksenOppiaine(aine("MU")),
        MuuNuortenPerusopetuksenOppiaine(aine("KU")),
        MuuNuortenPerusopetuksenOppiaine(aine("KS")),
        MuuNuortenPerusopetuksenOppiaine(aine("LI")),
    MuuNuortenPerusopetuksenOppiaine(aine("KO")),
        MuuNuortenPerusopetuksenOppiaine(aine("OP"))
  )

  private lazy val aikuistenPäättötodistuksenOppiaineet = List(
    AikuistenPerusopetuksenÄidinkieliJaKirjallisuus(tunniste = aine("AI"), kieli = koodi("oppiaineaidinkielijakirjallisuus", "AI1")),
    AikuistenPerusopetuksenVierasTaiToinenKotimainenKieli(tunniste = aine("A1"), kieli = koodi("kielivalikoima", "EN")),
    AikuistenPerusopetuksenVierasTaiToinenKotimainenKieli(tunniste = aine("B1"), kieli = koodi("kielivalikoima", "SV")),
    MuuAikuistenPerusopetuksenOppiaine(aine("MA")),
    MuuAikuistenPerusopetuksenOppiaine(aine("BI")),
    MuuAikuistenPerusopetuksenOppiaine(aine("GE")),
    MuuAikuistenPerusopetuksenOppiaine(aine("FY")),
    MuuAikuistenPerusopetuksenOppiaine(aine("KE")),
    MuuAikuistenPerusopetuksenOppiaine(aine("TE")),
    MuuAikuistenPerusopetuksenOppiaine(aine("KT")),
    MuuAikuistenPerusopetuksenOppiaine(aine("HI")),
    MuuAikuistenPerusopetuksenOppiaine(aine("YH")),
    MuuAikuistenPerusopetuksenOppiaine(aine("MU")),
    MuuAikuistenPerusopetuksenOppiaine(aine("KU")),
    MuuAikuistenPerusopetuksenOppiaine(aine("KS")),
    MuuAikuistenPerusopetuksenOppiaine(aine("LI")),
    MuuAikuistenPerusopetuksenOppiaine(aine("KO")),
    MuuAikuistenPerusopetuksenOppiaine(aine("OP"))
  )

  private lazy val luokkaAsteiden1_2Oppiaineet = List(
        NuortenPerusopetuksenÄidinkieliJaKirjallisuus(tunniste = aine("AI"), kieli = koodi("oppiaineaidinkielijakirjallisuus", "AI1")),
        MuuNuortenPerusopetuksenOppiaine(aine("MA")),
        MuuNuortenPerusopetuksenOppiaine(aine("YL")),
        MuuNuortenPerusopetuksenOppiaine(aine("KT")),
        MuuNuortenPerusopetuksenOppiaine(aine("MU")),
        MuuNuortenPerusopetuksenOppiaine(aine("KU")),
        MuuNuortenPerusopetuksenOppiaine(aine("KS")),
        MuuNuortenPerusopetuksenOppiaine(aine("LI")),
        MuuNuortenPerusopetuksenOppiaine(aine("OP"))
  )
}
