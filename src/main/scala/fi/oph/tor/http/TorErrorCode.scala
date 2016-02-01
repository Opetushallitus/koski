package fi.oph.tor.http

import fi.oph.tor.http.TorErrorCode.TorErrorCode

// TODO: organize/review error codes
object TorErrorCode {
  type TorErrorCode = String

  val notFound = "notfound"

  val internalError = "internalerror"

  object Forbidden {
    val organisaatio = "forbidden.organisaatio"
  }

  object Conflict {
    val versionumero = "validation.conflict.versionumero"
    val hetuExists = "validation.conflict.hetu"
  }

  object InvalidFormat {
    val number: TorErrorCode = "invalid.number"
    val json = "invalid.json"
    val pvm = "invalid.date"
  }

  object Validation {
    object QueryParam {
      val unknown = "validation.queryparam.unknown"
      val tooShort = "validation.queryparam.tooshort"
    }
    val jsonSchema = "validation.jsonschema"
    val zeroLength = "validation.zerolength"
    val organisaatioTuntematon = "validation.organisaatio.tuntematon"
    val organisaatioVääränTyyppinen = "validation.organisaatio.väärätyyppi"
    val henkilötietojaPuuttuu = "validation.missing.henkilötiedot"
    val hetu = "validation.hetu"
    val henkilötiedot = "validation.invalid.henkilötiedot"
    val henkilöOid = "validation.invalid.henkilöoid"
    val date = "validation.date"

    object Koodisto {
      val notFound = "validation.koodisto.tuntematonkoodi"
    }

    object Rakenne {
      val tuntematonOsa: TorErrorCode = "validation.rakenne.tuntematontutkinnonosa"
      val suoritustapaPuuttuu: TorErrorCode = "validation.rakenne.suoritustapapuuttuu"
      val diaariPuuttuu = "validation.rakenne.diaarinumeropuuttuu"
      val tuntematonDiaari = "validation.rakenne.tuntematondiaarinumero"
      val tuntematonOsaamisala = "validation.rakenne.tuntematonosaamisala"
    }
  }
}

case class ErrorDetail(torErrorCode: TorErrorCode, message: AnyRef) {
  override def toString = torErrorCode + " (" + message + ")"
}