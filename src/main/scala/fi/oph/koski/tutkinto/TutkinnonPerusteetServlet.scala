package fi.oph.koski.tutkinto

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{UserLanguage, Unauthenticated}
import fi.oph.koski.schema.{Koodistokoodiviite, LocalizedString}
import fi.oph.koski.servlet.{ApiServlet, Cached, KoskiSpecificApiServlet, LanguageSupport}

import scala.concurrent.duration.{Duration, _}

class TutkinnonPerusteetServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with Unauthenticated with Cached with LanguageSupport {
  private val perusteetService = new TutkinnonPerusteetService(application)

  get("/oppilaitos/:oppilaitosId") {
   renderEither[List[TutkintoPeruste]]((params.get("query"), params.get("oppilaitosId")) match {
     case (Some(query), Some(oppilaitosId)) if (query.length >= 3) => Right(application.tutkintoRepository.findTutkinnot(oppilaitosId, query))
     case _ => Left(KoskiErrorCategory.badRequest.queryParam.searchTermTooShort())
   })
  }

  get("/diaarinumerot/koulutustyyppi/:koulutustyypit") {
    val koulutustyypit: Set[Koodistokoodiviite] = params("koulutustyypit").split(",").map(t => Koodistokoodiviite(t, "koulutustyyppi")).toSet
    perusteetService.diaarinumerotByKoulutustyypit(koulutustyypit)
  }

  get("/diaarinumerot/suorituksentyyppi/:suorituksenTyyppi") {
    val koodistokoodiviite = Koodistokoodiviite(params("suorituksenTyyppi"), "suorituksentyyppi")
    perusteetService.diaarinumerotBySuorituksenTyyppi(koodistokoodiviite)
  }

  get("/tutkinnonosat/:diaari") {
    val ryhmä = params.get("tutkinnonOsanRyhmä")
    renderEither[LisättävätTutkinnonOsat](for {
      tutkinnonRakenne <- perusteenRakenne(failWhenNotFound = false)
      ryhmänRakenne <- haeRakenne(ryhmä)
    } yield {
      lisättävätTutkinnonOsat(ryhmä.map(toRyhmäkoodi), ryhmänRakenne, tutkinnonRakenne)
    })
  }

  get("/tutkinnonosat/ryhmat/:diaari/:suoritustapa") {
    val ryhmät: List[Koodistokoodiviite] = application.koodistoViitePalvelu.getKoodistoKoodiViitteet(application.koodistoPalvelu.getLatestVersionRequired("ammatillisentutkinnonosanryhma"))
    perusteenRakenne(failWhenNotFound = false).map(filterRyhmät(ryhmät)).getOrElse(Nil)
  }

  get("/peruste/:diaari/linkki") {
    val diaari = params("diaari")
    val lang = UserLanguage.sanitizeLanguage(params.get("lang")).getOrElse("fi")
    renderEither[Map[String, String]](
      application.ePerusteet.findLinkToEperusteetWeb(diaari, lang)
        .map(url => Map("url" -> url))
        .toRight(KoskiErrorCategory.notFound())
    )
  }

  private def haeRakenne(ryhmä: Option[String]) =
    ryhmä.map(r => perusteenRakenne()
      .map(_.flatMap(rakenneOsa => findRyhmä(toRyhmäkoodi(r), rakenneOsa))))
      .getOrElse(perusteenRakenne(failWhenNotFound = false))

  private def toRyhmäkoodi(ryhmä: String): Koodistokoodiviite =
    application.koodistoViitePalvelu.validate("ammatillisentutkinnonosanryhma", ryhmä)
      .getOrElse(haltWithStatus(KoskiErrorCategory.badRequest.validation.koodisto.tuntematonKoodi(s"Tuntematon tutkinnon osan ryhmä: $ryhmä")))

  private def lisättävätTutkinnonOsat(ryhmäkoodi: Option[Koodistokoodiviite], ryhmänRakenne: Iterable[RakenneOsa], tutkinto: Iterable[RakenneOsa]) = {
    val diaari: String = params("diaari")

    val määrittelemättömiä = (ryhmänRakenne.isEmpty || ryhmänRakenne.exists(_.sisältääMäärittelemättömiäOsia)) && ryhmäkoodi != yhteisetTutkinnonOsat
    val voiLisätäTutkinnonOsanToisestaTutkinnosta = if (isTelma(diaari)) false else määrittelemättömiä
    val osat = (if (määrittelemättömiä) tutkinto else ryhmänRakenne).flatMap(tutkinnonOsienKoodit).toList.distinct // Jos sisältää määrittelemättömiä, haetaan tutkinnon osia koko tutkinnon rakenteesta tähän ryhmään.

    LisättävätTutkinnonOsat(
      osat = osat,
      osaToisestaTutkinnosta = voiLisätäTutkinnonOsanToisestaTutkinnosta,
      paikallinenOsa = määrittelemättömiä
    )
  }

  private def isTelma(diaari: String) = {
    val telmaDiaarit = application.koodistoViitePalvelu.getSisältyvätKoodiViitteet(
      application.koodistoViitePalvelu.getLatestVersionRequired("koskikoulutustendiaarinumerot"),
      Koulutustyyppi.telma
    )

    telmaDiaarit match {
      case Some(diaarit) => diaarit.map(_.koodiarvo).contains(diaari)
      case None => false
    }
  }

  private def tutkinnonOsienKoodit(rakenne: Option[RakenneOsa]): List[Koodistokoodiviite] = rakenne.toList.flatMap(tutkinnonOsienKoodit)
  private def tutkinnonOsienKoodit(rakenneOsa: RakenneOsa): List[Koodistokoodiviite] = rakenneOsa.tutkinnonOsat.map(_.tunniste).distinct.sortBy(_.nimi.map(_.get(lang)))

  get("/suoritustavat/:diaari") {
    val diaari = params("diaari")
    renderEither[List[Koodistokoodiviite]](application.tutkintoRepository.findPerusteRakenne(diaari) match {
      case None => Left(KoskiErrorCategory.notFound.diaarinumeroaEiLöydy(s"Rakennetta ei löydy diaarinumerolla $diaari"))
      case Some(rakenne) => Right(rakenne.suoritustavat.map(_.suoritustapa))
    })
  }

  get[Map[String, TutkinnonOsanLaajuus]]("/tutkinnonosaryhma/laajuus/:diaari/:suoritustapa/") {
    Map.empty
  }

  get("/tutkinnonosaryhma/laajuus/:diaari/:suoritustapa/:ryhmat") {
    val ryhmät = params("ryhmat").split(',')
    val ryhmäkoodit: Array[Koodistokoodiviite] = ryhmät.map(ryhmä =>
      application.koodistoViitePalvelu.validate("ammatillisentutkinnonosanryhma", ryhmä)
        .getOrElse(haltWithStatus(KoskiErrorCategory.badRequest.validation.koodisto.tuntematonKoodi(s"Tuntematon tutkinnon osan ryhmä: $ryhmä")))
    )

    val diaari = params("diaari")
    val suoritustapa = params("suoritustapa")

    val laajuudet: Either[HttpStatus, List[(String, TutkinnonOsanLaajuus)]] = HttpStatus.foldEithers(ryhmäkoodit.map(rk => {
      perusteenRakenne().flatMap {
        case Nil => Left(KoskiErrorCategory.notFound.diaarinumeroaEiLöydy(s"Rakennetta ei löydy diaarinumerolla $diaari ja suoritustavalla $suoritustapa"))
        case List(osa) => Right(findRyhmä(rk, osa).map(_.tutkinnonRakenneLaajuus))
        case _ => Right(None)
      }.map(laajuus => (rk.koodiarvo, laajuus.getOrElse(TutkinnonOsanLaajuus(None, None))))
    }))

    renderEither[Map[String,TutkinnonOsanLaajuus]](laajuudet.map(_.toMap))
  }

  private def perusteenRakenne(failWhenNotFound: Boolean = true): Either[HttpStatus, List[RakenneOsa]] = {
    val diaari = params("diaari")
    val suoritustapa = params.get("suoritustapa")

    val rakenne: Option[TutkintoRakenne] = application.tutkintoRepository.findPerusteRakenne(diaari)
    val rakenteenSuoritustavat = rakenne.toList.flatMap(_.suoritustavat)
    val suoritustavat = suoritustapa match {
      case Some(suoritustapa) =>
        rakenteenSuoritustavat.filter(_.suoritustapa.koodiarvo == suoritustapa)
      case None =>
        rakenteenSuoritustavat
    }
    val rakenteet: List[RakenneOsa] = suoritustavat.flatMap(_.rakenne)
    rakenteet match {
      case Nil if failWhenNotFound =>
        Left(KoskiErrorCategory.notFound.diaarinumeroaEiLöydy(s"Rakennetta ei löydy diaarinumerolla $diaari ja suoritustavalla $suoritustapa"))
      case rakenteet =>
        Right(rakenteet)
    }
  }

  private def findRyhmä(ryhmä: Koodistokoodiviite, rakenneOsa: RakenneOsa): Option[RakenneModuuli] = {
    def nameMatches(nimi: LocalizedString): Boolean = {
      nimi.get("fi") == ryhmä.nimi.map(_.get("fi")).getOrElse("")
    }

    rakenneOsa match {
      case r: RakenneModuuli if nameMatches(r.nimi) =>
        Some(r)
      case r: RakenneModuuli =>
        r.osat.flatMap(findRyhmä(ryhmä, _)).headOption
      case _ => None
    }
  }

  private def filterRyhmät(ryhmät: List[Koodistokoodiviite])(rakenneOsat: List[RakenneOsa]) =
    ryhmät.filter { ryhmä =>
      rakenneOsat.exists(osa => findRyhmä(ryhmä, osa).isDefined)
    }

  private lazy val yhteisetTutkinnonOsat: Option[Koodistokoodiviite] = application.koodistoViitePalvelu.validate(Koodistokoodiviite("2", "ammatillisentutkinnonosanryhma"))

  override def cacheDuration: Duration = 1.hours
}

case class LisättävätTutkinnonOsat(osat: List[Koodistokoodiviite], osaToisestaTutkinnosta: Boolean, paikallinenOsa: Boolean)
