import path from 'node:path'
import { Tail } from 'tail'
import { HealthSource, isHealthDataEntry } from './HealthSource.ts'

type LogLine = {
  '@timestamp': string
  message: string
  source_host: string
}

export class LocalHealthSource extends HealthSource {
  tail: Tail

  constructor(koskiDir: string) {
    super()
    this.tail = new Tail(path.join(koskiDir, 'log', 'health.log'), {
      fromBeginning: true
    })
    this.tail.on('line', this.parseLine.bind(this))
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
