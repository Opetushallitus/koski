package fi.oph.koski.schema

trait Ulkomaajaksollinen {
  def ulkomaanjaksot: Option[List[Ulkomaanjakso]]
}

trait SisäoppilaitosmainenMajoitus {
  def sisäoppilaitosmainenMajoitus: Option[List[Aikajakso]]
}

trait OikeusmaksuttomaanAsuntolapaikkaan {
  def oikeusMaksuttomaanAsuntolapaikkaan: Option[Aikajakso]
}
