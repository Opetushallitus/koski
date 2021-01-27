package fi.oph.koski.schema

import fi.oph.koski.koodisto.MockKoodistoViitePalvelu
import fi.oph.scalaschema.annotation.Description

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
  def maksuttomuus: Option[List[Maksuttomuus]]
  def oikeuttaMaksuttomuuteenPidennetty: Option[List[MaksuttomuuttaPidennetty]]

  final def withMaksuttomus(maksuttomuus: Option[List[Maksuttomuus]]): MaksuttomuusTieto =
    shapeless.lens[MaksuttomuusTieto].field[Option[List[Maksuttomuus]]]("maksuttomuus").set(this)(maksuttomuus)

  final def withOikeuttaMaksuttomuuteenPidennetty(oikeuttaMaksuttomuuteenPidennetty: Option[List[MaksuttomuuttaPidennetty]]): MaksuttomuusTieto =
    shapeless.lens[MaksuttomuusTieto].field[Option[List[MaksuttomuuttaPidennetty]]]("oikeuttaMaksuttomuuteenPidennetty").set(this)(oikeuttaMaksuttomuuteenPidennetty)
}
