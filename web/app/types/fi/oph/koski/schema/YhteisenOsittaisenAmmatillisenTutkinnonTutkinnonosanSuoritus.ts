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
import { HenkilövahvistusValinnaisellaTittelillä } from './HenkilovahvistusValinnaisellaTittelilla'

/**
 * Ammatilliseen tutkintoon liittyvän yhteisen tutkinnonosan suoritus
 *
 * @see `fi.oph.koski.schema.YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus`
 */
export type YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus = {
  $class: 'fi.oph.koski.schema.YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus'
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
  tutkinnonOsanRyhmä?: Koodistokoodiviite<'ammatillisentutkinnonosanryhma', '2'>
  osasuoritukset?: Array<YhteisenTutkinnonOsanOsaAlueenSuoritus>
  tutkinto?: AmmatillinenTutkintoKoulutus
  vahvistus?: HenkilövahvistusValinnaisellaTittelillä
}

export const YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus =
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
    tutkinto?: AmmatillinenTutkintoKoulutus
    vahvistus?: HenkilövahvistusValinnaisellaTittelillä
  }): YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'ammatillisentutkinnonosa',
      koodistoUri: 'suorituksentyyppi'
    }),
    $class:
      'fi.oph.koski.schema.YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus',
    ...o
  })

YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus.className =
  'fi.oph.koski.schema.YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus' as const

export const isYhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus = (
  a: any
): a is YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus'
