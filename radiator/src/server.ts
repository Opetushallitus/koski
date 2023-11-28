import http from 'node:http'
import {indexPage} from './pages/indexPage'
import {fromState} from './representationalState'
import {getState} from './state'

const hostname = '127.0.0.1'
const port = 3003

export const startServer = (env: string) => {
  const server = http.createServer((req, res) => {
    res.statusCode = 200
    res.setHeader('Content-Type', 'text/html')
    res.end(indexPage(fromState(getState(env))))
  })

  server.listen(port, hostname, () => {
    console.log(
      `Radiator running at http://${hostname}:${port} on environment ${env.toUpperCase()}`
    )
  })
}
