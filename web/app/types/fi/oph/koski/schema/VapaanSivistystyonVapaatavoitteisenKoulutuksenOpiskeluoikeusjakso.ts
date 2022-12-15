import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Opiskeluoikeuden tilahistoria (Läsnä, Eronnut, Valmistunut...) jaksoittain
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso`
 */
export type VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso =
  {
    $class: 'fi.oph.koski.schema.VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso'
    alku: string
    tila: Koodistokoodiviite<
      'koskiopiskeluoikeudentila',
      'hyvaksytystisuoritettu' | 'keskeytynyt' | 'mitatoity'
    >
  }

export const VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso =
  (o: {
    alku: string
    tila: Koodistokoodiviite<
      'koskiopiskeluoikeudentila',
      'hyvaksytystisuoritettu' | 'keskeytynyt' | 'mitatoity'
    >
  }): VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso => ({
    $class:
      'fi.oph.koski.schema.VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso',
    ...o
  })

export const isVapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso =
  (
    a: any
  ): a is VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso =>
    a?.$class ===
    'VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso'
