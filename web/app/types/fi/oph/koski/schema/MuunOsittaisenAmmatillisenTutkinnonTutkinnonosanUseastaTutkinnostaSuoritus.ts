import { AmmatillinenArviointi } from './AmmatillinenArviointi'
import { Näyttö } from './Naytto'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { AmmatillisenTutkinnonOsanLisätieto } from './AmmatillisenTutkinnonOsanLisatieto'
import { MuuKuinYhteinenTutkinnonOsa } from './MuuKuinYhteinenTutkinnonOsa'
import { OsaamisenTunnustaminen } from './OsaamisenTunnustaminen'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus } from './AmmatillisenTutkinnonOsaaPienemmanKokonaisuudenSuoritus'
import { AmmatillinenTutkintoKoulutus } from './AmmatillinenTutkintoKoulutus'

/**
 * Ammatilliseen tutkintoon liittyvän, muun kuin yhteisen tutkinnonosan suoritus
 *
 * @see `fi.oph.koski.schema.MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus`
 */
export type MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus =
  {
    $class: 'fi.oph.koski.schema.MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus'
    arviointi?: Array<AmmatillinenArviointi>
    näyttö?: Näyttö
    tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ammatillisentutkinnonosa'>
    tila?: Koodistokoodiviite<'suorituksentila', string>
    alkamispäivä?: string
    suorituskieli?: Koodistokoodiviite<'kieli', string>
    lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
    korotettu?: Koodistokoodiviite<'ammatillisensuorituksenkorotus', string>
    koulutusmoduuli: MuuKuinYhteinenTutkinnonOsa
    tunnustettu?: OsaamisenTunnustaminen
    toimipiste?: OrganisaatioWithOid
    tutkinnonOsanRyhmä?: Koodistokoodiviite<
      'ammatillisentutkinnonosanryhma',
      '1'
    >
    osasuoritukset?: Array<AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus>
    tutkinto: AmmatillinenTutkintoKoulutus
  }

export const MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus =
  (o: {
    arviointi?: Array<AmmatillinenArviointi>
    näyttö?: Näyttö
    tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'ammatillisentutkinnonosa'>
    tila?: Koodistokoodiviite<'suorituksentila', string>
    alkamispäivä?: string
    suorituskieli?: Koodistokoodiviite<'kieli', string>
    lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
    korotettu?: Koodistokoodiviite<'ammatillisensuorituksenkorotus', string>
    koulutusmoduuli: MuuKuinYhteinenTutkinnonOsa
    tunnustettu?: OsaamisenTunnustaminen
    toimipiste?: OrganisaatioWithOid
    tutkinnonOsanRyhmä?: Koodistokoodiviite<
      'ammatillisentutkinnonosanryhma',
      '1'
    >
    osasuoritukset?: Array<AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus>
    tutkinto: AmmatillinenTutkintoKoulutus
  }): MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'ammatillisentutkinnonosa',
      koodistoUri: 'suorituksentyyppi'
    }),
    $class:
      'fi.oph.koski.schema.MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus',
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
