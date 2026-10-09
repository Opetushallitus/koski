import path from 'node:path'
import { followProcessOutput } from './followProcessOutput.ts'
import { HealthSource, isHealthDataEntry } from './HealthSource.ts'

type LogLine = {
  '@timestamp': string
  message: string
  source_host: string
}

export class LocalHealthSource extends HealthSource {
  constructor(koskiDir: string) {
    super()
    followProcessOutput(
      'tail',
      ['-F', '-n', '+1', path.join(koskiDir, 'log', 'health.log')],
      this.parseLine.bind(this)
    )
  }

  parseLine(line: string) {
    const data = JSON.parse(line) as LogLine
    const entry: unknown = JSON.parse(data.message)
    if (isHealthDataEntry(entry)) {
      this.emit([
        {
          ...entry,
          timestamp: data['@timestamp'],
          instance: data.source_host
        }
      ])
    }
  }
}
