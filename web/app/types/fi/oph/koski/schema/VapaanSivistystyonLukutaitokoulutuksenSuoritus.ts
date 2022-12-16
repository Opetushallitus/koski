import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { VapaanSivistystyönLukutaitokoulutus } from './VapaanSivistystyonLukutaitokoulutus'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus } from './VapaanSivistystyonLukutaitokoulutuksenKokonaisuudenSuoritus'
import { HenkilövahvistusValinnaisellaPaikkakunnalla } from './HenkilovahvistusValinnaisellaPaikkakunnalla'

/**
 * Laajennetun oppivelvollisuuden suoritus
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönLukutaitokoulutuksenSuoritus`
 */
export type VapaanSivistystyönLukutaitokoulutuksenSuoritus = {
  $class: 'fi.oph.koski.schema.VapaanSivistystyönLukutaitokoulutuksenSuoritus'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'vstlukutaitokoulutus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: VapaanSivistystyönLukutaitokoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}

export const VapaanSivistystyönLukutaitokoulutuksenSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'vstlukutaitokoulutus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli?: VapaanSivistystyönLukutaitokoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}): VapaanSivistystyönLukutaitokoulutuksenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'vstlukutaitokoulutus',
    koodistoUri: 'suorituksentyyppi'
  }),
  koulutusmoduuli: VapaanSivistystyönLukutaitokoulutus({
    tunniste: Koodistokoodiviite({
      koodiarvo: '999911',
      koodistoUri: 'koulutus'
    })
  }),
  $class: 'fi.oph.koski.schema.VapaanSivistystyönLukutaitokoulutuksenSuoritus',
  ...o
})

export const isVapaanSivistystyönLukutaitokoulutuksenSuoritus = (
  a: any
): a is VapaanSivistystyönLukutaitokoulutuksenSuoritus =>
  a?.$class === 'VapaanSivistystyönLukutaitokoulutuksenSuoritus'
