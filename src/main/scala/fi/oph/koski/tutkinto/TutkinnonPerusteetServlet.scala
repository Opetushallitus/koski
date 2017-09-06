package fi.oph.koski.tutkinto

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.Unauthenticated
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.schema.Koodistokoodiviite
import fi.oph.koski.servlet.{ApiServlet, Cached24Hours}

class TutkinnonPerusteetServlet(implicit val application: KoskiApplication) extends ApiServlet with Unauthenticated with Cached24Hours {
  get("/oppilaitos/:oppilaitosId") {
   (params.get("query"), params.get("oppilaitosId")) match {
     case (Some(query), Some(oppilaitosId)) if (query.length >= 3) => application.tutkintoRepository.findTutkinnot(oppilaitosId, query)
     case _ => KoskiErrorCategory.badRequest.queryParam.searchTermTooShort()
   }
  }

  get("/diaarinumerot/koulutustyyppi/:koulutustyyppi") {
    val koulutusTyyppi = params("koulutustyyppi")
    application.koodistoViitePalvelu.getSisältyvätKoodiViitteet(application.koodistoViitePalvelu.getLatestVersion("koskikoulutustendiaarinumerot").get, Koodistokoodiviite(koulutusTyyppi, "koulutustyyppi"))
  }

  get("/tutkinnonosat/:diaari/:suoritustapa") {
    perusteenRakenne.map(tutkinnonOsienKoodit)
  }

  get("/tutkinnonosat/:diaari/:suoritustapa/:ryhma") {
    val ryhmä = params("ryhma")
    val ryhmäkoodi = application.koodistoViitePalvelu.getKoodistoKoodiViite("ammatillisentutkinnonosanryhma", ryhmä).getOrElse(haltWithStatus(KoskiErrorCategory.badRequest.validation.koodisto.tuntematonKoodi(s"Tuntematon tutkinnon osan ryhmä: $ryhmä")))
    perusteenRakenne.map ( osa =>
      tutkinnonOsienKoodit(osa.flatMap(findRyhmä(ryhmäkoodi, _)))
    )
  }

  private def tutkinnonOsienKoodit(rakenne: Option[RakenneOsa]): List[Koodistokoodiviite] = rakenne match {
    case None => List.empty
    case Some(rakenneOsa) =>
      rakenneOsa.tutkinnonOsat.map(_.tunniste).distinct.sortBy(_.nimi.map(_.get(lang)))
  }

  get("/suoritustavat/:diaari") {
    val diaari = params("diaari")
    application.tutkintoRepository.findPerusteRakenne(diaari) match {
      case None => renderStatus(KoskiErrorCategory.notFound.diaarinumeroaEiLöydy("Rakennetta ei löydy diaarinumerolla $diaari"))
      case Some(rakenne) => rakenne.suoritustavat.map(_.suoritustapa)
    }
  }

  get("/tutkinnonosaryhma/laajuus/:diaari/:suoritustapa/") {
    List()
  }

  get("/tutkinnonosaryhma/laajuus/:diaari/:suoritustapa/:ryhmat") {
    val ryhmät = params("ryhmat").split(',')
    val ryhmäkoodit: Array[Koodistokoodiviite] = ryhmät.map(ryhmä =>
      application.koodistoViitePalvelu.getKoodistoKoodiViite("ammatillisentutkinnonosanryhma", ryhmä)
        .getOrElse(haltWithStatus(KoskiErrorCategory.badRequest.validation.koodisto.tuntematonKoodi(s"Tuntematon tutkinnon osan ryhmä: $ryhmä")))
    )

    val diaari = params("diaari")
    val suoritustapa = params("suoritustapa")

    val laajuudet: Array[Either[HttpStatus, Option[TutkinnonOsanLaajuus]]] = ryhmäkoodit.map(rk => {
      perusteenRakenne.flatMap {
        case Some(osa) => Right(findRyhmä(rk, osa).map(_.tutkinnonRakenneLaajuus))
        case None => Left(KoskiErrorCategory.notFound.diaarinumeroaEiLöydy(s"Rakennetta ei löydy diaarinumerolla $diaari ja suoritustavalla $suoritustapa"))
      }
    })

    ryhmät.zip(laajuudet).map(z => z._1 -> z._2.right.get.getOrElse(TutkinnonOsanLaajuus(None, None))).toMap
  }

  private def perusteenRakenne: Either[HttpStatus, Option[RakenneOsa]] = {
    val diaari = params("diaari")
    val suoritustapa = params("suoritustapa")

    val suoritustapaJaRakenne: Option[SuoritustapaJaRakenne] = application.tutkintoRepository.findPerusteRakenne(diaari).flatMap(_.suoritustavat.find(_.suoritustapa.koodiarvo == suoritustapa))
    suoritustapaJaRakenne match {
      case None =>
        Left(KoskiErrorCategory.notFound.diaarinumeroaEiLöydy(s"Rakennetta ei löydy diaarinumerolla $diaari ja suoritustavalla $suoritustapa"))
      case Some(s) =>
        Right(s.rakenne)
    }
  }

  private def findRyhmä(ryhmä: Koodistokoodiviite, rakenneOsa: RakenneOsa): Option[RakenneModuuli] = {
    def nameMatches(nimi: LocalizedString): Boolean = {
      def normalize(n: String): String = n.toLowerCase.replace("Vapaavalintaiset", "Vapaasti valittavat")
      normalize(nimi.get("fi")) == normalize(ryhmä.nimi.map(_.get("fi")).getOrElse(""))
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