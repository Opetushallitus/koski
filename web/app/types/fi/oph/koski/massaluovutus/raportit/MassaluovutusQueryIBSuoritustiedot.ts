/**
 * Palauttaa IB-tutkinnon suoritustietojen tarkistusraportin.
 *
 * @see `fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryIBSuoritustiedot`
 */
export type MassaluovutusQueryIBSuoritustiedot = {
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryIBSuoritustiedot'
  loppu: string
  language?: 'fi' | 'sv' | 'en'
  osasuoritustenAikarajaus?: boolean
  type: 'ibSuoritustiedot'
  alku: string
  raportinTyyppi: 'ibtutkinto' | 'preiboppimaara'
  password?: string
  organisaatioOid?: string
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}

export const MassaluovutusQueryIBSuoritustiedot = (o: {
  loppu: string
  language?: 'fi' | 'sv' | 'en'
  osasuoritustenAikarajaus?: boolean
  type?: 'ibSuoritustiedot'
  alku: string
  raportinTyyppi: 'ibtutkinto' | 'preiboppimaara'
  password?: string
  organisaatioOid?: string
  format?: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
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
