import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { SecondaryLowerLuokkaAste } from './SecondaryLowerLuokkaAste'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { SecondaryLowerOppiaineenSuoritus } from './SecondaryLowerOppiaineenSuoritus'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'

/**
 * SecondaryLowerVuosiluokanSuoritus
 *
 * @see `fi.oph.koski.schema.SecondaryLowerVuosiluokanSuoritus`
 */
export type SecondaryLowerVuosiluokanSuoritus = {
  $class: 'fi.oph.koski.schema.SecondaryLowerVuosiluokanSuoritus'
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkivuosiluokkasecondarylower'
  >
  luokka?: string
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  jääLuokalle: boolean
  koulutusmoduuli: SecondaryLowerLuokkaAste
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<SecondaryLowerOppiaineenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
}

export const SecondaryLowerVuosiluokanSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkivuosiluokkasecondarylower'
  >
  luokka?: string
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  jääLuokalle?: boolean
  koulutusmoduuli: SecondaryLowerLuokkaAste
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<SecondaryLowerOppiaineenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
}): SecondaryLowerVuosiluokanSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'europeanschoolofhelsinkivuosiluokkasecondarylower',
    koodistoUri: 'suorituksentyyppi'
  }),
  jääLuokalle: false,
  $class: 'fi.oph.koski.schema.SecondaryLowerVuosiluokanSuoritus',
  ...o
})

SecondaryLowerVuosiluokanSuoritus.className =
  'fi.oph.koski.schema.SecondaryLowerVuosiluokanSuoritus' as const

export const isSecondaryLowerVuosiluokanSuoritus = (
  a: any
): a is SecondaryLowerVuosiluokanSuoritus =>
  a?.$class === 'fi.oph.koski.schema.SecondaryLowerVuosiluokanSuoritus'
