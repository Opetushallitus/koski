import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { VapaanSivistystyönJotpaKoulutus } from './VapaanSivistystyonJotpaKoulutus'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus } from './VapaanSivistystyonJotpaKoulutuksenOsasuorituksenSuoritus'
import { HenkilövahvistusValinnaisellaPaikkakunnalla } from './HenkilovahvistusValinnaisellaPaikkakunnalla'

/**
 * VapaanSivistystyönJotpaKoulutuksenSuoritus
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönJotpaKoulutuksenSuoritus`
 */
export type VapaanSivistystyönJotpaKoulutuksenSuoritus = {
  $class: 'fi.oph.koski.schema.VapaanSivistystyönJotpaKoulutuksenSuoritus'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'vstjotpakoulutus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: VapaanSivistystyönJotpaKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}

export const VapaanSivistystyönJotpaKoulutuksenSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'vstjotpakoulutus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: VapaanSivistystyönJotpaKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}): VapaanSivistystyönJotpaKoulutuksenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'vstjotpakoulutus',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.VapaanSivistystyönJotpaKoulutuksenSuoritus',
  ...o
})

export const isVapaanSivistystyönJotpaKoulutuksenSuoritus = (
  a: any
): a is VapaanSivistystyönJotpaKoulutuksenSuoritus =>
  a?.$class === 'VapaanSivistystyönJotpaKoulutuksenSuoritus'
