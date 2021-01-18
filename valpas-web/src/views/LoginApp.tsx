import React, { useEffect, useState } from "react"
import { fetchLogin } from "../api/api"
import { SubmitButton } from "../components/buttons/SubmitButton"
import { Card, CardBody, CardHeader } from "../components/containers/cards"
import { Page } from "../components/containers/Page"
import { Form } from "../components/forms/Form"
import { TextField } from "../components/forms/TextField"
import { t, T } from "../i18n/i18n"
import { User } from "../state/auth"
import { formSubmitHandler } from "../utils/eventHandlers"
import { Error } from "../components/typography/error"
import { renderResponse, useApiState } from "../api/apiReact"
import { forNullableEither } from "../utils/fp"

export type LoginAppProps = {
  onLogin: (user: User) => void
}

export const LoginApp = (props: LoginAppProps) => {
  const [username, setUsername] = useState("")
  const [password, setPassword] = useState("")
  const [loginResponse, setLoginResponse] = useApiState<User>()

  const submitDisabled = username === "" || password === ""

  const login = formSubmitHandler(async () => {
    if (!submitDisabled) {
      setLoginResponse(await fetchLogin(username, password))
    }
  })

  useEffect(() => {
    forNullableEither(loginResponse, (login) => props.onLogin(login.data))
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
              error: ({ errors }) => (
                <Error>
                  <ul>
                    {errors.map((error, index) => (
                      <li key={index}>{error.message}</li>
                    ))}
                  </ul>
                </Error>
              ),
            })}
          </Form>
        </CardBody>
      </Card>
    </Page>
  )
}
