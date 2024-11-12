package fi.oph.koski.koskiuser

object AccessType extends Enumeration {
  val
    read,
    write,
    tiedonsiirronMitätöinti,
    käyttöliittymäsiirronMitätöinti,
    lähdejärjestelmäkytkennänPurkaminen,
    editOnly = Value
}
