package fi.oph.koski.validation


import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.opiskeluoikeus.{CompositeOpiskeluoikeusRepository, Päivämääräväli}
import fi.oph.koski.oppivelvollisuustieto.Oppivelvollisuustiedot
import fi.oph.koski.raportointikanta.RaportointiDatabase
import fi.oph.koski.schema._
import fi.oph.koski.util.{DateOrdering, FinnishDateFormat}
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasRajapäivätService

import java.time.LocalDate
import java.time.LocalDate.{of => date}

object MaksuttomuusValidation {

  def checkOpiskeluoikeudenMaksuttomuus(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
                                        oppijanHenkilötiedot: Option[LaajatOppijaHenkilöTiedot],
                                        oppijanOid: String,
                                        opiskeluoikeusRepository: CompositeOpiskeluoikeusRepository,
                                        rajapäivät: ValpasRajapäivätService): HttpStatus = {
    val oppijanSyntymäpäivä = oppijanHenkilötiedot.flatMap(_.syntymäaika)
    val perusopetuksenAikavälit = opiskeluoikeusRepository.getPerusopetuksenAikavälitIlmanKäyttöoikeustarkistusta(oppijanOid)

    val maksuttomuustietoSiirretty = opiskeluoikeus.lisätiedot.collect { case l: MaksuttomuusTieto => l.maksuttomuus.toList.flatten.length > 0 }.getOrElse(false)
    val maksuttomuudenPidennysSiirretty = opiskeluoikeus.lisätiedot.collect { case l : MaksuttomuusTieto => l.oikeuttaMaksuttomuuteenPidennetty.toList.flatten.length > 0 }.getOrElse(false)

    // Peruskoulun jälkeinen koulutus on uuden lain mukaiseksi peruskoulun jälkeiseksi oppivelvollisuuskoulutukseksi kelpaavaa
    val koulutusOppivelvollisuuskoulutukseksiKelpaavaa = oppivelvollisuudenSuorittamiseenKelpaavaMuuKuinPeruskoulunOpiskeluoikeus(opiskeluoikeus)

    // Maksuttomuutta ei voi olla opiskeluoikeudessa, joka alkaa myöhemmin kuin vuonna, jolloin oppija täyttää 20 vuotta
    val opiskeluoikeusAlkanutHenkilönOllessaLiianVanha = (oppijanSyntymäpäivä.map(_.getYear), opiskeluoikeus.alkamispäivä.map(_.getYear)) match {
      case (Some(oppijanSyntymävuosi), Some(opiskeluoikeudenAlkamisvuosi)) if opiskeluoikeudenAlkamisvuosi > oppijanSyntymävuosi + rajapäivät.maksuttomuusLoppuuIka => true
      case _ => false
    }

    // Lukion vanhan opsin mukaiseen opiskeluoikeuteen maksuttomuustiedon saa siirtää vain jos se on alkanut 1.1.2021 tai myöhemmin
    val lukioVanhallaOpsillaSallittuAlkamisjakso = new Aikajakso(
      date(2021, 1, 1),
      None
    )
    val kelpaamatonLukionVanhanOpsinOpiskeluoikeus = (opiskeluoikeus, opiskeluoikeus.alkamispäivä) match {
      case (oo: LukionOpiskeluoikeus, Some(alkamispäivä))
        if (oo.on2015Opiskeluoikeus && !lukioVanhallaOpsillaSallittuAlkamisjakso.contains(alkamispäivä)) => true
      case _ => false
    }

    // Tilanteet, joissa maksuttomuustietoja ei saa siirtää. Jos tuplen ensimmäinen arvo on true, ehto aktivoituu ja toinen arvon kertoo syyn.
    val eiLaajennettuOppivelvollinenSyyt = eiOppivelvollisuudenLaajentamislainPiirissäSyyt(oppijanSyntymäpäivä, perusopetuksenAikavälit, rajapäivät)
    val maksuttomuustietoEiSallittuSyyt =
      eiLaajennettuOppivelvollinenSyyt ++
        List(
          (!koulutusOppivelvollisuuskoulutukseksiKelpaavaa, "koulutus ei siirrettyjen tietojen perusteella kelpaa oppivelvollisuuden suorittamiseen (tarkista, että koulutuskoodi, käytetyn opetussuunnitelman perusteen diaarinumero, suorituksen tyyppi ja/tai suoritustapa ovat oikein)"),
          (opiskeluoikeusAlkanutHenkilönOllessaLiianVanha, s"opiskeluoikeus on merkitty alkavaksi vuonna, jona oppija täyttää enemmän kuin ${rajapäivät.maksuttomuusLoppuuIka} vuotta"),
          (kelpaamatonLukionVanhanOpsinOpiskeluoikeus,
            s"oppija on aloittanut vanhojen lukion opetussuunnitelman perusteiden mukaisen koulutuksen aiemmin kuin ${lukioVanhallaOpsillaSallittuAlkamisjakso.alku}"),
          (preIBMaksuttomuusTietoEiSallittu(opiskeluoikeus, rajapäivät), s"oppija on aloittanut Pre-IB opinnot aiemmin kuin ${rajapäivät.lakiVoimassaPeruskoulustaValmistuneillaAlku.format(FinnishDateFormat.finnishDateFormat)}"),
        ).filter(_._1).map(_._2)

    val maksuttomuustietojaSiirretty = maksuttomuustietoSiirretty || maksuttomuudenPidennysSiirretty

    if (maksuttomuustietoEiSallittuSyyt.nonEmpty) {
      HttpStatus.validate(!maksuttomuustietojaSiirretty) {
        val syyt = maksuttomuustietoEiSallittuSyyt.mkString(" ja ")
        KoskiErrorCategory.badRequest.validation(s"Tieto koulutuksen maksuttomuudesta ei ole relevantti tässä opiskeluoikeudessa, sillä $syyt.")
      }
    } else {
      HttpStatus.ok
    }
  }

  def preIBMaksuttomuusTietoEiSallittu(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, rajapäivät: ValpasRajapäivätService): Boolean = opiskeluoikeus.suoritukset.exists {
    case _: PreIBSuoritus2015 => opiskeluoikeus.alkamispäivä.exists(_.isBefore(rajapäivät.lakiVoimassaPeruskoulustaValmistuneillaAlku))
    case _ => false
  }

  // Huom! Valpas käyttää myös tätä funktiota!
  def oppivelvollisuudenSuorittamiseenKelpaavaMuuKuinPeruskoulunOpiskeluoikeus(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): Boolean =
    opiskeluoikeus.suoritukset.collectFirst {
      case myp: MYPVuosiluokanSuoritus
        if InternationalSchoolOpiskeluoikeus.onLukiotaVastaavaInternationalSchoolinSuoritus(myp.tyyppi.koodiarvo, myp.koulutusmoduuli.tunniste.koodiarvo) => myp
      case s: SuoritusVaatiiMahdollisestiMaksuttomuusTiedonOpiskeluoikeudelta => s
    }.isDefined

  // Huom! Valpas käyttää myös tätä funktiota!
  def eiOppivelvollisuudenLaajentamislainPiirissäSyyt(
    oppijanSyntymäpäivä: Option[LocalDate],
    perusopetuksenAikavälit: Seq[Päivämääräväli],
    rajapäivät: ValpasRajapäivätService
  ): List[String] =
  {
    val lakiVoimassaPeruskoulustaValmistuneille = rajapäivät.lakiVoimassaPeruskoulustaValmistuneillaAlku
    val lakiVoimassaVanhinSyntymäaika = rajapäivät.lakiVoimassaVanhinSyntymäaika


    // Oppijalla on Koskessa valmistumismerkintä peruskoulusta (tai vastaavasta) 31.12.2020 tai aiemmin
    val valmistunutPeruskoulustaEnnen2021 = perusopetuksenAikavälit.exists(p => p.vahvistuspäivä.exists(_.isBefore(lakiVoimassaPeruskoulustaValmistuneille)))

    val oppijanIkäOikeuttaaMaksuttomuuden = oppijanSyntymäpäivä.exists(bd => !lakiVoimassaVanhinSyntymäaika.isAfter(bd))

    List(
      (valmistunutPeruskoulustaEnnen2021, s"oppija on suorittanut oppivelvollisuutensa ennen ${lakiVoimassaPeruskoulustaValmistuneille.format(FinnishDateFormat.finnishDateFormat)} eikä tästä syystä kuulu laajennetun oppivelvollisuuden piiriin"),
      (oppijanSyntymäpäivä.isEmpty, "oppijan syntymäaika puuttuu oppijanumerorekisteristä"),
      (oppijanSyntymäpäivä.isDefined && !oppijanIkäOikeuttaaMaksuttomuuden, s"oppija on syntynyt ennen vuotta ${lakiVoimassaVanhinSyntymäaika.getYear()} eikä tästä syystä kuulu laajennetun oppivelvollisuuden piiriin"),
    ).filter(_._1).map(_._2)
  }

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
