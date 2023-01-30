import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { MYPLuokkaAste } from './MYPLuokkaAste'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { MYPOppiaineenSuoritus } from './MYPOppiaineenSuoritus'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'

/**
 * MYPVuosiluokanSuoritus
 *
 * @see `fi.oph.koski.schema.MYPVuosiluokanSuoritus`
 */
export type MYPVuosiluokanSuoritus = {
  $class: 'fi.oph.koski.schema.MYPVuosiluokanSuoritus'
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'internationalschoolmypvuosiluokka'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli: Koodistokoodiviite<'kieli', string>
  luokka?: string
  koulutusmoduuli: MYPLuokkaAste
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<MYPOppiaineenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export const MYPVuosiluokanSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'internationalschoolmypvuosiluokka'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli: Koodistokoodiviite<'kieli', string>
  luokka?: string
  koulutusmoduuli: MYPLuokkaAste
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<MYPOppiaineenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}): MYPVuosiluokanSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'internationalschoolmypvuosiluokka',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.MYPVuosiluokanSuoritus',
  ...o
})

MYPVuosiluokanSuoritus.className =
  'fi.oph.koski.schema.MYPVuosiluokanSuoritus' as const

export const isMYPVuosiluokanSuoritus = (a: any): a is MYPVuosiluokanSuoritus =>
  a?.$class === 'fi.oph.koski.schema.MYPVuosiluokanSuoritus'
