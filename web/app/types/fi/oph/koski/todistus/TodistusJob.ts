/**
 * TodistusJob
 *
 * @see `fi.oph.koski.todistus.TodistusJob`
 */
export type TodistusJob = {
  $class: 'fi.oph.koski.todistus.TodistusJob'
  attempts?: number
  opiskeluoikeusVersionumero?: number
  state: string
  completedAt?: string
  opiskeluoikeusOid: string
  oppijaHenkilötiedotHash?: string
  id: string
  oppijaOid: string
  language: string
  error?: string
  createdAt: string
  startedAt?: string
  userOid?: string
  worker?: string
}

export const TodistusJob = (o: {
  attempts?: number
  opiskeluoikeusVersionumero?: number
  state: string
  completedAt?: string
  opiskeluoikeusOid: string
  oppijaHenkilötiedotHash?: string
  id: string
  oppijaOid: string
  language: string
  error?: string
  createdAt: string
  startedAt?: string
  userOid?: string
  worker?: string
}): TodistusJob => ({ $class: 'fi.oph.koski.todistus.TodistusJob', ...o })

TodistusJob.className = 'fi.oph.koski.todistus.TodistusJob' as const

export const isTodistusJob = (a: any): a is TodistusJob =>
  a?.$class === 'fi.oph.koski.todistus.TodistusJob'
