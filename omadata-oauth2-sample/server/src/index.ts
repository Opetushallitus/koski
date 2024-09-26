import express, {Request, Response, Application, NextFunction} from 'express'
import path from "node:path"
import helmet from "helmet"
import RateLimit from 'express-rate-limit'

import {
  SecretsManagerClient,
  GetSecretValueCommand,
} from "@aws-sdk/client-secrets-manager"
import * as https from "https";

// Noden built-in fetch ei tue client certin TLS-headerien lisäämistä
// Se ei myöskään toimi importin kanssa suoraan jostain syystä, minkä debuggaamisen jätin kesken.
// eslint-disable-next-line no-new-func
const importDynamic = new Function('modulePath', 'return import(modulePath)');
const fetch = async (...args:any[]) => {
  const module = await importDynamic('node-fetch');
  return module.default(...args);
};

interface AccessTokenData {
  access_token: string,
  token_type: string,
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
const port = process.env.PORT || 7051

const authorizationServerUrl = process.env.AUTHORIZATION_SERVER_URL || 'http://localhost:7021/koski/api/luovutuspalvelu/omadata-oauth2/authorization-server'
const resourceServerUrl = process.env.RESOURCE_SERVER_URL || 'http://localhost:7021/koski/api/luovutuspalvelu/omadata-oauth2/resource-server'

// Käytössä, jos mTLS on päällä:
const clientCertSecretName = process.env.CLIENT_CERT_SECRET_NAME || "omadataoauth2sample-client-cert"
const enableMTLS = process.env.ENABLE_MTLS ? process.env.ENABLE_MTLS !== 'false' : true

// Käytössä, jos mTLS on disabloitu:
const username = process.env.USERNAME || 'oauth2client'
const password = process.env.PASSWORD || 'oauth2client'

const limiter = RateLimit({
  windowMs: 1 * 60 * 1000,
  limit: 1000,
  standardHeaders: 'draft-7',
  legacyHeaders: false
});

app.use(limiter)

app.use(helmet())

const staticFilesPath = path.resolve(__dirname, '../../client/build')

async function getClientCertSecret() : Promise<ClientCert> {
  const fullchainSecretName = clientCertSecretName+"-fullchain"
  const privkeySecretName = clientCertSecretName+"-privkey"

  console.log(`Getting client cert fullchain from \`${fullchainSecretName}`)
  const cert= await getSecret(fullchainSecretName)

  console.log(`Getting client cert privkey from \`${privkeySecretName}`)
  const privkey = await getSecret(privkeySecretName)

  return {
    'fullchain.pem': cert,
    'privkey.pem': privkey
  }
}

async function getSecret(secretName: string): Promise<string> {
  try {
    const client = new SecretsManagerClient({
      region: "eu-west-1",
    });

    const response = await client.send(
      new GetSecretValueCommand({
        SecretId: secretName,
        VersionStage: "AWSCURRENT",
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

app.get('/api', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const accessTokenData = await fetchAccessToken(authorizationServerUrl)

    // TODO: Kun reitti valmis
    // const data = await fetchData(accessTokenData.access_token, resourceServerUrl)
    // res.json({...accessTokenData, ...data})

    res.json({...accessTokenData})
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
  const response = enableMTLS ? await fetchAccessTokenMTLS(url) : await fetchAccessTokenBasicAuth(url)

  // TODO: Poista/karsi, ettei vahingossakaan salaisuuksia lokitu
  console.log('response.ok', response.ok)
  console.log('response.status', response.status)
  console.log('response.statusText', response.statusText)
  console.log('response.headers.raw()', response.headers.raw())
  console.log('response.headers.get(\'content-type\')', response.headers.get('content-type'))

  if (!response.ok) {
    console.log('response.text()', await response.text())
    throw new Error(response.statusText)
  }
  const jsonData: AccessTokenData = <AccessTokenData>await response.json()
  console.log('response.json()', jsonData)

  return jsonData
}

async function fetchAccessTokenMTLS(url: string) {
  const certs = await getClientCertSecret()

  const options = {
    cert: certs["fullchain.pem"],
    key: certs["privkey.pem"]
  }

  const myHeaders: HeadersInit = {
    'content-type': 'application/x-www-form-urlencoded'
  }

  const mtlsAgent = new https.Agent(options)

  const body = new URLSearchParams({grant_type: 'authorization_code', code: 'foobar'}).toString()

  // TODO: poista authorization coden debuggaus
  console.log(`POST to ${url} with body ${body}`)

  const response = await fetch(url, {
    method: "POST",
    headers: myHeaders,
    body: body,
    agent: mtlsAgent
  })
  return response;
}

async function fetchAccessTokenBasicAuth(url: string) {
  const myHeaders: HeadersInit = {
    'Authorization': 'Basic ' + Buffer.from(username + ":" + password).toString('base64'),
    'content-type': 'application/x-www-form-urlencoded'
  }

  const response = await fetch(url, {
    method: "POST",
    headers: myHeaders,
    body: new URLSearchParams({grant_type: 'authorization_code', code: 'foobar'}).toString(),
  })
  return response;
}

// TODO: mTLS-autentikointi (vaatii sitten myös Authorization => X-Auth -headermuutoksen toteuttamisen luovutuspalvelun nginx:ään.
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
