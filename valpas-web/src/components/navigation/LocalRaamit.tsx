import bem from "bem-ts"
import React, { useState } from "react"
import { ApiResponse } from "../../api/apiFetch"
import { useApiMethod } from "../../api/apiHooks"
import { clearMockData, resetMockData } from "../../api/testApi"
import {
  getLanguage,
  Language,
  setLanguage,
  supportedLanguages,
  T,
} from "../../i18n/i18n"
import { CurrentUser } from "../../state/auth"
import { joinClassNames } from "../../utils/classnames"
import "./LocalRaamit.less"

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
      <TestApiButtons />
      <UserInfo user={user} currentLanguage={getLanguage()} />
    </div>
  )
}

const TestApiButtons = () => {
  const [tarkasteluPäivä, setTarkasteluPäivä] = useState("2021-09-05")

  return (
    <>
      <TestApiButton
        fetchFunc={resetMockData}
        id={"resetMockData"}
        title={"Reset mock data"}
      />
      <SimpleTextField
        value={tarkasteluPäivä}
        onChange={(value) => {
          console.log(value)
          setTarkasteluPäivä(value)
        }}
      />
      <TestApiButton
        fetchFunc={clearMockData}
        id={"clearMockData"}
        title={"Clear mock data"}
      />
    </>
  )
}

type TestApiButtonProps = {
  fetchFunc: () => Promise<ApiResponse<string>>
  id: string
  title: string
}

const TestApiButton = ({ fetchFunc, id, title }: TestApiButtonProps) => {
  const apiFetch = useApiMethod(fetchFunc)

  return (
    <button
      className={b("testapibutton")}
      id={id}
      onClick={() => {
        apiFetch.clear()
        return apiFetch.call()
      }}
    >
      {title}{" "}
      <span id={id + "State"} className={b("testapistate")}>
        {apiFetch.state}
      </span>
    </button>
  )
}

export type SimpleTextFieldProps = {
  value: string
  onChange: (value: string) => void
  id?: string
}

export const SimpleTextField = (props: SimpleTextFieldProps) => (
  <input
    id={props.id}
    className={b("input")}
    value={props.value}
    onChange={(event) => props.onChange(event.target.value)}
  />
)

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
  document.location.href = "/koski/valpas/logout"
}
