import express, { NextFunction, Request, Response, Router } from 'express'
import * as client from 'openid-client'
import {
  buildAuthorizationUrl,
  fetchAccessToken,
  fetchData
} from '../oauth2-client/oauth2-client.js'

const router: Router = express.Router()

const code_verifier: string = client.randomPKCECodeVerifier()
const state = 'state-placeholder'

const scope: string =
  'HENKILOTIEDOT_NIMI HENKILOTIEDOT_SYNTYMAAIKA HENKILOTIEDOT_HETU OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT'

router.get('/', async (req: Request, res: Response, next: NextFunction) => {
  try {
    let redirectTo = await buildAuthorizationUrl(code_verifier, state, scope)

    res.redirect(redirectTo.href)
  } catch (err) {
    next(err)
  }
})

const sampleAppUrl = process.env.SAMPLE_APP_URL || 'http://localhost:7051'

// Huomaa, että tämän saman URI:n pitää löytyä Koski-backendin konffeista
export const redirectUri: string = `${sampleAppUrl}/api/openid-api-test/form-post-response-cb`

router.post(
  '/form-post-response-cb',
  async (req: Request, res: Response, next: NextFunction) => {
    try {
      const token = await fetchAccessToken(req, code_verifier, state)

      const protectedResource = await fetchData(token)

      res.json(protectedResource)
      // TODO: TOR-2210: jos tehtäisiin oikeaa clientia, niin tässä kohtaa kuuluisi vielä
      //  redirectata clientin näkymään (mikä vaatii CSP:n höllennyksen, tavan välittää dataa, yms.)
    } catch (err) {
      next(err)
    }
  }
)

export default router
