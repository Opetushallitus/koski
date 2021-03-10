package fi.oph.koski.schema

import fi.oph.koski.koodisto.MockKoodistoViitePalvelu
import fi.oph.scalaschema.annotation.{Description, Title}

trait OpiskeluoikeudenLisätiedot

trait TukimuodollisetLisätiedot extends OpiskeluoikeudenLisätiedot {
  def sisältääOsaAikaisenErityisopetuksen: Boolean
}

object TukimuodollisetLisätiedot {
  def tukimuodoissaOsaAikainenErityisopetus(t: Option[List[Tukimuodollinen]]) = {
    val tukimuodot = t.getOrElse(List()).flatMap(_.tukimuotoLista)
    tukimuodot.contains(osaAikainenErityisopetusKoodistokoodiviite)
  }

  private lazy val osaAikainenErityisopetusKoodistokoodiviite =
    MockKoodistoViitePalvelu.validateRequired(Koodistokoodiviite("1", "perusopetuksentukimuoto"))
}

trait Ulkomaajaksollinen {
  def ulkomaanjaksot: Option[List[Ulkomaanjakso]]
}

trait SisäoppilaitosmainenMajoitus {
  def sisäoppilaitosmainenMajoitus: Option[List[Aikajakso]]
}

trait OikeusmaksuttomaanAsuntolapaikkaan {
  def oikeusMaksuttomaanAsuntolapaikkaan: Option[Aikajakso]
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

trait Majoitusetuinen {
  def majoitusetu: Option[Aikajakso]
}

trait Kuljetusetuinen {
  def kuljetusetu: Option[Aikajakso]
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
