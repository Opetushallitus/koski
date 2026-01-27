const MINUTES_PER_HOUR = 60
const MINUTES_PER_DAY = MINUTES_PER_HOUR * 24

export type TimeUnits = {
  days: number
  hours: number
  minutes: number
}

export type PaattymisajankohtaResult = {
  templateName: string
  days?: number
  hours?: number
  minutes?: number
}

export const calculateTimeUnits = (durationInMin: number): TimeUnits => {
  const days = Math.floor(durationInMin / MINUTES_PER_DAY)
  const hours = Math.floor((durationInMin % MINUTES_PER_DAY) / MINUTES_PER_HOUR)
  const minutes = durationInMin % MINUTES_PER_HOUR

  return { days, hours, minutes }
}

export const buildPaattymisajankohtaTemplateName = (
  units: TimeUnits
): PaattymisajankohtaResult => {
  const { days, hours, minutes } = units
  const plural = (n: number) => (n === 1 ? '' : 's')

  const includeDays = days >= 1
  const includeHours = hours >= 1
  const includeMinutes = minutes >= 1 || (!includeDays && !includeHours)

  const templateParts: string[] = []
  const result: PaattymisajankohtaResult = { templateName: '' }

  if (includeDays) {
    templateParts.push(`day${plural(days)}`)
    result.days = days
  }
  if (includeHours) {
    templateParts.push(`hour${plural(hours)}`)
    result.hours = hours
  }
  if (includeMinutes) {
    templateParts.push(`min${plural(minutes)}`)
    result.minutes = minutes
  }

  result.templateName = `omadataoauth2_suostumuksesi_paattymisajankohta_${templateParts.join('_')}`

  return result
}

export const calculatePaattymisajankohta = (
  durationInMin: number
): PaattymisajankohtaResult => {
  return buildPaattymisajankohtaTemplateName(calculateTimeUnits(durationInMin))
}
