import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { PYPLuokkaAste } from './PYPLuokkaAste'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { PYPOppiaineenSuoritus } from './PYPOppiaineenSuoritus'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'

/**
 * PYPVuosiluokanSuoritus
 *
 * @see `fi.oph.koski.schema.PYPVuosiluokanSuoritus`
 */
export type PYPVuosiluokanSuoritus = {
  $class: 'fi.oph.koski.schema.PYPVuosiluokanSuoritus'
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'internationalschoolpypvuosiluokka'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli: Koodistokoodiviite<'kieli', string>
  luokka?: string
  koulutusmoduuli: PYPLuokkaAste
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<PYPOppiaineenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export const PYPVuosiluokanSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'internationalschoolpypvuosiluokka'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli: Koodistokoodiviite<'kieli', string>
  luokka?: string
  koulutusmoduuli: PYPLuokkaAste
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<PYPOppiaineenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}): PYPVuosiluokanSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'internationalschoolpypvuosiluokka',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.PYPVuosiluokanSuoritus',
  ...o
})

export const isPYPVuosiluokanSuoritus = (a: any): a is PYPVuosiluokanSuoritus =>
  a?.$class === 'fi.oph.koski.schema.PYPVuosiluokanSuoritus'
