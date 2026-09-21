/**
 * Palauttaa lukioon valmistavan koulutuksen (LUVA) opiskelijamääräraportin.
 *
 * @see `fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLuvaOpiskelijamaarat`
 */
export type MassaluovutusQueryLuvaOpiskelijamaarat = {
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLuvaOpiskelijamaarat'
  paiva: string
  language?: 'fi' | 'sv' | 'en'
  type: 'luvaOpiskelijamaarat'
  password?: string
  organisaatioOid?: string
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}

export const MassaluovutusQueryLuvaOpiskelijamaarat = (o: {
  paiva: string
  language?: 'fi' | 'sv' | 'en'
  type?: 'luvaOpiskelijamaarat'
  password?: string
  organisaatioOid?: string
  format?: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}): MassaluovutusQueryLuvaOpiskelijamaarat => ({
  type: 'luvaOpiskelijamaarat',
  $class:
    'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLuvaOpiskelijamaarat',
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  ...o
})

MassaluovutusQueryLuvaOpiskelijamaarat.className =
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLuvaOpiskelijamaarat' as const

export const isMassaluovutusQueryLuvaOpiskelijamaarat = (
  a: any
): a is MassaluovutusQueryLuvaOpiskelijamaarat =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLuvaOpiskelijamaarat'
