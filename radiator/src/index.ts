import * as E from 'fp-ts/lib/Either.js'
import { pipe } from 'fp-ts/lib/function.js'
import { LocalHealthSource } from './health/LocalHealthSource.ts'
import { CloudWatchHealthSource } from './health/CloudWatchHealthSource.ts'
import { startServer } from './server.ts'
import { updateWithHealthData } from './state.ts'
import { RadiatorApiHealthSource } from './health/RadiatorApiHealthSource.ts'

const getHealthSource = (env?: string, koskiDir?: string) => {
  switch (env) {
    case 'local': {
      return apiKey
        ? E.right(new RadiatorApiHealthSource(env, apiKey))
        : koskiDir !== undefined
          ? E.right(new LocalHealthSource(koskiDir))
          : E.left('Undefined Koski directory')
    }
    case 'dev':
    case 'qa':
    case 'prod':
      return E.right(
        apiKey
          ? new RadiatorApiHealthSource(env, apiKey)
          : new CloudWatchHealthSource(env)
      )
    default:
      return E.left(`Unknown environment: ${env ?? 'n/a'}`)
  }
}

const env = process.argv[2]
const koskiDir = process.argv[3]
const apiKey =
  process.env.APIKEY || process.env[`${env.toUpperCase()}_RADIATOR_KEY`] || ''

pipe(
  getHealthSource(env, koskiDir),
  E.map((health) => {
    startServer(env)
    health.addListener((d) => updateWithHealthData(env, d))
    return null
  }),
  E.mapLeft((error) => console.error(error))
)
