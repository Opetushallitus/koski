package fi.oph.koski.valpas.henkilo

import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.schema.UusiHenkilö

object ValpasMockOppijat {
  private val valpasOppijat = new MockOppijat

  val oppivelvollinenYsiluokkaKeskenKeväällä2021 = valpasOppijat.oppija("Oppivelvollinen-ysiluokka-kesken-keväällä-2021", "Valpas", "221105A3023")
  val eiOppivelvollinenSyntynytEnnen2004 = valpasOppijat.oppija("Ei-oppivelvollinen-syntynyt-ennen-2004", "Valpas", "210303A707J")
  val päällekkäisiäOppivelvollisuuksia = valpasOppijat.oppija("Päällekkäisiä", "Oppivelvollisuuksia", "060605A083N")

  def defaultOppijat = valpasOppijat.getOppijat
}
