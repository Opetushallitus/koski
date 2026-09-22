/**
 * Palauttaa esiopetuksen raportin.
 *
 * @see `fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryEsiopetus`
 */
export type MassaluovutusQueryEsiopetus = {
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryEsiopetus'
  paiva: string
  type: 'esiopetus'
  password?: string
  organisaatioOid?: string
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  language?: 'fi' | 'sv' | 'en'
  kotikuntaPvm?: string
}

export const MassaluovutusQueryEsiopetus = (o: {
  paiva: string
  type?: 'esiopetus'
  password?: string
  organisaatioOid?: string
  format?: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  language?: 'fi' | 'sv' | 'en'
  kotikuntaPvm?: string
}): MassaluovutusQueryEsiopetus => ({
  type: 'esiopetus',
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryEsiopetus',
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  ...o
})

MassaluovutusQueryEsiopetus.className =
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryEsiopetus' as const

export const isMassaluovutusQueryEsiopetus = (
  a: any
): a is MassaluovutusQueryEsiopetus =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryEsiopetus'
