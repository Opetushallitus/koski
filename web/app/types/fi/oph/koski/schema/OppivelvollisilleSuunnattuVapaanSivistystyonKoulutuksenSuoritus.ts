import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus } from './OppivelvollisilleSuunnattuVapaanSivistystyonKoulutus'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { OppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus } from './OppivelvollisilleSuunnatunVapaanSivistystyonOsasuoritus'
import { HenkilövahvistusValinnaisellaPaikkakunnalla } from './HenkilovahvistusValinnaisellaPaikkakunnalla'

/**
 * Laajennetun oppivelvollisuuden suoritus
 *
 * @see `fi.oph.koski.schema.OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus`
 */
export type OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus = {
  $class: 'fi.oph.koski.schema.OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus'
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'vstoppivelvollisillesuunnattukoulutus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<OppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}

export const OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus =
  (o: {
    tyyppi?: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstoppivelvollisillesuunnattukoulutus'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    suorituskieli: Koodistokoodiviite<'kieli', string>
    todistuksellaNäkyvätLisätiedot?: LocalizedString
    koulutusmoduuli?: OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus
    toimipiste: OrganisaatioWithOid
    osasuoritukset?: Array<OppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus>
    vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
  }): OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'vstoppivelvollisillesuunnattukoulutus',
      koodistoUri: 'suorituksentyyppi'
    }),
    koulutusmoduuli: OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus({
      tunniste: Koodistokoodiviite({
        koodiarvo: '999909',
        koodistoUri: 'koulutus'
      })
    }),
    $class:
      'fi.oph.koski.schema.OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus',
    ...o
  })

export const isOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus =
  (
    a: any
  ): a is OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus =>
    a?.$class ===
    'OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus'
