const MINUTES_PER_HOUR = 60
const MINUTES_PER_DAY = MINUTES_PER_HOUR * 24

export type TimeUnits = {
  days: number
  hours: number
  minutes: number
}

export type TranslateFn = (key: string) => string

export const calculateTimeUnits = (durationInMin: number): TimeUnits => {
  const days = Math.floor(durationInMin / MINUTES_PER_DAY)
  const hours = Math.floor((durationInMin % MINUTES_PER_DAY) / MINUTES_PER_HOUR)
  const minutes = durationInMin % MINUTES_PER_HOUR

  return { days, hours, minutes }
}

export const buildLocalizedTimeString = (
  units: TimeUnits,
  t: TranslateFn
): string => {
  const { days, hours, minutes } = units

  const parts: string[] = []

  if (days >= 1) {
    const unitKey =
      days === 1 ? 'omadataoauth2_aika_paiva' : 'omadataoauth2_aika_paivaa'
    parts.push(`${days} ${t(unitKey)}`)
  }

  if (hours >= 1) {
    const unitKey =
      hours === 1 ? 'omadataoauth2_aika_tunti' : 'omadataoauth2_aika_tuntia'
    parts.push(`${hours} ${t(unitKey)}`)
  }

  if (minutes >= 1 || parts.length === 0) {
    const unitKey =
      minutes === 1
        ? 'omadataoauth2_aika_minuutti'
        : 'omadataoauth2_aika_minuuttia'
    parts.push(`${minutes} ${t(unitKey)}`)
  }

  return parts.join(' ')
}

export const buildLocalizedPaattymisajankohtaText = (
  durationInMin: number,
  t: TranslateFn
): string => {
  const timeString = buildLocalizedTimeString(
    calculateTimeUnits(durationInMin),
    t
  )
  return t('omadataoauth2_suostumuksesi_paattymisajankohta_template').replace(
    '{{time}}',
    timeString
  )
}
