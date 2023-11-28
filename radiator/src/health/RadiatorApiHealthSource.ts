import {HealthData, HealthSource} from './HealthSource'

export type ApiEnv = 'local' | 'dev' | 'qa' | 'prod'

const baseUrls: Record<ApiEnv, string> = {
  local: 'http://localhost:7021',
  dev: 'https://untuvaopintopolku.fi',
  qa: 'https://testiopintopolku.fi',
  prod: 'https://opintopolku.fi'
}

const refreshInterval = 60

export const supportedApiEnvs = Object.values(baseUrls) as ApiEnv[]

type ApiResponse = {
  entries: HealthData
}

export class RadiatorApiHealthSource extends HealthSource {
  apiUrl: string
  headers: object

  constructor(env: ApiEnv, apiKey: string) {
    super()
    this.apiUrl = `${baseUrls[env]}/koski/api/healthcheck/radiator`
    this.headers = {
      Authorization: `Bearer ${apiKey}`
    }
    this.startScheduling(refreshInterval)
  }

  private startScheduling(intervalSeconds: number) {
    this.fetchHealth()
    setInterval(() => this.fetchHealth(), intervalSeconds * 1000)
  }

  private async fetchHealth() {
    try {
      const response = await fetch(this.apiUrl, {
        // @ts-ignore
        headers: this.headers
      })
      if (response.status !== 200) {
        console.error(response)
        this.emitError(`${response.status} ${response.statusText}`)
      } else {
        const data = (await response.json()) as ApiResponse
        this.emit(data.entries)
      }
    } catch (err) {
      console.error(err)
      this.emitError(`${err}`)
    }
  }

  private emitError(message: string) {
    this.emit([
      {
        timestamp: new Date().toISOString(),
        instance: '',
        subsystem: 'Radiator API',
        operational: false,
        external: false,
        message
      }
    ])
  }
}
