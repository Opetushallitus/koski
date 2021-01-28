import bem from "bem-ts"
import React from "react"
import {
  getLanguage,
  Language,
  setLanguage,
  supportedLanguages,
  T,
} from "../../i18n/i18n"
import { CurrentUser } from "../../state/auth"
import "./LocalRaamit.less"
import { joinClassNames } from "../../utils/classnames"

const b = bem("localraamit")

export type LocalRaamitProps = {
  user: CurrentUser
}

export default ({ user }: LocalRaamitProps) => {
  return (
    <div id="localraamit" className={b()}>
      <div id="logo">Opintopolku.fi</div>
      <h1>
        <a href="/valpas/">
          <T id="title__Valpas" />
        </a>
      </h1>
      <UserInfo user={user} currentLanguage={getLanguage()} />
    </div>
  )
}

type UserInfoProps = {
  user: CurrentUser
  currentLanguage: Language
}

const UserInfo = ({ user, currentLanguage }: UserInfoProps) => (
  <div className={b("userinfo")}>
    {user !== "unauthorized" && user !== "forbidden" && (
      <span className={b("username")}>{user.name}</span>
    )}
    <LanguageButtons currentLanguage={currentLanguage} />
    {user !== "unauthorized" && (
      <button
        className={b("logoutbutton")}
        id="logout"
        onClick={logout}
        tabIndex={0}
      >
        <T id={"localraamit__logout"} />
      </button>
    )}
  </div>
)

type LanguageButtonsProps = {
  currentLanguage: Language
}

const LanguageButtons = ({ currentLanguage }: LanguageButtonsProps) => (
  <>
    {supportedLanguages.map((language) => (
      <button
        className={joinClassNames(
          b("languagebutton"),
          language === currentLanguage ? b("currentlanguage") : ""
        )}
        id={language}
        key={language}
        onClick={() => setLanguage(language)}
      >
        {language}
      </button>
    ))}
  </>
)

const logout = () => {
  document.location.href = "/valpas/logout"
}
