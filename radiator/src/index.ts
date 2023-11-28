import * as E from 'fp-ts/Either'
import {pipe} from 'fp-ts/lib/function'
import {LocalHealthSource} from './health/LocalHealthSource'
import {CloudWatchHealthSource} from './health/CloudWatchHealthSource'
import {startServer} from './server'
import {updateWithHealthData} from './state'
import {RadiatorApiHealthSource} from './health/RadiatorApiHealthSource'

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
        apiKey ? new RadiatorApiHealthSource(env, apiKey) : new CloudWatchHealthSource(env)
      )
    default:
      return E.left(`Unknown environment: ${env ?? 'n/a'}`)
  }
}

const env = process.argv[2]
const koskiDir = process.argv[3]
const apiKey = process.env.APIKEY || ''

pipe(
  getHealthSource(env, koskiDir),
  E.map(health => {
    startServer(env)
    health.addListener(d => updateWithHealthData(env, d))
    return null
  }),
  E.mapLeft(error => console.error(error))
)
