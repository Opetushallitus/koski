import * as client from 'openid-client'
import { Configuration } from 'openid-client'
import { memoize } from '../util/memoize.js'
import { getClientCertSecret } from './client-cert-config.js'
import * as undici from 'undici'
import { koskiBackendHost } from './koski-backend-config.js'

const authorizationEndpointUrl = `${koskiBackendHost}/koski/omadata-oauth2/authorize`
const tokenEndpointUrl =
  process.env.TOKEN_ENDPOINT_URL ||
  `${koskiBackendHost}/koski/api/omadata-oauth2/authorization-server`
const clientId = process.env.CLIENT_ID || 'oauth2client'
const serverMetadata: client.ServerMetadata = {
  issuer: 'KOSKI',

  authorization_endpoint: authorizationEndpointUrl,
  response_types_supported: ['code'],
  response_modes_supported: ['form_post'],
  scopes_supported: [
    'HENKILOTIEDOT_NIMI',
    'HENKILOTIEDOT_SYNTYMAAIKA',
    'HENKILOTIEDOT_HETU',
    'HENKILOTIEDOT_KAIKKI_TIEDOT',
    'OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT',
    'OPISKELUOIKEUDET_AKTIIVISET_JA_PAATTYNEET_OPINNOT',
    'OPISKELUOIKEUDET_KAIKKI_TIEDOT'
  ],
  code_challenge_methods_supported: ['S256'],

  token_endpoint: tokenEndpointUrl,
  token_endpoint_auth_methods_supported: ['tls_client_auth'],
  token_endpoint_auth_signing_alg_values_supported: undefined,
  grant_types_supported: ['authorization_code'],

  tls_client_certificate_bound_access_tokens: false,

  service_documentation:
    'https://github.com/Opetushallitus/koski/blob/master/documentation/oauth2toteutus.md'
}
const clientMetadata: client.ClientMetadata = {
  client_id: clientId,
  use_mtls_endpoint_aliases: false
}
export const getOAuthClientConfig = memoize(
  async (): Promise<Configuration> => {
    let config = new client.Configuration(
      serverMetadata,
      clientId,
      clientMetadata,
      client.TlsClientAuth()
    )

    const certs = await getClientCertSecret()

    const options = {
      cert: certs['fullchain.pem'],
      key: certs['privkey.pem']
    }

    const agent = new undici.Agent({ connect: options })
    config[client.customFetch] = (...args) =>
      // @ts-expect-error
      undici.fetch(args[0], { ...args[1], dispatcher: agent })

    client.allowInsecureRequests(config)

    return config
  },
  () => 'oauthClientConfig'
)
