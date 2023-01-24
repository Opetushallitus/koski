/**
 * OikeuttaMaksuttomuuteenPidennetty
 *
 * @see `fi.oph.koski.schema.OikeuttaMaksuttomuuteenPidennetty`
 */
export type OikeuttaMaksuttomuuteenPidennetty = {
  $class: 'fi.oph.koski.schema.OikeuttaMaksuttomuuteenPidennetty'
  alku: string
  loppu: string
}

export const OikeuttaMaksuttomuuteenPidennetty = (o: {
  alku: string
  loppu: string
}): OikeuttaMaksuttomuuteenPidennetty => ({
  $class: 'fi.oph.koski.schema.OikeuttaMaksuttomuuteenPidennetty',
  ...o
})

OikeuttaMaksuttomuuteenPidennetty.className =
  'fi.oph.koski.schema.OikeuttaMaksuttomuuteenPidennetty' as const

export const isOikeuttaMaksuttomuuteenPidennetty = (
  a: any
): a is OikeuttaMaksuttomuuteenPidennetty =>
  a?.$class === 'fi.oph.koski.schema.OikeuttaMaksuttomuuteenPidennetty'
