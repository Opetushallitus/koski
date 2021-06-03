import { render, RenderResult } from "@testing-library/react"
import React from "react"
import { MemoryRouter, Switch } from "react-router"
import { Route } from "react-router-dom"
import {
  KäyttöoikeusroolitProvider,
  WithRequiresAccessRightsProps,
  withRequiresHakeutumisenOrMaksuttomuudenValvontaOrKunta,
  withRequiresHakeutumisenValvonta,
} from "./accessRights"
import { Kayttooikeusrooli, OrganisaatioJaKayttooikeusrooli } from "./common"

describe("accessRights hocit", () => {
  const HakeutumisenValvonta = withRequiresHakeutumisenValvonta(() => (
    <div>Hakeutumisen valvonta</div>
  ))

  const HakeutumisenTaiMaksuttomuudenTaiKunnanValvonta = withRequiresHakeutumisenOrMaksuttomuudenValvontaOrKunta(
    () => <div>Hakeutumisen tai maksuttomuuden valvonta tai kunta</div>
  )

  it("Hakeutumisen valvonta: ei käyttöoikeuksia", async () => {
    const app = renderApp([], HakeutumisenValvonta)
    await expectResult(app, "Ei oikeuksia")
  })

  it("Hakeutumisen valvonta: oikeat käyttöoikeudet", async () => {
    const app = renderApp(
      [rooli("OPPILAITOS_HAKEUTUMINEN")],
      HakeutumisenValvonta
    )
    await expectResult(app, "Hakeutumisen valvonta")
  })

  it("Hakeutumisen valvonta: väärät käyttöoikeudet", async () => {
    const app = renderApp(
      [rooli("OPPILAITOS_MAKSUTTOMUUS"), rooli("OPPILAITOS_SUORITTAMINEN")],
      HakeutumisenValvonta
    )
    await expectResult(app, "Ei oikeuksia")
  })

  it("Hakeutumisen- tai maksuttomuuden tai kunnan valvonta: hakeutumisoikeudet", async () => {
    const app = renderApp(
      [rooli("OPPILAITOS_HAKEUTUMINEN")],
      HakeutumisenTaiMaksuttomuudenTaiKunnanValvonta
    )
    await expectResult(
      app,
      "Hakeutumisen tai maksuttomuuden valvonta tai kunta"
    )
  })

  it("Hakeutumisen- tai maksuttomuuden tai kunnan valvonta: maksuttomuusoikeudet", async () => {
    const app = renderApp(
      [rooli("OPPILAITOS_MAKSUTTOMUUS")],
      HakeutumisenTaiMaksuttomuudenTaiKunnanValvonta
    )
    await expectResult(
      app,
      "Hakeutumisen tai maksuttomuuden valvonta tai kunta"
    )
  })

  it("Hakeutumisen- tai maksuttomuuden tai kunnan valvonta: kunnan oikeudet", async () => {
    const app = renderApp(
      [rooli("KUNTA")],
      HakeutumisenTaiMaksuttomuudenTaiKunnanValvonta
    )
    await expectResult(
      app,
      "Hakeutumisen tai maksuttomuuden valvonta tai kunta"
    )
  })

  it("Hakeutumisen- tai maksuttomuuden tai kunnan valvonta: väärät käyttöoikeudet", async () => {
    const app = renderApp(
      [rooli("OPPILAITOS_SUORITTAMINEN")],
      HakeutumisenTaiMaksuttomuudenTaiKunnanValvonta
    )
    await expectResult(app, "Ei oikeuksia")
  })
})

type TestAppProps = {
  roolit: OrganisaatioJaKayttooikeusrooli[]
  children: React.ReactNode
}

const TestApp = (props: TestAppProps) => (
  <KäyttöoikeusroolitProvider value={props.roolit}>
    <MemoryRouter>
      <Switch>
        <Route exact path="/">
          {props.children}
        </Route>
        <Route exact path="/noaccess">
          Ei oikeuksia
        </Route>
      </Switch>
    </MemoryRouter>
  </KäyttöoikeusroolitProvider>
)

const renderApp = (
  roolit: OrganisaatioJaKayttooikeusrooli[],
  Component: React.ComponentType<WithRequiresAccessRightsProps>
) =>
  render(
    <TestApp roolit={roolit}>
      <Component redirectUserWithoutAccessTo="/noaccess" />
    </TestApp>
  )

const rooli = (
  kayttooikeusrooli: Kayttooikeusrooli
): OrganisaatioJaKayttooikeusrooli => ({
  organisaatioHierarkia: {
    oid: "1.2.3.4.5.6.7",
    nimi: {
      fi: "Testi",
    },
    aktiivinen: true,
    organisaatiotyypit: [],
    children: [],
  },
  kayttooikeusrooli,
})

const expectResult = async (app: RenderResult, text: string) => {
  expect(await app.findByText(text)).toBeTruthy()
}
