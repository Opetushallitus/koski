/**
 * TutkinnonOsanLaajuus
 *
 * @see `fi.oph.koski.tutkinto.TutkinnonOsanLaajuus`
 */
export type TutkinnonOsanLaajuus = {
  $class: 'fi.oph.koski.tutkinto.TutkinnonOsanLaajuus'
  min?: number
  max?: number
}

export const TutkinnonOsanLaajuus = (
  o: {
    min?: number
    max?: number
  } = {}
): TutkinnonOsanLaajuus => ({
  $class: 'fi.oph.koski.tutkinto.TutkinnonOsanLaajuus',
  ...o
})

TutkinnonOsanLaajuus.className =
  'fi.oph.koski.tutkinto.TutkinnonOsanLaajuus' as const

export const isTutkinnonOsanLaajuus = (a: any): a is TutkinnonOsanLaajuus =>
  a?.$class === 'fi.oph.koski.tutkinto.TutkinnonOsanLaajuus'
