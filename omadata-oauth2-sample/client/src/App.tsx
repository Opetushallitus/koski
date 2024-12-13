import React from "react"
import "./App.css"

function App() {
  return (
    <div className="App">
      <header className="App-header">
        <p>OmaDataOAuth2 Sample app</p>
      </header>
      <p>
        <a
          href={
            "/api/openid-api-test?scope=HENKILOTIEDOT_KAIKKI_TIEDOT+OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT"
          }
        >
          Test whole authorization code flow - suoritetut tutkinnot
        </a>
      </p>
      <p>
        <a
          href={
            "/api/openid-api-test?scope=HENKILOTIEDOT_KAIKKI_TIEDOT+OPISKELUOIKEUDET_AKTIIVISET_JA_PAATTYNEET_OPINNOT"
          }
        >
          Test whole authorization code flow - aktiiviset ja päättyneet opinnot
        </a>
      </p>
      <p>
        <a
          href={
            "/api/openid-api-test?scope=HENKILOTIEDOT_KAIKKI_TIEDOT+OPISKELUOIKEUDET_KAIKKI_TIEDOT"
          }
        >
          Test whole authorization code flow - kaikki tiedot
        </a>
      </p>
      <p>
        <a href={"/api/openid-api-test/invalid-redirect-uri"}>
          Test authorization code flow with invalid redirect_uri
        </a>
      </p>
    </div>
  )
}

export default App
