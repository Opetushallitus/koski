import {
  GetSecretValueCommand,
  SecretsManagerClient
} from '@aws-sdk/client-secrets-manager'
import { memoize } from '../util/memoize.js'
import { promises as fs } from 'fs'

interface ClientCertConfig {
  'fullchain.pem': string
  'privkey.pem': string
}

export const enableLocalMTLS = process.env.ENABLE_LOCAL_MTLS
  ? process.env.ENABLE_LOCAL_MTLS !== 'false'
  : false

export async function getClientCertSecret(): Promise<ClientCertConfig> {
  if (enableLocalMTLS) {
    const cert = await getLocalMTLSFullchain()
    const privkey = await getLocalMTLSPrivkey()

    return {
      'fullchain.pem': cert,
      'privkey.pem': privkey
    }
  } else {
    const fullchainSecretName = `${clientCertSecretName}-fullchain`
    const privkeySecretName = `${clientCertSecretName}-privkey`

    const cert = await getSecret(fullchainSecretName)

    const privkey = await getSecret(privkeySecretName)

    return {
      'fullchain.pem': cert,
      'privkey.pem': privkey
    }
  }
}

///////////////////////////////////////////////////////////////////
// Fetch certiticate content from local files

const localClientCertSecretFullchainFilename =
  process.env.CLIENT_CERT_SECRET_FULLCHAIN_FILENAME ||
  '../../koski-luovutuspalvelu/proxy/test/testca/certs/client.crt'
const localClientCertSecretPrivKeyFilename =
  process.env.CLIENT_CERT_SECRET_PRIVKEY_FILENAME ||
  '../../koski-luovutuspalvelu/proxy/test/testca/private/client.key'

const getLocalMTLSFullchain = memoize(
  async (): Promise<string> => {
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
    const privkey = await fs.readFile(
      localClientCertSecretPrivKeyFilename,
      'utf8'
    )
    return privkey
  },
  () => localClientCertSecretPrivKeyFilename
)

///////////////////////////////////////////////////////////////////
// Fetch certificate content from AWS secrets manager

const clientCertSecretName =
  process.env.CLIENT_CERT_SECRET_NAME || 'omadataoauth2sample-client-cert'

const getSecretsManagerClient = memoize(
  (): SecretsManagerClient => {
    return new SecretsManagerClient({
      region: 'eu-west-1'
    })
  },
  () => 'secretsManager'
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
