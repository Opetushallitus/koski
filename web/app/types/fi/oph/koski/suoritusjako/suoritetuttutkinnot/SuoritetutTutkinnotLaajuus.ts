import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'

/**
 * SuoritetutTutkinnotLaajuus
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotLaajuus`
 */
export type SuoritetutTutkinnotLaajuus = {
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotLaajuus'
  arvo: number
  yksikkö: SuoritetutTutkinnotKoodistokoodiviite
}

export const SuoritetutTutkinnotLaajuus = (o: {
  arvo: number
  yksikkö: SuoritetutTutkinnotKoodistokoodiviite
}): SuoritetutTutkinnotLaajuus => ({
  $class:
    'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotLaajuus',
  ...o
})

SuoritetutTutkinnotLaajuus.className =
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotLaajuus' as const

export const isSuoritetutTutkinnotLaajuus = (
  a: any
): a is SuoritetutTutkinnotLaajuus =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotLaajuus'
