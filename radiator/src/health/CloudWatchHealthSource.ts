import { spawn } from 'child_process'
import { HealthSource, isHealthDataEntry } from './HealthSource.ts'

export type RemoteEnv = 'dev' | 'qa' | 'prod'

type LogLine = {
  message: string
  source_host: string
}

export class CloudWatchHealthSource extends HealthSource {
  constructor(environment: RemoteEnv) {
    super()
    tailLogs(environment)(this.parseLine.bind(this))
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

const tailLogs = (environment: RemoteEnv) =>
  runAwsCli('logs tail koski-health', {
    profile: `oph-koski-${environment}`,
    format: 'short',
    follow: true
  })

const runAwsCli =
  (command: string, params: Record<string, string | boolean>) =>
  (onData: (data: string) => void) => {
    const process = spawn('aws', [
      ...command.split(' '),
      ...Object.entries(params)
        .flatMap(([key, value]) =>
          value !== undefined && value !== false
            ? [`--${key}`, value === true ? '' : value]
            : []
        )
        .filter((x) => x.length > 0)
    ])

    process.stdout.on('data', (data: Buffer) => {
      const entries = data.toString().split('\n')
      entries.forEach(onData)
    })

    process.stderr.on('data', (error: Buffer) =>
      console.error(error.toString())
    )
    process.on('error', (error) => console.error(error))
    //   process.on('close', onClose)
  }
