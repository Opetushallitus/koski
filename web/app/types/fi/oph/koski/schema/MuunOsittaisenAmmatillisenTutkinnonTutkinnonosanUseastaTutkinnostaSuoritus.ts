import { AmmatillinenArviointi } from './AmmatillinenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { AmmatillisenTutkinnonOsanLisätieto } from './AmmatillisenTutkinnonOsanLisatieto'
import { MuuKuinYhteinenTutkinnonOsa } from './MuuKuinYhteinenTutkinnonOsa'
import { AmmatillinenTutkintoKoulutus } from './AmmatillinenTutkintoKoulutus'
import { NäyttöAmmatillinenOsittainen } from './NayttoAmmatillinenOsittainen'
import { OsaamisenTunnustaminen } from './OsaamisenTunnustaminen'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus } from './AmmatillisenTutkinnonOsaaPienemmanKokonaisuudenSuoritus'

/**
 * Ammatilliseen tutkintoon liittyvän, muun kuin yhteisen tutkinnonosan suoritus
 *
 * @see `fi.oph.koski.schema.MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus`
 */
export type MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus =
  {
    $class: 'fi.oph.koski.schema.MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus'
    arviointi?: Array<AmmatillinenArviointi>
    suorituskieli?: Koodistokoodiviite<'kieli', string>
    lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
    koulutusmoduuli: MuuKuinYhteinenTutkinnonOsa
    tutkinto: AmmatillinenTutkintoKoulutus
    näyttö?: NäyttöAmmatillinenOsittainen
    tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ammatillisentutkinnonosa'>
    tila?: Koodistokoodiviite<'suorituksentila', string>
    alkamispäivä?: string
    tunnustettu?: OsaamisenTunnustaminen
    toimipiste?: OrganisaatioWithOid
    tutkinnonOsanRyhmä?: Koodistokoodiviite<
      'ammatillisentutkinnonosanryhma',
      '1'
    >
    osasuoritukset?: Array<AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus>
  }

export const MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus =
  (o: {
    arviointi?: Array<AmmatillinenArviointi>
    suorituskieli?: Koodistokoodiviite<'kieli', string>
    lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
    koulutusmoduuli: MuuKuinYhteinenTutkinnonOsa
    tutkinto: AmmatillinenTutkintoKoulutus
    näyttö?: NäyttöAmmatillinenOsittainen
    tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'ammatillisentutkinnonosa'>
    tila?: Koodistokoodiviite<'suorituksentila', string>
    alkamispäivä?: string
    tunnustettu?: OsaamisenTunnustaminen
    toimipiste?: OrganisaatioWithOid
    tutkinnonOsanRyhmä?: Koodistokoodiviite<
      'ammatillisentutkinnonosanryhma',
      '1'
    >
    osasuoritukset?: Array<AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus>
  }): MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus => ({
    $class:
      'fi.oph.koski.schema.MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus',
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'ammatillisentutkinnonosa',
      koodistoUri: 'suorituksentyyppi'
    }),
    ...o
  })

MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus.className =
  'fi.oph.koski.schema.MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus' as const

export const isMuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus =
  (
    a: any
  ): a is MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus =>
    a?.$class ===
    'fi.oph.koski.schema.MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus'
