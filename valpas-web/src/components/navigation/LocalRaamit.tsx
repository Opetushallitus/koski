import bem from "bem-ts"
import React, { useEffect, useState } from "react"
import { ApiResponse } from "../../api/apiFetch"
import { useApiMethod, useApiOnce } from "../../api/apiHooks"
import { mapSuccess } from "../../api/apiUtils"
import {
  clearMockData,
  FixtureState,
  getMockStatus,
  resetMockDataToDate,
} from "../../api/testApi"
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
  const [state, setState] = useState<FixtureState | null>(null)
  const current = useApiOnce(getMockStatus)
  useEffect(() => {
    mapSuccess(current, setState)
  }, [current])

  const setTarkasteluPäivä = (tarkasteluPäivä: string) =>
    state &&
    setState({
      ...state,
      rajapäivät: {
        ...state.rajapäivät,
        tarkasteluPäivä,
      },
    })

  return (
    state && (
      <>
        <Fixture>{state.fixture}</Fixture>
        <TestApiButton
          fetchFunc={resetMockDataToDate(state.rajapäivät.tarkasteluPäivä)}
          id={"resetMockData"}
          title={"Use Valpas mock data"}
          onStateUpdated={() => current.call()}
        />
        <SimpleTextField
          value={state.rajapäivät.tarkasteluPäivä}
          onChange={setTarkasteluPäivä}
          id={"tarkasteluPäivä"}
        />
        {state.fixture === "VALPAS" && (
          <TestApiButton
            fetchFunc={clearMockData}
            id={"clearMockData"}
            title={"Unload Valpas mock data"}
            onStateUpdated={() => current.call()}
          />
        )}
      </>
    )
  )
}

type TestApiButtonProps = {
  fetchFunc: () => Promise<ApiResponse<FixtureState>>
  id: string
  title: string
  onStateUpdated: () => void
}

const TestApiButton = ({
  fetchFunc,
  id,
  title,
  onStateUpdated,
}: TestApiButtonProps) => {
  const apiFetch = useApiMethod(fetchFunc)

  return (
    <button
      className={b("testapibutton")}
      id={id}
      onClick={async () => {
        apiFetch.clear()
        await apiFetch.call()
        onStateUpdated()
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

const Fixture = ({ children, ...rest }: React.HTMLAttributes<HTMLElement>) => (
  <span {...rest} className={b("fixture")}>
    Fixture:{" "}
    <span id="current-fixture" className={b("fixturevalue")}>
      {children}
    </span>
  </span>
)

const logout = () => {
  document.location.href = "/koski/valpas/logout"
}
