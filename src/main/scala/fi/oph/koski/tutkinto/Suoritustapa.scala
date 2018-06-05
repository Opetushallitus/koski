package fi.oph.koski.tutkinto

import fi.oph.koski.koodisto.MockKoodistoViitePalvelu
import fi.oph.koski.schema.Koodistokoodiviite

object Suoritustapa {

  // https://koski.opintopolku.fi/koski/api/koodisto/ammatillisentutkinnonsuoritustapa/1
  val ops = apply("ops")
  val naytto = apply("naytto")
  val reformi = apply("reformi")

  def apply(koodiarvo: String) = MockKoodistoViitePalvelu.validateRequired(Koodistokoodiviite(koodiarvo, "ammatillisentutkinnonsuoritustapa"))
}
