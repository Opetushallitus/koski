import React, { useEffect, useState } from "react"
import { fetchLogin } from "../api/api"
import { SubmitButton } from "../components/buttons/SubmitButton"
import { Card, CardBody, CardHeader } from "../components/containers/cards"
import { Page } from "../components/containers/Page"
import { Form } from "../components/forms/Form"
import { TextField } from "../components/forms/TextField"
import { t, T } from "../i18n/i18n"
import { formSubmitHandler } from "../utils/eventHandlers"
import { Error } from "../components/typography/error"
import { useApiMethod } from "../api/apiHooks"
import { publicUrl } from "../utils/url"

export default () => {
  const [username, setUsername] = useState("")
  const [password, setPassword] = useState("")
  const login = useApiMethod(fetchLogin)

  const submitDisabled = username === "" || password === ""

  const doLogin = formSubmitHandler(async () => {
    if (!submitDisabled && login.state !== "loading") {
      login.call(username, password)
    }
  })

  useEffect(() => {
    if (login.state === "success") {
      location.href = publicUrl()
    }
  }, [login])

  return (
    <Page id="login-app">
      <Card>
        <CardHeader>
          <T id="login__otsikko" />
        </CardHeader>
        <CardBody>
          <Form onSubmit={doLogin} onChange={login.clear}>
            <TextField
              id="username"
              label={t("login__kayttaja")}
              value={username}
              onChange={setUsername}
            />
            <TextField
              id="password"
              label={t("login__salasana")}
              value={password}
              onChange={setPassword}
              type="password"
            />
            <SubmitButton
              value={t("login__btn_kirjaudu")}
              disabled={submitDisabled}
            />
            {login.state === "error" ? (
              <Error>
                <ul>
                  {login.errors.map((error, index) => (
                    <li key={index}>{error.message}</li>
                  ))}
                </ul>
              </Error>
            ) : null}
          </Form>
        </CardBody>
      </Card>
    </Page>
  )
}
