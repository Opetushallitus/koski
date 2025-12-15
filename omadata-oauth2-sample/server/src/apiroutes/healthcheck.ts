import express, { Request, Response, Router } from 'express'

const router: Router = express.Router()

router.get('/', async (req: Request, res: Response) => {
  res.status(200).send('Ok')
})

export default router
