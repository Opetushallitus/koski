import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Opiskeluoikeuden tilahistoria (Läsnä, Eronnut, Valmistunut...) jaksoittain
 *
 * @see `fi.oph.koski.schema.TaiteenPerusopetuksenOpiskeluoikeusjakso`
 */
export type TaiteenPerusopetuksenOpiskeluoikeusjakso = {
  $class: 'fi.oph.koski.schema.TaiteenPerusopetuksenOpiskeluoikeusjakso'
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    'lasna' | 'mitatoity' | 'paattynyt' | 'hyvaksytystisuoritettu'
  >
}

export const TaiteenPerusopetuksenOpiskeluoikeusjakso = (o: {
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    'lasna' | 'mitatoity' | 'paattynyt' | 'hyvaksytystisuoritettu'
  >
}): TaiteenPerusopetuksenOpiskeluoikeusjakso => ({
  $class: 'fi.oph.koski.schema.TaiteenPerusopetuksenOpiskeluoikeusjakso',
  ...o
})

TaiteenPerusopetuksenOpiskeluoikeusjakso.className =
  'fi.oph.koski.schema.TaiteenPerusopetuksenOpiskeluoikeusjakso' as const

export const isTaiteenPerusopetuksenOpiskeluoikeusjakso = (
  a: any
): a is TaiteenPerusopetuksenOpiskeluoikeusjakso =>
  a?.$class === 'fi.oph.koski.schema.TaiteenPerusopetuksenOpiskeluoikeusjakso'
