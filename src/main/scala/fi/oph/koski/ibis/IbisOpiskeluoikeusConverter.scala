package fi.oph.koski.ibis

import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.localization.LocalizationRepository
import fi.oph.koski.log.Logging
import fi.oph.koski.schema._
import fi.oph.koski.validation.ValidatingAndResolvingExtractor
import fi.oph.scalaschema.{SerializationContext, Serializer}

import java.time.LocalDate

// Koulutuksen opiskeluoikeus (IBOpiskeluoikeus tai InternationalSchoolOpiskeluoikeus), johon IBIS-rivi on yhdistetty
// iboTunnisteen perusteella. Diploman oppilaitos ja koulutustoimija kopioidaan siitä.
// TODO TOR-747: Yhdistäminen (haku iboTunnisteella) ja tuontipalvelu käyttöliittymineen puuttuvat vielä.
case class IbisKohde(
  oppijaOid: String,
  oppilaitos: Oppilaitos,
  koulutustoimija: Option[Koulutustoimija],
)

case class IbisKonversiovirhe(personalCode: String, syy: String)

class IbisOpiskeluoikeusConverter(
  koodistoViitePalvelu: KoodistoViitePalvelu,
  localizations: LocalizationRepository,
  validatingAndResolvingExtractor: ValidatingAndResolvingExtractor,
  oppiainekartta: IbisOppiainekartta,
) extends Logging {
  private val serializationContext = SerializationContext(KoskiSchema.schemaFactory)
  private val deserializationContext = KoskiSchema.strictDeserialization

  // TODO TOR-747: Vain myönnetyt diplomat (tuloskoodit D ja B) tuodaan. Hylätyn diploman (F, oppiainetulokset myönnetty),
  // CP:n (J, K) ja pelkkien kurssien (COURSE) käsittely on päättämättä.
  private val tuotavatKategoriat = Set(IbisCategory.Diploma, IbisCategory.Retake)
  private val tuotavatTulokset = Set(IbisResultCode.DiplomaAwarded, IbisResultCode.BilingualDiplomaAwarded)

  // vahvistuspäivä: todistuksen myöntämispäivä, jota ei ole IBIS-tiedostossa. TODO TOR-747: tuoja antaa sen eräkohtaisesti.
  def convert(record: IbisRecord, kohde: IbisKohde, vahvistuspäivä: LocalDate): Either[IbisKonversiovirhe, IBDiplomaOpiskeluoikeus] = {
    for {
      _ <- tarkistaTuotava(record)
      tulos <- koodi(record, "ibdiplomantulos", record.resultCode)
      istunto <- tutkintokerta(record, record.session)
      osasuoritukset <- sequence(arvioidutOppiaineet(record).map(convertSubject(record, vahvistuspäivä)))
      opiskeluoikeus <- täydennäKoodistoYmsMerkkijonot(record, IBDiplomaOpiskeluoikeus(
        oppilaitos = Some(kohde.oppilaitos),
        koulutustoimija = kohde.koulutustoimija,
        tila = IBDiplomaOpiskeluoikeudenTila(List(
          IBDiplomaOpiskeluoikeusjakso(vahvistuspäivä, Koodistokoodiviite("valmistunut", "koskiopiskeluoikeudentila"))
        )),
        suoritukset = List(IBDiplomanSuoritus(
          toimipiste = kohde.oppilaitos,
          vahvistus = Some(Päivämäärävahvistus(vahvistuspäivä, kohde.oppilaitos)),
          tutkintokerta = istunto,
          kokonaispistemäärä = record.totalPoints,
          lisäpisteet = record.eeTokPoints,
          tulos = tulos,
          osasuoritukset = Some(osasuoritukset),
        )),
        iboTunniste = record.personalCode,
      ))
    } yield opiskeluoikeus
  }

  private def tarkistaTuotava(record: IbisRecord): Either[IbisKonversiovirhe, Unit] = {
    if (!tuotavatKategoriat.contains(record.category)) {
      Left(IbisKonversiovirhe(record.personalCode, s"Kategoriaa ${record.category} ei tuoda Koskeen"))
    } else if (!tuotavatTulokset.contains(record.resultCode)) {
      Left(IbisKonversiovirhe(record.personalCode, s"Tuloskoodia '${record.resultCode}' ei tuoda Koskeen"))
    } else {
      Right(())
    }
  }

  // TODO TOR-747: Arvosanat N (no grade) ja P (pending) puuttuvat arviointiasteikkoib-koodistosta, joten ne jätetään pois.
  private def arvioidutOppiaineet(record: IbisRecord): List[IbisSubject] = {
    val (arvioimattomat, arvioidut) = record.subjects.partition(s => IbisSubject.gradelessGrades.contains(s.grade))
    arvioimattomat.foreach(s => logger.warn(s"IBIS: ohitetaan oppiaine ${s.code} (${s.level}) arvosanalla ${s.grade}"))
    arvioidut
  }

  private def convertSubject(record: IbisRecord, vahvistuspäivä: LocalDate)(subject: IbisSubject): Either[IbisKonversiovirhe, IBDiplomanOsasuoritus] = {
    // TODO TOR-747: Arviointipäiväksi merkitään vahvistuspäivä, koska IBIS ei kerro tulosten julkaisupäivää.
    val päivä = Some(vahvistuspäivä)
    for {
      istunto <- tutkintokerta(record, subject.session)
      osasuoritus <- subject.level match {
        case "EE" | "TK" =>
          for {
            tunniste <- koodi(record, "oppiaineetib", if (subject.level == "EE") "EE" else "TOK")
            aine <- sequenceOption(oppiainekartta.get(subject.code).filter(_ => subject.level == "EE").map(a => koodi(record, "oppiaineetib", a.tunniste)))
            arvosana <- koodi(record, "arviointiasteikkocorerequirementsib", subject.grade)
          } yield IBDiplomanCoreSuoritus(
            koulutusmoduuli = IBDiplomanCoreOppiaine(tunniste, aine, Some(subject.code)),
            tutkintokerta = istunto,
            arviointi = Some(List(IBDiplomanCoreArviointi(arvosana, päivä))),
          )
        case "HL" | "SL" =>
          for {
            oppiaine <- oppiainekartta.get(subject.code).toRight(IbisKonversiovirhe(record.personalCode, s"Tuntematon IBO:n oppiainekoodi ${subject.code}"))
            koulutusmoduuli <- oppiaineenKoulutusmoduuli(record, subject, oppiaine)
            arvosana <- koodi(record, "arviointiasteikkoib", subject.grade)
          } yield IBDiplomanOppiaineenSuoritus(
            koulutusmoduuli = koulutusmoduuli,
            tutkintokerta = istunto,
            arviointi = Some(List(IBDiplomanOppiaineenArviointi(arvosana, päivä))),
            lisäaine = subject.additional,
          )
        case muu =>
          Left(IbisKonversiovirhe(record.personalCode, s"Tuntematon oppiaineen taso $muu"))
      }
    } yield osasuoritus
  }

  private def oppiaineenKoulutusmoduuli(record: IbisRecord, subject: IbisSubject, oppiaine: IbisOppiaine): Either[IbisKonversiovirhe, IBDiplomanOppiaine] = {
    for {
      tunniste <- koodi(record, "oppiaineetib", oppiaine.tunniste)
      taso <- koodi(record, "oppiaineentasoib", subject.level)
      koulutusmoduuli <- oppiaine match {
        case IbisKieliOppiaine(_, kieli) => koodi(record, "kielivalikoima", kieli).map(IBDiplomanKieliOppiaine(tunniste, taso, _, Some(subject.code)))
        case IbisMuuOppiaine(_) => Right(IBDiplomanMuuOppiaine(tunniste, taso, Some(subject.code)))
      }
    } yield koulutusmoduuli
  }

  private def tutkintokerta(record: IbisRecord, session: IbisSession): Either[IbisKonversiovirhe, IBTutkintokerta] =
    IbisSession.lokalisointiavaimet.get(session.month)
      .map(avain => IBTutkintokerta(session.koodiarvo, session.year, localizations.get(avain)))
      .toRight(IbisKonversiovirhe(record.personalCode, s"Tuntematon tutkintokerran kuukausi ${session.month}"))

  private def koodi(record: IbisRecord, koodistoUri: String, koodiarvo: String): Either[IbisKonversiovirhe, Koodistokoodiviite] =
    koodistoViitePalvelu.validate(koodistoUri, koodiarvo)
      .toRight(IbisKonversiovirhe(record.personalCode, s"Koodiarvoa '$koodiarvo' ei löydy koodistosta $koodistoUri"))

  // Serialisoi ja deserialisoi opiskeluoikeus, jotta koodistoarvoille ja organisaatioille haetaan nimet samoin kuin
  // muille Koskeen tallennettaville opiskeluoikeuksille (vrt. YtrDownloadOppijaConverter).
  private def täydennäKoodistoYmsMerkkijonot(record: IbisRecord, opiskeluoikeus: IBDiplomaOpiskeluoikeus): Either[IbisKonversiovirhe, IBDiplomaOpiskeluoikeus] = {
    val json = Serializer.serialize(opiskeluoikeus, serializationContext)
    validatingAndResolvingExtractor.extract[IBDiplomaOpiskeluoikeus](deserializationContext)(json)
      .left.map(status => IbisKonversiovirhe(record.personalCode, s"Opiskeluoikeuden täydentäminen epäonnistui: ${status.errorString.getOrElse(status.statusCode.toString)}"))
  }

  private def sequence[A](xs: List[Either[IbisKonversiovirhe, A]]): Either[IbisKonversiovirhe, List[A]] =
    xs.partitionMap(identity) match {
      case (Nil, rights) => Right(rights)
      case (virhe :: _, _) => Left(virhe)
    }

  private def sequenceOption[A](x: Option[Either[IbisKonversiovirhe, A]]): Either[IbisKonversiovirhe, Option[A]] =
    x.map(_.map(Some(_))).getOrElse(Right(None))
}
