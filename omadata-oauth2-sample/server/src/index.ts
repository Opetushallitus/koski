import express, { Application, Request, Response } from 'express'
import path from 'node:path'
import helmet from 'helmet'
import RateLimit from 'express-rate-limit'
import openidApiTest from './apiroutes/openid-api-test.js'
import healthCheck from './apiroutes/healthcheck.js'
import bodyParser from 'body-parser'

const app: Application = express()
app.set('trust proxy', 1)
const port = process.env.PORT || 7051

const limiter = RateLimit({
  windowMs: 1 * 60 * 1000,
  limit: 1000,
  standardHeaders: 'draft-7',
  legacyHeaders: false
})

app.use(limiter)

app.use(helmet())

app.use(bodyParser.urlencoded({ extended: false }))

const staticFilesPath = path.resolve('./public')
const indexFilePath = path.join(staticFilesPath, 'index.html')

app.use(express.static(staticFilesPath))

app.use('/api/openid-api-test', openidApiTest)

app.use('/api/healthcheck', healthCheck)

app.get('/{*splat}', (req: Request, res: Response) => {
  res.sendFile(indexFilePath)
})

app.listen(port, () => {
  console.log(`Running at http://localhost:${port}`)
})
