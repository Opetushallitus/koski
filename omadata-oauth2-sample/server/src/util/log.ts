type Level = 'info' | 'warn' | 'error'

export function log(
  level: Level,
  message: string,
  extra: Record<string, unknown> = {}
): void {
  const line = JSON.stringify({
    level,
    message,
    time: new Date().toISOString(),
    ...extra
  })
  if (level === 'error') {
    console.error(line)
  } else {
    console.log(line)
  }
}
