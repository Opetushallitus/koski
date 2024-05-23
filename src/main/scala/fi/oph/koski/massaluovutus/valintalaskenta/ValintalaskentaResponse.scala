package fi.oph.koski.massaluovutus.valintalaskenta

import cats.data.NonEmptyList
import fi.oph.koski.history.JsonPatchException
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.schema._
import fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus

import java.time.{LocalDate, LocalDateTime}

case class ValintalaskentaResult(
  oppijaOid: String,
  opiskeluoikeudet: Option[List[ValintalaskentaOpiskeluoikeus]],
  virheet: Option[List[ValintalaskentaError]],
)

case class ValintalaskentaOpiskeluoikeus(
  tyyppi: Koodistokoodiviite,
  oid: String,
  versionumero: Int,
  aikaleima: LocalDateTime,
  oppilaitos: Option[Oppilaitos],
  tila: ValintalaskentaOpiskeluoikeudenTila,
  suoritukset: Seq[ValintalaskentaSuoritus],
)

object ValintalaskentaOpiskeluoikeus {
  def apply(oo: KoskeenTallennettavaOpiskeluoikeus): ValintalaskentaOpiskeluoikeus = ValintalaskentaOpiskeluoikeus(
    tyyppi = oo.tyyppi,
    oid = oo.oid.get,
    versionumero = oo.versionumero.get,
    aikaleima = oo.aikaleima.get,
    oppilaitos = oo.oppilaitos,
    tila = ValintalaskentaOpiskeluoikeudenTila(oo.tila),
    suoritukset = oo.suoritukset.map(ValintalaskentaSuoritus.apply),
  )
}

case class ValintalaskentaOpiskeluoikeudenTila(
  opiskeluoikeusjaksot: Seq[Koodistokoodiviite],
)

object ValintalaskentaOpiskeluoikeudenTila {
  def apply(tila: OpiskeluoikeudenTila): ValintalaskentaOpiskeluoikeudenTila = ValintalaskentaOpiskeluoikeudenTila(
    opiskeluoikeusjaksot = tila.opiskeluoikeusjaksot.map(_.tila),
  )
}

case class ValintalaskentaSuoritus(
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: ValintalaskentaKoulutusmoduuli,
  suoritustapa: Option[Koodistokoodiviite],
  vahvistus: Option[ValintalaskentaVahvistus],
  keskiarvo: Option[Double],
  korotettuOpiskeluoikeusOid: Option[String],
  korotettuKeskiarvo: Option[Double],
  osasuoritukset: Seq[ValintalaskentaOsasuoritus],
)

object ValintalaskentaSuoritus {
  def apply(s: KoskeenTallennettavaPäätasonSuoritus): ValintalaskentaSuoritus = ValintalaskentaSuoritus(
    tyyppi = s.tyyppi,
    koulutusmoduuli = ValintalaskentaKoulutusmoduuli(s.koulutusmoduuli),
    suoritustapa = s match {
      case ss: AmmatillisenTutkinnonOsittainenTaiKokoSuoritus => Some(ss.suoritustapa)
      case ss: SuoritustavallinenPerusopetuksenSuoritus => Some(ss.suoritustapa)
      case _ => None
    },
    vahvistus = s.vahvistus.map(ValintalaskentaVahvistus.apply),
    keskiarvo = s match {
      case ss: MahdollisestiKeskiarvollinen => ss.keskiarvo
      case _ => None
    },
    korotettuOpiskeluoikeusOid = s match {
      case ss: AmmatillisenTutkinnonOsittainenSuoritus => ss.korotettuOpiskeluoikeusOid
      case _ => None
    },
    korotettuKeskiarvo = s match {
      case ss: AmmatillisenTutkinnonOsittainenSuoritus => ss.korotettuKeskiarvo
      case _ => None
    },
    osasuoritukset = s.osasuoritukset.toList.flatten.map(ValintalaskentaOsasuoritus.apply),
  )
}

case class ValintalaskentaKoulutusmoduuli(
  tunniste: KoodiViite,
  koulutustyyppi: Option[Koodistokoodiviite],
)

object ValintalaskentaKoulutusmoduuli {
  def apply(km: Koulutusmoduuli): ValintalaskentaKoulutusmoduuli = ValintalaskentaKoulutusmoduuli(
    tunniste = km.tunniste,
    koulutustyyppi = km match {
      case kkm: KoulutustyypinSisältäväKoulutusmoduuli => kkm.koulutustyyppi
      case _ => None
    }
  )
}

case class ValintalaskentaVahvistus(
  päivä: LocalDate,
)

object ValintalaskentaVahvistus {
  def apply(v: Vahvistus): ValintalaskentaVahvistus = ValintalaskentaVahvistus(
    päivä = v.päivä,
  )
}
case class ValintalaskentaOsasuoritus(
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: ValintalaskentaOsasuorituksenKoulutusmoduuli,
  arviointi: Seq[ValintalaskentaArviointi],
  keskiarvoSisältääMukautettujaArvosanoja: Boolean,
  korotettuKeskiarvoSisältääMukautettujaArvosanoja: Boolean,
)

object ValintalaskentaOsasuoritus {
  def apply(os: Suoritus): ValintalaskentaOsasuoritus = ValintalaskentaOsasuoritus(
    tyyppi = os.tyyppi,
    koulutusmoduuli = ValintalaskentaOsasuorituksenKoulutusmoduuli(os.koulutusmoduuli),
    arviointi = os.arviointi.toList.flatten.map(ValintalaskentaArviointi.apply),
    keskiarvoSisältääMukautettujaArvosanoja = os match {
      case a: AmmatillisenTutkinnonOsittainenSuoritus => a.keskiarvoSisältääMukautettujaArvosanoja.getOrElse(false)
      case _ => false
    },
    korotettuKeskiarvoSisältääMukautettujaArvosanoja = os match {
      case a: AmmatillisenTutkinnonOsittainenSuoritus => a.korotettuKeskiarvoSisältääMukautettujaArvosanoja.getOrElse(false)
      case _ => false
    },
  )
}

case class ValintalaskentaArviointi(
  arvosana: KoodiViite,
  päivä: Option[LocalDate],
  hyväksytty: Boolean,
)

object ValintalaskentaArviointi {
  def apply(a: Arviointi): ValintalaskentaArviointi = ValintalaskentaArviointi(
    arvosana = a.arvosana,
    päivä = a.arviointipäivä,
    hyväksytty = a.hyväksytty,
  )
}

case class ValintalaskentaOsasuorituksenKoulutusmoduuli(
  laajuus: Option[Double],
  tunniste: KoodiViite,
)

object ValintalaskentaOsasuorituksenKoulutusmoduuli {
  def apply(km: Koulutusmoduuli): ValintalaskentaOsasuorituksenKoulutusmoduuli = ValintalaskentaOsasuorituksenKoulutusmoduuli(
    laajuus = km.getLaajuus.map(_.arvo),
    tunniste = km.tunniste,
  )
}

case class ValintalaskentaError(
  id: String,
  message: String,
)

object ValintalaskentaError {
  def apply(status: HttpStatus): ValintalaskentaError = status match {
    case e: HttpStatus if e.isOk => internal("Yritettiin palauttaa onnistunutta tilannetta virheenä")
    case e: HttpStatus => ValintalaskentaError("error", e.errorString.getOrElse("Tuntematon virhe"))
  }

  def apply(error: Throwable): ValintalaskentaError = error match {
    case e: JsonPatchException => ValintalaskentaError("brokenHistory", s"${e.getMessage}: ${e.cause.getMessage}")
    case e: Throwable => internal(e.getMessage)
  }

  def internal(message: String): ValintalaskentaError = ValintalaskentaError("internal", message)
}
