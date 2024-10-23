import express, { Request, Response, Application, NextFunction } from 'express'
import path from 'node:path'
import helmet from 'helmet'
import RateLimit from 'express-rate-limit'

import {
  SecretsManagerClient,
  GetSecretValueCommand
} from '@aws-sdk/client-secrets-manager'
import * as https from 'https'

import { Response as FetchResponse } from 'node-fetch'
import { promises as fs } from 'fs'
// Noden built-in fetch ei tue client certin TLS-headerien lisäämistä
// Se ei myöskään toimi importin kanssa suoraan jostain syystä, minkä debuggaamisen jätin kesken.
// eslint-disable-next-line no-new-func
const importDynamic = new Function('modulePath', 'return import(modulePath)')
const fetch = async (...args: any[]): Promise<FetchResponse> => {
  const module = await importDynamic('node-fetch')
  return module.default(...args)
}

const memoize = <A extends any[], T>(
  fn: (...a: A) => T,
  getKey: (...a: A) => string
) => {
  const cache: Record<string, T> = {}
  return (...args: A): T => {
    const key = getKey(...args)
    if (cache[key]) {
      return cache[key]
    }
    const result = fn(...args)
    cache[key] = result
    return result
  }
}

interface AccessTokenData {
  access_token: string
  token_type: string
  expires_in: number
}

interface DummyResourceResponse {
  data: string
}

interface ClientCert {
  'fullchain.pem': string
  'privkey.pem': string
}

const app: Application = express()
app.set('trust proxy', 1)
const port = process.env.PORT || 7051

const koskiBackendHost =
  process.env.KOSKI_BACKEND_HOST || 'http://localhost:7021'

const authorizationServerUrl =
  process.env.AUTHORIZATION_SERVER_URL ||
  `${koskiBackendHost}/koski/api/omadata-oauth2/authorization-server`
const resourceServerUrl =
  process.env.RESOURCE_SERVER_URL ||
  `${koskiBackendHost}/koski/api/omadata-oauth2/resource-server`

const clientId = process.env.CLIENT_ID || 'oauth2client'

const enableMTLS = process.env.ENABLE_MTLS
  ? process.env.ENABLE_MTLS !== 'false'
  : true

const enableLocalMTLS = process.env.ENABLE_LOCAL_MTLS
  ? process.env.ENABLE_LOCAL_MTLS !== 'false'
  : false

// Käytössä, jos mTLS on päällä. Tämän perusteella client-side varmenne haetaan
// AWS secrets managerista.
const clientCertSecretName =
  process.env.CLIENT_CERT_SECRET_NAME || 'omadataoauth2sample-client-cert'

// Käytössä, jos LocalMTLS on päällä. Tämän perusteella client-side varmenne haetaan
// lokaalista tiedostosta
const localClientCertSecretFullchainFilename =
  process.env.CLIENT_CERT_SECRET_FULLCHAIN_FILENAME ||
  '../../koski-luovutuspalvelu/proxy/test/testca/certs/client.crt'
const localClientCertSecretPrivKeyFilename =
  process.env.CLIENT_CERT_SECRET_PRIVKEY_FILENAME ||
  '../../koski-luovutuspalvelu/proxy/test/testca/private/client.key'

// Käytössä, jos mTLS on disabloitu:
const username = process.env.USERNAME || 'oauth2client'
const password = process.env.PASSWORD || 'oauth2client'

const limiter = RateLimit({
  windowMs: 1 * 60 * 1000,
  limit: 1000,
  standardHeaders: 'draft-7',
  legacyHeaders: false
})

app.use(limiter)

app.use(helmet())

const staticFilesPath = path.resolve(__dirname, '../../client/build')

async function getClientCertSecret(): Promise<ClientCert> {
  if (enableMTLS) {
    const fullchainSecretName = `${clientCertSecretName}-fullchain`
    const privkeySecretName = `${clientCertSecretName}-privkey`

    console.log(`Getting client cert fullchain from \`${fullchainSecretName}`)
    const cert = await getSecret(fullchainSecretName)

    console.log(`Getting client cert privkey from \`${privkeySecretName}`)
    const privkey = await getSecret(privkeySecretName)

    return {
      'fullchain.pem': cert,
      'privkey.pem': privkey
    }
  } else if (enableLocalMTLS) {
    const cert = await getLocalMTLSFullchain()
    const privkey = await getLocalMTLSPrivkey()

    return {
      'fullchain.pem': cert,
      'privkey.pem': privkey
    }
  } else {
    throw Error('Not implemented')
  }
}

const getSecretsManagerClient = memoize(
  (): SecretsManagerClient => {
    if (enableMTLS) {
      return new SecretsManagerClient({
        region: 'eu-west-1'
      })
    } else {
      throw Error('Not implemented')
    }
  },
  () => 'secretsManager'
)

const getLocalMTLSFullchain = memoize(
  async (): Promise<string> => {
    console.log(
      `Reading client cert fullchain from \`${localClientCertSecretFullchainFilename}`
    )
    const cert = await fs.readFile(
      localClientCertSecretFullchainFilename,
      'utf8'
    )
    return cert
  },
  () => localClientCertSecretFullchainFilename
)

const getLocalMTLSPrivkey = memoize(
  async (): Promise<string> => {
    console.log(
      `Reading client cert privkey from \`${localClientCertSecretPrivKeyFilename}`
    )
    const privkey = await fs.readFile(
      localClientCertSecretPrivKeyFilename,
      'utf8'
    )
    return privkey
  },
  () => localClientCertSecretPrivKeyFilename
)

async function getSecret(secretName: string): Promise<string> {
  try {
    const response = await getSecretsManagerClient().send(
      new GetSecretValueCommand({
        SecretId: secretName,
        VersionStage: 'AWSCURRENT'
      })
    )
    if (response.SecretString) {
      return response.SecretString
    } else {
      throw new Error(`No data found in secret ${secretName}`)
    }
  } catch (error) {
    // For a list of exceptions thrown, see
    // https://docs.aws.amazon.com/secretsmanager/latest/apireference/API_GetSecretValue.html
    throw error
  }
}

app.use(express.static(staticFilesPath))

app.get(
  '/healthcheck',
  async (req: Request, res: Response, next: NextFunction) => {
    res.status(200).send('Ok')
  }
)

app.get('/api', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const accessTokenData = await fetchAccessToken(authorizationServerUrl)

    const data = await fetchData(
      accessTokenData.access_token,
      resourceServerUrl
    )
    res.json({ ...accessTokenData, ...data })
  } catch (err) {
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
  const response = await handleAccessTokenRequest(url)

  // TODO: Poista/karsi, ettei vahingossakaan salaisuuksia lokitu
  console.log(
    JSON.stringify(
      {
        operation: 'fetchAccessToken',
        'response.ok': response.ok,
        'response.status': response.status,
        'response.statusText': response.statusText,
        'response.headers.raw()': response.headers.raw(),
        "response.headers.get('content-type')":
          response.headers.get('content-type')
      },
      null,
      2
    )
  )

  if (!response.ok) {
    // TODO: Poista/karsi, ettei vahingossakaan salaisuuksia lokitu
    console.log(
      JSON.stringify(
        { 'fetchAccessToken response.text()': await response.text() },
        null,
        2
      )
    )
    throw new Error(response.statusText)
  }
  // TODO: parempi parsinta eikä vain typecastia, jos tämä koodi jää elämään.
  const jsonData: AccessTokenData = (await response.json()) as AccessTokenData

  // TODO: Poista/karsi, ettei vahingossakaan salaisuuksia lokitu
  console.log(
    JSON.stringify({ 'fetchAccessToken response.json()': jsonData }, null, 2)
  )

  return jsonData
}

async function handleAccessTokenRequest(url: string): Promise<FetchResponse> {
  if (enableMTLS || enableLocalMTLS) {
    return await handleAccessTokenRequestMTLS(url)
  } else {
    return await handleAccessTokenRequestBasicAuth(url)
  }
}
async function handleAccessTokenRequestMTLS(
  url: string
): Promise<FetchResponse> {
  const certs = await getClientCertSecret()

  const options = {
    cert: certs['fullchain.pem'],
    key: certs['privkey.pem']
  }

  const myHeaders: HeadersInit = {
    'content-type': 'application/x-www-form-urlencoded'
  }

  const mtlsAgent = new https.Agent(options)

  const body = new URLSearchParams({
    grant_type: 'authorization_code',
    code: 'foobar',
    code_verifier: 'barfoobar',
    client_id: clientId
  }).toString()

  // TODO: poista authorization coden debuggaus
  console.log(`POST to ${url} with body ${body}`)

  const response = await fetch(url, {
    method: 'POST',
    headers: myHeaders,
    body: body,
    agent: mtlsAgent
  })
  return response
}

async function handleAccessTokenRequestBasicAuth(
  url: string
): Promise<FetchResponse> {
  const base64Auth = Buffer.from(`${username}:${password}`).toString('base64')

  const myHeaders: HeadersInit = {
    Authorization: `Basic ${base64Auth}`,
    'content-type': 'application/x-www-form-urlencoded'
  }

  const response: FetchResponse = await fetch(url, {
    method: 'POST',
    headers: myHeaders,
    body: new URLSearchParams({
      grant_type: 'authorization_code',
      code: 'foobar',
      code_verifier: 'barfoobar',
      client_id: clientId
    }).toString()
  })
  return response
}

async function fetchData(
  accessToken: string,
  url: string
): Promise<DummyResourceResponse> {
  const response = await handleDataRequest(accessToken, url)

  // TODO: Poista/karsi, ettei vahingossakaan salaisuuksia lokitu
  console.log(
    JSON.stringify(
      {
        operation: 'fetchData',
        'response.ok': response.ok,
        'response.status': response.status,
        'response.statusText': response.statusText,
        'response.headers.raw()': response.headers.raw(),
        "response.headers.get('content-type')":
          response.headers.get('content-type')
      },
      null,
      2
    )
  )

  if (!response.ok) {
    // TODO: Poista/karsi, ettei vahingossakaan salaisuuksia lokitu
    console.log(
      JSON.stringify(
        { 'fetchData response.text()': await response.text() },
        null,
        2
      )
    )
    throw new Error(response.statusText)
  }
  // TODO: parempi parsinta eikä vain typecastia, jos tämä koodi jää elämään.
  const jsonData: DummyResourceResponse =
    (await response.json()) as DummyResourceResponse

  // TODO: Poista/karsi, ettei vahingossakaan salaisuuksia lokitu
  console.log(
    JSON.stringify({ 'fetchData response.json()': jsonData }, null, 2)
  )

  return jsonData
}

async function handleDataRequest(
  accessToken: string,
  url: string
): Promise<FetchResponse> {
  if (enableMTLS || enableLocalMTLS) {
    return await handleDataRequestMTLS(accessToken, url)
  } else {
    return await handleDataRequestBasicAuth(accessToken, url)
  }
}
async function handleDataRequestMTLS(
  accessToken: string,
  url: string
): Promise<FetchResponse> {
  const certs = await getClientCertSecret()

  const options = {
    cert: certs['fullchain.pem'],
    key: certs['privkey.pem']
  }

  const myHeaders: HeadersInit = {
    Authorization: 'Bearer ' + accessToken
  }

  const mtlsAgent = new https.Agent(options)

  // TODO: poista authorization coden debuggaus
  console.log(`POST to ${url}`)

  const response = await fetch(url, {
    method: 'POST',
    headers: myHeaders,
    agent: mtlsAgent
  })
  return response
}

async function handleDataRequestBasicAuth(
  accessToken: string,
  url: string
): Promise<FetchResponse> {
  const base64Auth = Buffer.from(`${username}:${password}`).toString('base64')

  const myHeaders: HeadersInit = {
    Authorization: `Basic ${base64Auth}`,
    'X-Auth': 'Bearer ' + accessToken
  }

  const response: FetchResponse = await fetch(url, {
    method: 'POST',
    headers: myHeaders
  })
  return response
}
