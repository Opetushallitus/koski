package fi.oph.koski.ibis

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession}
import fi.oph.koski.log.KoskiOperation.{IBIS_ESIKATSELU, IBIS_TALLENNUS, KoskiOperation}
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, KoskiAuditLogMessageField}
import fi.oph.koski.schema.{IBDiplomaOpiskeluoikeus, IBOpiskeluoikeus, OidHenkilö, Oppija}

import java.time.LocalDate

case class IbisYhdistettyOppija(
  oppijaOid: String,
  diplomanOid: Option[String],
)

case class IbisTuonninRaportti(
  tallennettu: Boolean,
  jäsennysvirheet: List[IbisParseError],
  rivit: List[IbisTuonninRivi],
)

case class IbisTuonninRivi(
  iboTunniste: String,
  tila: String,
  oppijaOid: Option[String] = None,
  diplomanOid: Option[String] = None,
  virheet: List[String] = Nil,
  varoitukset: List[String] = Nil,
)

object IbisTuonninRivinTila {
  val Uusi = "uusi"
  val Päivitys = "päivitys"
  val Hylätty = "hylätty"
}

class IbisService(application: KoskiApplication) {
  // TODO TOR-747: Vahvistuspäivä on kiinteä, kunnes PO päättää, mistä se saadaan.
  private val vahvistuspäivä = LocalDate.of(2026, 7, 6)

  // Audit-lokiviestin pituus on rajattu (ks. OpiskeluoikeusOiditAuditLog).
  private val tunnisteitaAuditLokiviestissä = 100

  private lazy val repository = new IbisOpiskeluoikeusRepository(application.masterDatabase.db)
  private lazy val converter = new IbisOpiskeluoikeusConverter(
    application.koodistoViitePalvelu,
    application.validatingAndResolvingExtractor,
  )

  // Validoi diplomat kuten tallennus, mutta ei tallenna niitä.
  def esikatsele(tiedosto: Array[Byte])(implicit user: KoskiSpecificSession): IbisTuonninRaportti =
    käsitteleCsv(tiedosto, IBIS_ESIKATSELU, tallennettu = false) { (oppija, diploma) =>
      validoi(oppija, diploma).map(_ => oppija.diplomanOid)
    }

  def tuo(tiedosto: Array[Byte])(implicit user: KoskiSpecificSession): IbisTuonninRaportti =
    käsitteleCsv(tiedosto, IBIS_TALLENNUS, tallennettu = true) { (oppija, diploma) =>
      // createOrUpdate ei aja KoskiValidatoria.
      validoi(oppija, diploma)
        .flatMap(validoitu => application.oppijaFacade.createOrUpdate(validoitu, allowUpdate = true))
        .map(_.opiskeluoikeudet.headOption.map(_.oid))
    }

  private def validoi(oppija: IbisYhdistettyOppija, diploma: IBDiplomaOpiskeluoikeus)(implicit user: KoskiSpecificSession): Either[HttpStatus, Oppija] =
    application.validator.updateFieldsAndValidateAsJson(Oppija(OidHenkilö(oppija.oppijaOid), List(diploma)))(user, AccessType.write)

  // käsitteleDiploma palauttaa diploman oidin, jos se on tiedossa.
  private def käsitteleCsv(tiedosto: Array[Byte], operaatio: KoskiOperation, tallennettu: Boolean)(
    käsitteleDiploma: (IbisYhdistettyOppija, IBDiplomaOpiskeluoikeus) => Either[HttpStatus, Option[String]]
  )(implicit user: KoskiSpecificSession): IbisTuonninRaportti = {
    val (jäsennysvirheet, rivit) = IbisCsvParser.parse(tiedosto)
    rivit.map(_.iboTunniste).grouped(tunnisteitaAuditLokiviestissä).foreach { tunnisteet =>
      AuditLog.log(KoskiAuditLogMessage(operaatio, user, Map(KoskiAuditLogMessageField.iboTunniste -> tunnisteet.mkString(","))))
    }
    IbisTuonninRaportti(
      tallennettu = tallennettu,
      jäsennysvirheet = jäsennysvirheet,
      rivit = yhdistäOppijoihin(rivit).map {
        case (rivi, Left(virhe)) =>
          IbisTuonninRivi(rivi.iboTunniste, IbisTuonninRivinTila.Hylätty, virheet = virhe.syyt)
        case (rivi, Right(oppija)) =>
          val tulos = converter.convert(rivi, oppija.diplomanOid, vahvistuspäivä).flatMap { diploma =>
            käsitteleDiploma(oppija, diploma).left.map(status => IbisKonversiovirhe(rivi.iboTunniste, IbisKonversiovirhe.syyt(status)))
          }
          IbisTuonninRivi(
            iboTunniste = rivi.iboTunniste,
            tila = tulos.fold(
              _ => IbisTuonninRivinTila.Hylätty,
              _ => if (oppija.diplomanOid.isDefined) IbisTuonninRivinTila.Päivitys else IbisTuonninRivinTila.Uusi,
            ),
            oppijaOid = Some(oppija.oppijaOid),
            diplomanOid = tulos.toOption.flatten.orElse(oppija.diplomanOid),
            virheet = tulos.fold(_.syyt, _ => Nil),
            varoitukset = converter.varoitukset(rivi),
          )
      },
    )
  }

  def yhdistäOppijoihin(rivit: List[IbisRecord]): List[(IbisRecord, Either[IbisKonversiovirhe, IbisYhdistettyOppija])] = {
    val tunnisteet = rivit.map(_.iboTunniste.toLowerCase)
    val löydetyt = masterOideilla(repository.findByIboTunnisteet(tunnisteet.toSet))
    rivit.zip(tunnisteet).map { case (rivi, tunniste) =>
      if (tunnisteet.count(_ == tunniste) > 1) {
        rivi -> Left(IbisKonversiovirhe(rivi.iboTunniste, List("Sama IBO-tunniste on tiedostossa usealla rivillä")))
      } else {
        rivi -> IbisService.yhdistäOppijaan(rivi.iboTunniste, löydetyt.filter(_.iboTunniste == tunniste))
      }
    }
  }

  // Saman henkilön opiskeluoikeudet voivat olla linkitetyillä oideilla, joten ne yhdistetään master-oidiin.
  private def masterOideilla(löydetyt: Seq[IbisTunnisteenOpiskeluoikeus]): Seq[IbisTunnisteenOpiskeluoikeus] =
    if (löydetyt.isEmpty) {
      löydetyt
    } else {
      val masterit = application.opintopolkuHenkilöFacade.findMasterOppijat(löydetyt.map(_.oppijaOid).distinct.toList)
      löydetyt.map(löydetty => löydetty.copy(oppijaOid = masterit.get(löydetty.oppijaOid).map(_.oid).getOrElse(löydetty.oppijaOid)))
    }
}

object IbisService {
  def yhdistäOppijaan(iboTunniste: String, löydetyt: Seq[IbisTunnisteenOpiskeluoikeus]): Either[IbisKonversiovirhe, IbisYhdistettyOppija] = {
    val ibKoulutukset = löydetyt.filter(_.opiskeluoikeus.isInstanceOf[IBOpiskeluoikeus])
    val valmistuneetIbKoulutukset = ibKoulutukset.filter(_.opiskeluoikeus.tila.opiskeluoikeusjaksot.lastOption.exists(_.tila.koodiarvo == "valmistunut"))
    val diplomat = löydetyt.filter(_.opiskeluoikeus.isInstanceOf[IBDiplomaOpiskeluoikeus])
    val oppijat = (valmistuneetIbKoulutukset ++ diplomat).map(_.oppijaOid).distinct

    if (oppijat.size > 1) {
      Left(IbisKonversiovirhe(iboTunniste, List("Tunnisteella löytyy usean oppijan opiskeluoikeuksia")))
    } else if (diplomat.size > 1) {
      Left(IbisKonversiovirhe(iboTunniste, List("Tunnisteella löytyy useampi IB Diploma -opiskeluoikeus")))
    } else {
      oppijat.headOption match {
        case Some(oppijaOid) =>
          Right(IbisYhdistettyOppija(oppijaOid, diplomat.headOption.flatMap(_.opiskeluoikeus.oid)))
        case None if ibKoulutukset.nonEmpty =>
          Left(IbisKonversiovirhe(iboTunniste, List("Tunnisteella löytyy IB-koulutuksen opiskeluoikeus, mutta se ei ole valmistunut")))
        case None =>
          Left(IbisKonversiovirhe(iboTunniste, List("Tunnisteella ei löydy valmistunutta IB-koulutuksen opiskeluoikeutta eikä IB Diploma -opiskeluoikeutta")))
      }
    }
  }
}
