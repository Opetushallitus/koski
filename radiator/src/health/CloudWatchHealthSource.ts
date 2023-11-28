/* eslint-disable @typescript-eslint/restrict-template-expressions */
import {spawn} from 'child_process'
import {HealthSource, isHealthDataEntry} from './HealthSource'

export type RemoteEnv = 'dev' | 'qa' | 'prod'

export class CloudWatchHealthSource extends HealthSource {
  constructor(environment: RemoteEnv) {
    super()
    tailLogs(environment)
      .then(start => start(this.parseLine.bind(this)))
      .catch(console.error)
  }

  parseLine(line: string) {
    const match = line.match(/(.*?)\s(.*)/)
    if (match) {
      try {
        const data = JSON.parse(match[2])
        const entry = JSON.parse(data.message)
        if (isHealthDataEntry(entry)) {
          this.emit([
            {
              ...entry,
              timestamp: match[1],
              instance: data.source_host
            }
          ])
        }
      } catch (_) {}
    }
  }
}

const tailLogs = async (environment: RemoteEnv) =>
  runAwsCli('logs tail koski-health', {
    profile: `oph-koski-${environment}`,
    format: 'short',
    follow: true
  })

const runAwsCli = async (command: string, params: object) => (onData: (data: any) => void) => {
  const process = spawn('aws', [
    ...command.split(' '),
    ...Object.entries(params)
      .flatMap(([key, value]) =>
        value !== undefined && value !== false ? [`--${key}`, value === true ? '' : value] : []
      )
      .filter(x => x.length > 0)
  ])

  process.stdout.on('data', data => {
    const entries = data.toString().split('\n')
    entries.forEach(onData)
  })

  process.stderr.on('data', error => console.error(error.toString()))
  process.on('error', error => console.error(error))
  //   process.on('close', onClose)
}
