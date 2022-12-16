import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Opiskeluoikeuden tilahistoria (Läsnä, Eronnut, Valmistunut...) jaksoittain
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso`
 */
export type VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso = {
  $class: 'fi.oph.koski.schema.VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso'
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    'hyvaksytystisuoritettu' | 'lasna' | 'keskeytynyt' | 'mitatoity'
  >
  opintojenRahoitus?: Koodistokoodiviite<'opintojenrahoitus', '14' | '15'>
}

export const VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso = (o: {
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    'hyvaksytystisuoritettu' | 'lasna' | 'keskeytynyt' | 'mitatoity'
  >
  opintojenRahoitus?: Koodistokoodiviite<'opintojenrahoitus', '14' | '15'>
}): VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso => ({
  $class:
    'fi.oph.koski.schema.VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso',
  ...o
})

export const isVapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso = (
  a: any
): a is VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso =>
  a?.$class === 'VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso'
