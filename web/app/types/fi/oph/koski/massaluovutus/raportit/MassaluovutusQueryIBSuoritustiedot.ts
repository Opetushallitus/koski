/**
 * Palauttaa IB-tutkinnon suoritustietojen tarkistusraportin.
 *
 * @see `fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryIBSuoritustiedot`
 */
export type MassaluovutusQueryIBSuoritustiedot = {
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryIBSuoritustiedot'
  loppu: string
  osasuoritustenAikarajaus?: boolean
  type: 'ibSuoritustiedot'
  alku: string
  raportinTyyppi: 'ibtutkinto' | 'preiboppimaara'
  password?: string
  organisaatioOid?: string
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  language?: 'fi' | 'sv' | 'en'
  kotikuntaPvm?: string
}

export const MassaluovutusQueryIBSuoritustiedot = (o: {
  loppu: string
  osasuoritustenAikarajaus?: boolean
  type?: 'ibSuoritustiedot'
  alku: string
  raportinTyyppi: 'ibtutkinto' | 'preiboppimaara'
  password?: string
  organisaatioOid?: string
  format?: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  language?: 'fi' | 'sv' | 'en'
  kotikuntaPvm?: string
}): MassaluovutusQueryIBSuoritustiedot => ({
  type: 'ibSuoritustiedot',
  $class:
    'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryIBSuoritustiedot',
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  ...o
})

MassaluovutusQueryIBSuoritustiedot.className =
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryIBSuoritustiedot' as const

export const isMassaluovutusQueryIBSuoritustiedot = (
  a: any
): a is MassaluovutusQueryIBSuoritustiedot =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryIBSuoritustiedot'
