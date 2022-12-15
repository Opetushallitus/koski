import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { KorkeakouluopinnotTutkinnonOsa } from './KorkeakouluopinnotTutkinnonOsa'
import { KorkeakouluopintojenSuoritus } from './KorkeakouluopintojenSuoritus'

/**
 * Korkeakouluopintoja
 *
 * @see `fi.oph.koski.schema.OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus`
 */
export type OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus = {
  $class: 'fi.oph.koski.schema.OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ammatillisentutkinnonosa'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: KorkeakouluopinnotTutkinnonOsa
  tutkinnonOsanRyhmä?: Koodistokoodiviite<'ammatillisentutkinnonosanryhma', '1'>
  osasuoritukset?: Array<KorkeakouluopintojenSuoritus>
}

export const OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus = (
  o: {
    tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'ammatillisentutkinnonosa'>
    tila?: Koodistokoodiviite<'suorituksentila', string>
    koulutusmoduuli?: KorkeakouluopinnotTutkinnonOsa
    tutkinnonOsanRyhmä?: Koodistokoodiviite<
      'ammatillisentutkinnonosanryhma',
      '1'
    >
    osasuoritukset?: Array<KorkeakouluopintojenSuoritus>
  } = {}
): OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ammatillisentutkinnonosa',
    koodistoUri: 'suorituksentyyppi'
  }),
  koulutusmoduuli: KorkeakouluopinnotTutkinnonOsa({
    tunniste: Koodistokoodiviite({
      koodiarvo: '2',
      koodistoUri: 'tutkinnonosatvalinnanmahdollisuus'
    })
  }),
  $class:
    'fi.oph.koski.schema.OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus',
  ...o
})

export const isOsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus = (
  a: any
): a is OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus =>
  a?.$class === 'OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus'
