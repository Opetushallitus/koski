package fi.oph.koski.validation

import com.typesafe.config.Config
import fi.oph.koski.config.Environment
import fi.oph.koski.documentation.ExamplesEsiopetus.{peruskoulunEsiopetuksenTunniste, päiväkodinEsiopetuksenTunniste}
import fi.oph.koski.eperusteetvalidation.{EPerusteetFiller, EPerusteetLops2019Validator, EPerusteisiinPerustuvaValidator}
import fi.oph.koski.fixture.ValidationTestContext
import fi.oph.koski.henkilo.HenkilöRepository
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession, OoPtsMask}
import fi.oph.koski.opiskeluoikeus.KoskiOpiskeluoikeusRepository
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.schema.Opiskeluoikeus.koulutustoimijaTraversal
import fi.oph.koski.schema._
import fi.oph.koski.suostumus.SuostumuksenPeruutusService
import fi.oph.koski.tutkinto.Koulutustyyppi._
import fi.oph.koski.util.DateOrdering.{localDateOptionOrdering, localDateOrdering}
import fi.oph.koski.util.{FinnishDateFormat, Timing}
import fi.oph.koski.validation.DateValidation._
import fi.oph.scalaschema.ExtractionContext
import mojave._
import org.json4s.{JArray, JValue}

import java.lang.Character.isDigit
import java.time.LocalDate

// scalastyle:off line.size.limit
// scalastyle:off number.of.methods

class KoskiValidator(
  organisaatioRepository: OrganisaatioRepository,
  koskiOpiskeluoikeudet: KoskiOpiskeluoikeusRepository,
  henkilöRepository: HenkilöRepository,
  ePerusteetValidator: EPerusteisiinPerustuvaValidator,
  ePerusteetLops2019Validator: EPerusteetLops2019Validator,
  ePerusteetFiller: EPerusteetFiller,
  validatingAndResolvingExtractor: ValidatingAndResolvingExtractor,
  suostumuksenPeruutusService: SuostumuksenPeruutusService,
  koodistoPalvelu: KoodistoViitePalvelu,
  config: Config,
  validationConfig: ValidationTestContext,
) extends Timing {
  def updateFieldsAndValidateAsJson(oppija: Oppija)(implicit user: KoskiSpecificSession, accessType: AccessType.Value): Either[HttpStatus, Oppija] = {
    val serialized = timed("Oppija serialization", 500) {
      JsonSerializer.serialize(oppija)
    }
    extractUpdateFieldsAndValidateOppija(serialized)
  }

  def extractUpdateFieldsAndValidateBatch(oppijatJson: JArray)(implicit user: KoskiSpecificSession, accessType: AccessType.Value): List[(Either[HttpStatus, Oppija], JValue)] = {
    timed("extractAndValidateBatch") {
      oppijatJson.arr.par.map { oppijaJson =>
        (extractUpdateFieldsAndValidateOppija(oppijaJson), oppijaJson)
      }.toList
    }
  }

  def extractUpdateFieldsAndValidateOppija(parsedJson: JValue)(implicit user: KoskiSpecificSession, accessType: AccessType.Value): Either[HttpStatus, Oppija] = {
    timed("extractAndValidateOppija", 200) {
      extractOppija(parsedJson).right.flatMap(updateFieldsAndValidateOpiskeluoikeudet)
    }
  }

  def extractUpdateFieldsAndValidateOpiskeluoikeus(parsedJson: JValue)(implicit user: KoskiSpecificSession, accessType: AccessType.Value): Either[HttpStatus, Opiskeluoikeus] = {
    timed("extractAndValidateOpiskeluoikeus") {
      extractOpiskeluoikeus(parsedJson).right.flatMap { opiskeluoikeus =>
        updateFieldsAndValidateOpiskeluoikeus(opiskeluoikeus, None)
      }
    }
  }

  def extractOppija(parsedJson: JValue, deserialization: ExtractionContext = strictDeserialization): Either[HttpStatus, Oppija] = {
    timed("extractOppija")(
      validatingAndResolvingExtractor.extract[Oppija](deserialization)(parsedJson)
    )
  }

  def extractOpiskeluoikeus(parsedJson: JValue): Either[HttpStatus, Opiskeluoikeus] = {
    timed("extractOpiskeluoikeus")(
      validatingAndResolvingExtractor.extract[Opiskeluoikeus](strictDeserialization)(parsedJson)
    )
  }

  def updateFieldsAndValidateOpiskeluoikeudet(oppija: Oppija)(implicit user: KoskiSpecificSession, accessType: AccessType.Value): Either[HttpStatus, Oppija] = {
    val results: Seq[Either[HttpStatus, Opiskeluoikeus]] = oppija.opiskeluoikeudet.map( oo => {
      val timedBlockname = s"updateFieldsAndValidateOpiskeluoikeus ${oo.tyyppi.koodiarvo}"
      timed(timedBlockname) {
        updateFieldsAndValidateOpiskeluoikeus(oo, Some(oppija.henkilö))
      }
    })
    HttpStatus.foldEithers(results).right.flatMap {
      case Nil => Left(KoskiErrorCategory.badRequest.validation.tyhjäOpiskeluoikeusLista())
      case opiskeluoikeudet => Right(oppija.copy(opiskeluoikeudet = opiskeluoikeudet))
    }
  }

  def updateFieldsAndValidateOpiskeluoikeus(ytrOpiskeluoikeus: YlioppilastutkinnonOpiskeluoikeus, henkilö: Option[Henkilö])(implicit user: KoskiSpecificSession, accessType: AccessType.Value): Either[HttpStatus, Opiskeluoikeus] = {
    updateFieldsAndValidateOpiskeluoikeus(ytrOpiskeluoikeus.asInstanceOf[Opiskeluoikeus], henkilö)
  }

  def updateFieldsAndValidateOpiskeluoikeus(opiskeluoikeus: Opiskeluoikeus, henkilö: Option[Henkilö])(implicit user: KoskiSpecificSession, accessType: AccessType.Value): Either[HttpStatus, Opiskeluoikeus] = {
    opiskeluoikeus match {
      case opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus if opiskeluoikeus.mitätöity &&
        (!opiskeluoikeus.isInstanceOf[YlioppilastutkinnonOpiskeluoikeus] ||
          user.hasTallennetutYlioppilastutkinnonOpiskeluoikeudetAccess) =>
        updateFields(opiskeluoikeus, lipsuTarvittaessaVirheistäMitätöinnissä = true)
          .flatMap(validateMitätöinti)
      case opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus if
        (!opiskeluoikeus.isInstanceOf[YlioppilastutkinnonOpiskeluoikeus] ||
         user.hasTallennetutYlioppilastutkinnonOpiskeluoikeudetAccess) =>
        updateFields(opiskeluoikeus).flatMap { oo =>
          if (validationConfig.validoiOpiskeluoikeudet) {
            validateOpiskeluoikeus(oo, henkilö)
          } else {
            Right(oo)
          }
        }
      case _ if accessType == AccessType.write => Left(KoskiErrorCategory.notImplemented.readOnly("Korkeakoulutuksen opiskeluoikeuksia ja ylioppilastutkintojen tietoja ei voi päivittää Koski-järjestelmässä"))
      case _ => Right(opiskeluoikeus)
    }
  }

  private def validateOpiskeluoikeus(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, henkilö: Option[Henkilö])(implicit user: KoskiSpecificSession, accessType: AccessType.Value): Either[HttpStatus, KoskeenTallennettavaOpiskeluoikeus] =
    validateAccess(opiskeluoikeus)
      .onSuccess {
        validateLähdejärjestelmä(opiskeluoikeus)
      }
      .onSuccess {
        validatePäätasonSuoritukset(opiskeluoikeus)
      }
      .onSuccess {
        ePerusteetValidator.validatePerusteVoimassa(opiskeluoikeus)
      }
      .onSuccess {
        ePerusteetValidator.validateTutkintorakenne(opiskeluoikeus)
      }
      .onSuccess {
        HttpStatus.fold(
          päätasonSuoritusTyypitEnabled(opiskeluoikeus),
          päätasonSuoritusLuokatEnabled(opiskeluoikeus),
          osasuoritusTyypitEnabled(opiskeluoikeus),
          validateOpintojenrahoitus(opiskeluoikeus),
          validateSisältyvyys(henkilö, opiskeluoikeus),
          validateOpiskeluoikeudenPäivämäärät(opiskeluoikeus),
          validatePäätasonSuoritustenStatus(opiskeluoikeus),
          validateOpiskeluoikeudenLisätiedot(opiskeluoikeus),
          PerusopetuksenOpiskeluoikeusValidation.validatePerusopetuksenOpiskeluoikeus(config)(opiskeluoikeus),
          TiedonSiirrostaPuuttuvatSuorituksetValidation.validateEiSamaaAlkamispaivaa(opiskeluoikeus, koskiOpiskeluoikeudet),
          HttpStatus.fold(opiskeluoikeus.suoritukset.map(validateSuoritus(_, opiskeluoikeus, henkilö, Nil))),
          TilanAsettaminenKunVahvistettuSuoritusValidation.validateOpiskeluoikeus(opiskeluoikeus),
          SuostumuksenPeruutusValidaatiot.validateSuostumuksenPeruutus(opiskeluoikeus, suostumuksenPeruutusService),
          Lukio2015Validation.validateOppimääräSuoritettu(opiskeluoikeus),
          AmmatillinenValidation.validateAmmatillinenOpiskeluoikeus(opiskeluoikeus, henkilö, koskiOpiskeluoikeudet),
          ePerusteetValidator.validateAmmatillinenOpiskeluoikeus(opiskeluoikeus),
          ePerusteetLops2019Validator.validate(opiskeluoikeus),
          VSTKotoutumiskoulutus2022Validation.validate(opiskeluoikeus),
          VapaaSivistystyöValidation.validateVapaanSivistystyönPäätasonOpintokokonaisuus(opiskeluoikeus),
          JotpaValidation.validateOpiskeluoikeus(opiskeluoikeus, config),
          TutkintokoulutukseenValmentavaKoulutusValidation.validateOpiskeluoikeus(opiskeluoikeus),
          EuropeanSchoolOfHelsinkiValidation.validateOpiskeluoikeus(config)(henkilöRepository, koskiOpiskeluoikeudet, henkilö, opiskeluoikeus),
          TaiteenPerusopetusValidation.validateOpiskeluoikeus(config)(opiskeluoikeus, suostumuksenPeruutusService),
          IBValidation.validateIbOpiskeluoikeus(config)(opiskeluoikeus),
          ToimintaAlueetValidation.validateToimintaAlueellinenOpiskeluoikeus(config)(opiskeluoikeus),
          KielitutkintoValidation.validateOpiskeluoikeus(opiskeluoikeus),
          EsiopetusValidation.validateOpiskeluoikeus(config)(opiskeluoikeus),
        )
      } match {
        case HttpStatus.ok => Right(opiskeluoikeus)
        case _ if (config.getStringList("validaatiot.ohitaValidaatiovirheetKäyttäjällä").contains(user.user.username)) =>
          logger.info(s"Ohitetaan käyttäjätunnuksen perusteella validaatiovirheitä")
          Right(opiskeluoikeus)
        case status: Any => Left(status)
      }

    private def updateFields(oo: KoskeenTallennettavaOpiskeluoikeus, lipsuTarvittaessaVirheistäMitätöinnissä: Boolean = false)(implicit user: KoskiSpecificSession): Either[HttpStatus, KoskeenTallennettavaOpiskeluoikeus] = {

    def lipsuTarvittaessa
      (f: KoskeenTallennettavaOpiskeluoikeus => Either[HttpStatus, KoskeenTallennettavaOpiskeluoikeus])
      (oo: KoskeenTallennettavaOpiskeluoikeus)
    : Either[HttpStatus, KoskeenTallennettavaOpiskeluoikeus] =
      f(oo) match {
        case Left(_) if lipsuTarvittaessaVirheistäMitätöinnissä => Right(oo)
        case result => result
      }

    fillMissingOrganisations(oo)
      .map(ePerusteetFiller.addKoulutustyyppi)
      .flatMap(lipsuTarvittaessa(ePerusteetValidator.validateKoulutustyypinLöytyminenAmmatillisissa))
      .flatMap(lipsuTarvittaessa(MaksuttomuusValidation.validateAndFillJaksot))
      .map(ePerusteetFiller.fillPerusteenNimi)
      .map(fillLaajuudet)
      .map(fillVieraatKielet)
      .map(clearVahvistukset)
      .map(fillOrganisaatioHistoria)
      .map(KoodistopoikkeustenKonversiot.konvertoiKoodit)
      .map(fillLukionOppimääräSuoritettu)
      .map(PerusopetuksenOpiskeluoikeusValidation.filterDeprekoidutKentät)
      .map(EuropeanSchoolOfHelsinkiValidation.fillRahoitusmuodot(koodistoPalvelu))
      .map(EuropeanSchoolOfHelsinkiValidation.fillKoulutustyyppi(koodistoPalvelu))
      .map(RedundantinDatanPoisto.dropRedundantData)
  }

  private def fillLaajuudet(oo: KoskeenTallennettavaOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus =
    oo.withSuoritukset(
      oo.suoritukset
        .map(fillOsasuoritustenLaajuudet)
        .map(fillPäätasonSuorituksenLaajuus)
    )

  private def fillOsasuoritustenLaajuudet(suoritus: PäätasonSuoritus): PäätasonSuoritus = suoritus match {
    case _: OpintopistelaajuuksienYhteislaskennallinenPäätasonSuoritus[_] =>
      suoritus.withOsasuoritukset(suoritus.osasuoritukset.map(_.map { os =>
        lazy val yhteislaajuus = os.osasuoritusLista.map(_.koulutusmoduuli.laajuusArvo(0.0)).map(BigDecimal.decimal).sum.toDouble
        os.withKoulutusmoduuli(os.koulutusmoduuli match {
          case k: OpintopistelaajuuksienYhteenlaskennanOhittavaKoulutusmoduuli[_] =>
            k
          case k: OpintopistelaajuuksienYhteenlaskennallinenKoulutusmoduuli[_] if yhteislaajuus > 0 => k.withLaajuus(yhteislaajuus)
          case k: OpintopistelaajuuksienYhteenlaskennallinenKoulutusmoduuli[_] => k.withLaajuusNone()
          case k => k
        })
      }))
    case _ => suoritus
  }

  private def fillPäätasonSuorituksenLaajuus(suoritus: PäätasonSuoritus): PäätasonSuoritus = {
    def yhteislaajuus: Double =
      suoritus.osasuoritusLista.map(_.koulutusmoduuli.laajuusArvo(0.0)).map(BigDecimal.decimal).sum.toDouble

    def yhteislaajuusHyväksytyt: Double =
      suoritus.osasuoritusLista.filter(os => os.viimeisinArviointi.exists(_.hyväksytty)).map(_.koulutusmoduuli.laajuusArvo(0.0)).map(BigDecimal.decimal).sum.toDouble

    suoritus match {
      case koto: OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus =>
        laajuusYlimpienOsasuoritustenLaajuuksista(koto, yhteislaajuus, l => LaajuusOpintopisteissä(l))
      case koto: OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022 =>
        laajuusYlimpienOsasuoritustenLaajuuksista(koto, yhteislaajuus, l => LaajuusOpintopisteissä(l))
      case lukutaito: VapaanSivistystyönLukutaitokoulutuksenSuoritus =>
        laajuusYlimpienOsasuoritustenLaajuuksista(lukutaito, yhteislaajuus, l => LaajuusOpintopisteissä(l))
      case tuva: TutkintokoulutukseenValmentavanKoulutuksenPäätasonSuoritus =>
        TutkintokoulutukseenValmentavaKoulutusValidation.validateLaajuusRajapäivääEnnenTaiJälkeen(
          config,
          () => laajuusYlimpienOsasuoritustenLaajuuksista(tuva, yhteislaajuus, l => LaajuusViikoissa(l)),
          () => laajuusYlimpienOsasuoritustenLaajuuksista(tuva, yhteislaajuusHyväksytyt, l => LaajuusViikoissa(l))
        )
      case _ => suoritus
    }
  }

  private def laajuusYlimpienOsasuoritustenLaajuuksista[A <: Laajuus](
    s: OpintopistelaajuuksienYhteislaskennallinenSuoritus[A] with PäätasonSuoritus,
    yhteislaajuus: Double,
    toLaajuus: Double => A
  ): PäätasonSuoritus = {
    val laajuus = if (yhteislaajuus > 0) Some(toLaajuus(yhteislaajuus)) else None
    s.withKoulutusmoduuli(s.koulutusmoduuli.withLaajuus(laajuus))
  }

  private def fillVieraatKielet(oo: KoskeenTallennettavaOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus =
    oo.withSuoritukset(oo.suoritukset.map {
      case s@(_: LukionPäätasonSuoritus2019 | _: PreIBSuoritus2019) => Lukio2019VieraatKieletValidation.fillVieraatKielet(s)
      case s: Any => s
    })

  private def fillLukionOppimääräSuoritettu(oo: KoskeenTallennettavaOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = {
    oo match {
      case lukio: LukionOpiskeluoikeus =>
        lukio.suoritukset.head match {
          case suoritus: LukionOppimääränSuoritus if suoritus.vahvistettu => lukio.copy(
            oppimääräSuoritettu = Some(true)
          )
          case _ => oo
        }
      case _ => oo
    }
  }

  private def clearVahvistukset(oo: KoskeenTallennettavaOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus =
    oo.withSuoritukset(oo.suoritukset.map {
      case l: LukionOppiaineidenOppimäärienSuoritus2019 => l.copy(vahvistus = None)
      case l: Any => l
    })

  private def fillOrganisaatioHistoria(oo: KoskeenTallennettavaOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = {
    oo match {
      case _: YlioppilastutkinnonOpiskeluoikeus => oo
      case _ => oo.withHistoria(None)
    }
  }

  private def fillMissingOrganisations(oo: KoskeenTallennettavaOpiskeluoikeus)(implicit user: KoskiSpecificSession): Either[HttpStatus, KoskeenTallennettavaOpiskeluoikeus] = {
    addOppilaitos(oo).flatMap(addKoulutustoimija).map(setOrganizationNames)
  }

  private def setOrganizationNames(oo: KoskeenTallennettavaOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = {
    def modifyName[O <: OrganisaatioWithOid](org: O, päivä: LocalDate): O = {
      val nimiPäättymispäivänä = organisaatioRepository.getOrganisaationNimiHetkellä(org.oid, päivä)
      traversal[OrganisaatioWithOid].field[Option[LocalizedString]]("nimi").modify(org)(nimi => nimiPäättymispäivänä.orElse(nimi)).asInstanceOf[O]
    }
    // Opiskeluoikeus on päättynyt, asetetaan organisaation nimi siksi, kuin mitä se oli päättymishetkellä.
    // Tämä siksi, ettei mahdollinen organisaation nimenmuutos opiskeluoikeuden päättymisen jälkeen vaikuttaisi näytettävään nimeen

    val ooWithModifiedOppilaitos = oo.oppilaitos match {
      case Some(ol) => oo.withOppilaitos(modifyName(ol, oo.päättymispäivä.getOrElse(LocalDate.now())))
      case _ => oo
    }
    val ooWithModifiedKoulutustoimija = koulutustoimijaTraversal.modify(ooWithModifiedOppilaitos)(org => modifyName(org, oo.päättymispäivä.getOrElse(LocalDate.now())))

    val ooWithModifiedSuoritukset = ooWithModifiedKoulutustoimija.withSuoritukset(ooWithModifiedKoulutustoimija.suoritukset.map(s => {
      val päivä = s.vahvistus.map(v => v.päivä).getOrElse(LocalDate.now())
      Suoritus.toimipisteetTraversal.modify(s)(org => modifyName(org, päivä))
    }))
    ooWithModifiedSuoritukset
  }

  private def addOppilaitos(oo: KoskeenTallennettavaOpiskeluoikeus): Either[HttpStatus, KoskeenTallennettavaOpiskeluoikeus] = {
    oo match {
      case ytrOo: YlioppilastutkinnonOpiskeluoikeus if ytrOo.oppilaitos.isEmpty =>
        // YO-tutkinnon opiskeluoikeudella ei ole oppilaitosta, koska sen myöntää koulutustoimijana toimiva ylioppilastutkintolautakunta
        Right(oo)
      case oo: KoskeenTallennettavaOpiskeluoikeus =>
        val oppilaitos: Either[HttpStatus, Oppilaitos] = oo.oppilaitos.map(Right(_)).getOrElse {
          val toimipisteet: List[OrganisaatioWithOid] = oo.suoritukset.map(_.toimipiste)
          val oppilaitokset: Either[HttpStatus, Seq[Oppilaitos]] = HttpStatus.foldEithers(toimipisteet.map { toimipiste =>
            organisaatioRepository.findOppilaitosForToimipiste(toimipiste) match {
              case Some(oppilaitos) => Right(oppilaitos)
              case None => Left(KoskiErrorCategory.badRequest.validation.organisaatio.eiOppilaitos(s"Toimipisteenä käytetylle organisaatiolle ${toimipiste.oid} ei löydy oppilaitos-tyyppistä yliorganisaatiota."))
            }
          })
          oppilaitokset.right.map(_.distinct).flatMap {
            case List(oppilaitos) => Right(oppilaitos)
            case _ => Left(KoskiErrorCategory.badRequest.validation.organisaatio.oppilaitosPuuttuu("Opiskeluoikeudesta puuttuu oppilaitos, eikä sitä voi yksiselitteisesti päätellä annetuista toimipisteistä."))
          }
        }
        oppilaitos.right.map(oo.withOppilaitos(_))
    }
  }

  private def addKoulutustoimija(oo: KoskeenTallennettavaOpiskeluoikeus)(implicit user: KoskiSpecificSession): Either[HttpStatus, KoskeenTallennettavaOpiskeluoikeus] = oo match {
    case e: EsiopetuksenOpiskeluoikeus if e.järjestämismuoto.isDefined => validateAndAddVarhaiskasvatusKoulutustoimija(e)
    case t: TaiteenPerusopetuksenOpiskeluoikeus if t.onHankintakoulutus => validateAndAddTaiteenPerusopetuksenKoulutustoimija(t)
    case ytrOo: YlioppilastutkinnonOpiskeluoikeus if ytrOo.oppilaitos.isEmpty && ytrOo.koulutustoimija.exists(_.oid == "1.2.246.562.10.43628088406") =>
      Right(oo)
    case _ => organisaatioRepository.findKoulutustoimijaForOppilaitos(oo.getOppilaitos) match {
      case Some(löydettyKoulutustoimija) =>
        oo.koulutustoimija.map(_.oid) match {
          case Some(oid) if oid != löydettyKoulutustoimija.oid =>
            Left(KoskiErrorCategory.badRequest.validation.organisaatio.vääräKoulutustoimija(s"Annettu koulutustoimija $oid ei vastaa organisaatiopalvelusta löytyvää koulutustoimijaa ${löydettyKoulutustoimija.oid}"))
          case _ =>
            Right(oo.withKoulutustoimija(löydettyKoulutustoimija))
        }
      case _ =>
        logger.warn(s"Koulutustoimijaa ei löydy oppilaitokselle ${oo.oppilaitos}")
        Right(oo)
    }
  }

  private def validateAndAddTaiteenPerusopetuksenKoulutustoimija(oo: TaiteenPerusopetuksenOpiskeluoikeus)
    (implicit user: KoskiSpecificSession): Either[HttpStatus, TaiteenPerusopetuksenOpiskeluoikeus] = {
    järjestettyOmanOrganisaationUlkopuolella(oo.oppilaitos, oo.koulutustoimija) match {
      // Ei koulutustoimija-organisaation käyttöoikeuksia eikä globaaleja kirjoitusoikeuksia
      case true if !user.hasKoulutustoimijaOrganisaatioTaiGlobaaliWriteAccess =>
        // Löytyy editOnly-access taiteen perusopetukseen, muuten ei oppilaitoksen käyttäjä saa kirjoittaa hankintakoulutuksen opiskeluoikeutta
        if(user.hasTaiteenPerusopetusAccess(oo.getOppilaitos.oid, oo.koulutustoimija.map(_.oid), AccessType.editOnly)) {
          Right(oo)
        } else {
          Left(KoskiErrorCategory.forbidden.vainTaiteenPerusopetuksenJärjestäjä())
        }
      case true =>
        if (oo.koulutustoimija.isDefined && user.hasTaiteenPerusopetusAccess(oo.getOppilaitos.oid, oo.koulutustoimija.map(_.oid), AccessType.write)) {
          Right(oo)
        } else {
          val koulutustoimija = inferTaiteenPerusopetuksenKoulutustoimija(user)
          koulutustoimija.map(oo.withKoulutustoimija)
        }
      case _ =>
        Left(KoskiErrorCategory.badRequest.validation.organisaatio.hankintakoulutus())
    }
  }

  private def validateAndAddVarhaiskasvatusKoulutustoimija(oo: EsiopetuksenOpiskeluoikeus)(implicit user: KoskiSpecificSession) = {
    val koulutustoimija = inferVarhaiskasvatuksenKoulutustoimija(user)
    (
      päiväkodinTaiPeruskoulunEsiopetus(oo),
      järjestettyOmanOrganisaationUlkopuolella(oo.oppilaitos, oo.koulutustoimija.orElse(koulutustoimija.toOption))
    ) match {
      case (true, true) =>
        if (oo.koulutustoimija.isDefined && user.hasVarhaiskasvatusAccess(oo.koulutustoimija.get.oid, oo.getOppilaitos.oid, AccessType.write)) {
          Right(oo)
        } else {
          koulutustoimija.map(oo.withKoulutustoimija)
        }
      case (false, true) =>
        Left(KoskiErrorCategory.badRequest.validation.koodisto.vääräKoulutuksenTunniste(s"Järjestämismuoto sallittu vain päiväkodissa tai peruskoulussa järjestettävälle esiopetukselle ($päiväkodinEsiopetuksenTunniste tai $peruskoulunEsiopetuksenTunniste)"))
      case _ =>
        Left(KoskiErrorCategory.badRequest.validation.organisaatio.järjestämismuoto())
    }
  }

  private def järjestettyOmanOrganisaationUlkopuolella(oppilaitos: Option[Oppilaitos], koulutustoimija: Option[Koulutustoimija]) = oppilaitos.exists { oppilaitos =>
    koulutustoimija.forall(kt => organisaatioRepository.findKoulutustoimijaForOppilaitos(oppilaitos).forall(_.oid != kt.oid))
  }

  private def päiväkodinTaiPeruskoulunEsiopetus(oo: EsiopetuksenOpiskeluoikeus): Boolean = {
    oo.suoritukset.forall(s =>
      päiväkodissaJärjestettyEsiopetuksenSuoritus(s) || peruskoulussaJärjestettyEsiopetuksenSuoritus(s)
    )
  }

  private def inferTaiteenPerusopetuksenKoulutustoimija(user: KoskiSpecificSession): Either[HttpStatus, Koulutustoimija] = {
    user.orgKäyttöoikeudet.flatMap(_.organisaatio.toKoulutustoimija).map(_.oid).toList match {
      case koulutustoimijaOid :: Nil =>
        organisaatioRepository.getOrganisaatio(koulutustoimijaOid)
          .flatMap(_.toKoulutustoimija)
          .toRight(KoskiErrorCategory.badRequest.validation.organisaatio.tuntematon(s"Koulutustoimijaa $koulutustoimijaOid ei löydy"))
      case Nil =>
        Left(KoskiErrorCategory.forbidden.vainTaiteenPerusopetuksenJärjestäjä())
      case _ =>
        Left(KoskiErrorCategory.badRequest.validation.organisaatio.koulutustoimijaPakollinen("Koulutustoimijaa ei voi yksiselitteisesti päätellä käyttäjätunnuksesta. Koulutustoimija on pakollinen."))
    }
  }
  private def inferVarhaiskasvatuksenKoulutustoimija(user: KoskiSpecificSession) = {
    user.varhaiskasvatusKäyttöoikeudet.map(_.koulutustoimija.oid).toList match {
      case koulutustoimijaOid :: Nil =>
        organisaatioRepository.getOrganisaatio(koulutustoimijaOid)
          .flatMap(_.toKoulutustoimija)
          .toRight(KoskiErrorCategory.badRequest.validation.organisaatio.tuntematon(s"Koulutustoimijaa $koulutustoimijaOid ei löydy"))
      case Nil =>
        Left(KoskiErrorCategory.forbidden.vainVarhaiskasvatuksenJärjestäjä("Operaatio on sallittu vain käyttäjälle joka on luotu varhaiskasvatusta järjestävälle koulutustoimijalle"))
      case _ =>
        Left(KoskiErrorCategory.badRequest.validation.organisaatio.koulutustoimijaPakollinen("Koulutustoimijaa ei voi yksiselitteisesti päätellä käyttäjätunnuksesta. Koulutustoimija on pakollinen."))
    }
  }

  private def validateOpintojenrahoitus(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus) = {
    HttpStatus.fold(opiskeluoikeus.tila.opiskeluoikeusjaksot.map {
      case j: AmmatillinenOpiskeluoikeusjakso => vaadiRahoitusmuotoTiloilta(j, "lasna", "valmistunut", "loma")
      case j: LukionOpiskeluoikeusjakso => vaadiRahoitusmuotoTiloilta(j, "lasna", "valmistunut")
      case j: AikuistenPerusopetuksenOpiskeluoikeusjakso => vaadiRahoitusmuotoTiloilta(j, "lasna", "valmistunut")
      case j: EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso => HttpStatus.fold(
        vaadiRahoitusmuotoTiloilta(j, "lasna", "valmistunut"),
        rahoitusmuotoKiellettyTiloilta(j, "eronnut", "mitatoity", "valiaikaisestikeskeytynyt")
      )
      case j: DIAOpiskeluoikeusjakso => HttpStatus.fold(
        vaadiRahoitusmuotoTiloilta(j, "lasna", "valmistunut"),
        rahoitusmuotoKiellettyTiloilta(j, "eronnut", "katsotaaneronneeksi", "mitatoity", "peruutettu", "valiaikaisestikeskeytynyt")
      )
      case j: MuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso => vaadiRahoitusmuotoTiloilta(j, "lasna", "hyvaksytystisuoritettu")
      case _ => HttpStatus.ok
    })
  }

  private def vaadiRahoitusmuotoTiloilta(jakso: KoskiOpiskeluoikeusjakso, tilat: String*) = {
    HttpStatus.validate(
      !tilat.contains(jakso.tila.koodiarvo) || jakso.opintojenRahoitus.isDefined
    )(KoskiErrorCategory.badRequest.validation.tila.tilaltaPuuttuuRahoitusmuoto(s"Opiskeluoikeuden tilalta ${jakso.tila.koodiarvo} puuttuu rahoitusmuoto"))
  }

  private def rahoitusmuotoKiellettyTiloilta(jakso: KoskiOpiskeluoikeusjakso, tilat: String*) = {
    HttpStatus.validate(
      !tilat.contains(jakso.tila.koodiarvo) || jakso.opintojenRahoitus.isEmpty
    )(KoskiErrorCategory.badRequest.validation.tila.tilallaEiSaaOllaRahoitusmuotoa(s"Opiskeluoikeuden tilalla ${jakso.tila.koodiarvo} ei saa olla rahoitusmuotoa"))
  }

  private def validateSisältyvyys(henkilö: Option[Henkilö], opiskeluoikeus: Opiskeluoikeus)(implicit user: KoskiSpecificSession, accessType: AccessType.Value): HttpStatus = opiskeluoikeus.sisältyyOpiskeluoikeuteen match {
    case Some(SisältäväOpiskeluoikeus(Oppilaitos(oppilaitosOid, _, _, _), oid)) if accessType == AccessType.write =>
      koskiOpiskeluoikeudet.findByOid(oid)(KoskiSpecificSession.systemUser) match {
        case Right(sisältäväOpiskeluoikeus) if sisältäväOpiskeluoikeus.oppilaitosOid != oppilaitosOid =>
          KoskiErrorCategory.badRequest.validation.sisältäväOpiskeluoikeus.vääräOppilaitos()
        case Right(sisältäväOpiskeluoikeus) =>
          val löydettyHenkilö: Either[HttpStatus, Oid] = henkilö match {
            case None => Left(HttpStatus.ok)
            case Some(h: HenkilöWithOid) => Right(h.oid)
            case Some(h: UusiHenkilö) => henkilöRepository.opintopolku.findByHetu(h.hetu) match {
              case Some(henkilö) => Right(henkilö.oid)
              case None => Left(KoskiErrorCategory.badRequest.validation.sisältäväOpiskeluoikeus.henkilöTiedot())
            }
          }

          löydettyHenkilö match {
            case Right(löydettyHenkilöOid) if löydettyHenkilöOid != sisältäväOpiskeluoikeus.oppijaOid =>
              henkilöRepository.findByOid(löydettyHenkilöOid, findMasterIfSlaveOid = true) match {
                case Some(hlö) if (hlö.oid :: hlö.linkitetytOidit).contains(sisältäväOpiskeluoikeus.oppijaOid) => HttpStatus.ok
                case _ => KoskiErrorCategory.badRequest.validation.sisältäväOpiskeluoikeus.henkilöTiedot()
              }
            case Left(status) => status
            case _ => HttpStatus.ok
          }
        case _ => KoskiErrorCategory.badRequest.validation.sisältäväOpiskeluoikeus.eiLöydy(s"Sisältävää opiskeluoikeutta ei löydy oid-arvolla $oid")
      }
    case _ => HttpStatus.ok
  }

  private def validateAccess(oo: Opiskeluoikeus)(implicit user: KoskiSpecificSession, accessType: AccessType.Value): HttpStatus = {
    HttpStatus.fold(
      validateOpiskeluoikeudenTyypinAccess(OoPtsMask.fromOpiskeluoikeus(oo)),
      validateOrganisaatioAccess(oo)
    )
  }

  private def validateOpiskeluoikeudenTyypinAccess(tyyppi: OoPtsMask)(implicit user: KoskiSpecificSession, accessType: AccessType.Value) =
    HttpStatus.validate(user.allowedOpiskeluoikeudetJaPäätasonSuoritukset.intersects(tyyppi)) {
      KoskiErrorCategory.forbidden.opiskeluoikeudenTyyppi("Ei oikeuksia opiskeluoikeuden tyyppiin " + tyyppi)
    }

  private def validateOrganisaatioAccess(oo: Opiskeluoikeus)(implicit user: KoskiSpecificSession, accessType: AccessType.Value): HttpStatus = {
    oo match {
      case _: YlioppilastutkinnonOpiskeluoikeus =>
        validateOrganisaatioAccess(oo, oo.getOppilaitosOrKoulutusToimija)
      case _ =>
        validateOrganisaatioAccess(oo, oo.getOppilaitos)
    }
  }

  private def validateOrganisaatioAccess(oo: Opiskeluoikeus, organisaatio: OrganisaatioWithOid)(implicit user: KoskiSpecificSession, accessType: AccessType.Value): HttpStatus = {
    val organisaationKoulutustoimija = organisaatioRepository.findKoulutustoimijaForOppilaitos(organisaatio).map(_.oid)
    val opiskeluoikeudenKoulutustoimija = oo.koulutustoimija.map(_.oid)

    oo match {
      case _: YlioppilastutkinnonOpiskeluoikeus =>
        HttpStatus.validate(user.hasTallennetutYlioppilastutkinnonOpiskeluoikeudetAccess) {
          KoskiErrorCategory.forbidden.organisaatio("Ei oikeuksia organisatioon " + organisaatio.oid)
        }
      case t: TaiteenPerusopetuksenOpiskeluoikeus =>
        val koulutustoimija = if(t.onHankintakoulutus) opiskeluoikeudenKoulutustoimija else organisaationKoulutustoimija
        HttpStatus.validate(user.hasTaiteenPerusopetusAccess(organisaatio.oid, koulutustoimija, accessType)) {
          KoskiErrorCategory.forbidden.organisaatio("Ei oikeuksia organisatioon " + organisaatio.oid)
        }
      case _: KielitutkinnonOpiskeluoikeus =>
        HttpStatus.validate(user.hasKielitutkintoAccess(organisaatio.oid, organisaationKoulutustoimija, accessType)) {
          KoskiErrorCategory.forbidden.organisaatio("Ei oikeuksia organisatioon " + organisaatio.oid)
        }
      case _ =>
        val koulutustoimija = oo match {
          case e: EsiopetuksenOpiskeluoikeus if e.järjestämismuoto.isDefined => opiskeluoikeudenKoulutustoimija
          case _ => organisaationKoulutustoimija
        }
        HttpStatus.validate(user.hasAccess(organisaatio.oid, koulutustoimija, accessType)) {
          KoskiErrorCategory.forbidden.organisaatio("Ei oikeuksia organisatioon " + organisaatio.oid)
        }
    }
  }

  private def validateLähdejärjestelmä(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus)(implicit user: KoskiSpecificSession): HttpStatus = {
    if (opiskeluoikeus.lähdejärjestelmänId.isDefined && !user.isPalvelukäyttäjä && !user.isRoot) {
      KoskiErrorCategory.forbidden.lähdejärjestelmäIdEiSallittu("Lähdejärjestelmä määritelty, mutta käyttäjä ei ole palvelukäyttäjä")
    } else if (user.isPalvelukäyttäjä && opiskeluoikeus.lähdejärjestelmänId.isEmpty) {
      KoskiErrorCategory.forbidden.lähdejärjestelmäIdPuuttuu("Käyttäjä on palvelukäyttäjä mutta lähdejärjestelmää ei ole määritelty")
    } else {
      HttpStatus.ok
    }
  }

  private def validatePäätasonSuoritukset(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    opiskeluoikeus match {
      case l: LukionOpiskeluoikeus if l.suoritukset.count(_.tyyppi.koodiarvo == "lukionaineopinnot") > 1 =>
        KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaSuorituksia("Opiskeluoikeudella on enemmän kuin yksi oppiaineiden oppimäärät ryhmittelevä lukionaineopinnot-tyyppinen suoritus")
      case l: LukionOpiskeluoikeus if l.suoritukset.exists(_.tyyppi.koodiarvo == "lukionoppimaara")
        && l.suoritukset.count { case _: LukionPäätasonSuoritus => true } > 1 =>
        KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaSuorituksia("Opiskeluoikeudelle yritetään lukion oppimäärän lisäksi tallentaa useampi päätason suoritus. Lukion oppimäärän opiskelijalla voi olla vain yksi päätason suoritus.")
      case a: AmmatillinenOpiskeluoikeus if a.suoritukset.exists(_.isInstanceOf[ValmaKoulutuksenSuoritus]) && a.tila.opiskeluoikeusjaksot.exists(_.alku.isAfter(LocalDate.of(2023, 5, 31))) => KoskiErrorCategory.badRequest.validation.tila.valmaTilaEiSallittu()
      case p: PerusopetuksenLisäopetuksenOpiskeluoikeus if p.suoritukset.exists(_.isInstanceOf[PerusopetuksenLisäopetuksenSuoritus]) && p.tila.opiskeluoikeusjaksot.exists(_.alku.isAfter(LocalDate.of(2023, 5, 31))) => KoskiErrorCategory.badRequest.validation.tila.perusopetuksenLisäopetuksenTilaEiSallittu()
      case p: IBOpiskeluoikeus
        if p.suoritukset.exists(_.isInstanceOf[PreIBSuoritus2019]) && p.suoritukset.exists(_.isInstanceOf[PreIBSuoritus2015]) =>
        KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaSuorituksia("Vanhan ja lukion opetussuunnitelman 2019 mukaisia Pre-IB-opintoja ei sallita samassa opiskeluoikeudessa")
      case _ => HttpStatus.ok
    }
  }

  val tukijaksollisetValidoidaanTuotannossa = !Environment.isProdEnvironment(config) || !LocalDate.now().isBefore(LocalDate.parse(config.getString("validaatiot.tukijaksollisetValidoidaanTuotannossaAlkaen")))

  private def validateOpiskeluoikeudenLisätiedot(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    HttpStatus.fold(
      validateErityisenKoulutustehtävänJakso(opiskeluoikeus.lisätiedot),
      validatePidennettyOppivelvollisuusAikarajastaAlkaen(opiskeluoikeus.lisätiedot, opiskeluoikeus.alkamispäivä, opiskeluoikeus.päättymispäivä),
      validateTuvaPerusopetusErityinenTukiJaVammaisuusAikarajastaAlkaen(opiskeluoikeus.lisätiedot),
      if (tukijaksollisetValidoidaanTuotannossa) validateTukijaksollinen(opiskeluoikeus.lisätiedot, opiskeluoikeus.alkamispäivä, opiskeluoikeus.päättymispäivä) else HttpStatus.ok,
    )
  }

  private def validateTukijaksollinen(lisätiedot: Option[OpiskeluoikeudenLisätiedot], alkamispäivä: Option[LocalDate], päättymispäivä: Option[LocalDate]): HttpStatus = {
    lisätiedot match {
      case Some(_: Tukipäätöksellinen) =>
        HttpStatus.fold(
          validateTuenPäätöksenJaksot(lisätiedot, alkamispäivä, päättymispäivä),
          validateVarhennettuOppivelvollisuus(lisätiedot),
          validateVammaSairausTaiRajoite(lisätiedot),
          validatePäätösToimintaAlueittainOpiskelusta(lisätiedot),
          validateTavoitekokonaisuuksittainOpiskelu(lisätiedot),
        )
      case _ => HttpStatus.ok
    }
  }

  private def validateTuenPäätöksenJaksot(lisätiedot: Option[OpiskeluoikeudenLisätiedot], alku: Option[LocalDate], loppu: Option[LocalDate]): HttpStatus = {
    val tukijaksotVoimaan = LocalDate.parse(config.getString("validaatiot.tukijaksotVoimaan"))
    lisätiedot match {
      case Some(lt: Tukipäätöksellinen) =>
        val ooAikajakso = SuljettuJakso(Aikajakso(alku, loppu))
        val tuenPäätöksenJaksoEiPäätyEnnenOpiskeluoikeudenAlkua =
          lt.tuenPäätöksenJaksot.forall(_.forall { tukijakso =>
            tukijakso.loppu.forall(!_.isBefore(ooAikajakso.alku))
          })
        val tuenPäätöksenJaksoEnnenVoimaantuloa = lt.tuenPäätöksenJaksot match {
          case Some(tukijaksot) => tukijaksot.exists(j => j.alku.exists(_.isBefore(tukijaksotVoimaan)))
          case _ => false
        }

        HttpStatus.fold(
          HttpStatus.validate(!tuenPäätöksenJaksoEnnenVoimaantuloa)(KoskiErrorCategory.badRequest.validation.date(
            s"Tuen päätöksen jakson varhaisin sallittu voimassaolopäivä on ${FinnishDateFormat.format(tukijaksotVoimaan)}"
          )),
          HttpStatus.validate(tuenPäätöksenJaksoEiPäätyEnnenOpiskeluoikeudenAlkua)(KoskiErrorCategory.badRequest.validation.date(
            s"Tuen päätöksen jakso ei saa päättyä ennen opiskeluoikeuden alkua"
          ))
        )
      case _ => HttpStatus.ok
    }
  }

  private def sisältymättömätAikajaksot(
    aikajaksotOpt: Option[List[Aikajakso]],
    tukipäätöksellinen: Tukipäätöksellinen
  ): Option[List[Aikajakso]] = {
    val yhdistetytTukijaksot: List[SuljettuJakso] =
      yhdistäPäällekäisetJaPeräkkäisetMahdollisestiAlkupäivällisetAikajaksot(tukipäätöksellinen.kaikkiTuenPäätöksenJaksot)

    val sisältymättömät = aikajaksotOpt.map { aikajaksot =>
      aikajaksot.filterNot { aikajakso =>
        yhdistetytTukijaksot.exists(_.contains(aikajakso))
      }
    }

    sisältymättömät.filter(_.nonEmpty) // Palauta None, jos lista on tyhjä
  }

  private def validateVammaSairausTaiRajoite(lisätiedot: Option[OpiskeluoikeudenLisätiedot]): HttpStatus = {
    val vammaSairausTaiRajoiteVoimaan = LocalDate.parse(config.getString("validaatiot.vammaSairausTaiRajoiteVoimaan"))
    lisätiedot match {
      case Some(lt: VammaSairausTaiRajoite with ToimintaAlueittainOpiskeleva) =>
        val toimintaAlueittainenPäällekkäin = lt.opetuksenJärjestäminenVammanSairaudenTaiRajoitteenPerusteella.map(_.filter(a =>
          lt.toimintaAlueittainOpiskelu.exists(_.exists(b => a.overlaps(b)))
        )).filter(_.nonEmpty)

        val sisältymättömätJaksot = sisältymättömätAikajaksot(lt.opetuksenJärjestäminenVammanSairaudenTaiRajoitteenPerusteella, lt)

        val alkaaLiianVarhain = lt.opetuksenJärjestäminenVammanSairaudenTaiRajoitteenPerusteella match {
          case Some(xs) => xs.exists(x => x.alku.isBefore(vammaSairausTaiRajoiteVoimaan))
          case _ => false
        }

        HttpStatus.fold(
          HttpStatus.validate(toimintaAlueittainenPäällekkäin.isEmpty)(KoskiErrorCategory.badRequest.validation.date(
            s"Opetuksen järjestäminen vamman, sairauden tai rajoitteen perusteella ei saa olla samaan aikaan kuin opiskelu toiminta-alueittain: ${toimintaAlueittainenPäällekkäin.get}"
          )),
          HttpStatus.validate(sisältymättömätJaksot.isEmpty)(KoskiErrorCategory.badRequest.validation.date(
            s"Opetuksen järjestäminen vamman, sairauden tai rajoitteen perusteella (${sisältymättömätJaksot.get.mkString(",")}) pitää sisältyä tuen päätöksen jaksoon"
          )),
          HttpStatus.validate(!alkaaLiianVarhain)(KoskiErrorCategory.badRequest.validation.date(
            s"Opetuksen järjestäminen vamman, sairauden tai rajoitteen perusteella -lisätiedon varhaisin sallittu voimassaolopäivä on $vammaSairausTaiRajoiteVoimaan"
          ))
        )

      case _ => HttpStatus.ok
    }
  }

  private def validateVarhennettuOppivelvollisuus(
    lisätiedot: Option[OpiskeluoikeudenLisätiedot]
  ): HttpStatus = {
    val varhennettuOppivelvollisuusVoimaan = LocalDate.parse(config.getString("validaatiot.varhennettuOppivelvollisuusVoimaan"))
    lisätiedot match {
      case Some(lt: VarhennettuOppivelvollisuus) =>
        val sisältymättömätJaksot = sisältymättömätAikajaksot(lt.varhennetunOppivelvollisuudenJaksot, lt)

        val aikaisinAlkamispäivä = findEnsimmäinenAlkamispäivä(lt.varhennetunOppivelvollisuudenJaksot)

        val varhennettuAlkaaLiianAikaisin = aikaisinAlkamispäivä match {
          case Some(x) => x.isBefore(varhennettuOppivelvollisuusVoimaan)
          case _ => false
        }
        HttpStatus.fold(
          HttpStatus.validate(!varhennettuAlkaaLiianAikaisin)(
            KoskiErrorCategory.badRequest.validation.date(
              s"Varhennetun oppivelvollisuuden jaksot -lisätiedon varhaisin sallittu voimassaolopäivä on $varhennettuOppivelvollisuusVoimaan"
            )
          ),
          HttpStatus.validate(sisältymättömätJaksot.isEmpty)(
            KoskiErrorCategory.badRequest.validation.date(
              s"Varhennetun oppivelvollisuuden jaksoissa (${sisältymättömätJaksot.get.mkString(",")}) on päiviä, joille ei ole tuen päätöksen jaksoa"
            )
          )
        )
      case _ => HttpStatus.ok
    }
  }

  private def findEnsimmäinenAlkamispäivä(l: Option[List[Aikajakso]]) = l
    .getOrElse(Nil)
    .map(_.alku)
    .sorted
    .headOption

  private def validatePäätösToimintaAlueittainOpiskelusta(lisätiedot: Option[OpiskeluoikeudenLisätiedot]): HttpStatus = {
    val toimintaAlueittainJärjestettyVoimaan = LocalDate.parse(config.getString("validaatiot.toimintaAlueittainJärjestettyVoimaan"))
    lisätiedot match {
      case Some(lt: ToimintaAlueittainOpiskeleva) =>
        val sisältymättömätJaksot = sisältymättömätAikajaksot(lt.toimintaAlueittainOpiskelu, lt)

        val aikaisinAlkamispäivä = findEnsimmäinenAlkamispäivä(lt.toimintaAlueittainOpiskelu)

        val toimintaAlueittainenAlkaaLiianAikaisin = aikaisinAlkamispäivä match {
          case Some(x) => x.isBefore(toimintaAlueittainJärjestettyVoimaan)
          case _ => false
        }

        HttpStatus.fold(
          HttpStatus.validate(!toimintaAlueittainenAlkaaLiianAikaisin)(
            KoskiErrorCategory.badRequest.validation.date(
              s"Toiminta-alueittain opiskelu -lisätiedon varhaisin sallittu voimassaolopäivä on $toimintaAlueittainJärjestettyVoimaan"
            )
          ),
          HttpStatus.validate(sisältymättömätJaksot.isEmpty)(KoskiErrorCategory.badRequest.validation.date(
            s"Toiminta-alueittain opiskelun (${sisältymättömätJaksot.get.mkString(",")}) täytyy sisältyä tuen päätöksen jaksoon"
          )),
        )
      case _ => HttpStatus.ok
    }
  }

  private def validateTavoitekokonaisuuksittainOpiskelu(lisätiedot: Option[OpiskeluoikeudenLisätiedot]): HttpStatus = {
    val tavoitekokonaisuuksittainOpiskeluVoimaan = LocalDate.parse(config.getString("validaatiot.tavoitekokonaisuuksittainOpiskeluVoimaan"))
    lisätiedot match {
      case Some(lt: TavoitekokonaisuuksittainOpiskeleva) =>
        val tukijaksonUlkopuollaOlevat = sisältymättömätAikajaksot(lt.tavoitekokonaisuuksittainOpiskelu, lt)

        val aikaisinAlkamispäivä = findEnsimmäinenAlkamispäivä(lt.tavoitekokonaisuuksittainOpiskelu)

        val tavoitekokonaisuuksittainenAlkaaLiianAikaisin = aikaisinAlkamispäivä match {
          case Some(x) => x.isBefore(tavoitekokonaisuuksittainOpiskeluVoimaan)
          case _ => false
        }

        HttpStatus.fold(
          HttpStatus.validate(!tavoitekokonaisuuksittainenAlkaaLiianAikaisin)(
            KoskiErrorCategory.badRequest.validation.date(
              s"Tavoitekokonaisuuksittain opiskelu -lisätiedon varhaisin sallittu voimassaolopäivä on $tavoitekokonaisuuksittainOpiskeluVoimaan"
            )
          ),
          HttpStatus.validate(tukijaksonUlkopuollaOlevat.isEmpty)(KoskiErrorCategory.badRequest.validation.date(
            s"Tavoitekokonaisuuksittain opiskelun (${tukijaksonUlkopuollaOlevat.get.mkString(",")}) täytyy sisältyä tuen päätöksen jaksoon"
          )),
        )
      case _ => HttpStatus.ok
    }
  }

  private def validateErityisenKoulutustehtävänJakso(lisätiedot: Option[OpiskeluoikeudenLisätiedot]): HttpStatus = {
    def validateKoodiarvo(koodistokoodiviite: Koodistokoodiviite): HttpStatus = {
      val vanhentuneetTehtäväKoodiarvot = Set(
        "ib", "kielijakansainvalisyys", "matematiikka-luonnontiede-ymparisto-tekniikka", "steiner", "taide", "urheilu", "muu"
      )
      val koodiarvo = koodistokoodiviite.koodiarvo
      if (vanhentuneetTehtäväKoodiarvot.contains(koodiarvo)) {
        KoskiErrorCategory.badRequest.validation.koodisto.tuntematonKoodi(
          s"Koodiarvo '${koodiarvo}' ei ole sallittu erityisen koulutustehtävän jaksolle"
        )
      } else {
        HttpStatus.ok
      }
    }

    lisätiedot match {
      case Some(lisätiedot: ErityisenKoulutustehtävänJaksollinen) =>
        lisätiedot
          .erityisenKoulutustehtävänJaksot
          .map(jaksot => HttpStatus.fold(jaksot.map(_.tehtävä).map(validateKoodiarvo)))
          .getOrElse(HttpStatus.ok)
      case _ => HttpStatus.ok
    }
  }

  private def validatePidennettyOppivelvollisuusAikarajastaAlkaen(
    lisätiedot: Option[OpiskeluoikeudenLisätiedot],
    opiskeluoikeudenAlkamispäivä: Option[LocalDate],
    opiskeluoikeudenPäättymispäivä: Option[LocalDate],
  ): HttpStatus = {
    val validaatioViimeinenPäiväEnnenVoimassaoloa = LocalDate.parse(config.getString("validaatiot.pidennetynOppivelvollisuudenYmsValidaatiotAstuvatVoimaan")).minusDays(1)
    val voimassaolotarkastusAstunutVoimaan = LocalDate.now().isAfter(validaatioViimeinenPäiväEnnenVoimassaoloa)

    if (Environment.isProdEnvironment(config)) {
      // Tuotannossa käytä vanhoja validointeja vaihtopäivämäärään asti
      if (voimassaolotarkastusAstunutVoimaan) {
        validatePidennettyOppivelvollisuus(lisätiedot, opiskeluoikeudenAlkamispäivä, opiskeluoikeudenPäättymispäivä)
      } else {
        validatePidennettyOppivelvollisuusVanha(lisätiedot, opiskeluoikeudenAlkamispäivä)
      }
    } else if (Environment.isServerEnvironment(config)) {
      // Muissa palvelinympäristöissä käytä vain uusia validointeja testausta ja järjestelmätoimittajia varten
      validatePidennettyOppivelvollisuus(lisätiedot, opiskeluoikeudenAlkamispäivä, opiskeluoikeudenPäättymispäivä)
    } else {
      // Muuten käytä uusia, jos ovat voimassa, eikä mitään, jos eivät ole voimassa (testejä varten lähinnä: vanhoilla validaatioilla ei enää ole testejä)
      if (voimassaolotarkastusAstunutVoimaan) {
        validatePidennettyOppivelvollisuus(lisätiedot, opiskeluoikeudenAlkamispäivä, opiskeluoikeudenPäättymispäivä)
      } else {
        HttpStatus.ok
      }
    }
  }

  private def validatePidennettyOppivelvollisuusVanha(
    lisätiedot: Option[OpiskeluoikeudenLisätiedot],
    opiskeluoikeudenAlkamispäivä: Option[LocalDate]
  ): HttpStatus = {

    // Yhdistää päällekkäiset aikajaksot sekä sellaiset jaksot, jotka alkavat seuraavana päivänä edellisen jakson päättymisestä
    // Palauttaa annetuista aikajaksoista yhdistetyt pisimmät mahdolliset yhtenäiset aikajaksot
    def foldAikajaksot(a: Option[List[Aikajakso]], b: Option[List[Aikajakso]]): Option[List[Aikajakso]] = {
      val kaikkiJaksot = a.getOrElse(List.empty) ++ b.getOrElse(List.empty)

      val jaksotFoldattu = kaikkiJaksot.sortBy(_.alku).foldLeft(List.empty[Aikajakso])((acc, seuraava) => {
        acc match {
          case Nil => List(seuraava)
          case edellinen :: js
            if edellinen.contains(seuraava.alku) || edellinen.contains(seuraava.alku.minusDays(1)) =>
            edellinen.copy(loppu = List(edellinen.loppu, seuraava.loppu).max(localDateOptionOrdering)) :: js
          case _ => seuraava :: acc
        }
      }).reverse

      Some(jaksotFoldattu).filter(_.nonEmpty)
    }

    def validatePidennettyOppivelvollisuusJakso(
      pidennettyOppivelvollisuus: Option[Aikajakso],
      vammaisuusJaksot: Option[List[Aikajakso]],
    ): HttpStatus = {
      def sisältyyJaksoon: Boolean =
        pidennettyOppivelvollisuus.exists(p => vammaisuusJaksot.exists(js => js.exists(j => j.contains(p))))
      def validitJaksot: Boolean = pidennettyOppivelvollisuus.nonEmpty && vammaisuusJaksot.nonEmpty && sisältyyJaksoon
      val eiJaksoja = pidennettyOppivelvollisuus.isEmpty && vammaisuusJaksot.isEmpty

      HttpStatus.validate(eiJaksoja || validitJaksot)(
        KoskiErrorCategory.badRequest.validation.date.vammaisuusjaksoPidennetynOppivelvollisuudenUlkopuolella()
      )
    }

    lisätiedot match {
      case Some(lt: PidennettyOppivelvollisuus) if !lt.isInstanceOf[TutkintokoulutukseenValmentavanOpiskeluoikeudenLisätiedot] =>
        val vammaisuusjaksot = foldAikajaksot(lt.vammainen, lt.vaikeastiVammainen)
        HttpStatus.validate(
          validatePidennettyOppivelvollisuusJakso(
            lt.pidennettyOppivelvollisuus,
            vammaisuusjaksot
          ).isOk
        ) {
          // Jos validointi ei onnistunut alkuperäisillä päivämäärillä,
          // ja pidennetty oppivelvollisuusjakso alkaa ennen opiskeluoikeuden alkamispäivää,
          // käytä validointiin pidennetyn oppivelvollisuuden alkupäivänä opiskeluoikeuden alkupäivää.
          if (
            lt.pidennettyOppivelvollisuus.exists(po => opiskeluoikeudenAlkamispäivä.exists(d => po.alku.isBefore(d)))
          ) {
            validatePidennettyOppivelvollisuusJakso(
              lt.pidennettyOppivelvollisuus.map(po => po.copy(alku = opiskeluoikeudenAlkamispäivä.getOrElse(po.alku))),
              vammaisuusjaksot
            )
          } else {
            KoskiErrorCategory.badRequest.validation.date.vammaisuusjaksoPidennetynOppivelvollisuudenUlkopuolella()
          }
        }
      case _ => HttpStatus.ok
    }
  }


  private def validatePidennettyOppivelvollisuus(
    lisätiedot: Option[OpiskeluoikeudenLisätiedot],
    opiskeluoikeudenAlkamispäivä: Option[LocalDate],
    opiskeluoikeudenPäättymispäivä: Option[LocalDate]
  ): HttpStatus = {

    // Yhdistää päällekkäiset aikajaksot sekä sellaiset jaksot, jotka alkavat seuraavana päivänä edellisen jakson päättymisestä
    // Palauttaa annetuista aikajaksoista yhdistetyt pisimmät mahdolliset yhtenäiset aikajaksot. Loppupäivämäärien käsittelyssä
    // poikkeuksellinen käsittely sen varmistamiseksi, että kronologisesti viimeisen jakson pitää päättyä pidennetyn
    // oppivelvollisuuden kanssa samaan aikaan.
    def foldAikajaksot(kaikkiJaksot: List[Aikajakso]): List[Aikajakso] = {

      def asettuvatPeräkkäin(edellinen: Aikajakso, seuraava: Aikajakso): Boolean = {
        edellinen.contains(seuraava.alku) || edellinen.contains(seuraava.alku.minusDays(1))
      }

      kaikkiJaksot.sortBy(r => (r.alku, r.loppu.getOrElse(LocalDate.MAX))).foldLeft(List.empty[Aikajakso])((acc, seuraava) => {
        acc match {
          case Nil => List(seuraava)
          case edellinen :: js
            if asettuvatPeräkkäin(edellinen, seuraava) => {
              // jos edellinen on alkanut oikeasti aiemmin, ja sillä on avoin päättyminen, niin loppupäivä otetaan aina jälkimmäisestä:
              // haluttu sääntö on, että kronologisesti viimeisen jakson pitää päättyä, jos pidennetty oppivelvollisuus on päättynyt.
              val loppu =
                if (edellinen.alku.isBefore(seuraava.alku) && edellinen.loppu.isEmpty) {
                  seuraava.loppu
                } else {
                  List(edellinen.loppu, seuraava.loppu).max(localDateOptionOrdering)
                }
              edellinen.copy(loppu = loppu) :: js
          }
          case _ => seuraava :: acc
        }
      }).reverse
    }

    lisätiedot match {
      case Some(lt: PidennettyOppivelvollisuus) if lt.pidennettyOppivelvollisuus.isDefined =>
        val kaikkiJaksot = lt.vammainen.getOrElse(List.empty) ++ lt.vaikeastiVammainen.getOrElse(List.empty)

        val kaikkiVammaisuusjaksotYhdistettynä = foldAikajaksot(kaikkiJaksot)
        val vammaisuusjaksotYhdistettynä = foldAikajaksot(lt.vammainen.getOrElse(List.empty))
        val vaikeastiVammaisuusjaksotYhdistettynä = foldAikajaksot(lt.vaikeastiVammainen.getOrElse(List.empty))

        val tuenPäätöksenJaksot = lt match {
          case tp: Tukipäätöksellinen => tp.kaikkiTuenPäätöksenJaksot
          case _ => List.empty
        }

        val tuenJaksotYhdistettynä = yhdistäPäällekäisetJaPeräkkäisetMahdollisestiAlkupäivällisetAikajaksot(
          lt.kaikkiErityisenTuenPäätöstenAikajaksot ++ tuenPäätöksenJaksot
        )

        val pidennettyOppivelvollisuusEiPäätyEnnenkuinOpiskeluoikeusAlkaa =
          lt.pidennettyOppivelvollisuus.get.loppu.isEmpty ||
          opiskeluoikeudenAlkamispäivä.isEmpty ||
          !lt.pidennettyOppivelvollisuus.get.loppu.get.isBefore(opiskeluoikeudenAlkamispäivä.get)

        val jokinVammaisuusjaksoKokoPidennetynOppivelvollisuudenAjan =
          kaikkiVammaisuusjaksotYhdistettynä.exists(j => j.contains(lt.pidennettyOppivelvollisuus.get))

        val kaikkiVammaisuusjaksotOsuvatPidennettyynOppivelvollisuuteen =
          kaikkiVammaisuusjaksotYhdistettynä.forall(j => lt.pidennettyOppivelvollisuus.get.contains(j))

        val viimeinenVammaisuusjaksoPäättyyOikeinPidennetynOppivelvollisuudenPäättyessä =
          lt.pidennettyOppivelvollisuus.get.loppu.isEmpty ||
          vammaisuusjaksotYhdistettynä.exists(_.loppu == lt.pidennettyOppivelvollisuus.get.loppu) ||
          vaikeastiVammaisuusjaksotYhdistettynä.exists(_.loppu == lt.pidennettyOppivelvollisuus.get.loppu)

        val eiPäällekäisiäEriVammaisuustyypinJaksoja =
          !vammaisuusjaksotYhdistettynä.exists(vj => vaikeastiVammaisuusjaksotYhdistettynä.exists(_.overlaps(vj)))

        val looginenTakaraja = List(lt.pidennettyOppivelvollisuus.get.loppu.getOrElse(LocalDate.MAX), opiskeluoikeudenPäättymispäivä.getOrElse(LocalDate.MAX)).min[LocalDate]
        val jokinErityisenTuenJaksoKokoPidennetynOppivelvollisuudenAjan =
          tuenJaksotYhdistettynä.exists(j => {
            j.contains(lt.pidennettyOppivelvollisuus.get.alku) &&
              j.contains(lt.pidennettyOppivelvollisuus.get.loppu.getOrElse(looginenTakaraja))
          })

        HttpStatus.fold(
          HttpStatus.validate(pidennettyOppivelvollisuusEiPäätyEnnenkuinOpiskeluoikeusAlkaa)(
            KoskiErrorCategory.badRequest.validation.date.pidennettyOppivelvollisuus(
              "Pidennetty oppivelvollisuusjakso ei voi loppua ennen opiskeluoikeuden alkua"
            )
          ),
          HttpStatus.validate(jokinVammaisuusjaksoKokoPidennetynOppivelvollisuudenAjan)(
            KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso(
              "Oppivelvollisuuden pidennyksessä on päiviä, joina ei ole voimassaolevaa vammaisuusjaksoa"
            )
          ),
          HttpStatus.validate(kaikkiVammaisuusjaksotOsuvatPidennettyynOppivelvollisuuteen)(
            KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso(
              "Jokin vammaisuusjaksoista on pidennetyn oppivelvollisuuden ulkopuolella"
            )
          ),
          HttpStatus.validate(viimeinenVammaisuusjaksoPäättyyOikeinPidennetynOppivelvollisuudenPäättyessä)(
            KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso(
              "Viimeisimmän vammaisuusjakson päättymispäivä ei ole sama kuin pidennetyn oppivelvollisuuden määritelty päättymispäivä"
            )
          ),
          HttpStatus.validate(eiPäällekäisiäEriVammaisuustyypinJaksoja)(
            KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso(
              "Vaikeasti vammaisuuden ja muun kuin vaikeasti vammaisuuden aikajaksot eivät voi olla voimassa samana päivänä"
            )
          ),
          HttpStatus.validate(jokinErityisenTuenJaksoKokoPidennetynOppivelvollisuudenAjan)(
            KoskiErrorCategory.badRequest.validation.date.erityisenTuenPäätös(
              "Oppivelvollisuuden pidennyksessä on päiviä, joina ei ole voimassaolevaa erityisen tuen jaksoa"
            )
          ),
        )
      case Some(lt: PidennettyOppivelvollisuus) =>
        val eiVammaisuusjaksoja =
          (lt.vammainen.isEmpty || lt.vammainen.get.isEmpty) &&
            (lt.vaikeastiVammainen.isEmpty || lt.vaikeastiVammainen.get.isEmpty)

        HttpStatus.validate(eiVammaisuusjaksoja)(
          KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso(
            "Vammaisuusjaksoja ei voi olla ilman vastaavaa pidennetyn oppivelvollisuuden jaksoa"
          )
        )
      case _ => HttpStatus.ok
    }
  }

  private def validateTuvaPerusopetusErityinenTukiJaVammaisuusAikarajastaAlkaen(
    lisätiedot: Option[OpiskeluoikeudenLisätiedot]
  ): HttpStatus = {
    val validaatioViimeinenPäiväEnnenVoimassaoloa = LocalDate.parse(config.getString("validaatiot.pidennetynOppivelvollisuudenYmsValidaatiotAstuvatVoimaan")).minusDays(1)
    val voimassaolotarkastusAstunutVoimaan = LocalDate.now().isAfter(validaatioViimeinenPäiväEnnenVoimassaoloa)

    if (Environment.isProdEnvironment(config)) {
      // Tuvassa tuotannossa ei ole vanhoja validointeja toteutettuna, koska pidennetty oppivelvollisuus ei enää ole osa tuvan tietomallia
      if (voimassaolotarkastusAstunutVoimaan) {
        validateTuvaPerusopetusErityinenTukiJaVammaisuus(lisätiedot)
      } else {
        HttpStatus.ok
      }
    } else if (Environment.isServerEnvironment(config)) {
      // Muissa palvelinympäristöissä käytä vain uusia validointeja testausta ja järjestelmätoimittajia varten
      validateTuvaPerusopetusErityinenTukiJaVammaisuus(lisätiedot)
    } else {
      // Muuten käytä uusia, jos ovat voimassa, eikä mitään, jos eivät ole voimassa (testejä varten lähinnä: vanhoilla validaatioilla ei enää ole testejä)
      if (voimassaolotarkastusAstunutVoimaan) {
        validateTuvaPerusopetusErityinenTukiJaVammaisuus(lisätiedot)
      } else {
        HttpStatus.ok
      }
    }
  }

  private def validateTuvaPerusopetusErityinenTukiJaVammaisuus(
    lisätiedot: Option[OpiskeluoikeudenLisätiedot]
  ): HttpStatus = {
    lisätiedot match {
      case Some(lt: TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot) =>
        val kaikkiJaksot = lt.vammainen.getOrElse(List.empty) ++ lt.vaikeastiVammainen.getOrElse(List.empty)

        val erityisenTuenJaksotYhdistettynä =
          yhdistäPäällekäisetJaPeräkkäisetMahdollisestiAlkupäivällisetAikajaksot(lt.kaikkiErityisenTuenPäätöstenAikajaksot)

        val vammaisuusJaksotYhdistettynä =
          yhdistäPäällekäisetJaPeräkkäisetAikajaksot(kaikkiJaksot)

        val erityinenTukiKaikkienVammaisuusjaksojenAjan =
          vammaisuusJaksotYhdistettynä.forall(j => erityisenTuenJaksotYhdistettynä.exists(_.contains(j)))

        val eiPäällekäisiäEriVammaisuustyypinJaksoja = {
          !lt.vammainen.getOrElse(List.empty).exists(vj => lt.vaikeastiVammainen.getOrElse(List.empty).exists(_.overlaps(vj)))
        }

        HttpStatus.fold(
          HttpStatus.validate(eiPäällekäisiäEriVammaisuustyypinJaksoja)(
            KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso(
              "Vaikeasti vammaisuuden ja muun kuin vaikeasti vammaisuuden aikajaksot eivät voi olla voimassa samana päivänä"
            )
          ),
          HttpStatus.validate(erityinenTukiKaikkienVammaisuusjaksojenAjan)(
            KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso(
              "Vammaisuusjaksot sisältävät päiviä, joina ei ole voimassaolevaa erityisen tuen jaksoa"
            )
          ),
        )
      case _ => HttpStatus.ok
    }
  }

  // Yhdistää päällekkäiset aikajaksot sekä sellaiset jaksot, jotka alkavat seuraavana päivänä edellisen jakson päättymisestä
  // Palauttaa annetuista aikajaksoista yhdistetyt pisimmät mahdolliset yhtenäiset aikajaksot
  private def yhdistäPäällekäisetJaPeräkkäisetMahdollisestiAlkupäivällisetAikajaksot(
    kaikkiJaksot: List[MahdollisestiAlkupäivällinenJakso]
  ): List[SuljettuJakso] = {
    SuljettuJakso.yhdistäPäällekäisetJaPeräkkäiset(kaikkiJaksot.map(SuljettuJakso.apply))
  }

  // Yhdistää päällekkäiset aikajaksot sekä sellaiset jaksot, jotka alkavat seuraavana päivänä edellisen jakson päättymisestä
  // Palauttaa annetuista aikajaksoista yhdistetyt pisimmät mahdolliset yhtenäiset aikajaksot
  private def yhdistäPäällekäisetJaPeräkkäisetAikajaksot(
    kaikkiJaksot: List[Aikajakso]
  ): List[SuljettuJakso] = {
    SuljettuJakso.yhdistäPäällekäisetJaPeräkkäiset(kaikkiJaksot.map(SuljettuJakso.apply))
  }

  case class SuljettuJakso(
    alku: LocalDate,
    loppu: LocalDate
  ) extends Alkupäivällinen with DateContaining {
    def contains(d: LocalDate): Boolean = !(d.isBefore(alku) || d.isAfter(loppu))

    def contains(j: SuljettuJakso): Boolean = contains(j.alku) && contains(j.loppu)

    def contains(j: Aikajakso): Boolean = contains(SuljettuJakso(j))

    def contains(j: MahdollisestiAlkupäivällinenJakso): Boolean = contains(SuljettuJakso(j))

    def toFinnishDateFormat: String = FinnishDateFormat.format(Some(alku), Some(loppu))
  }

  object SuljettuJakso {
    def apply(j: MahdollisestiAlkupäivällinenJakso): SuljettuJakso = SuljettuJakso(
      alku = j.alku.getOrElse(LocalDate.MIN),
      loppu = j.loppu.getOrElse(LocalDate.MAX)
    )

    def apply(j: Aikajakso): SuljettuJakso = SuljettuJakso(
      alku = j.alku,
      loppu = j.loppu.getOrElse(LocalDate.MAX)
    )

    def yhdistäPäällekäisetJaPeräkkäiset(
      kaikkiJaksot: List[SuljettuJakso]
    ): List[SuljettuJakso] = {
      järjestäAikajärjestykseen(kaikkiJaksot).foldLeft(List.empty[SuljettuJakso])((acc, seuraava) => {
        acc match {
          case Nil => List(seuraava)
          case edellinen :: js if asettuvatPeräkkäin(edellinen, seuraava) =>
            SuljettuJakso(
              alku = edellinen.alku,
              loppu = List(edellinen.loppu, seuraava.loppu).max(localDateOrdering)
            ) :: js
          case _ => seuraava :: acc
        }
      }).reverse
    }

    private def järjestäAikajärjestykseen(
      jaksot: List[SuljettuJakso]
    ): List[SuljettuJakso] = {
      jaksot.sortBy(r => (r.alku, r.loppu))
    }

    private def asettuvatPeräkkäin(edellinen: SuljettuJakso, seuraava: SuljettuJakso): Boolean = {
      edellinen.contains(seuraava.alku) || edellinen.contains(seuraava.alku.minusDays(1))
    }
  }

  private def validateSuoritus(suoritus: Suoritus, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, henkilö: Option[Henkilö], parent: List[Suoritus])(implicit user: KoskiSpecificSession, accessType: AccessType.Value): HttpStatus = {
    val arviointipäivät: List[LocalDate] = suoritus.sortedArviointi.flatMap(_.arviointipäivä)
    val alkamispäivä: (String, Iterable[LocalDate]) = ("suoritus.alkamispäivä", suoritus.alkamispäivä)
    val vahvistuspäivät: Option[LocalDate] = suoritus.vahvistus.map(_.päivä)
    val parentVahvistuspäivät = parent.flatMap(_.vahvistus.map(_.päivä))
    HttpStatus.fold(
      validateVahvistusAndPäättymispäiväDateOrder(suoritus, opiskeluoikeus, vahvistuspäivät) ::
        validateDateOrder(("osasuoritus.vahvistus.päivä", vahvistuspäivät), ("suoritus.vahvistus.päivä", parentVahvistuspäivät), KoskiErrorCategory.badRequest.validation.date.suorituksenVahvistusEnnenSuorituksenOsanVahvistusta) ::
        validateDateOrder(alkamispäivä, ("suoritus.arviointi.päivä", arviointipäivät), KoskiErrorCategory.badRequest.validation.date.arviointiEnnenAlkamispäivää)
          .onSuccess(validateDateOrder(("suoritus.arviointi.päivä", arviointipäivät), ("suoritus.vahvistus.päivä", vahvistuspäivät), KoskiErrorCategory.badRequest.validation.date.vahvistusEnnenArviointia)
            .onSuccess(validateDateOrder(alkamispäivä, ("suoritus.vahvistus.päivä", vahvistuspäivät), KoskiErrorCategory.badRequest.validation.date.vahvistusEnnenAlkamispäivää)))
        :: validateAlkamispäivä(suoritus)
        :: validateToimipiste(opiskeluoikeus, suoritus)
        :: validateStatus(suoritus, opiskeluoikeus)
        :: validateArvioinnit(suoritus)
        :: validateArviointienOlemassaolo(suoritus, opiskeluoikeus)
        :: validateLaajuus(suoritus)
        :: validateNuortenPerusopetuksenPakollistenOppiaineidenLaajuus(suoritus, opiskeluoikeus)
        :: validateSuoritustenLuokkaAsteet(suoritus, opiskeluoikeus)
        :: validateOppiaineet(suoritus)
        :: validatePäiväkodinEsiopetus(suoritus, opiskeluoikeus)
        :: ePerusteetValidator.validateTutkinnonosanRyhmä(suoritus, opiskeluoikeus.getVaadittuPerusteenVoimassaolopäivä)
        :: validateOsaamisenHankkimistavat(suoritus)
        :: validateYhteisetTutkinnonOsat(suoritus, opiskeluoikeus)
        :: validateÄidinkielenOmainenKieli(suoritus)
        :: validateSuorituskieli(suoritus)
        :: validateRajattuOppimäärä(suoritus, opiskeluoikeus)
        :: Lukio2019OsasuoritusValidation.validate(suoritus, parent)
        :: Lukio2019VieraatKieletValidation.validate(suoritus, parent)
        :: Lukio2019ArvosanaValidation.validateOsasuoritus(suoritus)
        :: LukionYhteisetValidaatiot.validateLukionPäätasonSuoritus(suoritus)
        :: LukioonValmistavanKoulutuksenValidaatiot.validateLukioonValmistava2019(suoritus)
        :: VapaaSivistystyöValidation.validateVapaanSivistystyönPäätasonSuoritus(config, henkilö, suoritus, opiskeluoikeus, VapaaSivistystyöValidation.vstJotpaAikaisinSallittuAlkamispäivä(config), henkilöRepository, koskiOpiskeluoikeudet)
        :: VSTKotoutumiskoulutus2022Validation.validate(suoritus)
        :: TutkintokoulutukseenValmentavaKoulutusValidation.validateTuvaSuoritus(config, suoritus, opiskeluoikeus)
        :: HttpStatus.validate(!suoritus.isInstanceOf[PäätasonSuoritus])(validateDuplicates(suoritus.osasuoritukset.toList.flatten))
        :: suoritus.osasuoritusLista.map(validateSuoritus(_, opiskeluoikeus, henkilö, suoritus :: parent))
    )
  }

  private def validateRajattuOppimäärä(suoritus: Suoritus, opiskeluoikeus: Opiskeluoikeus): HttpStatus = {
    val oppiaineenRajattuOppimääräVoimaan = LocalDate.parse(config.getString("validaatiot.oppiaineenRajattuOppimääräVoimaan"))
    val yksilöllistettyOppimääräViimeinenKäyttöpäivä = LocalDate.parse(config.getString("validaatiot.yksilöllistetynOppimääränViimeinenKäyttöpäivä"))

    def sisältyyTuenPäätöksenJaksoon(d: LocalDate): Boolean = {
      opiskeluoikeus.lisätiedot match {
        case Some(x: PerusopetuksenOpiskeluoikeudenLisätiedot) if x.tuenPäätöksenJaksot.nonEmpty =>
          val tukijaksot = yhdistäPäällekäisetJaPeräkkäisetMahdollisestiAlkupäivällisetAikajaksot(x.kaikkiTuenPäätöksenJaksot)
          tukijaksot.exists(_.contains(d))
        case _ => false
      }
    }

    def vainSallittuArvosana(os: Suoritus, arvosana: String): Boolean = {
      val arvosanat = os.arviointi.toList.flatten.collect {
        case a: PerusopetuksenOppiaineenArviointi => a.arvosana.koodiarvo
      }
      arvosanat.nonEmpty && arvosanat.forall(_ == arvosana)
    }

    suoritus match {
      case s: PerusopetuksenPäätasonSuoritus if s.vahvistus.nonEmpty =>
        val vahvistuspvm = s.vahvistus.get.päivä
        HttpStatus.fold(
          suoritus.osasuoritusLista.map {
            case os: RajattavaOppimäärä if os.rajattuOppimäärä =>
              HttpStatus.fold(
                HttpStatus.validate(!vahvistuspvm.isBefore(oppiaineenRajattuOppimääräVoimaan))(KoskiErrorCategory.badRequest.validation.date(s"Tietoa rajattuOppimäärä ei saa siirtää ennen $oppiaineenRajattuOppimääräVoimaan alkaneelle suoritukselle")),
                HttpStatus.validate(sisältyyTuenPäätöksenJaksoon(vahvistuspvm))(KoskiErrorCategory.badRequest.validation.date(s"Tieto rajattuOppimäärä vaatii tukijakson suorituksen vahvistuspäivälle: $vahvistuspvm")),
                suoritus.koulutusmoduuli.tunniste.koodiarvo match {
                  case "201101" =>
                    HttpStatus.validate(vainSallittuArvosana(os, "5"))(KoskiErrorCategory.badRequest.validation.date(s"Rajatulle oppimäärälle sallitaan vain arvosana 5 kun kyseessä on perusopetuksen oppimäärän suoritus"))
                  case "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" =>
                    HttpStatus.validate(vainSallittuArvosana(os, "S"))(KoskiErrorCategory.badRequest.validation.date(s"Rajatulle oppimäärälle sallitaan vain arvosana S kun kyseessä on 1. - 8. lk suoritus"))
                  case _ => HttpStatus.ok
                }
              )
            case os: RajattavaOppimäärä if os.yksilöllistettyOppimäärä =>
              HttpStatus.validate(vahvistuspvm.isBefore(yksilöllistettyOppimääräViimeinenKäyttöpäivä))(KoskiErrorCategory.badRequest.validation.date(s"Tietoa yksilöllistettyOppimäärä ei saa siirtää $yksilöllistettyOppimääräViimeinenKäyttöpäivä jälkeen alkaneelle suoritukselle"))
            case _ => HttpStatus.ok
          }
        )
      case _ => HttpStatus.ok
    }
  }

  private def validateVahvistusAndPäättymispäiväDateOrder(suoritus: Suoritus, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, vahvistuspäivät: Option[LocalDate]): HttpStatus = {
    // Kun suoritetaan ammatillista tutkintoa näyttönä, voi tutkinnon vahvistus (tutkintotoimikunnalta) tulla opiskeluoikeuden päättymisen jälkeen.
    // Kun suoritetaan VALMA-koulutusta on tyypillistä, että opiskelija saa opiskelupaikan muualta, jolloin opiskeluoikeus päättyy välittömästi, mutta hänen suorituksensa arviointi ja vahvistus tapahtuu myöhemmin.
    suoritus match {
      case s: AmmatillisenTutkinnonOsittainenTaiKokoSuoritus if s.suoritustapa.koodiarvo == "naytto" => HttpStatus.ok
      case s: ValmaKoulutuksenSuoritus => HttpStatus.ok
      case _ => validateDateOrder(("suoritus.vahvistus.päivä", vahvistuspäivät), ("päättymispäivä", opiskeluoikeus.päättymispäivä), KoskiErrorCategory.badRequest.validation.date.päättymispäiväEnnenVahvistusta)
    }
  }

  private def validateDuplicates(suoritukset: Seq[Suoritus]) = {
    HttpStatus.fold(suoritukset
      .filterNot(_.salliDuplikaatit)
      .groupBy(osasuoritus => (osasuoritus.koulutusmoduuli.identiteetti, osasuoritus.ryhmittelytekijä))
      .collect { case (group, osasuoritukset) if osasuoritukset.length > 1 => group }
      .map { case (tutkinnonOsa, ryhmä) =>
        val ryhmänKuvaus = ryhmä.map(r => " ryhmässä " + r).getOrElse("")
        KoskiErrorCategory.badRequest.validation.rakenne.duplikaattiOsasuoritus(s"Osasuoritus ${tutkinnonOsa} esiintyy useammin kuin kerran" + ryhmänKuvaus)
      }
    )
  }

  private def validateAlkamispäivä(suoritus: Suoritus): HttpStatus = {
    lazy val virhe = KoskiErrorCategory.badRequest.validation.tila.alkamispäiväPuuttuu("Suoritukselle " + suorituksenTunniste(suoritus) + " ei ole merkitty alkamispäivää")

    suoritus match {
      case s: PerusopetuksenVuosiluokanSuoritus => HttpStatus.validate(s.alkamispäivä.isDefined)(virhe)
      case s: DiplomaVuosiluokanSuoritus => HttpStatus.validate(s.alkamispäivä.isDefined)(virhe)
      case s: MYPVuosiluokanSuoritus if s.koulutusmoduuli.tunniste.koodiarvo == "10" => HttpStatus.validate(s.alkamispäivä.isDefined)(virhe)
      case s: EuropeanSchoolOfHelsinkiVuosiluokanSuoritus => HttpStatus.validate(s.alkamispäivä.isDefined)(virhe)
      case _ => HttpStatus.ok
    }
  }

  private def validateToimipiste(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, suoritus: Suoritus)(implicit user: KoskiSpecificSession, accessType: AccessType.Value): HttpStatus = suoritus match {
    case s: Toimipisteellinen => validateOrganisaatioAccess(opiskeluoikeus, s.toimipiste)
    case _ => HttpStatus.ok
  }

  private def validateLaajuus(suoritus: Suoritus): HttpStatus = {
    def validateLaajuus(laajuus: Laajuus, osasuoritustenLaajuudet: List[Laajuus]) = (osasuoritustenLaajuudet, suoritus.valmis) match {
      case (_, false) => HttpStatus.ok
      case (Nil, _) => HttpStatus.ok
      case (_, _) =>
        osasuoritustenLaajuudet.map(_.arvo).sum match {
          case summa if Math.abs(summa - laajuus.arvo) < 0.001 =>
            HttpStatus.ok
          case summa =>
            KoskiErrorCategory.badRequest.validation.laajuudet.osasuoritustenLaajuuksienSumma("Suorituksen " + suorituksenTunniste(suoritus) + " osasuoritusten laajuuksien summa " + summa + " ei vastaa suorituksen laajuutta " + laajuus.arvo)
        }
    }

    def osasuoritustenLaajuudet: List[Laajuus] = suoritus.osasuoritusLista.map(_.koulutusmoduuli).flatMap(_.getLaajuus)
    def osasuoritustenLaajuudetHyväksytty: List[Laajuus] = suoritus.osasuoritusLista.filter(os => os.viimeisinArviointi.exists(_.hyväksytty)).map(_.koulutusmoduuli).flatMap(_.getLaajuus)

    val perusopetukseenValmistavaOpetusKokonaislaajuusYksikköTunneissaAlkaa = LocalDate.parse(config.getString("validaatiot.perusopetukseenValmistavaOpetusKokonaislaajuusYksikköTunneissaAlkaa")).minusDays(1)
    val perusopetukseenValmistavaOpetusKokonaislaajuusYksikköTunneissaVoimassa = LocalDate.now().isAfter(perusopetukseenValmistavaOpetusKokonaislaajuusYksikköTunneissaAlkaa)

    (suoritus.koulutusmoduuli.getLaajuus, suoritus) match {
      case (Some(laajuus: Laajuus), _) =>
        val yksikköValidaatio = HttpStatus.fold(suoritus.osasuoritusLista.map { case osasuoritus =>
          osasuoritus.koulutusmoduuli.getLaajuus match {
            case Some(osasuorituksenLaajuus: Laajuus) if laajuus.yksikkö != osasuorituksenLaajuus.yksikkö =>
              KoskiErrorCategory.badRequest.validation.laajuudet.osasuorituksellaEriLaajuusyksikkö("Osasuorituksella " + suorituksenTunniste(osasuoritus) + " eri laajuuden yksikkö kuin suorituksella " + suorituksenTunniste(suoritus))
            case _ => HttpStatus.ok
          }
        })

        yksikköValidaatio.onSuccess({
          suoritus.koulutusmoduuli match {
            case _: LaajuuttaEiValidoida => HttpStatus.ok
            case _: TutkintokoulutukseenValmentavanKoulutus | _: TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa =>
              TutkintokoulutukseenValmentavaKoulutusValidation.validateLaajuusRajapäivääEnnenTaiJälkeen(
                config,
                () => validateLaajuus(laajuus, osasuoritustenLaajuudet),
                () => validateLaajuus(laajuus, osasuoritustenLaajuudetHyväksytty)
              )
            case _ =>
              validateLaajuus(laajuus, osasuoritustenLaajuudet)
          }
        })

      case (_, s: DIAPäätasonSuoritus) if s.valmis && s.osasuoritusLista.map(_.koulutusmoduuli).exists(_.getLaajuus.isEmpty) =>
        KoskiErrorCategory.badRequest.validation.laajuudet.oppiaineenLaajuusPuuttuu("Suoritus " + suorituksenTunniste(suoritus) + " on merkitty valmiiksi, mutta se sisältää oppiaineen, jolta puuttuu laajuus")

      case (_, s: NuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa) if s.suoritustapa.exists(kviite => kviite.koodiarvo == "erityinentutkinto") =>
        HttpStatus.ok

      case (laajuus, s: Laajuudellinen) if laajuus.isEmpty =>
        KoskiErrorCategory.badRequest.validation.laajuudet.oppiaineenLaajuusPuuttuu(s"Oppiaineen ${suorituksenTunniste(suoritus)} laajuus puuttuu")

      case (_, o: PerusopetukseenValmistavanOpetuksenSuoritus) if perusopetukseenValmistavaOpetusKokonaislaajuusYksikköTunneissaVoimassa && o.kokonaislaajuus.exists(l => l.yksikkö.koodiarvo != "5") =>
        KoskiErrorCategory.badRequest.validation.laajuudet.perusopetukseenValmistavaOpetusKokonaislaajuusYksikköEiTunneissa()
      case _ => HttpStatus.ok
    }
  }

  private def validateStatus(suoritus: Suoritus, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    val rinnastettavissaVahvistettuun = opiskeluoikeus match {
      case oo: LukionOpiskeluoikeus => oo.isOppimääräSuoritettu
      case _ => false
    }

    if (suoritus.vahvistettu && suoritus.arviointiPuuttuu) {
      KoskiErrorCategory.badRequest.validation.tila.vahvistusIlmanArviointia("Suorituksella " + suorituksenTunniste(suoritus) + " on vahvistus, vaikka arviointi puuttuu")
    } else {
      suoritus match {
        case _ if suoritus.kesken && !rinnastettavissaVahvistettuun => HttpStatus.ok
        case _: Välisuoritus if !rinnastettavissaVahvistettuun =>
          HttpStatus.ok // Välisuoritus on statukseltaan aina "valmis" -> ei validoida niiden sisältämien osasuoritusten statusta
        case p: KoskeenTallennettavaPäätasonSuoritus =>
          validatePäätasonSuorituksenStatus(opiskeluoikeus, p).onSuccess(validateLinkitettyTaiSisältääOsasuorituksia(opiskeluoikeus, p))
        case s => validateValmiinSuorituksenStatus(s)
      }
    }
  }

  private def validatePäätasonSuorituksenStatus(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, suoritus: KoskeenTallennettavaPäätasonSuoritus) = suoritus match {
    case a: AmmatillisenTutkinnonOsittainenSuoritus => validateValmiinAmmatillisenTutkinnonOsittainenSuoritus(a, opiskeluoikeus)
    case _: VapaanSivistystyönKoulutuksenPäätasonSuoritus => HttpStatus.ok // Osalle VST:n opiskeluoikeustyyppejä tämä validaatio ei päde. VST:llä omia validaatio tätä tapausta varten.
    case _: YlioppilastutkinnonSuoritus => HttpStatus.ok // Ylioppilastutkinnon opiskeluoikeudessa voi tutkinnon suorituksesta huolimatta olla esim. keskeytettyjä osasuorituksia
    case s => validateValmiinSuorituksenStatus(s)
  }

  private def validateValmiinAmmatillisenTutkinnonOsittainenSuoritus(suoritus: AmmatillisenTutkinnonOsittainenSuoritus, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    HttpStatus.fold(suoritus.osasuoritusLista.map {
      case y: YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus if y.kesken =>
        HttpStatus.validate(y.osasuoritusLista.forall(_.valmis) && y.osasuoritukset.nonEmpty)(
          KoskiErrorCategory.badRequest.validation.tila.keskeneräinenOsasuoritus("Valmiiksi merkityllä suorituksella " + suorituksenTunniste(suoritus) + " on keskeneräinen osasuoritus " + suorituksenTunniste(y))
        )
      case x => validateValmiinSuorituksenStatus(x)
    })
  }

  private def validateLinkitettyTaiSisältääOsasuorituksia(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, suoritus: KoskeenTallennettavaPäätasonSuoritus) = {
    if (osasuorituksetKunnossa(suoritus, opiskeluoikeus) || ostettuOpiskeluoikeusValmisEnnenVuotta2019(opiskeluoikeus)) {
      HttpStatus.ok
    } else if (koskiOpiskeluoikeudet.isKuoriOpiskeluoikeus(opiskeluoikeus)) {
      HttpStatus.ok
    } else {
      valmiiksiMerkitylläEiOsasuorituksia(suoritus)
    }
  }

  private def osasuorituksetKunnossa(suoritus: PäätasonSuoritus, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus) = suoritus match {
      case _: EsiopetuksenSuoritus |
           _: MuunAmmatillisenKoulutuksenSuoritus |
           _: OppiaineenSuoritus |
           _: OppiaineenOppimääränSuoritus |
           _: NäyttötutkintoonValmistavanKoulutuksenSuoritus
      => true
      case s: PerusopetuksenVuosiluokanSuoritus if s.koulutusmoduuli.tunniste.koodiarvo == "9" || s.jääLuokalle => true
      case _: PerusopetuksenVuosiluokanSuoritus
        if PerusopetuksenOpiskeluoikeusValidation.onVuosiluokkiinSitoutumatonOpetus(opiskeluoikeus) => true
      case s: PerusopetuksenVuosiluokanSuoritus
        if s.vahvistettu && PerusopetuksenOpiskeluoikeusValidation.tavoitekokonaisuuksittainOpiskeluVoimassa(opiskeluoikeus, suoritus.vahvistus.map(_.päivä).get) => true
      case s: LukionOppimääränSuoritus2019
      => osasuorituksetKunnossaLukio2019(s, opiskeluoikeus.oppilaitos.map(_.oid))
      case s: LukionOppiaineidenOppimäärienSuoritus2019 if opiskeluoikeus.asInstanceOf[LukionOpiskeluoikeus].oppimääräSuoritettu.getOrElse(false)
      => osasuorituksetKunnossaLukio2019(s, None)
      case s: NurseryVuosiluokanSuoritus
        => true
      case s: SecondaryUpperVuosiluokanSuoritus if s.koulutusmoduuli.tunniste.koodiarvo == "S7"
        => EuropeanSchoolOfHelsinkiValidation.osasuorituksetKunnossa(s)
      case s: EBTutkinnonSuoritus
        => EuropeanSchoolOfHelsinkiValidation.osasuorituksetKunnossa(s)
      case s: TaiteenPerusopetuksenPäätasonSuoritus => true
      case s: VapaanSivistystyönOsaamismerkinSuoritus => true
      case s => s.osasuoritusLista.nonEmpty
    }

  private def osasuorituksetKunnossaLukio2019(
    suoritus: LukionPäätasonSuoritus2019,
    oppilaitosOid: Option[Organisaatio.Oid]
  ): Boolean = {
    (sisältääErityisenTutkinnonSuorittamisen(suoritus), sisältääOmanÄidinkielenOpintojenSuorituksia(suoritus), suoritus.oppimäärä.koodiarvo) match {
      case (false, false, "nuortenops")
        => lukio2019TarpeeksiOsasuorituksia(suoritus.osasuoritukset.getOrElse(List()), 150, 20)
      case (false, false, "aikuistenops") if LukionYhteisetValidaatiot.laajuusValidoitavaOppilaitoksessa(oppilaitosOid)
        => lukio2019TarpeeksiOsasuorituksia(suoritus.osasuoritukset.getOrElse(List()), 88, 0)
      case _
        => suoritus.osasuoritusLista.nonEmpty
    }
  }

  private def sisältääErityisenTutkinnonSuorittamisen(suoritus: LukionPäätasonSuoritus2019) = {
    suoritus match {
      case s: LukionOppimääränSuoritus2019 => s.suoritettuErityisenäTutkintona ||
        suoritus.osasuoritukset.exists(_.exists({
          case os: LukionOppiaineenSuoritus2019 if os.suoritettuErityisenäTutkintona => true
          case _ => false
        }))
      case _ => false
    }
  }

  private def sisältääOmanÄidinkielenOpintojenSuorituksia(suoritus: LukionPäätasonSuoritus2019) =
    suoritus match {
      case s: LukionOppimääränSuoritus2019 => s.omanÄidinkielenOpinnot.exists(_.osasuoritukset.exists(_.nonEmpty))
      case _ => false
    }

  private def lukio2019TarpeeksiOsasuorituksia(osasuoritukset: List[LukionOppimääränOsasuoritus2019], minimiLaajuus: Double, minimiValinnaistenLaajuus: Double): Boolean = {
    val kaikki = osasuoritukset.flatMap(_.osasuoritusLista.map(_.koulutusmoduuli.laajuus.arvo))
    val kaikkiYhteensä = kaikki.map(BigDecimal.decimal).sum

    val valinnaiset = osasuoritukset.flatMap(_.osasuoritusLista.filterNot(_.koulutusmoduuli.pakollinen).map(_.koulutusmoduuli.laajuus.arvo))
    val valinnaisetYhteensä = valinnaiset.map(BigDecimal.decimal).sum

    kaikkiYhteensä >= BigDecimal.decimal(minimiLaajuus) && valinnaisetYhteensä >= BigDecimal.decimal(minimiValinnaistenLaajuus)
  }

  private def ostettuOpiskeluoikeusValmisEnnenVuotta2019(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus) = opiskeluoikeus match {
    case a: AmmatillinenOpiskeluoikeus => a.ostettu && a.päättymispäivä.exists(_.isBefore(LocalDate.of(2019, 1, 1)))
    case _ => false
  }

  private def valmiiksiMerkitylläEiOsasuorituksia(suoritus: PäätasonSuoritus) = suoritus match {
    case s: AmmatillisenTutkinnonOsittainenTaiKokoSuoritus =>
      KoskiErrorCategory.badRequest.validation.tila.valmiiksiMerkityltäPuuttuuOsasuorituksia(s"Suoritus ${suorituksenTunniste(suoritus)} on merkitty valmiiksi, mutta sillä ei ole ammatillisen tutkinnon osan suoritusta tai opiskeluoikeudelta puuttuu linkitys")
    case s: PerusopetuksenOppimääränSuoritus =>
      KoskiErrorCategory.badRequest.validation.tila.oppiaineetPuuttuvat("Suorituksella ei ole osasuorituksena yhtään oppiainetta, vaikka sillä on vahvistus")
    case s: PerusopetuksenVuosiluokanSuoritus =>
      KoskiErrorCategory.badRequest.validation.tila.oppiaineetPuuttuvat(s"Suorituksella ei ole osasuorituksena yhtään oppiainetta, vaikka sillä on vahvistus, eikä oppija ole vuosiluokkiin sitomattomassa opetuksessa (vuosiluokka ${s.koulutusmoduuli.luokkaAste}).")
    case s: LukionOppimääränSuoritus2019 if s.oppimäärä.koodiarvo == "nuortenops" =>
      KoskiErrorCategory.badRequest.validation.tila.valmiiksiMerkityltäPuuttuuOsasuorituksia(s"Suoritus ${suorituksenTunniste(suoritus)} on merkitty valmiiksi tai opiskeluoikeuden tiedoissa oppimäärä on merkitty suoritetuksi, mutta sillä ei ole 150 op osasuorituksia, joista vähintään 20 op valinnaisia, tai opiskeluoikeudelta puuttuu linkitys")
    case s: LukionOppimääränSuoritus2019 if s.oppimäärä.koodiarvo == "aikuistenops" =>
      KoskiErrorCategory.badRequest.validation.tila.valmiiksiMerkityltäPuuttuuOsasuorituksia(s"Suoritus ${suorituksenTunniste(suoritus)} on merkitty valmiiksi tai opiskeluoikeuden tiedoissa oppimäärä on merkitty suoritetuksi, mutta sillä ei ole 88 op osasuorituksia, tai opiskeluoikeudelta puuttuu linkitys")
    case s: LukionOppiaineidenOppimäärienSuoritus2019 if s.oppimäärä.koodiarvo == "nuortenops" =>
      KoskiErrorCategory.badRequest.validation.tila.valmiiksiMerkityltäPuuttuuOsasuorituksia(s"Suorituksen ${suorituksenTunniste(suoritus)} opiskeluoikeuden tiedoissa oppimäärä on merkitty suoritetuksi, mutta sillä ei ole 150 op osasuorituksia, joista vähintään 20 op valinnaisia, tai opiskeluoikeudelta puuttuu linkitys")
    case s: LukionOppiaineidenOppimäärienSuoritus2019 if s.oppimäärä.koodiarvo == "aikuistenops" =>
      KoskiErrorCategory.badRequest.validation.tila.valmiiksiMerkityltäPuuttuuOsasuorituksia(s"Suorituksen ${suorituksenTunniste(suoritus)} opiskeluoikeuden tiedoissa oppimäärä on merkitty suoritetuksi, mutta sillä ei ole 88 op osasuorituksia, tai opiskeluoikeudelta puuttuu linkitys")
    case s: SecondaryUpperVuosiluokanSuoritus =>
      KoskiErrorCategory.badRequest.validation.tila.valmiiksiMerkityltäPuuttuuOsasuorituksia(s"Suoritus ${suorituksenTunniste(s)} on merkitty valmiiksi, mutta sillä on tyhjä osasuorituslista tai joltain sen osasuoritukselta puuttuu vaadittavat arvioidut osasuoritukset (joko A ja B, tai yearmark), tai opiskeluoikeudelta puuttuu linkitys")
    case s: EBTutkinnonSuoritus =>
      KoskiErrorCategory.badRequest.validation.tila.valmiiksiMerkityltäPuuttuuOsasuorituksia(s"Suoritus ${suorituksenTunniste(s)} on merkitty valmiiksi, mutta sillä on tyhjä osasuorituslista tai joltain sen osasuoritukselta puuttuu vaadittava arvioitu Final-osasuoritus, tai opiskeluoikeudelta puuttuu linkitys")
    case s =>
      KoskiErrorCategory.badRequest.validation.tila.valmiiksiMerkityltäPuuttuuOsasuorituksia(s"Suoritus ${suorituksenTunniste(s)} on merkitty valmiiksi, mutta sillä on tyhjä osasuorituslista tai opiskeluoikeudelta puuttuu linkitys")
  }

  private def linkitysTehty(opiskeluoikeusOid: String, oppilaitosOid: Oid, oppijaOids: List[Oid]) =
    koskiOpiskeluoikeudet.findByOppijaOids(oppijaOids)(KoskiSpecificSession.systemUser)
      .exists(_.sisältyyOpiskeluoikeuteen.exists(_.oid == opiskeluoikeusOid))

  private def validateValmiinSuorituksenStatus(suoritus: Suoritus) = {
    suoritus.rekursiivisetOsasuoritukset.find(_.kesken).fold(HttpStatus.ok) { keskeneräinenOsasuoritus =>
      KoskiErrorCategory.badRequest.validation.tila.keskeneräinenOsasuoritus("Valmiiksi merkityllä suorituksella " + suorituksenTunniste(suoritus) + " on keskeneräinen osasuoritus " + suorituksenTunniste(keskeneräinenOsasuoritus))
    }
  }

  private def validateArviointienOlemassaolo(suoritus: Suoritus, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus) = {
    lazy val katsotaanEronneeksi = opiskeluoikeus.tila.opiskeluoikeusjaksot.exists(_.tila.koodiarvo == "katsotaaneronneeksi")
    lazy val arviointejaPuuttuu = suoritus.rekursiivisetOsasuoritukset.exists(_.arviointiPuuttuu)

    suoritus match {
      case _: TutkintokoulutukseenValmentavanKoulutuksenSuoritus |
           _: OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus
        if katsotaanEronneeksi && arviointejaPuuttuu =>
        KoskiErrorCategory.badRequest.validation.tila.eronneeksiKatsotunOpiskeluoikeudenArvioinnit()
      case _ => HttpStatus.ok
    }
  }

  private def validateArvioinnit(suoritus: Suoritus): HttpStatus = suoritus match {
    case a: AmmatillinenPäätasonSuoritus =>
      val käytetytArviointiasteikot = a.osasuoritusLista.flatMap(extractNumeerisetArvosanat).map(_.koodistoUri).distinct.sorted

      if (käytetytArviointiasteikot.size > 1) {
        KoskiErrorCategory.badRequest.validation.arviointi.useitaArviointiasteikoita(s"Suoritus käyttää useampaa kuin yhtä numeerista arviointiasteikkoa: ${käytetytArviointiasteikot.mkString(", ")}")
      } else {
        HttpStatus.ok
      }

    case ib: IBTutkinnonSuoritus =>
      def viimeisinArviointiNumeerinen(suoritus: Suoritus) = suoritus.viimeisinArviointi.exists(_.arvosana.koodiarvo forall isDigit)

      HttpStatus.fold(ib.osasuoritusLista
        .groupBy(_.koulutusmoduuli.identiteetti)
        .collect { case (identiteetti, oppiaineet) if oppiaineet.count(viimeisinArviointiNumeerinen) > 1 => identiteetti }
        .map(identiteetti => KoskiErrorCategory.badRequest.validation.rakenne.kaksiSamaaOppiainettaNumeroarvioinnilla(s"Kahdella saman oppiaineen suorituksella $identiteetti ei molemmilla voi olla numeerista arviointia"))
      )
    case n: NuortenPerusopetuksenOppimääränSuoritus if n.vahvistettu =>
      validatePäättötodistuksenSanallinenArviointi(n)
    case _: LukionPäätasonSuoritus2019 | _: PreIBSuoritus2019 =>
      Lukio2019ArvosanaValidation.validatePäätasonSuoritus(suoritus)
    case s: EBTutkinnonSuoritus =>
      EuropeanSchoolOfHelsinkiValidation.validateEBTutkinnonArvioinnit(s)
    case _ => HttpStatus.ok
  }

  private def extractNumeerisetArvosanat(suoritus: Suoritus): List[Koodistokoodiviite] = {
    def numeerisetArvosanat(arvioinnit: List[Arviointi]) = arvioinnit.collect {
      case k: KoodistostaLöytyväArviointi if k.arvosana.koodiarvo.forall(isDigit) => k.arvosana
    }

    def näytönArvosanat = suoritus match {
      case atos: AmmatillisenTutkinnonOsanSuoritus =>
        val näytönArviointi = atos.näyttö.flatMap(_.arviointi).toList
        val arviointikohteidenArvosanat = näytönArviointi.flatMap(_.arviointikohteet.toList.flatten).filter(_.arvosana.koodiarvo.forall(isDigit)).map(_.arvosana)
        numeerisetArvosanat(näytönArviointi) ++ arviointikohteidenArvosanat
      case _ => Nil
    }

    numeerisetArvosanat(suoritus.sortedArviointi) ++ näytönArvosanat ++ suoritus.osasuoritusLista.flatMap(extractNumeerisetArvosanat)
  }

  private def validatePäätasonSuoritustenStatus(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus) = {
    def valmiitaOppimääriäLöytyy =
      opiskeluoikeus.suoritukset.exists(s => s.valmis && s.isInstanceOf[OppiaineenOppimääränSuoritus] && !s.koulutusmoduuli.isInstanceOf[EiTiedossaOppiaine])

    if (opiskeluoikeus.isInstanceOf[YlioppilastutkinnonOpiskeluoikeus]) {
      HttpStatus.ok
    } else if (opiskeluoikeus.tila.opiskeluoikeusjaksot.last.tila.koodiarvo != "valmistunut" || valmiitaOppimääriäLöytyy) {
      HttpStatus.ok
    } else if (opiskeluoikeus.tyyppi.koodiarvo == "aikuistenperusopetus") {
      validateAikuistenPerusopetuksenSuoritustenStatus(opiskeluoikeus)
    } else if (opiskeluoikeus.tyyppi.koodiarvo == "lukiokoulutus" && opiskeluoikeus.suoritukset.exists(_.isInstanceOf[LukionOppiaineidenOppimäärienSuoritus2019])) {
      validateLukionOppiaineidenOppimäärienSuoritus2019Status(opiskeluoikeus)
    } else {
      HttpStatus.fold(opiskeluoikeus.suoritukset.map(validateSuoritusVahvistettu))
    }
  }

  private def validateAikuistenPerusopetuksenSuoritustenStatus(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus) = {
    val muutKuinAlkuvaihe = opiskeluoikeus.suoritukset.filterNot(_.tyyppi.koodiarvo == "aikuistenperusopetuksenoppimaaranalkuvaihe")
    if (muutKuinAlkuvaihe.isEmpty) {
      KoskiErrorCategory.badRequest.validation.tila.suoritusPuuttuu("Opiskeluoikeutta aikuistenperusopetus ei voi merkitä valmiiksi kun siitä puuttuu suoritus aikuistenperusopetuksenoppimaara tai perusopetuksenoppiaineenoppimaara")
    } else {
      HttpStatus.fold(muutKuinAlkuvaihe.map(validateSuoritusVahvistettu))
    }
  }

  private def validateLukionOppiaineidenOppimäärienSuoritus2019Status(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus) = {
    val osasuoritukset = opiskeluoikeus.suoritukset.collect {
      case l: LukionOppiaineidenOppimäärienSuoritus2019 => l.osasuoritusLista
    }.flatten

    if (!osasuoritukset.exists(!_.arviointiPuuttuu)) {
      KoskiErrorCategory.badRequest.validation.tila.osasuoritusPuuttuu("Lukion oppiaineiden oppimäärien suorituksen 2019 sisältävää opiskeluoikeutta ei voi merkitä valmiiksi ilman arvioitua oppiaineen osasuoritusta")
    } else {
      HttpStatus.ok
    }
  }

  private def validateSuoritusVahvistettu(suoritus: PäätasonSuoritus): HttpStatus = if (suoritus.kesken) {
    KoskiErrorCategory.badRequest.validation.tila.vahvistusPuuttuu("Suoritukselta " + suorituksenTunniste(suoritus) + " puuttuu vahvistus, vaikka opiskeluoikeus on tilassa Valmistunut")
  } else {
    HttpStatus.ok
  }

  private def suorituksenTunniste(suoritus: Suoritus): KoodiViite = {
    suoritus.koulutusmoduuli.tunniste
  }

  private def validatePäättötodistuksenSanallinenArviointi(oppimäärä: NuortenPerusopetuksenOppimääränSuoritus) = {
    def erikoistapaus(s: Suoritus) = {
      val opintoOhjaus = s.koulutusmoduuli.tunniste.koodiarvo == "OP"
      val kieliaineArvosanallaS = s.koulutusmoduuli.isInstanceOf[NuortenPerusopetuksenVierasTaiToinenKotimainenKieli] && s.viimeisinArvosana.contains("S")
      val paikallinenLaajuusAlle2TaiArvosanaS = s.koulutusmoduuli.isInstanceOf[NuortenPerusopetuksenPaikallinenOppiaine] && (s.koulutusmoduuli.getLaajuus.exists(_.arvo < 2) || s.viimeisinArvosana.contains("S"))
      opintoOhjaus || kieliaineArvosanallaS || paikallinenLaajuusAlle2TaiArvosanaS
    }

    HttpStatus.fold(oppimäärä.osasuoritusLista
      .filterNot(erikoistapaus)
      .collect(validateSanallinenArviointi)
    )
  }

  private def validateSanallinenArviointi: PartialFunction[Suoritus, HttpStatus] = {
    case o: NuortenPerusopetuksenOppiaineenSuoritus =>
      val arvioituSanallisesti = o.viimeisinArvosana.exists(SanallinenPerusopetuksenOppiaineenArviointi.valinnaisilleSallitutArvosanat.contains)
      val eiArvioituSanallisesti = o.viimeisinArvosana.isDefined && !arvioituSanallisesti
      if (arvioituSanallisesti && !o.yksilöllistettyOppimäärä && !o.rajattuOppimäärä && (o.koulutusmoduuli.pakollinen || o.koulutusmoduuli.laajuus.exists(_.arvo >= 2))) {
        val väliaikainenValidaationLöystyttämienPoistettavaSyksyllä2020 = o.viimeisinArvosana.contains("S") && !o.koulutusmoduuli.pakollinen
        if (väliaikainenValidaationLöystyttämienPoistettavaSyksyllä2020) {
          HttpStatus.ok
        } else {
          KoskiErrorCategory.badRequest.validation.arviointi.sallittuVainValinnaiselle(s"Arviointi ${o.viimeisinArviointi.map(_.arvosana.koodiarvo).mkString} on sallittu vain jos oppimäärä on rajattu (yksilöllistetty) tai valinnaisille oppiaineille joiden laajuus on alle kaksi vuosiviikkotuntia")
        }
      } else if (eiArvioituSanallisesti && !o.yksilöllistettyOppimäärä && !o.rajattuOppimäärä && !o.koulutusmoduuli.pakollinen && o.koulutusmoduuli.laajuus.exists(_.arvo < 2)) {
        KoskiErrorCategory.badRequest.validation.arviointi.eiSallittuSuppealleValinnaiselle("Vain arvioinnit 'S' ja 'O' on sallittu valinnaiselle valtakunnalliselle oppiaineelle, jonka laajuus on alle kaksi vuosiviikkotuntia (" + suorituksenTunniste(o) + ")")
      } else {
        HttpStatus.ok
      }
  }

  private def validateSuoritustenLuokkaAsteet(suoritus: Suoritus, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = HttpStatus.fold(
    suoritus match {
      case s: NuortenPerusopetuksenOppiaineenOppimääränSuoritus =>
        if (s.luokkaAste.isDefined && s.suoritustapa.koodiarvo == "koulutus") {
          KoskiErrorCategory.badRequest.validation.tila.nuortenPerusopetuksenLuokkaAsteIlmanErityistäTutkintoa("""Luokka-aste voi olla valittuna vain nuorten perusopetuksen suorituksille, jos suoritustavaksi on valittu erityinen tutkinto""")
        } else {
          HttpStatus.ok
        }
      case _ => HttpStatus.ok
    },
    opiskeluoikeus match {
      case oo: PerusopetuksenOpiskeluoikeus =>
        val nuortenPerusopetuksenErityinenTutkintoSuoritukset = oo.suoritukset.collect({ case s: NuortenPerusopetuksenOppiaineenOppimääränSuoritus => s }).filter(_.suoritustapa.koodiarvo == "erityinentutkinto")
        val groupedNuortenPerusopetuksenErityinenTutkintoSuoritukset = nuortenPerusopetuksenErityinenTutkintoSuoritukset.groupBy(_.koulutusmoduuli.tunniste.koodiarvo)
        val duplikaatitLuokkaAsteet = groupedNuortenPerusopetuksenErityinenTutkintoSuoritukset.flatMap({
          case (avain, suoritukset) if suoritukset.map(_.luokkaAste).distinct.size != suoritukset.size => List(avain)
          case _ => List.empty
        })
        if (duplikaatitLuokkaAsteet.nonEmpty) {
          KoskiErrorCategory.badRequest.validation.tila.nuortenPerusopetuksenLuokkaAsteSamaUseammassaSuorituksessa("""Samaa luokka-astetta ei voi olla useammalla nuorten perusopetuksen erityisen tutkinnon suorituksella.""")
        } else {
          HttpStatus.ok
        }
      case _ => HttpStatus.ok
    }
  )

  private def validateOppiaineet(suoritus: Suoritus) = suoritus match {
    case _: NuortenPerusopetuksenOppiaineenOppimääränSuoritus | _: AikuistenPerusopetuksenOppiaineenOppimääränSuoritus | _: LukionOppiaineenOppimääränSuoritus2015 =>
      if (suoritus.koulutusmoduuli.tunniste.koodiarvo == "XX" && suoritus.valmis) {
        KoskiErrorCategory.badRequest.validation.tila.tyhjänOppiaineenVahvistus(""""Ei tiedossa"-oppiainetta ei voi merkitä valmiiksi""")
      } else HttpStatus.ok
    case s: PerusopetuksenVuosiluokanSuoritus if s.koulutusmoduuli.luokkaAste == "9" && s.valmis && !s.jääLuokalle && s.osasuoritusLista.nonEmpty =>
      KoskiErrorCategory.badRequest.validation.tila.oppiaineitaEiSallita("9.vuosiluokan suoritukseen ei voi syöttää oppiaineita, kun sillä on vahvistus, eikä oppilas jää luokalle")
    case s: NuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa if s.luokkaAsteVaaditaan && s.luokkaAste.isEmpty =>
      KoskiErrorCategory.badRequest.validation.rakenne.luokkaAstePuuttuu("Luokka-aste vaaditaan kun viimeisin arviointi on muuta kuin 'O'")
    case _ =>
      HttpStatus.ok
  }

  private def validatePäiväkodinEsiopetus(suoritus: Suoritus, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus) = suoritus match {
    case e: EsiopetuksenSuoritus if !päiväkodissaJärjestettyEsiopetuksenSuoritus(e) && organisaatioRepository.getOrganisaatioHierarkia(e.toimipiste.oid).exists(_.varhaiskasvatusToimipaikka) =>
      KoskiErrorCategory.badRequest.validation.koodisto.vääräKoulutuksenTunniste(s"Varhaiskasvatustoimipisteeseen voi tallentaa vain päiväkodin esiopetusta (koulutus 001102)")
    case _ => HttpStatus.ok
  }

  private def päiväkodissaJärjestettyEsiopetuksenSuoritus(suoritus: EsiopetuksenSuoritus): Boolean =
    suoritus.koulutusmoduuli.tunniste.koodiarvo == päiväkodinEsiopetuksenTunniste

  private def peruskoulussaJärjestettyEsiopetuksenSuoritus(suoritus: EsiopetuksenSuoritus): Boolean =
    suoritus.koulutusmoduuli.tunniste.koodiarvo == peruskoulunEsiopetuksenTunniste

  private def päätasonSuoritusTyypitEnabled(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    val disabled = config.getStringList("features.disabledPäätasonSuoritusTyypit")
    val päätasonSuoritusTyypit = opiskeluoikeus.suoritukset.map(_.tyyppi.koodiarvo)
    päätasonSuoritusTyypit.find(disabled.contains(_)) match {
      case Some(tyyppi) => KoskiErrorCategory.notImplemented(s"Päätason suorituksen tyyppi $tyyppi ei ole käytössä tässä ympäristössä")
      case _ => HttpStatus.ok
    }
  }

  private def päätasonSuoritusLuokatEnabled(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    val disabled = config.getStringList("features.disabledPäätasonSuoritusLuokat")
    val päätasonSuoritusLuokat = opiskeluoikeus.suoritukset.map(_.getClass.getSimpleName)
    päätasonSuoritusLuokat.find(disabled.contains(_)) match {
      case Some(luokka) => KoskiErrorCategory.notImplemented(s"Päätason suorituksen luokka $luokka ei ole käytössä tässä ympäristössä")
      case _ => HttpStatus.ok
    }
  }

  private def osasuoritusTyypitEnabled(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    val disabled = config.getStringList("features.disabledOsasuoritusTyypit")
    val osasuoritusTyypit = opiskeluoikeus.suoritukset.flatMap(_.rekursiivisetOsasuoritukset).map(_.tyyppi.koodiarvo)
    osasuoritusTyypit.find(disabled.contains(_)) match {
      case Some(tyyppi) => KoskiErrorCategory.notImplemented(s"Osasuorituksen tyyppi $tyyppi ei ole käytössä tässä ympäristössä")
      case _ => HttpStatus.ok
    }
  }

  private def validateYhteisetTutkinnonOsat(suoritus: Suoritus, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    def validateEiSamojaKoodeja(s: AmmatillisenTutkinnonSuoritus): HttpStatus = {
      if (suoritus.valmis) {
        val yhteistenOsasuoritustenKoodit = s.osasuoritusLista.filter(o => o.arvioitu && AmmatillisenTutkinnonOsa.yhteisetTutkinnonOsat.contains(o.koulutusmoduuli.tunniste)).map(_.koulutusmoduuli.tunniste.koodiarvo)
        HttpStatus.validate(
          yhteistenOsasuoritustenKoodit.distinct.size == yhteistenOsasuoritustenKoodit.size
        )(KoskiErrorCategory.badRequest.validation.rakenne.duplikaattiOsasuoritus(
          s"Suorituksella ${suorituksenTunniste(suoritus)} on useampi yhteinen osasuoritus samalla koodilla"))
      } else HttpStatus.ok
    }

    val yhteistenOsienLaajuudenSumma = 35

    def validateYhteislaajuus(s: AmmatillisenTutkinnonSuoritus): HttpStatus = {
      if (s.valmis && s.suoritustapa.koodiarvo == "reformi") {
        val yhteislaajuus = s.osasuoritusLista.filter(o => o.arvioitu && (AmmatillisenTutkinnonOsa.yhteisetTutkinnonOsat).contains(o.koulutusmoduuli.tunniste))
          .map(_.koulutusmoduuli).map(_.getLaajuus.map(_.arvo).getOrElse(0.0))
          .sum
        HttpStatus.validate(
          yhteislaajuus.round >= yhteistenOsienLaajuudenSumma
        )(KoskiErrorCategory.badRequest.validation.laajuudet.osasuoritustenLaajuuksienSumma(
          s"Valmiiksi merkityn suorituksen ${suorituksenTunniste(suoritus)} yhteisten tutkinnon osien laajuuden tulee olla vähintään ${yhteistenOsienLaajuudenSumma}"))
      } else HttpStatus.ok
    }

    def validateOnOsaAlueita(s: AmmatillisenTutkinnonSuoritus): HttpStatus = {
      if (s.valmis && s.suoritustapa.koodiarvo == "reformi") {
        val yhteisetOsaSuoritukset = s.osasuoritusLista.filter(o => o.arvioitu && AmmatillisenTutkinnonOsa.yhteisetTutkinnonOsat.contains(o.koulutusmoduuli.tunniste))

        val osasuorituksettomatYhteisetSuoritukset = yhteisetOsaSuoritukset.filter(yht => {
          yht.osasuoritukset.getOrElse(List()).isEmpty
        })

        val osasuorituksettomienTunnisteet = osasuorituksettomatYhteisetSuoritukset.map(osa => {
          s"'${osa.koulutusmoduuli.tunniste.getNimi.map(_.get("Finnish")).getOrElse(osa.koulutusmoduuli.tunniste.koodiarvo)}'"
        })

        HttpStatus.validate(
          osasuorituksettomatYhteisetSuoritukset.isEmpty
        )(KoskiErrorCategory.badRequest.validation.rakenne.yhteiselläOsuudellaEiOsasuorituksia(
          s"Arvioidulla yhteisellä tutkinnon osalla ${osasuorituksettomienTunnisteet.mkString(", ")} ei ole osa-alueita"))
      } else HttpStatus.ok
    }

    def validateYhteistenOsienLaajuus(s: AmmatillisenTutkinnonSuoritus): HttpStatus = {
      val yhteisetOsaSuoritukset = s.osasuoritusLista.filter(o => o.arvioitu && AmmatillisenTutkinnonOsa.yhteisetTutkinnonOsat.contains(o.koulutusmoduuli.tunniste))

      val mismatchingLaajuudet = yhteisetOsaSuoritukset.filter(yht => {
        val yläsuorituksenLaajuus = yht.koulutusmoduuli.getLaajuus.map(_.arvo).getOrElse(0.0)
        val alasuoritustenLaajuus = yht.osasuoritusLista.map(_.koulutusmoduuli).map(_.getLaajuus.getOrElse(LaajuusOsaamispisteissä(0.0)).arvo).sum
        yläsuorituksenLaajuus != alasuoritustenLaajuus && alasuoritustenLaajuus != 0.0
      })

      val yhteistenKooditJoillaVääräOsasuoritustenYhteisLaajuus = mismatchingLaajuudet.map(osa => {
        s"'${osa.koulutusmoduuli.tunniste.getNimi.map(_.get("Finnish")).getOrElse(osa.koulutusmoduuli.tunniste.koodiarvo)}'"
      })

      HttpStatus.validate(
        mismatchingLaajuudet.isEmpty
      )(KoskiErrorCategory.badRequest.validation.laajuudet.osasuoritustenLaajuuksienSumma(
        s"Yhteisillä tutkinnon osilla ${yhteistenKooditJoillaVääräOsasuoritustenYhteisLaajuus.mkString(", ")} on eri laajuus kun tutkinnon osien osa-alueiden yhteenlaskettu summa"))
    }

    def validateYhteistenOsienKoodit(s: AmmatillisenTutkinnonSuoritus): HttpStatus = {
      s.suoritustapa.koodiarvo match {
        case "reformi" => {
          HttpStatus.validate(
            !s.osasuoritusLista.exists(o => AmmatillisenTutkinnonOsa.opsMuotoisenTutkinnonYhteisetOsat.contains(o.koulutusmoduuli.tunniste))
          )(KoskiErrorCategory.badRequest.validation.rakenne.vääränKoodinYhteinenOsasuoritus(
            s"Suorituksella ${suorituksenTunniste(suoritus)} on Ops-muotoiselle tutkinnolle tarkoitettu yhteinen osasuoritus"))
        }
        case "ops" => {
          HttpStatus.validate(
            !s.osasuoritusLista.exists(o => AmmatillisenTutkinnonOsa.reformiMuotoisenTutkinnonYhteisetOsat.contains(o.koulutusmoduuli.tunniste))
          )(KoskiErrorCategory.badRequest.validation.rakenne.vääränKoodinYhteinenOsasuoritus(
            s"Suorituksella ${suorituksenTunniste(suoritus)} on reformi-muotoiselle tutkinnolle tarkoitettu yhteinen osasuoritus"))
        }
        case _ => HttpStatus.ok
      }
    }

    def yhteistenValidaatiot(suoritus: AmmatillisenTutkinnonSuoritus): HttpStatus = {
      HttpStatus.fold(List(validateOnOsaAlueita(suoritus),
        validateYhteistenOsienLaajuus(suoritus),
        validateYhteislaajuus(suoritus),
        validateEiSamojaKoodeja(suoritus),
        validateYhteistenOsienKoodit(suoritus)))
    }

    suoritus match {
      case s: AmmatillisenTutkinnonSuoritus if s.koulutusmoduuli.koulutustyyppi.contains(ammatillinenPerustutkinto) =>
        // Jätetään validaation tulokset huomioimatta, jos kyseessä kuoriopiskeluoikeus eli linkitetty opiskeluoikeus.
        // Tämä tutkiminen tehdään vasta validaatioiden jälkeen, koska linkitysten tutkiminen aiheuttaa ylimääräisiä
        // tietokantakyselyitä.
        val validaationTulos = yhteistenValidaatiot(s)
        if (validaationTulos.isOk) {
          validaationTulos
        } else if (koskiOpiskeluoikeudet.isKuoriOpiskeluoikeus(opiskeluoikeus)) {
          HttpStatus.ok
        } else {
          validaationTulos
        }
      case _ => HttpStatus.ok
    }
  }

  private def validateOsaamisenHankkimistavat(suoritus: Suoritus): HttpStatus = suoritus match {
    case a: AmmatillisenTutkinnonSuoritus =>
      HttpStatus.validate(
        a.osaamisenHankkimistavat.toList.flatten
          .map(_.osaamisenHankkimistapa)
          .collect { case o: OsaamisenHankkimistapaIlmanLisätietoja => o }
          .forall(hankkimistapa => List("koulutussopimus", "oppilaitosmuotoinenkoulutus").contains(hankkimistapa.tunniste.koodiarvo))
      )(KoskiErrorCategory.badRequest.validation.rakenne.deprekoituOsaamisenHankkimistapa())
    case _ => HttpStatus.ok
  }

 private def validateNuortenPerusopetuksenPakollistenOppiaineidenLaajuus(suoritus: Suoritus, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus)  = {
   val kotiopetusVoimassaVahvistusPäivänä = opiskeluoikeus.lisätiedot.exists {
     case lisätiedot: Kotiopetuksellinen =>
       val kaikkiKotiopetusJaksot =  lisätiedot.kotiopetus.toList ::: lisätiedot.kotiopetusjaksot.toList.flatten
       suoritus.vahvistus.map(_.päivä)
         .exists(vahvistusPäivä => kaikkiKotiopetusJaksot.exists(_.contains(vahvistusPäivä)))
     case _ => false
   }

   val suoritusTapanaErityinenTutkinto = suoritus match {
     case s: PerusopetuksenVuosiluokanSuoritus => s.suoritustapa.exists(_.koodiarvo == "erityinentutkinto")
     case s: NuortenPerusopetuksenOppimääränSuoritus => s.suoritustapa.koodiarvo == "erityinentutkinto"
     case _ => false
   }

   val vahvistettuPäivänJälkeenJolloinLaajuusVaaditaan = suoritus.vahvistus.exists(v => !v.päivä.isBefore(LocalDate.of(2020, 8, 1)))

   suoritus match {
     case _: NuortenPerusopetuksenOppimääränSuoritus | _: PerusopetuksenVuosiluokanSuoritus
       if vahvistettuPäivänJälkeenJolloinLaajuusVaaditaan && !(kotiopetusVoimassaVahvistusPäivänä || suoritusTapanaErityinenTutkinto) =>
       HttpStatus.fold(
         suoritus.osasuoritusLista.collect {
           case o: NuortenPerusopetuksenOppiaineenSuoritus
             if o.koulutusmoduuli.pakollinen && o.suoritustapa.forall(_.koodiarvo != "erityinentutkinto") =>
             HttpStatus.validate(o.koulutusmoduuli.laajuusArvo(0.0) > 0) { KoskiErrorCategory.badRequest.validation.laajuudet.oppiaineenLaajuusPuuttuu(s"Oppiaineen ${suorituksenTunniste(o)} laajuus puuttuu") }
         })
     case _ => HttpStatus.ok
   }
 }


  private def validateÄidinkielenOmainenKieli(suoritus: Suoritus) = {
    def validateSuomiTaiRuotsi(koodiarvo: String) = HttpStatus.validate(List("SV", "FI").contains(koodiarvo)) {
      KoskiErrorCategory.badRequest.validation.rakenne.deprekoituKielikoodi("Äidinkielen omaisen oppiaineen kieli tulee olla suomi tai ruotsi")
    }

    suoritus.koulutusmoduuli match {
      case k: NuortenPerusopetuksenVierasTaiToinenKotimainenKieli if k.tunniste.koodiarvo == "AOM" => validateSuomiTaiRuotsi(k.kieli.koodiarvo)
      case k: VierasTaiToinenKotimainenKieli2019 if k.tunniste.koodiarvo == "AOM" => validateSuomiTaiRuotsi(k.kieli.koodiarvo)
      case k: VierasTaiToinenKotimainenKieli2015 if k.tunniste.koodiarvo == "AOM" => validateSuomiTaiRuotsi(k.kieli.koodiarvo)
      case _ => HttpStatus.ok
    }
  }

  private def validateSuorituskieli(suoritus: Suoritus) = {
    def validateSuomiTaiRuotsi(koodiarvo: String) = HttpStatus.validate(List("SV", "FI").contains(koodiarvo)) {
      KoskiErrorCategory.badRequest.validation.rakenne.virheellinenSuorituskieli("Suorituskielen tulee olla suomi tai ruotsi")
    }
    suoritus match {
      case k: PerusopetukseenValmistavanOpetuksenSuoritus => validateSuomiTaiRuotsi(k.suorituskieli.koodiarvo)
      case _ => HttpStatus.ok
    }
  }

  private def validateMitätöinti(oo: KoskeenTallennettavaOpiskeluoikeus): Either[HttpStatus, KoskeenTallennettavaOpiskeluoikeus] = {
    if (oo.mitätöintiPäivä.exists(_.isAfter(LocalDate.now()))) {
      Left(KoskiErrorCategory.badRequest.validation.tila.mitätöintiTulevaisuudessa())
    } else {
      Right(oo)
    }
  }
}
