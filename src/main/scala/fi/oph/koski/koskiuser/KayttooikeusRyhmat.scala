package fi.oph.koski.koskiuser

import com.typesafe.config.Config
import fi.oph.koski.cache.Cached
import fi.oph.koski.henkilo.{AuthenticationServiceClient, UusiKäyttöoikeusryhmä}
import fi.oph.koski.koodisto.{KoodistoKoodi, KoodistoPalvelu, KoodistoViite}
import fi.oph.koski.koskiuser.AccessType.{read, write}
import fi.oph.koski.schema.Organisaatio

object Käyttöoikeusryhmät {
  type OrganisaatioKäyttöoikeusryhmä = (Organisaatio.Oid, Käyttöoikeusryhmä)
  private var ryhmät: List[Käyttöoikeusryhmä] = Nil

  val orgKatselija = käyttöoikeusryhmä("koski-oppilaitos-katselija", "oman organisaation suoritus- ja opiskeluoikeustietojen katselu").withOrgAccess(read)
  // TODO: todistuksenmyöntäjäroolilla ei vielä käyttöä
  val orgTodistuksenMyöntäjä = käyttöoikeusryhmä("koski-oppilaitos-todistuksen-myöntäjä", "Tietojen hyväksyntä (tutkinnon myöntäjä, ts. todistuksen myöntäjä (tutkinnon/tutkinnon osan)) Tutkinto/tutkinnon osa vahvistetaan. Tämän jälkeen tutkintoa/tutkinnon osaa ei voida muokata rajapinnan/kälin kautta.").withOrgAccess(read)
  val orgTallentaja = käyttöoikeusryhmä("koski-oppilaitos-tallentaja", "tietojen muokkaaminen (suoritus ja opiskelijatietojen tallentaja, oppilaitos: lisäys, muokkaus, passivointi) käyttöliittymässä").withOrgAccess(read, write)
  val orgPalvelukäyttäjä = käyttöoikeusryhmä("koski-oppilaitos-palvelukäyttäjä", "palvelutunnus tiedonsiirroissa: tietojen muokkaaminen (suoritus ja opiskelijatietojen tallentaja, oppilaitos: lisäys, muokkaus, passivointi)").withOrgAccess(read, write)

  val ophPääkäyttäjä = käyttöoikeusryhmä("koski-oph-pääkäyttäjä", "CRUP-oikeudet (lisäys, muokkaus, passivointi) Koskessa").withUniversalAccess(read, write)
  val ophKatselija = käyttöoikeusryhmä("koski-oph-katselija", "näkee kaikki Koski-tiedot").withUniversalAccess(read)

  val virKatselija = käyttöoikeusryhmä("koski-viranomainen-katselija", "näkee oikeuksiensa mukaisesti Koski-tiedot").withUniversalAccess(read)
  val virPääkäyttäjä = käyttöoikeusryhmä("koski-viranomainen-pääkäyttäjä", "katseluoikeudet, antaa oikeudet Tor-viranomaistietojen katselijalle").withUniversalAccess(read)
  val virPalvelu = käyttöoikeusryhmä("koski-viranomainen-palvelukäyttäjä", "palvelutunnus, hakee oikeuksiensa mukaiset Koski-tiedot").withUniversalAccess(read)

  // TODO: remove this
  val old = käyttöoikeusryhmä("2aste-rajapinnat", "Aiemmin käytetty käyttöoikeusryhmä, poistuu Koski-käytöstä").withOrgAccess(read, write)

  def byName(name: String) = ryhmät.find(_.nimi == name)

  def käyttöoikeusryhmät = ryhmät

  private def käyttöoikeusryhmä(name: String, kuvaus: String) = {
    val ryhmä = new Käyttöoikeusryhmä(name, Nil, Nil, kuvaus)
    ryhmät = ryhmät ++ List(ryhmä)
    ryhmä
  }
}

class Käyttöoikeusryhmä private[koskiuser](val nimi: String, val orgAccessType: List[AccessType.Value], val universalAccessType: List[AccessType.Value], val kuvaus: String) {
  def withOrgAccess(newAccess: AccessType.Value*) = {
    new Käyttöoikeusryhmä(nimi, newAccess.toList, universalAccessType, kuvaus)
  }
  def withUniversalAccess(newAccess: AccessType.Value*) = {
    new Käyttöoikeusryhmä(nimi, orgAccessType, newAccess.toList, kuvaus)
  }
  override def toString = "Käyttöoikeusryhmä " + nimi
}

object KäyttöoikeusRyhmätCreator {
  def luoKäyttöoikeusRyhmät(config: Config): Unit = {
    val client: AuthenticationServiceClient = AuthenticationServiceClient(config)
    val olemassaOlevatRyhmät = client.käyttöoikeusryhmät
    val koodistopalvelu: KoodistoPalvelu = KoodistoPalvelu(config)
    val oppilaitostyypit: List[String] = koodistopalvelu.getLatestVersion("oppilaitostyyppi").flatMap(koodistopalvelu.getKoodistoKoodit(_)).toList.flatten.map(_.koodiArvo)

    Käyttöoikeusryhmät.käyttöoikeusryhmät foreach { ryhmä =>
      val olemassaOlevaRyhmä = olemassaOlevatRyhmät.find(olemassaOlevaRyhmä => olemassaOlevaRyhmä.toKoskiKäyttöoikeusryhmä.map(_.nimi) == Some(ryhmä.nimi))
      val organisaatioTyypit = (ryhmä.orgAccessType, ryhmä.universalAccessType) match {
        case (Nil, _) => Nil // käyttöoikeusryhmää ei liity oppilaitoksiin
        case (_, Nil) => oppilaitostyypit // käyttöoikeusryhmä liittyy oppilaitoksiin, eikä sisällä yleistä pääsyä
        case _ => Nil
      }
      val tiedot = UusiKäyttöoikeusryhmä(ryhmä.nimi, ryhmä.nimi, ryhmä.nimi, organisaatioTyypit = organisaatioTyypit)
      olemassaOlevaRyhmä match {
        case Some(o) =>
          println("päivitetään " + ryhmä)
          client.muokkaaKäyttöoikeusryhmä(o.id, tiedot)
        case None =>
          println("luodaan " + ryhmä)
          client.luoKäyttöoikeusryhmä(tiedot)
      }
    }
  }
}