import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { NurseryLuokkaAste } from './NurseryLuokkaAste'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'

/**
 * NurseryVuosiluokanSuoritus
 *
 * @see `fi.oph.koski.schema.NurseryVuosiluokanSuoritus`
 */
export type NurseryVuosiluokanSuoritus = {
  $class: 'fi.oph.koski.schema.NurseryVuosiluokanSuoritus'
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkivuosiluokkanursery'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  luokka?: string
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  jääLuokalle: boolean
  koulutusmoduuli: NurseryLuokkaAste
  toimipiste: OrganisaatioWithOid
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export const NurseryVuosiluokanSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkivuosiluokkanursery'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  luokka?: string
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  jääLuokalle?: boolean
  koulutusmoduuli: NurseryLuokkaAste
  toimipiste: OrganisaatioWithOid
  vahvistus?: HenkilövahvistusPaikkakunnalla
}): NurseryVuosiluokanSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'europeanschoolofhelsinkivuosiluokkanursery',
    koodistoUri: 'suorituksentyyppi'
  }),
  jääLuokalle: false,
  $class: 'fi.oph.koski.schema.NurseryVuosiluokanSuoritus',
  ...o
})

NurseryVuosiluokanSuoritus.className =
  'fi.oph.koski.schema.NurseryVuosiluokanSuoritus' as const

export const isNurseryVuosiluokanSuoritus = (
  a: any
): a is NurseryVuosiluokanSuoritus =>
  a?.$class === 'fi.oph.koski.schema.NurseryVuosiluokanSuoritus'
