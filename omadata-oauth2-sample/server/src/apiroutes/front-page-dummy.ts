import express, { NextFunction, Request, Response, Router } from 'express'
import { fetchData } from '../oauth2-client/oauth2-client.js'

const router: Router = express.Router()

router.get('/', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const data = await fetchData('dummy-access-token')
    res.json({ ...data })
  } catch (err) {
    next(err)
  }
})

export default router
