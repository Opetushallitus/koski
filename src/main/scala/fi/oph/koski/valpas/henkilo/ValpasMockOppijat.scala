package fi.oph.koski.valpas.henkilo

import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.schema.UusiHenkilö

object ValpasMockOppijat {
  private val valpasOppijat = new MockOppijat

  val oppivelvollinenYsiluokkaKeskenKeväällä2021 = valpasOppijat.oppija("Oppivelvollinen-ysiluokka-kesken-keväällä-2021", "Valpas", "221105A3023")
  val eiOppivelvollinenSyntynytEnnen2004 = valpasOppijat.oppija("Ei-oppivelvollinen-syntynyt-ennen-2004", "Valpas", "210303A707J")
  val päällekkäisiäOppivelvollisuuksia = valpasOppijat.oppija("Päällekkäisiä", "Oppivelvollisuuksia", "060605A083N")
  val lukioOpiskelija = valpasOppijat.oppija("Lukio-opiskelija", "Valpas", "070504A717P")
  val kasiluokkaKeskenKeväällä2021 = valpasOppijat.oppija("Kasiluokka-kesken-keväällä-2021", "Valpas", "191106A1384")
  val kotiopetusMeneilläänOppija = valpasOppijat.oppija("Kotiopetus-meneillä", "Valpas", "210905A2151")
  val kotiopetusMenneisyydessäOppija = valpasOppijat.oppija("Kotiopetus-menneisyydessä", "Valpas", "060205A8805")
  val eronnutOppija = valpasOppijat.oppija("Eroaja", "Valpas", "240905A0078")
  val luokalleJäänytYsiluokkalainen = valpasOppijat.oppija("LuokallejäänytYsiluokkalainen", "Valpas", "020805A5625")
  val luokallejäänytYsiluokkalainenJollaUusiYsiluokka = valpasOppijat.oppija("LuokallejäänytYsiluokkalainenJatkaa", "Valpas", "060205A7222")
  val valmistunutYsiluokkalainen = valpasOppijat.oppija("Ysiluokka-valmis-keväällä-2021", "Valpas", "190605A006K")
  val luokalleJäänytYsiluokkalainenVaihtanutKoulua = valpasOppijat.oppija("LuokallejäänytYsiluokkalainenKouluvaihto", "Valpas", "050605A7684")

  def defaultOppijat = valpasOppijat.getOppijat
}
