import React, { useEffect, useState } from "react"
import * as E from "fp-ts/Either"
import { fetchLogin, Login } from "../api/api"
import { SubmitButton } from "../components/buttons/SubmitButton"
import { Card, CardBody, CardHeader } from "../components/containers/cards"
import { Page } from "../components/containers/Page"
import { Form } from "../components/forms/Form"
import { TextField } from "../components/forms/TextField"
import { t, T } from "../i18n/i18n"
import { setLogin } from "../state/auth"
import { ApiSuccess } from "../api/apiFetch"
import { formSubmitHandler } from "../utils/eventHandlers"
import { Error } from "../components/typography/error"
import { renderResponse, useApiState } from "../api/apiReact"

const processLogin = E.map((login: ApiSuccess<Login>) => {
  setLogin(login.data.session)
  location.reload()
})

export const LoginApp = () => {
  const [username, setUsername] = useState("")
  const [password, setPassword] = useState("")
  const [loginResponse, setLoginResponse] = useApiState<Login>()

  const submitDisabled = username === "" || password === ""

  const login = formSubmitHandler(async () => {
    if (!submitDisabled) {
      setLoginResponse(await fetchLogin(username, password))
    }
  })

  useEffect(() => {
    loginResponse && processLogin(loginResponse)
  }, [loginResponse])

  return (
    <Page>
      <Card>
        <CardHeader>
          <T id="login__otsikko" />
        </CardHeader>
        <CardBody>
          <Form onSubmit={login} onChange={() => setLoginResponse(null)}>
            <TextField
              label={t("login__kayttaja")}
              value={username}
              onChange={setUsername}
            />
            <TextField
              label={t("login__salasana")}
              value={password}
              onChange={setPassword}
              type="password"
            />
            <SubmitButton
              value={t("login__btn_kirjaudu")}
              disabled={submitDisabled}
            />
            {renderResponse(loginResponse, {
              error: ({ message }) => <Error>{message}</Error>,
            })}
          </Form>
        </CardBody>
      </Card>
    </Page>
  )
}
