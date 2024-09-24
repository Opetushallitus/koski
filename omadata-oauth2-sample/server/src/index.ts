import express, {Request, Response, Application, NextFunction} from 'express'
import path from "node:path"
import helmet from "helmet"
import RateLimit from 'express-rate-limit'

interface AccessTokenData {
  access_token: string,
  token_type: string,
  expires_in: number
}

interface DummyResourceResponse {
  data: string
}

const app: Application = express()
const port = process.env.PORT || 7051

// TODO: Nämä voisi ympäristössä lukea AWS:stä, esim. parameter storesta
const username = process.env.USERNAME || 'oauth2client'
const password = process.env.PASSWORD || 'oauth2client'
const authorizationServerUrl = process.env.AUTHORIZATION_SERVER_URL || 'http://localhost:7021/koski/api/omadata-oauth2/authorization-server'
const resourceServerUrl = process.env.RESOURCE_SERVER_URL || 'http://localhost:7021/koski/api/omadata-oauth2/resource-server'

const limiter = RateLimit({
  windowMs: 1 * 60 * 1000,
  limit: 1000,
  standardHeaders: 'draft-7',
  legacyHeaders: false
});

app.use(limiter)

app.use(helmet())

const staticFilesPath = path.resolve(__dirname, '../../client/build')
app.use(express.static(staticFilesPath))

app.get('/api', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const accessTokenData = await fetchAccessToken(authorizationServerUrl)

    const data = await fetchData(accessTokenData.access_token, resourceServerUrl)

    res.json({...accessTokenData, ...data})
  } catch (err){
    next(err)
  }
})

app.get('*', (req: Request, res: Response) => {
  res.sendFile(path.resolve(__dirname, './client/build', 'index.html'))
})

app.listen(port, () => {
  console.log(`Running at http://localhost:${port}`)
})

async function fetchAccessToken(url: string): Promise<AccessTokenData> {
  const response = await fetch(url, {
    method: "POST",
    headers: {
      // TODO: Autentikointi mTLS:llä ympäristöissä.
      'Authorization': 'Basic ' + Buffer.from(username + ":" + password).toString('base64'),
      'content-type': 'application/x-www-form-urlencoded'
    },
    body: new URLSearchParams({grant_type: 'authorization_code', code: 'foobar'}).toString()
  })

  if (!response.ok) {
    throw new Error(response.statusText)
  }
  const jsonData: AccessTokenData = await response.json()

  return jsonData
}

async function fetchData(accessToken: string, url: string): Promise<DummyResourceResponse> {
  const response = await fetch(url, {
    method: "POST",
    // TODO: Autentikointi mTLS:llä ympäristöissä, Bearer tokenin välitys silloin Authorization-headerissä
    headers: {
      'Authorization': 'Basic ' + Buffer.from(username + ":" + password).toString('base64'),
      'X-Auth': 'Bearer ' + accessToken
    }
  })

  if (!response.ok) {
    throw new Error(response.statusText)
  }

  return await response.json()
}
