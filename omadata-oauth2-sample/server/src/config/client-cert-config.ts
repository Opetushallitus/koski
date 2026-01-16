import {
  GetSecretValueCommand,
  SecretsManagerClient
} from '@aws-sdk/client-secrets-manager'
import { memoize } from '../util/memoize.js'
import { promises as fs } from 'fs'
import path from 'node:path'

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
// Fetch certificate content from local files

const defaultCertBase =
  process.env.CLIENT_CERT_BASE || path.resolve(process.cwd(), 'testca')

const localClientCertSecretFullchainFilename =
  process.env.CLIENT_CERT_SECRET_FULLCHAIN_FILENAME ||
  path.join(defaultCertBase, 'certs/client.crt')
const localClientCertSecretPrivKeyFilename =
  process.env.CLIENT_CERT_SECRET_PRIVKEY_FILENAME ||
  path.join(defaultCertBase, 'private/client.key')

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

const localCACertFilename =
  process.env.LOCAL_CA_CERT_FILENAME ||
  path.join(defaultCertBase, 'certs/root-ca.crt')

export const getLocalCACert = memoize(
  async (): Promise<string> => {
    return await fs.readFile(localCACertFilename, 'utf8')
  },
  () => localCACertFilename
)

///////////////////////////////////////////////////////////////////
// Fetch certificate content from AWS secrets manager

const clientCertSecretName =
  process.env.CLIENT_CERT_SECRET_NAME || 'omadataoauth2sample-client-cert'

const getSecretsManagerClient = memoize(
  async (): Promise<SecretsManagerClient> => {
    return new SecretsManagerClient({
      region: 'eu-west-1'
    })
  },
  () => 'secretsManager'
)
async function getSecret(secretName: string): Promise<string> {
  const response = await (
    await getSecretsManagerClient()
  ).send(
    new GetSecretValueCommand({
      SecretId: secretName,
      VersionStage: 'AWSCURRENT'
    })
  )
  if (response.SecretString) {
    return response.SecretString
  }

  throw new Error(`No data found in secret ${secretName}`)
}
