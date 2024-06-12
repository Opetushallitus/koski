/**
 * SuoritusClass
 *
 * @see `fi.oph.koski.typemodel.SuoritusClass`
 */
export type SuoritusClass = {
  $class: 'fi.oph.koski.typemodel.SuoritusClass'
  className: string
  tyyppi: string
}

export const SuoritusClass = (o: {
  className: string
  tyyppi: string
}): SuoritusClass => ({ $class: 'fi.oph.koski.typemodel.SuoritusClass', ...o })

SuoritusClass.className = 'fi.oph.koski.typemodel.SuoritusClass' as const

export const isSuoritusClass = (a: any): a is SuoritusClass =>
  a?.$class === 'fi.oph.koski.typemodel.SuoritusClass'
