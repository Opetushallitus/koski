import { NonEmptyArray } from "fp-ts/lib/NonEmptyArray"
import {
  hakeutumisvalvonnanKunnalleIlmoitetutPathWithOrg,
  nivelvaiheenHakutilannePathWithOrg,
  oppijaPath,
} from "../../src/state/paths"
import {
  clickElement,
  expectElementEventuallyNotVisible,
  expectElementEventuallyVisible,
} from "../integrationtests-env/browser/content"
import {
  goToLocation,
  pathToUrl,
  urlIsEventually,
} from "../integrationtests-env/browser/core"
import { dataTableEventuallyEquals } from "../integrationtests-env/browser/datatable"
import { loginAs } from "../integrationtests-env/browser/reset"
import {
  kuntailmoitusRowSelector,
  oppijaRowSelector,
} from "./hakutilanne.shared"
import {
  fillTekijänTiedot,
  getCloseButton,
  getIlmoitusData,
  getIlmoitusForm,
  Oppija,
  Tekijä,
  täytäJaLähetäLomake,
} from "./kuntailmoitus.shared"
import {
  jyväskylänNormaalikouluNivelvaiheTableContent,
  ressunLukioTableContent_syyskuu2021,
} from "./nivelvaihehakutilanne.shared"
import { jyväskylänNormaalikouluOid, ressunLukioOid } from "./oids"

const ressunLukioHakutilannePath = nivelvaiheenHakutilannePathWithOrg.href(
  "/virkailija",
  {
    organisaatioOid: ressunLukioOid,
  }
)

const jyväskylänNormaalikouluHakutilannePath =
  nivelvaiheenHakutilannePathWithOrg.href("/virkailija", {
    organisaatioOid: jyväskylänNormaalikouluOid,
  })

const opo: Tekijä = {
  nimi: "käyttäjä valpas-monta",
  email: "integraatiotesti@oph.fi",
  puhelin: "0505050505050",
  organisaatio: "Ressun lukio",
  organisaatioOid: ressunLukioOid,
}

const teeOppijat = (tekijä: Tekijä): NonEmptyArray<Oppija> => [
  {
    oid: "1.2.246.562.24.00000000118",
    title:
      "Aikuisten-perusopetuksesta-alle-2kk-aiemmin-valmistunut Valpas (131004A1477)",
    prefill: 0,
    expected: {
      kohde: "Helsinki",
      tekijä: [
        tekijä.nimi,
        tekijä.email,
        tekijä.puhelin,
        tekijä.organisaatio,
      ].join("\n"),
      lähiosoite: "Esimerkkitie 10",
      postitoimipaikka: "99999 Helsinki",
      maa: "Costa Rica",
      puhelin: "0401122334",
      email: "valpas@gmail.com",
      muuHaku: "Ei",
    },
  },
]

describe("Nivelvaiheen näkymästä kuntailmoituksen tekeminen", () => {
  it("happy path nivelvaiheen hakeutumisen valvojana", async () => {
    await teeKuntailmoitusHakutilannenäkymästä("valpas-monta", opo)
  })

  it("Näytä aikavälillä 1.4.-31.7. eronneet tai eronneiksi katsotut nivelvaiheen oppijat", async () => {
    await loginAs(jyväskylänNormaalikouluHakutilannePath, "valpas-monta")
    await urlIsEventually(jyväskylänNormaalikouluHakutilannePath)
    await dataTableEventuallyEquals(
      ".hakutilanne",
      jyväskylänNormaalikouluNivelvaiheTableContent,
      "|"
    )
  })
})

const teeKuntailmoitusHakutilannenäkymästä = async (
  username: string,
  tekijä: Tekijä
) => {
  const oppijat = teeOppijat(tekijä)

  await openHakutilanneView(username)

  for (const oppija of oppijat) {
    await selectOppija(oppija.oid)
  }

  await openKuntailmoitus()
  await fillTekijänTiedot(tekijä)

  const forms = await getIlmoitusForm()
  expect(
    forms.length,
    "Lomakkeita näkyy yhtä monta kuin valittuja oppijoita"
  ).toBe(oppijat.length)

  for (const form of forms) {
    // Tarkista että valitut oppijat ja lomakkeet mäppäytyvät toisiinsa
    const oppija = oppijat.find((o) => form.subtitle.includes(o.oid))!!
    expect(
      oppija,
      `Lomakkeen oppija "${form.title}" "${form.subtitle}" on valittujen oppijoiden joukossa`
    ).toBeDefined()
    expect(form.title).toBe(oppija.title)

    await täytäJaLähetäLomake(oppija, form)
  }

  // Tarkista, että sulkemisnappulan teksti on vaihtunut
  const button = await getCloseButton()
  expect(await button.getText()).toBe("Valmis")

  // Sulje lomake
  await button.click()
  await expectElementEventuallyNotVisible(".modal__container")

  // Tarkista että oppijat ovat poistuneet listasta
  for (const oppija of oppijat) {
    await expectElementEventuallyNotVisible(oppijaRowSelector(oppija.oid))
  }

  // Tarkista että oppijat ovat ilmestyneet "kunnalle tehdyt ilmoitukset" -tauluun
  for (const oppija of oppijat) {
    const ilmotuksetPath =
      hakeutumisvalvonnanKunnalleIlmoitetutPathWithOrg.href("/virkailija", {
        organisaatioOid: tekijä.organisaatioOid,
      })
    await goToLocation(ilmotuksetPath)
    await urlIsEventually(pathToUrl(ilmotuksetPath))
    await expectElementEventuallyVisible(kuntailmoitusRowSelector(oppija.oid))
  }

  // Tarkista oppijakohtaisista näkymistä, että ilmoituksen tiedot ovat siellä
  for (const oppija of oppijat) {
    const path = oppijaPath.href("/virkailija", {
      oppijaOid: oppija.oid,
      hakutilanneRef: jyväskylänNormaalikouluOid,
    })
    await goToLocation(path)
    await urlIsEventually(pathToUrl(path))
    expect(await getIlmoitusData()).toEqual(oppija.expected)
  }
}

const openHakutilanneView = async (username: string) => {
  await loginAs(ressunLukioHakutilannePath, username, true)
  await dataTableEventuallyEquals(
    ".hakutilanne",
    ressunLukioTableContent_syyskuu2021,
    "|"
  )
}

const selectOppija = async (oppijaOid: string) => {
  const checkboxSelector = `.table__body .table__row[data-row*="${oppijaOid}"]  td:first-child .checkbox`
  await clickElement(checkboxSelector)
}

const openKuntailmoitus = async () => {
  await clickElement(".hakutilannedrawer__ilmoittaminen button")
  await expectElementEventuallyVisible(".modal__container")
}
