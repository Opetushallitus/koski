package fi.oph.koski.etk

object ElaketurvakeskusTutkintotiedotFormatter {

  def format(tutkintotiedot: EtkResponse): EtkResponse = {
    val formatted = tutkintotiedot.tutkinnot.map(tutkintotieto => tutkintotieto.copy(tutkinto = formatTutkinnonTaso(tutkintotieto.tutkinto)))

    tutkintotiedot.copy(tutkinnot = formatted)
  }

  private def formatTutkinnonTaso(tutkinto: EtkTutkinto) = {
    tutkinto.copy(tutkinnonTaso = format(tutkinto.tutkinnonTaso))
  }

  private def format(taso: Option[String]): Option[String] = taso match {
    case Some(t) => format(t)
    case _ => None
  }

  private def format(taso: String) = taso match {
    case "ammatillinenkoulutus" => Some("ammatillinenperustutkinto")
    case "1" => Some("ammattikorkeakoulututkinto")
    case "2" => Some("alempikorkeakoulututkinto")
    case "3" => Some("ylempiammattikorkeakoulututkinto")
    case "4" => Some("ylempikorkeakoulututkinto")
    case "" => None
    case _ => throw new Exception(s"tutkintotason koodia vastaavaa koodia ei löytynyt. Koodi:${taso}")
  }
}
