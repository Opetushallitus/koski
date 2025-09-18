import { AmmatillinenArviointi } from './AmmatillinenArviointi'
import { Näyttö } from './Naytto'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { AmmatillisenTutkinnonOsanLisätieto } from './AmmatillisenTutkinnonOsanLisatieto'
import { YhteinenTutkinnonOsa } from './YhteinenTutkinnonOsa'
import { OsaamisenTunnustaminen } from './OsaamisenTunnustaminen'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { YhteisenTutkinnonOsanOsaAlueenSuoritus } from './YhteisenTutkinnonOsanOsaAlueenSuoritus'
import { AmmatillinenTutkintoKoulutus } from './AmmatillinenTutkintoKoulutus'

/**
 * Ammatilliseen tutkintoon liittyvän yhteisen tutkinnonosan suoritus
 *
 * @see `fi.oph.koski.schema.YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus`
 */
export type YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus =
  {
    $class: 'fi.oph.koski.schema.YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus'
    arviointi?: Array<AmmatillinenArviointi>
    näyttö?: Näyttö
    tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ammatillisentutkinnonosa'>
    tila?: Koodistokoodiviite<'suorituksentila', string>
    alkamispäivä?: string
    suorituskieli?: Koodistokoodiviite<'kieli', string>
    lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
    koulutusmoduuli: YhteinenTutkinnonOsa
    tunnustettu?: OsaamisenTunnustaminen
    toimipiste?: OrganisaatioWithOid
    tutkinnonOsanRyhmä?: Koodistokoodiviite<
      'ammatillisentutkinnonosanryhma',
      '2'
    >
    osasuoritukset?: Array<YhteisenTutkinnonOsanOsaAlueenSuoritus>
    tutkinto: AmmatillinenTutkintoKoulutus
  }

export const YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus =
  (o: {
    arviointi?: Array<AmmatillinenArviointi>
    näyttö?: Näyttö
    tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'ammatillisentutkinnonosa'>
    tila?: Koodistokoodiviite<'suorituksentila', string>
    alkamispäivä?: string
    suorituskieli?: Koodistokoodiviite<'kieli', string>
    lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
    koulutusmoduuli: YhteinenTutkinnonOsa
    tunnustettu?: OsaamisenTunnustaminen
    toimipiste?: OrganisaatioWithOid
    tutkinnonOsanRyhmä?: Koodistokoodiviite<
      'ammatillisentutkinnonosanryhma',
      '2'
    >
    osasuoritukset?: Array<YhteisenTutkinnonOsanOsaAlueenSuoritus>
    tutkinto: AmmatillinenTutkintoKoulutus
  }): YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'ammatillisentutkinnonosa',
      koodistoUri: 'suorituksentyyppi'
    }),
    $class:
      'fi.oph.koski.schema.YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus',
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
