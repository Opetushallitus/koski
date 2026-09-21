/**
 * Palauttaa lukion kurssikertymät-raportin.
 *
 * @see `fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukioKurssikertymat`
 */
export type MassaluovutusQueryLukioKurssikertymat = {
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukioKurssikertymat'
  loppu: string
  language?: 'fi' | 'sv' | 'en'
  type: 'lukioKurssikertymat'
  alku: string
  password?: string
  organisaatioOid?: string
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}

export const MassaluovutusQueryLukioKurssikertymat = (o: {
  loppu: string
  language?: 'fi' | 'sv' | 'en'
  type?: 'lukioKurssikertymat'
  alku: string
  password?: string
  organisaatioOid?: string
  format?: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}): MassaluovutusQueryLukioKurssikertymat => ({
  type: 'lukioKurssikertymat',
  $class:
    'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukioKurssikertymat',
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  ...o
})

MassaluovutusQueryLukioKurssikertymat.className =
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukioKurssikertymat' as const

export const isMassaluovutusQueryLukioKurssikertymat = (
  a: any
): a is MassaluovutusQueryLukioKurssikertymat =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukioKurssikertymat'
