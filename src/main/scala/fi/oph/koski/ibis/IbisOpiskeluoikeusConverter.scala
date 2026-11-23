package fi.oph.koski.ibis

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.localization.LocalizationRepository
import fi.oph.koski.schema._
import fi.oph.koski.validation.ValidatingAndResolvingExtractor
import fi.oph.scalaschema.{SerializationContext, Serializer}
import org.json4s.JsonAST.JString
import org.json4s.jackson.JsonMethods

import java.time.LocalDate

case class IbisKonversiovirhe(iboTunniste: String, syyt: List[String])

object IbisKonversiovirhe {
  def syyt(status: HttpStatus): List[String] = status.errors.map(_.message match {
    case JString(syy) => syy
    case syy => JsonMethods.compact(syy)
  })
}

class IbisOpiskeluoikeusConverter(
  koodistoViitePalvelu: KoodistoViitePalvelu,
  localizations: LocalizationRepository,
  validatingAndResolvingExtractor: ValidatingAndResolvingExtractor,
) {
  // Virheet kerätään listaan, jotta rivin kaikki virheet saadaan raportille kerralla.
  private type Muunnos[A] = Either[List[String], A]

  private val serializationContext = SerializationContext(KoskiSchema.schemaFactory)
  private val deserializationContext = KoskiSchema.strictDeserialization

  // TODO TOR-747: Vain myönnetyt diplomat (tuloskoodit D ja B) tuodaan. Tuloskoodin F (Failed diploma, individual course awarded),
  // CP:n (J, K) ja CATEGORY-arvon COURSE käsittely on päättämättä.
  private val tuotavatCategoryArvot = Set(IbisCategory.Diploma, IbisCategory.Retake)
  private val tuotavatTulokset = Set(IbisResultCode.DiplomaAwarded, IbisResultCode.BilingualDiplomaAwarded)

  // vahvistuspäivä: todistuksen myöntämispäivä, jota ei ole IBIS-tiedostossa.
  def convert(record: IbisRecord, oppija: IbisYhdistettyOppija, vahvistuspäivä: LocalDate): Either[IbisKonversiovirhe, IBDiplomaOpiskeluoikeus] = {
    val opiskeluoikeus = for {
      // Riviltä, jota ei tuoda, ei kerätä muita virheitä.
      _ <- tarkistaTuotava(record)
      diploma <- yhdessä(
        koodi("ibdiplomaresult", record.resultCode),
        tutkintokerta(record.session),
        kaikki(arvioidutOppiaineet(record).map(convertSubject(vahvistuspäivä))),
      ) { (result, kerta, osasuoritukset) =>
        IBDiplomaOpiskeluoikeus(
          oid = oppija.diplomanOid,
          oppilaitos = Some(oppija.oppilaitos),
          tila = IBDiplomaOpiskeluoikeudenTila(List(
            IBDiplomaOpiskeluoikeusjakso(vahvistuspäivä, Koodistokoodiviite("valmistunut", "koskiopiskeluoikeudentila"))
          )),
          suoritukset = List(IBDiplomanSuoritus(
            toimipiste = oppija.oppilaitos,
            vahvistus = Some(Päivämäärävahvistus(vahvistuspäivä, oppija.oppilaitos)),
            tutkintokerta = kerta,
            totalPoints = record.totalPoints,
            combinedGrade = record.combinedGrade,
            result = result,
            osasuoritukset = Some(osasuoritukset),
          )),
          iboTunniste = record.iboTunniste,
        )
      }
      täydennetty <- täydennäKoodistoYmsMerkkijonot(diploma)
    } yield täydennetty
    // Sama virhe, kuten tuntematon kuukausi, voi toistua usean oppiaineen kohdalla.
    opiskeluoikeus.left.map(syyt => IbisKonversiovirhe(record.iboTunniste, syyt.distinct))
  }

  // Tuonnin raportille: tiedot, jotka jätetään pois hylkäämättä riviä.
  def varoitukset(record: IbisRecord): List[String] = record.subjects.flatMap { subject =>
    if (IbisSubject.noGradeCodes.contains(subject.grade)) {
      List(s"Oppiaine ${subject.name} (${subject.level}) jätetään pois, koska arvosanan tilalla on ${subject.grade} (${IbisSubject.noGradeCodes(subject.grade)})")
    } else {
      List(
        Option.when(subject.level == "EE" && IbisOppiainekartta.get(subject.name).isEmpty)(
          s"Extended essayn aineelle ${subject.name} ei ole vastinetta Koskessa, joten aine jätetään pois"
        ),
        Option.when(subject.language.trim.nonEmpty && suorituskieli(subject).isEmpty)(
          s"Oppiaineen ${subject.name} (${subject.level}) kielelle ${subject.language} ei ole vastinetta Koskessa, joten suorituskieli jätetään pois"
        ),
      ).flatten
    }
  }

  private def tarkistaTuotava(record: IbisRecord): Muunnos[Unit] = {
    if (!tuotavatCategoryArvot.contains(record.category)) {
      Left(List(s"CATEGORY-arvoa ${record.category} ei tuoda Koskeen"))
    } else if (!tuotavatTulokset.contains(record.resultCode)) {
      Left(List(s"Tuloskoodia '${record.resultCode}' ei tuoda Koskeen"))
    } else {
      Right(())
    }
  }

  // TODO TOR-747: Oppiaineet ilman arvosanaa (N tai P) jätetään pois. Koodit eivät ole arvosanoja: arviointiasteikkoib ei tunne
  // niitä, ja arviointiasteikkocorerequirementsib:n P tarkoittaa hyväksyttyä (pass) eikä odottavaa tulosta.
  private def arvioidutOppiaineet(record: IbisRecord): List[IbisSubject] =
    record.subjects.filterNot(s => IbisSubject.noGradeCodes.contains(s.grade))

  private def convertSubject(vahvistuspäivä: LocalDate)(subject: IbisSubject): Muunnos[IBDiplomanOsasuoritus] = {
    // TODO TOR-747: Arviointipäiväksi merkitään vahvistuspäivä, koska IBIS ei kerro tulosten julkaisupäivää.
    val päivä = Some(vahvistuspäivä)
    val kieli = suorituskieli(subject)
    subject.level match {
      // Extended essayn aine on vapaaehtoinen tieto, joten vastineeton aine ei hylkää riviä kuten HL/SL-oppiaine.
      case "EE" =>
        val essay = sequenceOption(IbisOppiainekartta.get(subject.name).map(diplomanOppiaine(_, taso = None)))
          .map(aine => IBDiplomanExtendedEssay(aine = aine))
        coreSuoritus(subject, essay, päivä, kieli)
      case "TK" =>
        coreSuoritus(subject, Right(IBDiplomanTheoryOfKnowledge()), päivä, kieli)
      case "HL" | "SL" =>
        val koulutusmoduuli = for {
          oppiaine <- IbisOppiainekartta.get(subject.name).toRight(List(s"IBO:n oppiaineelle '${subject.name}' ei ole vastinetta Koskessa"))
          taso <- koodi("oppiaineentasoib", subject.level)
          moduuli <- diplomanOppiaine(oppiaine, Some(taso))
        } yield moduuli
        yhdessä(koulutusmoduuli, tutkintokerta(subject.session), koodi("arviointiasteikkoib", subject.grade)) { (moduuli, kerta, arvosana) =>
          IBDiplomanOppiaineenSuoritus(
            koulutusmoduuli = moduuli,
            tutkintokerta = kerta,
            arviointi = Some(List(IBDiplomanOppiaineenArviointi(arvosana, päivä))),
            suorituskieli = kieli,
            additionalSubject = subject.additional,
          )
        }
      case muu =>
        Left(List(s"Tuntematon oppiaineen taso $muu"))
    }
  }

  private def coreSuoritus(
    subject: IbisSubject,
    koulutusmoduuli: Muunnos[IBDiplomanCoreOppiaine],
    päivä: Option[LocalDate],
    kieli: Option[Koodistokoodiviite],
  ): Muunnos[IBDiplomanCoreSuoritus] =
    yhdessä(koulutusmoduuli, tutkintokerta(subject.session), koodi("arviointiasteikkocorerequirementsib", subject.grade)) { (moduuli, kerta, arvosana) =>
      IBDiplomanCoreSuoritus(
        koulutusmoduuli = moduuli,
        tutkintokerta = kerta,
        arviointi = Some(List(IBDiplomanCoreArviointi(arvosana, päivä))),
        suorituskieli = kieli,
      )
    }

  // Suorituskieli on vapaaehtoinen tieto, joten vastineeton kieli ei hylkää riviä.
  private def suorituskieli(subject: IbisSubject): Option[Koodistokoodiviite] =
    IbisKielikartta.get(subject.language).flatMap(koodiarvo => koodistoViitePalvelu.validate("kieli", koodiarvo))

  private def diplomanOppiaine(oppiaine: IbisOppiaine, taso: Option[Koodistokoodiviite]): Muunnos[IBDiplomanOppiaine] =
    koodi("oppiaineetib", oppiaine.tunniste).flatMap { tunniste =>
      oppiaine match {
        case IbisKieliOppiaine(_, kieli) => koodi("kielivalikoima", kieli).map(IBDiplomanKieliOppiaine(tunniste, taso, _))
        case IbisMuuOppiaine(_) => Right(IBDiplomanMuuOppiaine(tunniste, taso))
      }
    }

  private def tutkintokerta(session: IbisSession): Muunnos[IBTutkintokerta] =
    IbisSession.lokalisointiavaimet.get(session.month)
      .map(avain => IBTutkintokerta(session.koodiarvo, session.year, localizations.get(avain)))
      .toRight(List(s"Tuntematon tutkintokerran kuukausi ${session.month}"))

  private def koodi(koodistoUri: String, koodiarvo: String): Muunnos[Koodistokoodiviite] =
    koodistoViitePalvelu.validate(koodistoUri, koodiarvo)
      .toRight(List(s"Koodiarvoa '$koodiarvo' ei löydy koodistosta $koodistoUri"))

  // Serialisoi ja deserialisoi opiskeluoikeus, jotta koodistoarvoille ja organisaatioille haetaan nimet samoin kuin
  // muille Koskeen tallennettaville opiskeluoikeuksille (vrt. YtrDownloadOppijaConverter).
  private def täydennäKoodistoYmsMerkkijonot(opiskeluoikeus: IBDiplomaOpiskeluoikeus): Muunnos[IBDiplomaOpiskeluoikeus] = {
    val json = Serializer.serialize(opiskeluoikeus, serializationContext)
    validatingAndResolvingExtractor.extract[IBDiplomaOpiskeluoikeus](deserializationContext)(json)
      .left.map(status => IbisKonversiovirhe.syyt(status).map(syy => s"Opiskeluoikeuden täydentäminen epäonnistui: $syy"))
  }

  private def yhdessä[A, B, C, D](a: Muunnos[A], b: Muunnos[B], c: Muunnos[C])(f: (A, B, C) => D): Muunnos[D] =
    (a, b, c) match {
      case (Right(x), Right(y), Right(z)) => Right(f(x, y, z))
      case _ => Left(List(a, b, c).flatMap(_.left.getOrElse(Nil)))
    }

  private def kaikki[A](muunnokset: List[Muunnos[A]]): Muunnos[List[A]] =
    muunnokset.partitionMap(identity) match {
      case (Nil, arvot) => Right(arvot)
      case (virheet, _) => Left(virheet.flatten)
    }

  private def sequenceOption[A](x: Option[Muunnos[A]]): Muunnos[Option[A]] =
    x.map(_.map(Some(_))).getOrElse(Right(None))
}
