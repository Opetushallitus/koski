import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { SecondaryUpperLuokkaAste } from './SecondaryUpperLuokkaAste'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { SecondaryUpperOppiaineenSuoritus } from './SecondaryUpperOppiaineenSuoritus'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'

/**
 * Laajennetun oppivelvollisuuden suoritus
 *
 * @see `fi.oph.koski.schema.SecondaryUpperVuosiluokanSuoritus`
 */
export type SecondaryUpperVuosiluokanSuoritus = {
  $class: 'fi.oph.koski.schema.SecondaryUpperVuosiluokanSuoritus'
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkivuosiluokkasecondaryupper'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  luokka?: string
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  jääLuokalle: boolean
  koulutusmoduuli: SecondaryUpperLuokkaAste
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<SecondaryUpperOppiaineenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export const SecondaryUpperVuosiluokanSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkivuosiluokkasecondaryupper'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  luokka?: string
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  jääLuokalle?: boolean
  koulutusmoduuli: SecondaryUpperLuokkaAste
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<SecondaryUpperOppiaineenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}): SecondaryUpperVuosiluokanSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'europeanschoolofhelsinkivuosiluokkasecondaryupper',
    koodistoUri: 'suorituksentyyppi'
  }),
  jääLuokalle: false,
  $class: 'fi.oph.koski.schema.SecondaryUpperVuosiluokanSuoritus',
  ...o
})

export const isSecondaryUpperVuosiluokanSuoritus = (
  a: any
): a is SecondaryUpperVuosiluokanSuoritus =>
  a?.$class === 'SecondaryUpperVuosiluokanSuoritus'
