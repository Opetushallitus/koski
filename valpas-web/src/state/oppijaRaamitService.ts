import { OppijaRaamitUser } from "./apitypes/appConfiguration"
import { kansalainenOmatTiedotPath } from "./kansalainenPaths"

export type OppijaRaamitService = {
  getUser: () => Promise<OppijaRaamitUser>
  login: () => void
  logout: () => void
}

window.Service = {
  getUser() {
    return window.oppijaRaamitUser
      ? Promise.resolve(window.oppijaRaamitUser)
      : Promise.reject()
  },

  login() {
    document.location.href = kansalainenOmatTiedotPath.href("/koski/valpas/v2")
  },

  logout() {
    document.location.href = "/koski/valpas/logout"
  },
}
