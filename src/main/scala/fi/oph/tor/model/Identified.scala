package fi.oph.tor.model

object Identified {
  type Id = Int

  def withoutId(t: Suoritus): Suoritus = t.copy(id = None, komoto = withoutId(t.komoto), osasuoritukset = t.osasuoritukset.map(withoutId), arviointi = t.arviointi.map(withoutId))
  def withoutId(t: Arviointi): Arviointi = t.copy(id = None)
  def withoutId(t: Komoto): Komoto = t.copy(id = None)
}

trait Identified {
  def id: Option[Identified.Id]
}