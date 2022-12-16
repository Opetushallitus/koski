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

export const isOikeuttaMaksuttomuuteenPidennetty = (
  a: any
): a is OikeuttaMaksuttomuuteenPidennetty =>
  a?.$class === 'OikeuttaMaksuttomuuteenPidennetty'
