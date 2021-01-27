package fi.oph.koski.validation


import java.time.LocalDate

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema._
import fi.oph.koski.util.DateOrdering

object MaksuttomuusValidation {

  def checkOpiskeluoikeudenMaksuttomuus(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, oppijanSyntymäpäivä: Option[LocalDate]): HttpStatus = {
    val suoritusVaatiiMaksuttomuusTiedon = opiskeluoikeus.suoritukset.collectFirst {
      case myp: MYPVuosiluokanSuoritus if myp.koulutusmoduuli.tunniste.koodiarvo == "10" => myp
      case s: SuoritusVaatiiMahdollisestiMaksuttomuusTiedonOpiskeluoikeudelta => s
    }.isDefined
    val oppijanIkäOikeuttaaMaksuttomuuden = oppijanSyntymäpäivä.exists(bd => !LocalDate.of(2004, 1, 1).isAfter(bd))
    val alkamispäiväOikeuttaaMaksuttomuuden = opiskeluoikeus.alkamispäivä.exists(d => !d.isBefore(LocalDate.of(2021, 8, 1)))
    val maksuttomuusTietoOnSiirretty = opiskeluoikeus.lisätiedot.collect { case l: MaksuttomuusTieto => l.maksuttomuus.toList.flatten.length > 0 }.getOrElse(false)
    val maksuttomuuttaPidennettyOnSiirretty = opiskeluoikeus.lisätiedot.collect { case l : MaksuttomuusTieto => l.oikeuttaMaksuttomuuteenPidennetty.toList.flatten.length > 0 }.getOrElse(false)

    if (suoritusVaatiiMaksuttomuusTiedon && oppijanIkäOikeuttaaMaksuttomuuden && alkamispäiväOikeuttaaMaksuttomuuden) {
      HttpStatus.validate(maksuttomuusTietoOnSiirretty) { KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta puuttuu.") }
    } else {
      HttpStatus.fold(
        HttpStatus.validate(!maksuttomuusTietoOnSiirretty) { KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta ei ole relevantti tässä opiskeluoikeudessa, sillä opiskeluoikeus on alkanut ennen 1.8.2021 ja/tai oppija ei annetun syntymäajan perusteella ole ikänsä puolesta laajennetun oppivelvollisuuden piirissä.")},
        HttpStatus.validate(!maksuttomuuttaPidennettyOnSiirretty) { KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta ei ole relevantti tässä opiskeluoikeudessa, sillä opiskeluoikeus on alkanut ennen 1.8.2021 ja/tai oppija ei annetun syntymäajan perusteella ole ikänsä puolesta laajennetun oppivelvollisuuden piirissä.")}
      )
    }
  }

  def validateAndFillJaksot(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): Either[HttpStatus, KoskeenTallennettavaOpiskeluoikeus] = {
   opiskeluoikeus.lisätiedot.collect {
     case lisätiedot: MaksuttomuusTieto => {
       val oikeuttaMaksuttomuuteenPidennetty = sortJaksonAlkupäivänMukaan(lisätiedot.oikeuttaMaksuttomuuteenPidennetty.toList.flatten)
       val maksuttomuus = sortJaksonAlkupäivänMukaan(lisätiedot.maksuttomuus.toList.flatten)

       val validationResult = HttpStatus.fold(
         validateMaksuttomuuttaPidennetty(oikeuttaMaksuttomuuteenPidennetty, opiskeluoikeus),
         validateMaksuttomuus(maksuttomuus, opiskeluoikeus)
       )
       if (validationResult.isOk) {
         Right(fill(opiskeluoikeus, lisätiedot, maksuttomuus, oikeuttaMaksuttomuuteenPidennetty))
       } else {
         Left(validationResult)
       }
     }
   }.getOrElse(Right(opiskeluoikeus))
  }

  private def sortJaksonAlkupäivänMukaan[A <: Alkupäivällinen](jaksot: List[A]): List[A] = jaksot.sortBy(_.alku)(DateOrdering.localDateOrdering)

  private def validateMaksuttomuus(jaksot: List[Maksuttomuus], opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus) = {
    val voimassaolonUlkopuolella = jaksot.map(_.alku).filterNot(d => between(opiskeluoikeus.alkamispäivä, opiskeluoikeus.päättymispäivä)(d))
    val samojaAlkamispäiviä = jaksot.map(_.alku).groupBy(x => x).filter(_._2.length > 1).values.flatten.toSeq

    HttpStatus.fold(
      validate(voimassaolonUlkopuolella)(x => KoskiErrorCategory.badRequest.validation(s"Opiskeluoikeudella on koulutuksen maksuttomuusjaksoja, jonka alkupäivä ${x.map(_.toString).mkString(", ")} ei ole opiskeluoikeuden voimassaolon (${voimassaolo(opiskeluoikeus)}) sisällä")),
      validate(samojaAlkamispäiviä)(x => KoskiErrorCategory.badRequest.validation(s"Opiskeluoikeudella on koulutuksen maksuttomuusjaksoja, joilla on sama alkupäivä ${x.map(_.toString).mkString(", ")}"))
    )
  }

  private def validateMaksuttomuuttaPidennetty(jaksot: List[MaksuttomuuttaPidennetty], opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus) = {
    def betweenOpiskeluoikeudenAlkamisPäättymis(jakso: MaksuttomuuttaPidennetty) = {
      val voimassaolonSisällä = between(opiskeluoikeus.alkamispäivä, opiskeluoikeus.päättymispäivä)_
      voimassaolonSisällä(jakso.alku) && voimassaolonSisällä(jakso.loppu)
    }

    val voimassaolonUlkopuolella = jaksot.filterNot(betweenOpiskeluoikeudenAlkamisPäättymis)
    val jaksonAlkuEnnenLoppua = jaksot.filterNot(jakso => !jakso.alku.isAfter(jakso.loppu))
    val päällekkäisiäJaksoja = jaksot.zip(jaksot.drop(1)).filter { case (a,b) => a.overlaps(b) }

    HttpStatus.fold(
      validate(voimassaolonUlkopuolella)(x => KoskiErrorCategory.badRequest.validation(s"Opiskeluoikeudella on koulutuksen maksuttomuuden pidennykseen liittyvä jakso, jonka alku- ja/tai loppupäivä ei ole opiskeluoikeuden voimassaolon (${voimassaolo(opiskeluoikeus)}) sisällä ${x.map(_.toString).mkString(", ")}")),
      validate(jaksonAlkuEnnenLoppua)(x => KoskiErrorCategory.badRequest.validation(s"Opiskeluoikeudella on koulutuksen maksuttomuuden pidennykseen liittyvä jakso, jonka loppupäivä on aikaisemmin kuin alkupäivä. ${x.map(y => s"${y.alku} (alku) - ${y.loppu} (loppu)").mkString(", ")}")),
      validate(päällekkäisiäJaksoja)(x => KoskiErrorCategory.badRequest.validation(s"Opiskeluoikeudella on koulutuksen maksuttomuuden pidennykseen liittyviä jaksoja, jotka ovat keskenään päällekkäisiä ${x.map(_.toString).mkString(", ")}"))
    )
  }

  private def fill(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, lisätiedot: MaksuttomuusTieto, maksuttomuus: List[Maksuttomuus], oikeuttaMaksuttomuuteenPidennetty: List[MaksuttomuuttaPidennetty]): KoskeenTallennettavaOpiskeluoikeus = {
    val jaksot = maksuttomuus.map(_.copy(loppu = None))
    val last = jaksot.lastOption.toList
    val filled = jaksot.zip(jaksot.drop(1)).map { case (a, b) => a.copy(loppu = Some(b.alku.minusDays(1))) }
    val maksuttomuusJaksot = filled ::: last

    opiskeluoikeus.withLisätiedot(Some(lisätiedot
      .withMaksuttomus(toOptional(maksuttomuusJaksot))
      .withOikeuttaMaksuttomuuteenPidennetty(toOptional(oikeuttaMaksuttomuuteenPidennetty))
    ))
  }

  private def validate[A](virheelliset: Seq[A])(virheviesti: Seq[A] => HttpStatus) =
    if (virheelliset.length > 0) virheviesti(virheelliset) else HttpStatus.ok

  private def between(start: Option[LocalDate], end: Option[LocalDate])(date: LocalDate) =
    start.map(alku => !date.isBefore(alku)).getOrElse(false) && end.map(loppu => !date.isAfter(loppu)).getOrElse(true)

  private def voimassaolo(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus) =
    s"${opiskeluoikeus.alkamispäivä.map(_.toString).getOrElse("")} - ${opiskeluoikeus.päättymispäivä.map(_.toString).getOrElse("")}"

  private def toOptional[A](xs: List[A]): Option[List[A]] = if (xs.isEmpty) None else Some(xs)
}
