import { VapaanSivistystyönOsaamismerkinArviointi } from './VapaanSivistystyonOsaamismerkinArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { VapaanSivistystyönOsaamismerkki } from './VapaanSivistystyonOsaamismerkki'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { HenkilövahvistusValinnaisellaPaikkakunnalla } from './HenkilovahvistusValinnaisellaPaikkakunnalla'

/**
 * VapaanSivistystyönOsaamismerkinSuoritus
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönOsaamismerkinSuoritus`
 */
export type VapaanSivistystyönOsaamismerkinSuoritus = {
  $class: 'fi.oph.koski.schema.VapaanSivistystyönOsaamismerkinSuoritus'
  arviointi?: Array<VapaanSivistystyönOsaamismerkinArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'vstosaamismerkki'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: VapaanSivistystyönOsaamismerkki
  toimipiste: OrganisaatioWithOid
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}

export const VapaanSivistystyönOsaamismerkinSuoritus = (o: {
  arviointi?: Array<VapaanSivistystyönOsaamismerkinArviointi>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'vstosaamismerkki'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: VapaanSivistystyönOsaamismerkki
  toimipiste: OrganisaatioWithOid
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}): VapaanSivistystyönOsaamismerkinSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'vstosaamismerkki',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.VapaanSivistystyönOsaamismerkinSuoritus',
  ...o
})

VapaanSivistystyönOsaamismerkinSuoritus.className =
  'fi.oph.koski.schema.VapaanSivistystyönOsaamismerkinSuoritus' as const

export const isVapaanSivistystyönOsaamismerkinSuoritus = (
  a: any
): a is VapaanSivistystyönOsaamismerkinSuoritus =>
  a?.$class === 'fi.oph.koski.schema.VapaanSivistystyönOsaamismerkinSuoritus'
