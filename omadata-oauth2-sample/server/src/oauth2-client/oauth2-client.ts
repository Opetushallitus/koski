import { getOAuthClientConfig } from '../config/oauth2-client-config.js'
import * as client from 'openid-client'
import { resourceEndpointUrl } from '../config/koski-backend-config.js'
import { Request } from 'express'
import { URLSearchParams } from 'url'
import { redirectUri } from '../apiroutes/openid-api-test.js'

export async function buildAuthorizationUrl(
  code_verifier: string,
  state: string,
  scope: string,
  redirectUriOverride: string | undefined = undefined,
  codeChallengeOverride: string | undefined = undefined
) {
  const config = await getOAuthClientConfig()

  const code_challenge: string =
    codeChallengeOverride ||
    (await client.calculatePKCECodeChallenge(code_verifier))

  const parameters: Record<string, string> = {
    redirect_uri: redirectUriOverride || redirectUri,
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

  const reqUrl = new URL(
    `${req.protocol}://${req.get('host')}${req.originalUrl}`
  )
  reqUrl.search = new URLSearchParams(req.body).toString()

  console.log(`[fetchAccessToken] Token endpoint: ${config.serverMetadata().token_endpoint}`)

  try {
    const tokens: client.TokenEndpointResponse =
      await client.authorizationCodeGrant(config, reqUrl, {
        pkceCodeVerifier: code_verifier,
        expectedState: state
      })

    const token = tokens.access_token
    return token
  } catch (err) {
    console.error('[fetchAccessToken] Failed:', err)
    throw err
  }
}

export async function fetchData(accessToken: string): Promise<unknown> {
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
