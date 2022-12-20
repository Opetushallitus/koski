import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { EBTutkinto } from './EBTutkinto'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { EBTutkinnonOsasuoritus } from './EBTutkinnonOsasuoritus'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'

/**
 * EBTutkinnonSuoritus
 *
 * @see `fi.oph.koski.schema.EBTutkinnonSuoritus`
 */
export type EBTutkinnonSuoritus = {
  $class: 'fi.oph.koski.schema.EBTutkinnonSuoritus'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ebtutkinto'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  luokka?: string
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  yleisarvosana?: number
  koulutusmoduuli: EBTutkinto
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<EBTutkinnonOsasuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export const EBTutkinnonSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'ebtutkinto'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  luokka?: string
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  yleisarvosana?: number
  koulutusmoduuli: EBTutkinto
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<EBTutkinnonOsasuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}): EBTutkinnonSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ebtutkinto',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.EBTutkinnonSuoritus',
  ...o
})

export const isEBTutkinnonSuoritus = (a: any): a is EBTutkinnonSuoritus =>
  a?.$class === 'fi.oph.koski.schema.EBTutkinnonSuoritus'
