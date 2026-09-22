/**
 * Palauttaa perusopetukseen valmistavan opetuksen suoritustietojen tarkistusraportin.
 *
 * @see `fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetukseenValmistava`
 */
export type MassaluovutusQueryPerusopetukseenValmistava = {
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetukseenValmistava'
  loppu: string
  osasuoritustenAikarajaus?: boolean
  type: 'perusopetukseenValmistava'
  alku: string
  password?: string
  organisaatioOid?: string
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  language?: 'fi' | 'sv' | 'en'
  kotikuntaPvm?: string
}

export const MassaluovutusQueryPerusopetukseenValmistava = (o: {
  loppu: string
  osasuoritustenAikarajaus?: boolean
  type?: 'perusopetukseenValmistava'
  alku: string
  password?: string
  organisaatioOid?: string
  format?: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  language?: 'fi' | 'sv' | 'en'
  kotikuntaPvm?: string
}): MassaluovutusQueryPerusopetukseenValmistava => ({
  type: 'perusopetukseenValmistava',
  $class:
    'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetukseenValmistava',
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  ...o
})

MassaluovutusQueryPerusopetukseenValmistava.className =
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetukseenValmistava' as const

export const isMassaluovutusQueryPerusopetukseenValmistava = (
  a: any
): a is MassaluovutusQueryPerusopetukseenValmistava =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetukseenValmistava'
