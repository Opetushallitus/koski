import React from "react"
import "./App.css"

function App() {
  return (
    <div className="App">
      <header className="App-header">
        <p>OmaDataOAuth2 Sample app</p>
      </header>
      <p>
        <a href={"/api/openid-api-test"}>Test whole authorization code flow</a>
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
