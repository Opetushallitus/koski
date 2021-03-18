import { Oid } from "./types"

export const externalHakemuspalvelu = `/koski/valpas/api/redirect/hakemus/`

export const externalHakemussivu = (oid: Oid) =>
  `/koski/valpas/api/redirect/hakemus/${oid}`
