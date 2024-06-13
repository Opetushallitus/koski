package fi.oph.koski.schema

import fi.oph.koski.koodisto.MockKoodistoViitePalvelu
import fi.oph.koski.koskiuser.Rooli
import fi.oph.koski.schema.annotation.{Deprecated, KoodistoUri, SensitiveData}
import fi.oph.scalaschema.annotation.{Description, Title}

trait OpiskeluoikeudenLisätiedot

object TukimuodollisetLisätiedot {
  def tukimuodoissaOsaAikainenErityisopetus(t: Option[List[Tukimuodollinen]]) = {
    val tukimuodot = t.getOrElse(List()).flatMap(_.tukimuotoLista)
    tukimuodot.contains(osaAikainenErityisopetusKoodistokoodiviite)
  }

  private lazy val osaAikainenErityisopetusKoodistokoodiviite =
    MockKoodistoViitePalvelu.validateRequired(Koodistokoodiviite("1", "perusopetuksentukimuoto"))
}

trait UlkomaanaikajaksojaSisältävä {
  def kaikkiUlkomaanaikajaksot: List[Aikajakso]
}

trait Ulkomaanaikajaksollinen extends UlkomaanaikajaksojaSisältävä{
  @Deprecated("Käytä korvaavaa kenttää Ulkomaanjaksot")
  def ulkomailla: Option[Aikajakso]
  def ulkomaanjaksot: Option[List[Aikajakso]]

  def kaikkiUlkomaanaikajaksot: List[Aikajakso] = {
    ulkomailla.toList ++ ulkomaanjaksot.toList.flatten
  }
}

trait Ulkomaanjaksollinen extends UlkomaanaikajaksojaSisältävä {
  def ulkomaanjaksot: Option[List[Ulkomaanjakso]]

  def kaikkiUlkomaanaikajaksot: List[Aikajakso] = {
    ulkomaanjaksot.getOrElse(List.empty).map(uj => Aikajakso(uj.alku, uj.loppu))
  }
}

trait SisäoppilaitosmainenMajoitus {
  def sisäoppilaitosmainenMajoitus: Option[List[Aikajakso]]
}

trait OikeusmaksuttomaanAsuntolapaikkaanAikajaksona extends OpiskeluoikeudenLisätiedot {
  def oikeusMaksuttomaanAsuntolapaikkaan: Option[Aikajakso]

  import mojave._
  def withOikeusMaksuttomaanAsuntolapaikkaan(maksuttomuus: Option[Aikajakso]): OikeusmaksuttomaanAsuntolapaikkaanAikajaksona =
    shapeless.lens[OikeusmaksuttomaanAsuntolapaikkaanAikajaksona].field[Option[Aikajakso]]("oikeusMaksuttomaanAsuntolapaikkaan").set(this)(maksuttomuus)
}

trait OikeusmaksuttomaanAsuntolapaikkaanBooleanina extends OpiskeluoikeudenLisätiedot {
  def oikeusMaksuttomaanAsuntolapaikkaan: Option[Boolean]

  import mojave._
  def withOikeusMaksuttomaanAsuntolapaikkaan(maksuttomuus: Option[Boolean]): OikeusmaksuttomaanAsuntolapaikkaanBooleanina =
    shapeless.lens[OikeusmaksuttomaanAsuntolapaikkaanBooleanina].field[Option[Boolean]]("oikeusMaksuttomaanAsuntolapaikkaan").set(this)(maksuttomuus)
}

trait ErityisenTuenPäätöksiäSisältäväLisätieto {
  def kaikkiErityisenTuenPäätöstenAikajaksot: List[MahdollisestiAlkupäivällinenJakso]
}

trait UlkomainenVaihtoopiskelija {
  def ulkomainenVaihtoopiskelija: Boolean
}

trait Vammainen {
  def vammainen: Option[List[Aikajakso]]
}

trait VaikeastiVammainen {
  def vaikeastiVammainen: Option[List[Aikajakso]]
}

trait VammainenJaAvustaja {
  def vammainenJaAvustaja: Option[List[Aikajakso]]
}

trait PidennettyOppivelvollisuus extends Vammainen with VaikeastiVammainen with ErityisenTuenPäätöksiäSisältäväLisätieto {
  def pidennettyOppivelvollisuus: Option[Aikajakso]
}

trait OsaAikaisuusjaksollinen {
  def osaAikaisuusjaksot: Option[List[OsaAikaisuusJakso]]
}

trait Majoituksellinen {
  def majoitus: Option[List[Aikajakso]]
}

trait Majoitusetuinen {
  def majoitusetu: Option[Aikajakso]
}

trait Kuljetusetuinen {
  def kuljetusetu: Option[Aikajakso]
}

trait Kotiopetuksellinen {
  def kotiopetus: Option[Aikajakso]
  def kotiopetusjaksot: Option[List[Aikajakso]]
}

trait VaativanErityisenTuenYhteydessäJärjestettävänMajoituksenSisältäväLisätieto {
  def vaativanErityisenTuenYhteydessäJärjestettäväMajoitus: Option[List[Aikajakso]]
}

trait VaativanErityisenTuenErityisenTehtävänSisältäväLisätieto {
  def vaativanErityisenTuenErityinenTehtävä: Option[List[Aikajakso]]
}

trait Vankilaopetuksessa {
  def vankilaopetuksessa: Option[List[Aikajakso]]
}

trait PidennettyPäättymispäivä {
  def pidennettyPäättymispäivä: Option[Boolean]
}

trait MaksuttomuusTieto extends OpiskeluoikeudenLisätiedot {
  import mojave._
  @Title("Koulutuksen maksuttomuus")
  @Description("Tieto siitä, onko koulutus maksutonta. Aikajaksotieto (lista aikajaksoja), jossa siirretään VAIN alkupäivä sekä tieto siitä, onko koulutus maksutonta (true/false). Jos jaksoja on useampi, edeltävän jakson loppupäivä päätellään seuraavan jakson alkupäivästä. Tieto koulutuksen maksuttomuudesta tulee siirtää opiskeluoikeuksiin, jotka ovat alkaneet 1.8.2021 tai sen jälkeen, jos oppija on syntynyt vuonna 2004 tai sen jälkeen ja opiskeluoikeus sisältää laajennetun oppivelvollisuuden piirissä olevan suorituksen")
  def maksuttomuus: Option[List[Maksuttomuus]]
  @Description("Tieto siitä, jos oppijan oikeutta maksuttomaan koulutukseen on pidennetty. Aikajaksotieto (lista aikajaksoja), jossa pakollisina tietoina sekä alku- että loppupäivä. Tiedon siirtäminen vaatii opiskeluoikeudelta tiedon koulutuksen maksuttomuudesta")
  def oikeuttaMaksuttomuuteenPidennetty: Option[List[OikeuttaMaksuttomuuteenPidennetty]]

  final def withMaksuttomus(maksuttomuus: Option[List[Maksuttomuus]]): MaksuttomuusTieto =
    shapeless.lens[MaksuttomuusTieto].field[Option[List[Maksuttomuus]]]("maksuttomuus").set(this)(maksuttomuus)

  final def withOikeuttaMaksuttomuuteenPidennetty(oikeuttaMaksuttomuuteenPidennetty: Option[List[OikeuttaMaksuttomuuteenPidennetty]]): MaksuttomuusTieto =
    shapeless.lens[MaksuttomuusTieto].field[Option[List[OikeuttaMaksuttomuuteenPidennetty]]]("oikeuttaMaksuttomuuteenPidennetty").set(this)(oikeuttaMaksuttomuuteenPidennetty)
}

trait JotpaAsianumero {
  @Title("JOTPA asianumero")
  @KoodistoUri("jotpaasianumero")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  def jotpaAsianumero: Option[Koodistokoodiviite]
}
