import { getOAuthClientConfig } from '../config/oauth-client-config.js'
import * as client from 'openid-client'
import { resourceEndpointUrl } from '../config/koski-backend-config.js'
import { Request } from 'express'
import { URLSearchParams } from 'url'
import { redirectUri } from '../apiroutes/openid-api-test.js'

export async function buildAuthorizationUrl(
  code_verifier: string,
  state: string,
  scope: string
) {
  const config = await getOAuthClientConfig()

  const code_challenge: string =
    await client.calculatePKCECodeChallenge(code_verifier)

  let parameters: Record<string, string> = {
    redirect_uri: redirectUri,
    scope,
    code_challenge,
    code_challenge_method: 'S256',
    state,
    response_mode: 'form_post'
  }

  return client.buildAuthorizationUrl(config, parameters)
}

export async function fetchAccessToken(
  req: Request,
  code_verifier: string,
  state: string
) {
  const config = await getOAuthClientConfig()

  var reqUrl = new URL(`${req.protocol}://${req.get('host')}${req.originalUrl}`)
  reqUrl.search = new URLSearchParams(req.body).toString()

  const tokens: client.TokenEndpointResponse =
    await client.authorizationCodeGrant(config, reqUrl, {
      pkceCodeVerifier: code_verifier,
      expectedState: state
    })

  const token = tokens.access_token
  return token
}

export async function fetchData(accessToken: string): Promise<any> {
  const config = await getOAuthClientConfig()

  const response = await client.fetchProtectedResource(
    config,
    accessToken,
    new URL(resourceEndpointUrl),
    'POST'
  )

  if (!response.ok) {
    throw new Error(response.statusText)
  }

  return await response.json()
}
