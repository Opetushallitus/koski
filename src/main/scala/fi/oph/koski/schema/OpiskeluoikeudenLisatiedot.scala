package fi.oph.koski.schema

import fi.oph.koski.koodisto.MockKoodistoViitePalvelu

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

trait VaikeastiVammainen {
  def vaikeastiVammainen: Option[List[Aikajakso]]
}
