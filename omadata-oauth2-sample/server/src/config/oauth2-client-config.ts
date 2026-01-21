import * as client from 'openid-client'
import { Configuration } from 'openid-client'
import { memoize } from '../util/memoize.js'
import {
  enableLocalMTLS,
  getClientCertSecret,
  getLocalCACert
} from './client-cert-config.js'
import * as undici from 'undici'
import { koskiBackendHost } from './koski-backend-config.js'

const clientId = process.env.CLIENT_ID || 'oauth2client'
const clientMetadata: client.ClientMetadata = {
  client_id: clientId,
  use_mtls_endpoint_aliases: false
}
export const getOAuthClientConfig = memoize(
  async (): Promise<Configuration> => {
    console.log(`[getOAuthClientConfig] Koski backend: ${koskiBackendHost}`)
    console.log(`[getOAuthClientConfig] Local mTLS: ${enableLocalMTLS}`)

    const discoveryOptions = enableLocalMTLS
      ? {
          execute: [client.allowInsecureRequests]
        }
      : undefined

    const discoveryUrl = `${koskiBackendHost}/koski/omadata-oauth2/.well-known/oauth-authorization-server`
    console.log(`[getOAuthClientConfig] Discovery URL: ${discoveryUrl}`)

    const config = await client.discovery(
      new URL(discoveryUrl),
      clientId,
      clientMetadata,
      client.TlsClientAuth(),
      discoveryOptions
    )

    console.log(`[getOAuthClientConfig] Token endpoint: ${config.serverMetadata().token_endpoint}`)

    const certs = await getClientCertSecret()
    console.log(`[getOAuthClientConfig] Client cert loaded: ${certs['fullchain.pem']?.substring(0, 50)}...`)

    const ca = enableLocalMTLS ? await getLocalCACert() : undefined

    const options: undici.Agent.Options['connect'] = {
      cert: certs['fullchain.pem'],
      key: certs['privkey.pem'],
      ...(ca && { ca })
    }

    const agent = new undici.Agent({ connect: options })
    config[client.customFetch] = (...args) =>
      // @ts-expect-error dispatcher is supported but not in undici's fetch types here
      undici.fetch(args[0], { ...args[1], dispatcher: agent })

    if (enableLocalMTLS) {
      client.allowInsecureRequests(config)
    }

    return config
  },
  () => 'oauthClientConfig'
)
