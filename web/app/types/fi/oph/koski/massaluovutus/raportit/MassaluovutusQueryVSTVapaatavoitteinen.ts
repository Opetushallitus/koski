/**
 * Palauttaa vapaan sivistystyön vapaatavoitteisen koulutuksen opiskeluoikeus- ja suoritustietojen tarkistusraportin. Raportille valitaan opiskeluoikeudet, joiden päättymispäivä osuu annetulle aikajaksolle.
 *
 * @see `fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryVSTVapaatavoitteinen`
 */
export type MassaluovutusQueryVSTVapaatavoitteinen = {
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryVSTVapaatavoitteinen'
  loppu: string
  language?: 'fi' | 'sv' | 'en'
  type: 'vstVapaatavoitteinen'
  alku: string
  password?: string
  organisaatioOid?: string
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}

export const MassaluovutusQueryVSTVapaatavoitteinen = (o: {
  loppu: string
  language?: 'fi' | 'sv' | 'en'
  type?: 'vstVapaatavoitteinen'
  alku: string
  password?: string
  organisaatioOid?: string
  format?: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}): MassaluovutusQueryVSTVapaatavoitteinen => ({
  type: 'vstVapaatavoitteinen',
  $class:
    'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryVSTVapaatavoitteinen',
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  ...o
})

MassaluovutusQueryVSTVapaatavoitteinen.className =
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryVSTVapaatavoitteinen' as const

export const isMassaluovutusQueryVSTVapaatavoitteinen = (
  a: any
): a is MassaluovutusQueryVSTVapaatavoitteinen =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryVSTVapaatavoitteinen'
