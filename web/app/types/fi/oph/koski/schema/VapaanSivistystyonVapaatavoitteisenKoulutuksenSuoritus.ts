import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { VapaanSivistystyönVapaatavoitteinenKoulutus } from './VapaanSivistystyonVapaatavoitteinenKoulutus'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus } from './VapaanSivistystyonVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus'
import { HenkilövahvistusValinnaisellaPaikkakunnalla } from './HenkilovahvistusValinnaisellaPaikkakunnalla'

/**
 * VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus`
 */
export type VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus = {
  $class: 'fi.oph.koski.schema.VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus'
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'vstvapaatavoitteinenkoulutus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: VapaanSivistystyönVapaatavoitteinenKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}

export const VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'vstvapaatavoitteinenkoulutus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli?: VapaanSivistystyönVapaatavoitteinenKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}): VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'vstvapaatavoitteinenkoulutus',
    koodistoUri: 'suorituksentyyppi'
  }),
  koulutusmoduuli: VapaanSivistystyönVapaatavoitteinenKoulutus({
    tunniste: Koodistokoodiviite({
      koodiarvo: '099999',
      koodistoUri: 'koulutus'
    })
  }),
  $class:
    'fi.oph.koski.schema.VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus',
  ...o
})

export const isVapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus = (
  a: any
): a is VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus'
