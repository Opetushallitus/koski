/**
 * TodistusJob
 *
 * @see `fi.oph.koski.todistus.TodistusJob`
 */
export type TodistusJob = {
  $class: 'fi.oph.koski.todistus.TodistusJob'
  attempts?: number
  opiskeluoikeusVersionumero?: number
  opiskeluoikeusOid: string
  oppijaHenkilötiedotHash?: string
  id: string
  worker?: string
  state: string
  completedAt?: string
  oppijaOid: string
  error?: string
  createdAt: string
  startedAt?: string
  templateVariant: string
  userOid?: string
}

export const TodistusJob = (o: {
  attempts?: number
  opiskeluoikeusVersionumero?: number
  opiskeluoikeusOid: string
  oppijaHenkilötiedotHash?: string
  id: string
  worker?: string
  state: string
  completedAt?: string
  oppijaOid: string
  error?: string
  createdAt: string
  startedAt?: string
  templateVariant: string
  userOid?: string
}): TodistusJob => ({ $class: 'fi.oph.koski.todistus.TodistusJob', ...o })

TodistusJob.className = 'fi.oph.koski.todistus.TodistusJob' as const

export const isTodistusJob = (a: any): a is TodistusJob =>
  a?.$class === 'fi.oph.koski.todistus.TodistusJob'
