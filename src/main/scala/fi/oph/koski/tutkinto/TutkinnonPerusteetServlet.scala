package fi.oph.koski.tutkinto

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.Unauthenticated
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.schema.Koodistokoodiviite
import fi.oph.koski.servlet.{ApiServlet, Cached24Hours}

class TutkinnonPerusteetServlet(implicit val application: KoskiApplication) extends ApiServlet with Unauthenticated with Cached24Hours {
  get("/oppilaitos/:oppilaitosId") {
   renderEither((params.get("query"), params.get("oppilaitosId")) match {
     case (Some(query), Some(oppilaitosId)) if (query.length >= 3) => Right(application.tutkintoRepository.findTutkinnot(oppilaitosId, query))
     case _ => Left(KoskiErrorCategory.badRequest.queryParam.searchTermTooShort())
   })
  }

  get("/diaarinumerot/koulutustyyppi/:koulutustyyppi") {
    val koulutusTyyppi = params("koulutustyyppi")
    application.koodistoViitePalvelu.getSisältyvätKoodiViitteet(application.koodistoViitePalvelu.getLatestVersion("koskikoulutustendiaarinumerot").get, Koodistokoodiviite(koulutusTyyppi, "koulutustyyppi"))
  }

  get("/tutkinnonosat/:diaari") {
    val ryhmä = params.get("tutkinnonOsanRyhmä")
    renderEither(for {
      tutkinnonRakenne <- perusteenRakenne
      ryhmänRakenne <- ryhmä match {
        case None => perusteenRakenne
        case Some(ryhmä: String) =>
          val ryhmäkoodi = application.koodistoViitePalvelu.getKoodistoKoodiViite("ammatillisentutkinnonosanryhma", ryhmä).getOrElse(haltWithStatus(KoskiErrorCategory.badRequest.validation.koodisto.tuntematonKoodi(s"Tuntematon tutkinnon osan ryhmä: $ryhmä")))
          perusteenRakenne.map(rakenne => rakenne.flatMap((rakenneOsa: RakenneOsa) => findRyhmä(ryhmäkoodi, rakenneOsa)))
      }
    } yield {
      lisättävätTutkinnonOsat(ryhmänRakenne, tutkinnonRakenne)
    })
  }

  private def lisättävätTutkinnonOsat(ryhmä: Iterable[RakenneOsa], tutkinto: Iterable[RakenneOsa]) = {
    val määrittelemättömiä = if (ryhmä.isEmpty) true else ryhmä.exists(_.sisältääMäärittelemättömiäOsia)
    val osat = (if (määrittelemättömiä) tutkinto else ryhmä).flatMap(tutkinnonOsienKoodit).toList.distinct // Jos sisältää määrittelemättömiä, haetaan tutkinnon osia koko tutkinnon rakenteesta tähän ryhmään.
    LisättävätTutkinnonOsat(osat, määrittelemättömiä, määrittelemättömiä)
  }
  private def tutkinnonOsienKoodit(rakenne: Option[RakenneOsa]): List[Koodistokoodiviite] = rakenne.toList.flatMap(tutkinnonOsienKoodit)
  private def tutkinnonOsienKoodit(rakenneOsa: RakenneOsa): List[Koodistokoodiviite] = rakenneOsa.tutkinnonOsat.map(_.tunniste).distinct.sortBy(_.nimi.map(_.get(lang)))

  get("/suoritustavat/:diaari") {
    val diaari = params("diaari")
    renderEither(application.tutkintoRepository.findPerusteRakenne(diaari) match {
      case None => Left(KoskiErrorCategory.notFound.diaarinumeroaEiLöydy("Rakennetta ei löydy diaarinumerolla $diaari"))
      case Some(rakenne) => Right(rakenne.suoritustavat.map(_.suoritustapa))
    })
  }

  get[Map[String, TutkinnonOsanLaajuus]]("/tutkinnonosaryhma/laajuus/:diaari/:suoritustapa/") {
    Map.empty
  }

  get[Map[String, TutkinnonOsanLaajuus]]("/tutkinnonosaryhma/laajuus/:diaari/:suoritustapa/:ryhmat") {
    val ryhmät = params("ryhmat").split(',')
    val ryhmäkoodit: Array[Koodistokoodiviite] = ryhmät.map(ryhmä =>
      application.koodistoViitePalvelu.getKoodistoKoodiViite("ammatillisentutkinnonosanryhma", ryhmä)
        .getOrElse(haltWithStatus(KoskiErrorCategory.badRequest.validation.koodisto.tuntematonKoodi(s"Tuntematon tutkinnon osan ryhmä: $ryhmä")))
    )

    val diaari = params("diaari")
    val suoritustapa = params("suoritustapa")

    val laajuudet: Array[Either[HttpStatus, Option[TutkinnonOsanLaajuus]]] = ryhmäkoodit.map(rk => {
      perusteenRakenne.flatMap {
        case Nil => Left(KoskiErrorCategory.notFound.diaarinumeroaEiLöydy(s"Rakennetta ei löydy diaarinumerolla $diaari ja suoritustavalla $suoritustapa"))
        case List(osa) => Right(findRyhmä(rk, osa).map(_.tutkinnonRakenneLaajuus))
        case _ => Right(None)
      }
    })

    ryhmät.zip(laajuudet).map(z => z._1 -> z._2.right.get.getOrElse(TutkinnonOsanLaajuus(None, None))).toMap
  }

  private def perusteenRakenne: Either[HttpStatus, List[RakenneOsa]] = {
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
    val rakenteet: List[RakenneOsa] = suoritustavat.flatMap(_.rakenne).toList
    rakenteet match {
      case Nil =>
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
}

case class LisättävätTutkinnonOsat(osat: List[Koodistokoodiviite], osaToisestaTutkinnosta: Boolean, paikallinenOsa: Boolean)