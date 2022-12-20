import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { KorkeakouluopinnotTutkinnonOsa } from './KorkeakouluopinnotTutkinnonOsa'
import { KorkeakouluopintojenSuoritus } from './KorkeakouluopintojenSuoritus'

/**
 * Korkeakouluopintoja
 *
 * @see `fi.oph.koski.schema.AmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus`
 */
export type AmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus = {
  $class: 'fi.oph.koski.schema.AmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ammatillisentutkinnonosa'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: KorkeakouluopinnotTutkinnonOsa
  tutkinnonOsanRyhmä?: Koodistokoodiviite<'ammatillisentutkinnonosanryhma', '1'>
  osasuoritukset?: Array<KorkeakouluopintojenSuoritus>
}

export const AmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus = (
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
): AmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus => ({
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
    'fi.oph.koski.schema.AmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus',
  ...o
})

export const isAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus = (
  a: any
): a is AmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.AmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus'
