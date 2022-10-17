package fi.oph.koski.kela

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.Logging
import fi.oph.koski.schema
import fi.oph.koski.schema.KoskiSchema
import fi.oph.scalaschema.{ExtractionContext, SchemaValidatingExtractor}

// TODO: Poista KelaOppijaConverter sitten kun sitä ei enää käytetä.
//  Käytössä tällä hetkellä vain Kelan käyttöliittymässä.
//  KelaOppijaConverterin käytön sijaan voit serialisoida KelaOppijan suoraan oppijan json-datasta.
object KelaOppijaConverter extends Logging {

  def convertOppijaToKelaOppija(oppija: schema.Oppija): Either[HttpStatus, KelaOppija] = {
    convertHenkilo(oppija.henkilö).flatMap(henkilo => {
      val opiskeluoikeudet = oppija.opiskeluoikeudet.flatMap(kelaaKiinnostavatOpinnot).map(convertOpiskeluoikeus).toList
      if (opiskeluoikeudet.isEmpty) {
        Left(KoskiErrorCategory.notFound())
      } else {
        Right(KelaOppija(henkilo, opiskeluoikeudet))
      }
    })
  }

  private def convertHenkilo(oppija: schema.Henkilö): Either[HttpStatus, Henkilo] = oppija match {
    case henkilotiedot: schema.Henkilötiedot =>
      Right(Henkilo(
        oid = henkilotiedot.oid,
        hetu = henkilotiedot.hetu,
        syntymäaika = henkilotiedot match {
          case t: schema.TäydellisetHenkilötiedot => t.syntymäaika
          case _ => None
        },
        etunimet = henkilotiedot.etunimet,
        sukunimi = henkilotiedot.sukunimi,
        kutsumanimi = henkilotiedot.kutsumanimi
      ))
    case x => {
      logger.error("KelaOppijaConverter: Unreachable match arm, expected Henkilötiedot, got " + x.toString)
      Left(KoskiErrorCategory.internalError())
    }
  }

  private def kelaaKiinnostavatOpinnot(opiskeluoikeus: schema.Opiskeluoikeus) = opiskeluoikeus match {
    case _: schema.AmmatillinenOpiskeluoikeus |
         _: schema.YlioppilastutkinnonOpiskeluoikeus |
         _: schema.LukionOpiskeluoikeus |
         _: schema.LukioonValmistavanKoulutuksenOpiskeluoikeus |
         _: schema.DIAOpiskeluoikeus |
         _: schema.IBOpiskeluoikeus |
         _: schema.InternationalSchoolOpiskeluoikeus |
         // TODO: TOR-1685 Eurooppalainen koulu
         _: schema.PerusopetuksenOpiskeluoikeus |
         _: schema.PerusopetukseenValmistavanOpetuksenOpiskeluoikeus |
         _: schema.PerusopetuksenLisäopetuksenOpiskeluoikeus |
         _: schema.AikuistenPerusopetuksenOpiskeluoikeus |
         _: schema.TutkintokoulutukseenValmentavanOpiskeluoikeus => Some(opiskeluoikeus)
    // Vapaatavoitteisen koulutuksen kohdalla täytyisi ottaa huomioon
    // suostumuksen peruminen jos Kelalle halutaan antaa tieto ko. suorituksesta.
    case o: schema.VapaanSivistystyönOpiskeluoikeus => {
      val suoritukset = o.suoritukset.collect {
        case s: schema.OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus => s
        case s: schema.OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus => s
        case s: schema.VapaanSivistystyönLukutaitokoulutuksenSuoritus => s
      }
      if (suoritukset.isEmpty) None else Some(opiskeluoikeus.withSuoritukset(suoritukset))
    }
    case _ => None
  }

  private def convertOpiskeluoikeus(opiskeluoikeus: schema.Opiskeluoikeus): KelaOpiskeluoikeus = {
    implicit val context: ExtractionContext = KoskiSchema.lenientDeserializationWithIgnoringNonValidatingListItemsWithoutValidation

    SchemaValidatingExtractor
      .extract[KelaOpiskeluoikeus](JsonSerializer.serializeWithRoot(opiskeluoikeus)).right.get match {
      case oo: KelaDIAOpiskeluoikeus => oo.withSuorituksetVastaavuusKopioitu.withOrganisaatiohistoria.withEmptyArvosana
      case oo: KelaOpiskeluoikeus => oo.withOrganisaatiohistoria.withEmptyArvosana
    }
  }
}
