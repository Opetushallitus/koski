import { followProcessOutput } from './followProcessOutput.ts'
import { HealthSource, isHealthDataEntry } from './HealthSource.ts'

export type RemoteEnv = 'dev' | 'qa' | 'prod'

type LogLine = {
  message: string
  source_host: string
}

export class CloudWatchHealthSource extends HealthSource {
  constructor(environment: RemoteEnv) {
    super()
    tailLogs(environment, this.parseLine.bind(this))
  }

  parseLine(line: string) {
    const match = line.match(/(.*?)\s(.*)/)
    if (match) {
      try {
        const data = JSON.parse(match[2]) as LogLine
        const entry: unknown = JSON.parse(data.message)
        if (isHealthDataEntry(entry)) {
          this.emit([
            {
              ...entry,
              timestamp: match[1],
              instance: data.source_host
            }
          ])
        }
      } catch {
        // Lokissa on myös muita kuin terveystietorivejä.
      }
    }
  }
}

const tailLogs = (environment: RemoteEnv, onLine: (line: string) => void) =>
  followProcessOutput(
    'aws',
    [
      'logs',
      'tail',
      'koski-health',
      '--profile',
      `oph-koski-${environment}`,
      '--format',
      'short',
      '--follow'
    ],
    onLine
  )
