package fi.oph.koski.validation


import java.time.LocalDate
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.opiskeluoikeus.CompositeOpiskeluoikeusRepository
import fi.oph.koski.schema._
import fi.oph.koski.util.DateOrdering
import fi.oph.koski.util.DateOrdering.localDateOrdering

object MaksuttomuusValidation {

  def checkOpiskeluoikeudenMaksuttomuus(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
                                        oppijanSyntymäpäivä: Option[LocalDate],
                                        oppijanOid: String,
                                        opiskeluoikeusRepository: CompositeOpiskeluoikeusRepository)
                                       (implicit user: KoskiSpecificSession): HttpStatus = {
    val suoritusVaatiiMaksuttomuusTiedon = oppivelvollisuudenSuorittamiseenKelpaavaMuuKuinPeruskoulunOpiskeluoikeus(opiskeluoikeus)
    val oppijanIkäOikeuttaaMaksuttomuuden = oppijanSyntymäpäivä.exists(bd => !LocalDate.of(2004, 1, 1).isAfter(bd))
    val alkamispäivä = if (opiskeluoikeus.tila.opiskeluoikeusjaksot.nonEmpty) Some(opiskeluoikeus.tila.opiskeluoikeusjaksot.minBy(_.alku).alku) else None
    val alkamispäiväOikeuttaaMaksuttomuuden = alkamispäivä.exists(d => !d.isBefore(LocalDate.of(2021, 1, 1)))
    val maksuttomuusTietoOnSiirretty = opiskeluoikeus.lisätiedot.collect { case l: MaksuttomuusTieto => l.maksuttomuus.toList.flatten.length > 0 }.getOrElse(false)
    val maksuttomuuttaPidennettyOnSiirretty = opiskeluoikeus.lisätiedot.collect { case l : MaksuttomuusTieto => l.oikeuttaMaksuttomuuteenPidennetty.toList.flatten.length > 0 }.getOrElse(false)

    val piirissä = !opiskeluoikeusRepository.checkValpasLainUlkopuolisiaPerusopetuksenVahvistettujaSuorituksia(oppijanOid)
    val eiPiirissäMuttaMaksuttomuusTietojaSiirretty = !piirissä && (maksuttomuusTietoOnSiirretty || maksuttomuuttaPidennettyOnSiirretty)

    if (suoritusVaatiiMaksuttomuusTiedon && oppijanIkäOikeuttaaMaksuttomuuden && alkamispäiväOikeuttaaMaksuttomuuden && piirissä) {
      HttpStatus.validate(maksuttomuusTietoOnSiirretty) { KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta puuttuu.") }
    } else if (!piirissä) {
      HttpStatus.validate(!eiPiirissäMuttaMaksuttomuusTietojaSiirretty) { KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta ei ole relevantti tässä opiskeluoikeudessa, sillä opiskelija on suorittanut perusopetuksen, aikuisten perusopetuksen oppimäärän tai International Schoolin 9. vuosiluokan ennen 1.1.2021.")}
    } else {
      HttpStatus.fold(
        HttpStatus.validate(!maksuttomuusTietoOnSiirretty) { KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta ei ole relevantti tässä opiskeluoikeudessa, sillä opiskeluoikeus on alkanut ennen 1.1.2021 ja/tai oppija ei annetun syntymäajan perusteella ole ikänsä puolesta laajennetun oppivelvollisuuden piirissä.")},
        HttpStatus.validate(!maksuttomuuttaPidennettyOnSiirretty) { KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta ei ole relevantti tässä opiskeluoikeudessa, sillä opiskeluoikeus on alkanut ennen 1.1.2021 ja/tai oppija ei annetun syntymäajan perusteella ole ikänsä puolesta laajennetun oppivelvollisuuden piirissä.")}
      )
    }
  }

  // Huom! Valpas käyttää myös tätä funktiota!
  def oppivelvollisuudenSuorittamiseenKelpaavaMuuKuinPeruskoulunOpiskeluoikeus(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): Boolean =
    opiskeluoikeus.suoritukset.collectFirst {
      case myp: MYPVuosiluokanSuoritus if myp.koulutusmoduuli.tunniste.koodiarvo == "10" => myp
      case s: SuoritusVaatiiMahdollisestiMaksuttomuusTiedonOpiskeluoikeudelta => s
    }.isDefined

  def validateAndFillJaksot(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): Either[HttpStatus, KoskeenTallennettavaOpiskeluoikeus] = {
   opiskeluoikeus.lisätiedot.collect {
     case lisätiedot: MaksuttomuusTieto => {
       val oikeuttaMaksuttomuuteenPidennetty = sortJaksonAlkupäivänMukaan(lisätiedot.oikeuttaMaksuttomuuteenPidennetty.toList.flatten)
       val maksuttomuus = sortJaksonAlkupäivänMukaan(lisätiedot.maksuttomuus.toList.flatten)

       for {
         validMaksuttomuus <- validateAndFillMaksuttomuusJaksot(maksuttomuus, opiskeluoikeus)
         validMaksuttomuuttaPidennetty <- validateMaksuttomuuttaPidennetty(oikeuttaMaksuttomuuteenPidennetty, validMaksuttomuus, opiskeluoikeus)
       } yield (
         opiskeluoikeus
           .withLisätiedot(Some(lisätiedot
             .withMaksuttomus(toOptional(validMaksuttomuus))
             .withOikeuttaMaksuttomuuteenPidennetty(toOptional(validMaksuttomuuttaPidennetty))
           ))
         )
     }
   }.getOrElse(Right(opiskeluoikeus))
  }

  private def sortJaksonAlkupäivänMukaan[A <: Alkupäivällinen](jaksot: List[A]): List[A] = jaksot.sortBy(_.alku)(DateOrdering.localDateOrdering)

  private def validateAndFillMaksuttomuusJaksot(jaksot: List[Maksuttomuus], opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus) = {
    val voimassaolonUlkopuolella = jaksot.map(_.alku).filterNot(d => between(opiskeluoikeus.alkamispäivä, opiskeluoikeus.päättymispäivä)(d))
    val samojaAlkamispäiviä = jaksot.map(_.alku).groupBy(x => x).filter(_._2.length > 1).values.flatten.toSeq

    val validationResult = HttpStatus.fold(
      validate(voimassaolonUlkopuolella)(x => KoskiErrorCategory.badRequest.validation(s"Opiskeluoikeudella on koulutuksen maksuttomuusjaksoja, jonka alkupäivä ${x.map(_.toString).mkString(", ")} ei ole opiskeluoikeuden voimassaolon (${voimassaolo(opiskeluoikeus)}) sisällä")),
      validate(samojaAlkamispäiviä)(x => KoskiErrorCategory.badRequest.validation(s"Opiskeluoikeudella on koulutuksen maksuttomuusjaksoja, joilla on sama alkupäivä ${x.map(_.toString).mkString(", ")}"))
    )

    if (validationResult.isOk) Right(fillPäättymispäivät(jaksot)) else Left(validationResult)
  }

  private def validateMaksuttomuuttaPidennetty(jaksot: List[OikeuttaMaksuttomuuteenPidennetty], maksuttomuus: List[Maksuttomuus], opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus) = {
    def betweenOpiskeluoikeudenAlkamisPäättymis(jakso: OikeuttaMaksuttomuuteenPidennetty) = {
      val voimassaolonSisällä = between(opiskeluoikeus.alkamispäivä, opiskeluoikeus.päättymispäivä)_
      voimassaolonSisällä(jakso.alku) && voimassaolonSisällä(jakso.loppu)
    }

    val voimassaolonUlkopuolella = jaksot.filterNot(betweenOpiskeluoikeudenAlkamisPäättymis)
    val jaksonAlkuEnnenLoppua = jaksot.filterNot(jakso => !jakso.alku.isAfter(jakso.loppu))
    val päällekkäisiäJaksoja = jaksot.zip(jaksot.drop(1)).filter { case (a,b) => a.overlaps(b) }

    val maksuttomatMaksuttomuusJaksot = maksuttomuus.filter(_.maksuton)
    val pidennysMaksuttomuudenUlkopuolella = jaksot.filterNot(pidennys => maksuttomatMaksuttomuusJaksot.exists(maksuton => maksuton.containsPidennysJakso(pidennys)))

    val validationResult = HttpStatus.fold(
      validate(voimassaolonUlkopuolella)(x => KoskiErrorCategory.badRequest.validation(s"Opiskeluoikeudella on koulutuksen maksuttomuuden pidennykseen liittyvä jakso, jonka alku- ja/tai loppupäivä ei ole opiskeluoikeuden voimassaolon (${voimassaolo(opiskeluoikeus)}) sisällä ${x.map(_.toString).mkString(", ")}")),
      validate(jaksonAlkuEnnenLoppua)(x => KoskiErrorCategory.badRequest.validation(s"Opiskeluoikeudella on koulutuksen maksuttomuuden pidennykseen liittyvä jakso, jonka loppupäivä on aikaisemmin kuin alkupäivä. ${x.map(y => s"${y.alku} (alku) - ${y.loppu} (loppu)").mkString(", ")}")),
      validate(päällekkäisiäJaksoja)(x => KoskiErrorCategory.badRequest.validation(s"Opiskeluoikeudella on koulutuksen maksuttomuuden pidennykseen liittyviä jaksoja, jotka ovat keskenään päällekkäisiä ${x.map(_.toString).mkString(", ")}")),
      validate(pidennysMaksuttomuudenUlkopuolella)(x => KoskiErrorCategory.badRequest.validation(s"Maksuttomuutta voidaan pidetäntää vain aikavälillä jolloin koulutus on maksutontonta"))
    )

    if (validationResult.isOk) Right(jaksot) else Left(validationResult)
  }

  private def fillPäättymispäivät(maksuttomuus: List[Maksuttomuus]) = {
    val jaksot = maksuttomuus.map(_.copy(loppu = None))
    val last = jaksot.lastOption.toList
    val filled = jaksot.zip(jaksot.drop(1)).map { case (a, b) => a.copy(loppu = Some(b.alku.minusDays(1))) }
    filled ::: last
  }

  private def validate[A](virheelliset: Seq[A])(virheviesti: Seq[A] => HttpStatus) =
    if (virheelliset.length > 0) virheviesti(virheelliset) else HttpStatus.ok

  private def between(start: Option[LocalDate], end: Option[LocalDate])(date: LocalDate) =
    start.map(alku => !date.isBefore(alku)).getOrElse(false) && end.map(loppu => !date.isAfter(loppu)).getOrElse(true)

  private def voimassaolo(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus) =
    s"${opiskeluoikeus.alkamispäivä.map(_.toString).getOrElse("")} - ${opiskeluoikeus.päättymispäivä.map(_.toString).getOrElse("")}"

  private def toOptional[A](xs: List[A]): Option[List[A]] = if (xs.isEmpty) None else Some(xs)
}
