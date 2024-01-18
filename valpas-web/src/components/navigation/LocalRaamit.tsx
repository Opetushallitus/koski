import bem from "bem-ts"
import React, { useEffect, useState } from "react"
import { ApiResponse } from "../../api/apiFetch"
import { useApiMethod, useApiOnce, useOnApiSuccess } from "../../api/apiHooks"
import { mapSuccess } from "../../api/apiUtils"
import {
  clearMockData,
  FixtureState,
  getMockStatus,
  loadRaportointikanta,
  resetMockDataToDate,
} from "../../api/testApi"
import { getLanguage, setLanguage, T } from "../../i18n/i18n"
import { supportedLanguages } from "../../state/apitypes/appConfiguration"
import { CurrentUser } from "../../state/auth"
import { Language } from "../../state/common"
import { useSafeState } from "../../state/useSafeState"
import { joinClassNames } from "../../utils/classnames"
import "./LocalRaamit.less"

const b = bem("localraamit")

export type LocalRaamitProps = {
  user: CurrentUser
  kansalainen?: boolean
}

export default ({ user, kansalainen }: LocalRaamitProps) => {
  return (
    <div id="localraamit" className={b({ kansalainen })}>
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
  const [force, setForce] = useState(false)
  const current = useApiOnce(getMockStatus)
  useEffect(() => {
    mapSuccess(current, setState)
  }, [current])

  const setTarkastelupäivä = (tarkastelupäivä: string) =>
    state &&
    setState({
      ...state,
      tarkastelupäivä: tarkastelupäivä,
    })

  return (
    state && (
      <>
        <Fixture>{state.fixture}</Fixture>
        <TestApiButton
          fetchFunc={resetMockDataToDate(state.tarkastelupäivä, force)}
          id={"resetMockData"}
          title={"Load mocks"}
          onStateUpdated={() => current.call()}
        />
        <SimpleTextField
          value={state.tarkastelupäivä}
          onChange={setTarkastelupäivä}
          id={"tarkastelupäivä"}
        />
        <label>
          <input
            type="checkbox"
            checked={force}
            onChange={(event) => setForce(event.target.checked)}
          />
          Full reset
        </label>
        <TestApiButton
          fetchFunc={loadRaportointikanta}
          id="loadRaportointikanta"
          title="Load raportointikanta"
        />
        {state.fixture === "VALPAS" && (
          <TestApiButton
            fetchFunc={clearMockData}
            id={"clearMockData"}
            title={"Reset to Koski data"}
            onStateUpdated={() => current.call()}
          />
        )}
      </>
    )
  )
}

type TestApiButtonProps<T> = {
  fetchFunc: () => Promise<ApiResponse<T>>
  id: string
  title: string
  onStateUpdated?: () => void
}

const TestApiButton = <T,>({
  fetchFunc,
  id,
  title,
  onStateUpdated,
}: TestApiButtonProps<T>) => {
  const apiFetch = useApiMethod(fetchFunc)
  const [successHidden, hideSuccess] = useSafeState(false)

  useOnApiSuccess(apiFetch, () => {
    setTimeout(() => hideSuccess(true), 3000)
  })

  return (
    <button
      className={b("testapibutton")}
      id={id}
      onClick={async () => {
        hideSuccess(false)
        apiFetch.clear()
        await apiFetch.call()
        onStateUpdated?.()
      }}
    >
      {title}{" "}
      <span id={id + "State"} className={b("testapistate")}>
        {apiFetch.state !== "initial" &&
        (apiFetch.state !== "success" || !successHidden)
          ? apiFetch.state
          : ""}
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
    value={props.value || ""}
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
          language === currentLanguage ? b("currentlanguage") : "",
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
  <span {...rest} className={b("fixture", { warning: children !== "VALPAS" })}>
    Fixture:{" "}
    <span id="current-fixture" className={b("fixturevalue")}>
      {children}
    </span>
  </span>
)

const logout = () => {
  document.location.href = "/koski/valpas/logout"
}
