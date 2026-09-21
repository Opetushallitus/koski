/**
 * Palauttaa TOPKS ammatillisen koulutuksen raportin (työhön ja itsenäiseen elämään valmentava koulutus).
 * Saatu tulostiedosto vastaa raporttinäkymästä ladattavaa tiedostoa.
 *
 * @see `fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryTOPKSAmmatillinen`
 */
export type MassaluovutusQueryTOPKSAmmatillinen = {
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryTOPKSAmmatillinen'
  loppu: string
  language?: 'fi' | 'sv' | 'en'
  type: 'topksAmmatillinen'
  alku: string
  password?: string
  organisaatioOid?: string
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}

export const MassaluovutusQueryTOPKSAmmatillinen = (o: {
  loppu: string
  language?: 'fi' | 'sv' | 'en'
  type?: 'topksAmmatillinen'
  alku: string
  password?: string
  organisaatioOid?: string
  format?: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}): MassaluovutusQueryTOPKSAmmatillinen => ({
  type: 'topksAmmatillinen',
  $class:
    'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryTOPKSAmmatillinen',
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  ...o
})

MassaluovutusQueryTOPKSAmmatillinen.className =
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryTOPKSAmmatillinen' as const

export const isMassaluovutusQueryTOPKSAmmatillinen = (
  a: any
): a is MassaluovutusQueryTOPKSAmmatillinen =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryTOPKSAmmatillinen'
