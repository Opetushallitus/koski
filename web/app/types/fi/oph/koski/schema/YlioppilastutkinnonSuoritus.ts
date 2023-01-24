import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Ylioppilastutkinto } from './Ylioppilastutkinto'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { YlioppilastutkinnonKokeenSuoritus } from './YlioppilastutkinnonKokeenSuoritus'
import { Organisaatiovahvistus } from './Organisaatiovahvistus'

/**
 * YlioppilastutkinnonSuoritus
 *
 * @see `fi.oph.koski.schema.YlioppilastutkinnonSuoritus`
 */
export type YlioppilastutkinnonSuoritus = {
  $class: 'fi.oph.koski.schema.YlioppilastutkinnonSuoritus'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ylioppilastutkinto'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulusivistyskieli?: Array<Koodistokoodiviite<'kieli', 'FI' | 'SV'>>
  pakollisetKokeetSuoritettu: boolean
  koulutusmoduuli: Ylioppilastutkinto
  toimipiste?: OrganisaatioWithOid
  osasuoritukset?: Array<YlioppilastutkinnonKokeenSuoritus>
  vahvistus?: Organisaatiovahvistus
}

export const YlioppilastutkinnonSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'ylioppilastutkinto'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulusivistyskieli?: Array<Koodistokoodiviite<'kieli', 'FI' | 'SV'>>
  pakollisetKokeetSuoritettu: boolean
  koulutusmoduuli?: Ylioppilastutkinto
  toimipiste?: OrganisaatioWithOid
  osasuoritukset?: Array<YlioppilastutkinnonKokeenSuoritus>
  vahvistus?: Organisaatiovahvistus
}): YlioppilastutkinnonSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ylioppilastutkinto',
    koodistoUri: 'suorituksentyyppi'
  }),
  koulutusmoduuli: Ylioppilastutkinto({
    tunniste: Koodistokoodiviite({
      koodiarvo: '301000',
      koodistoUri: 'koulutus'
    })
  }),
  $class: 'fi.oph.koski.schema.YlioppilastutkinnonSuoritus',
  ...o
})

YlioppilastutkinnonSuoritus.className =
  'fi.oph.koski.schema.YlioppilastutkinnonSuoritus' as const

export const isYlioppilastutkinnonSuoritus = (
  a: any
): a is YlioppilastutkinnonSuoritus =>
  a?.$class === 'fi.oph.koski.schema.YlioppilastutkinnonSuoritus'
