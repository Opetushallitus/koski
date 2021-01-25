import bem from "bem-ts"
import React from "react"
import { T } from "../../i18n/i18n"
import "./LocalRaamit.less"
import { CurrentUser } from "../../state/auth"
import { logError } from "~utils/log"

const b = bem("localraamit")

export type LocalRaamitProps = {
  user: CurrentUser
}

const logout = () => {
  // TODO: Toteuta
  logError("Logout ei toteutettu")
  // document.location = '/valpas/logout'
}

const UserInfo = ({ user }: LocalRaamitProps) => (
  <div className="user-info">
    {user !== "unauthorized" && user !== "forbidden" && (
      <span className="name">{user.name}</span>
    )}
    {user !== "unauthorized" && (
      <button
        className="logout-button"
        id="logout"
        onClick={logout}
        tabIndex={0}
      >
        <T id={"localraamit__logout"} />
      </button>
    )}
  </div>
)

export default ({ user }: LocalRaamitProps) => {
  return (
    <div id="localraamit" className={b()}>
      <div id="logo">Opintopolku.fi</div>
      <h1>
        <a href="/valpas/">
          <T id="title__Valpas" />
        </a>
      </h1>
      <UserInfo user={user} />
    </div>
  )
}
