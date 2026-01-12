import { AmmatillinenArviointi } from './AmmatillinenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { AmmatillisenTutkinnonOsanLisätieto } from './AmmatillisenTutkinnonOsanLisatieto'
import { YhteinenTutkinnonOsa } from './YhteinenTutkinnonOsa'
import { AmmatillinenTutkintoKoulutus } from './AmmatillinenTutkintoKoulutus'
import { NäyttöAmmatillinenOsittainen } from './NayttoAmmatillinenOsittainen'
import { OsaamisenTunnustaminen } from './OsaamisenTunnustaminen'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { YhteisenTutkinnonOsanOsaAlueenSuoritus } from './YhteisenTutkinnonOsanOsaAlueenSuoritus'

/**
 * Ammatilliseen tutkintoon liittyvän yhteisen tutkinnonosan suoritus
 *
 * @see `fi.oph.koski.schema.YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus`
 */
export type YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus =
  {
    $class: 'fi.oph.koski.schema.YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus'
    arviointi?: Array<AmmatillinenArviointi>
    suorituskieli?: Koodistokoodiviite<'kieli', string>
    lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
    koulutusmoduuli: YhteinenTutkinnonOsa
    tutkinto: AmmatillinenTutkintoKoulutus
    näyttö?: NäyttöAmmatillinenOsittainen
    tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ammatillisentutkinnonosa'>
    tila?: Koodistokoodiviite<'suorituksentila', string>
    alkamispäivä?: string
    tunnustettu?: OsaamisenTunnustaminen
    toimipiste?: OrganisaatioWithOid
    tutkinnonOsanRyhmä?: Koodistokoodiviite<
      'ammatillisentutkinnonosanryhma',
      '2'
    >
    osasuoritukset?: Array<YhteisenTutkinnonOsanOsaAlueenSuoritus>
  }

export const YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus =
  (o: {
    arviointi?: Array<AmmatillinenArviointi>
    suorituskieli?: Koodistokoodiviite<'kieli', string>
    lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
    koulutusmoduuli: YhteinenTutkinnonOsa
    tutkinto: AmmatillinenTutkintoKoulutus
    näyttö?: NäyttöAmmatillinenOsittainen
    tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'ammatillisentutkinnonosa'>
    tila?: Koodistokoodiviite<'suorituksentila', string>
    alkamispäivä?: string
    tunnustettu?: OsaamisenTunnustaminen
    toimipiste?: OrganisaatioWithOid
    tutkinnonOsanRyhmä?: Koodistokoodiviite<
      'ammatillisentutkinnonosanryhma',
      '2'
    >
    osasuoritukset?: Array<YhteisenTutkinnonOsanOsaAlueenSuoritus>
  }): YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus => ({
    $class:
      'fi.oph.koski.schema.YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus',
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'ammatillisentutkinnonosa',
      koodistoUri: 'suorituksentyyppi'
    }),
    ...o
  })

YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus.className =
  'fi.oph.koski.schema.YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus' as const

export const isYhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus =
  (
    a: any
  ): a is YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus =>
    a?.$class ===
    'fi.oph.koski.schema.YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus'
