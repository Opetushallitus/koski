import {
  createKunnanHetuhakuPath,
  createMaksuttomuusPath,
  createOppijaPath,
  createSuorittaminenHetuhakuPath,
} from "../../src/state/paths"
import {
  clickElement,
  expectElementEventuallyVisible,
  expectElementNotVisible,
} from "../integrationtests-env/browser/content"
import { $, pathToUrl } from "../integrationtests-env/browser/core"
import { allowNetworkError } from "../integrationtests-env/browser/fail-on-console"
import {
  clearTextInput,
  inputIsEnabled,
  setTextInput,
} from "../integrationtests-env/browser/forms"
import { loginAs } from "../integrationtests-env/browser/reset"
import { defaultTimeout } from "../integrationtests-env/browser/timeouts"

describe("Oppijahaku", () => {
  it("Maksuttomuus: Haku löytää henkilötunnuksen perusteella oppijan, jonka tietojen näkemiseen käyttäjällä on oikeus, ja linkkaa detaljisivulle", async () => {
    await hakuLogin()
    await fillQueryField("221105A3023", "maksuttomuusoppijasearch")
    await submit("maksuttomuusoppijasearch")
    await expectResultToBe(
      "Löytyi: Oppivelvollinen-ysiluokka-kesken-keväällä-2021 Valpas (221105A3023)",
      createOppijaPath("/virkailija", {
        oppijaOid: "1.2.246.562.24.00000000001",
        prev: createMaksuttomuusPath(),
      }),
      "maksuttomuusoppijasearch"
    )
  })

  it("Maksuttomuus: Haku löytää oppijanumeron perusteella oppijan, jonka tietojen näkemiseen käyttäjällä on oikeus, ja linkkaa detaljisivulle", async () => {
    await hakuLogin()
    await fillQueryField(
      "1.2.246.562.24.00000000001",
      "maksuttomuusoppijasearch"
    )
    await submit("maksuttomuusoppijasearch")
    await expectResultToBe(
      "Löytyi: Oppivelvollinen-ysiluokka-kesken-keväällä-2021 Valpas (221105A3023)",
      createOppijaPath("/virkailija", {
        oppijaOid: "1.2.246.562.24.00000000001",
        prev: createMaksuttomuusPath(),
      }),
      "maksuttomuusoppijasearch"
    )
  })

  it("Maksuttomuus: Haku löytää henkilötunnuksen perusteella oppijan, jonka tietojen näkemiseen käyttäjällä on vain maksuttomuusoikeus, ja linkkaa detaljisivulle", async () => {
    await hakuLogin("valpas-pelkkä-maksuttomuus")
    await fillQueryField("070504A717P", "maksuttomuusoppijasearch")
    await submit("maksuttomuusoppijasearch")
    await expectResultToBe(
      "Löytyi: Lukio-opiskelija Valpas (070504A717P)",
      createOppijaPath("/virkailija", {
        oppijaOid: "1.2.246.562.24.00000000004",
        prev: createMaksuttomuusPath(),
      }),
      "maksuttomuusoppijasearch"
    )
  })

  it("Suorittaminen: Haku löytää henkilötunnuksen perusteella oppijan, jonka tietojen näkemiseen käyttäjällä on vain suorittamisoikeus, ja linkkaa detaljisivulle", async () => {
    await hakuLogin(
      "valpas-pelkkä-suorittaminen",
      createSuorittaminenHetuhakuPath("/virkailija"),
      "article#suorittaminenhetuhaku"
    )
    await fillQueryField("070504A717P")
    await submit()
    await expectResultToBe(
      "Löytyi: Lukio-opiskelija Valpas (070504A717P)",
      createOppijaPath("/virkailija", {
        oppijaOid: "1.2.246.562.24.00000000004",
        prev: createSuorittaminenHetuhakuPath(),
      })
    )
  })

  it("Kunta: Haku löytää henkilötunnuksen perusteella oppijan, jonka tietojen näkemiseen käyttäjällä on vain kuntaoikeus, ja linkkaa detaljisivulle", async () => {
    await hakuLogin(
      "valpas-helsinki",
      createKunnanHetuhakuPath("/virkailija"),
      "article#kuntahetuhaku"
    )
    await fillQueryField("070504A717P")
    await submit()
    await expectResultToBe(
      "Löytyi: Lukio-opiskelija Valpas (070504A717P)",
      createOppijaPath("/virkailija", {
        oppijaOid: "1.2.246.562.24.00000000004",
        prev: createKunnanHetuhakuPath(),
      })
    )
  })

  it("Maksuttomuus: Haku löytää oppijanumeron perusteella oppijan, jonka tietojen näkemiseen käyttäjällä on vain maksuttomuusoikeus, ja linkkaa detaljisivulle", async () => {
    await hakuLogin("valpas-pelkkä-maksuttomuus")
    await fillQueryField(
      "1.2.246.562.24.00000000004",
      "maksuttomuusoppijasearch"
    )
    await submit("maksuttomuusoppijasearch")
    await expectResultToBe(
      "Löytyi: Lukio-opiskelija Valpas (070504A717P)",
      createOppijaPath("/virkailija", {
        oppijaOid: "1.2.246.562.24.00000000004",
        prev: createMaksuttomuusPath(),
      }),
      "maksuttomuusoppijasearch"
    )
  })

  it("Suorittaminen: Haku löytää oppijanumeron perusteella oppijan, jonka tietojen näkemiseen käyttäjällä on vain suorittamisoikeus, ja linkkaa detaljisivulle", async () => {
    await hakuLogin(
      "valpas-pelkkä-suorittaminen",
      createSuorittaminenHetuhakuPath("/virkailija"),
      "article#suorittaminenhetuhaku"
    )
    await fillQueryField("1.2.246.562.24.00000000004")
    await submit()
    await expectResultToBe(
      "Löytyi: Lukio-opiskelija Valpas (070504A717P)",
      createOppijaPath("/virkailija", {
        oppijaOid: "1.2.246.562.24.00000000004",
        prev: createSuorittaminenHetuhakuPath(),
      })
    )
  })

  it("Kunta: Haku löytää oppijanumeron perusteella oppijan, jonka tietojen näkemiseen käyttäjällä on vain kuntaoikeus, ja linkkaa detaljisivulle", async () => {
    await hakuLogin(
      "valpas-helsinki",
      createKunnanHetuhakuPath("/virkailija"),
      "article#kuntahetuhaku"
    )
    await fillQueryField("1.2.246.562.24.00000000004")
    await submit()
    await expectResultToBe(
      "Löytyi: Lukio-opiskelija Valpas (070504A717P)",
      createOppijaPath("/virkailija", {
        oppijaOid: "1.2.246.562.24.00000000004",
        prev: createKunnanHetuhakuPath(),
      })
    )
  })

  it("Maksuttomuus: Haku kertoo ettei maksuttomuutta voida päätellä, jos oppijan tietoja ei löydy rekistereistä", async () => {
    await hakuLogin()
    await fillQueryField("040392-530U", "maksuttomuusoppijasearch")
    await submit("maksuttomuusoppijasearch")
    await expectResultToBe(
      "Maksuttomuutta ei pystytä päättelemään",
      undefined,
      "maksuttomuusoppijasearch"
    )
  })

  it("Maksuttomuus: Haku kertoo ettei maksuttomuutta voida päätellä, jos ikänsä puolesta uuden lain mukaan oppivelvollinen löytyy oppijanumerorekisteristä mutta ei Koskesta", async () => {
    await hakuLogin()
    await fillQueryField("110405A6951", "maksuttomuusoppijasearch")
    await submit("maksuttomuusoppijasearch")
    await expectResultToBe(
      "Maksuttomuutta ei pystytä päättelemään",
      undefined,
      "maksuttomuusoppijasearch"
    )
  })

  it("Maksuttomuus: Haku kertoo ettei henkilö ole maksuttomuuden piirissä, jos ikänsä puolesta uuden lain ulkopuolella oleva oppija löytyy oppijanumerorekisteristä mutta ei Koskesta", async () => {
    await hakuLogin()
    await fillQueryField("070302A402D", "maksuttomuusoppijasearch")
    await submit("maksuttomuusoppijasearch")
    await expectResultToBe(
      "Henkilö ei ole laajennetun oppivelvollisuuden piirissä, tai hän on suorittanut oppivelvollisuutensa eikä hänellä ole oikeutta maksuttomaan koulutukseen.",
      undefined,
      "maksuttomuusoppijasearch"
    )
  })

  it("Suorittaminen: Haku kertoo ettei oppijaa löydy, jos oppijan tietoja ei löydy rekistereistä", async () => {
    await hakuLogin(
      "valpas-pelkkä-suorittaminen",
      createSuorittaminenHetuhakuPath("/virkailija"),
      "article#suorittaminenhetuhaku"
    )
    await fillQueryField("040392-530U")
    await submit()
    await expectResultToBe(
      "Hakuehdolla ei löytynyt oppijaa tai sinulla ei ole oikeuksia nähdä kyseisen oppijan tietoja"
    )
  })

  it("Kunta: Haku kertoo ettei oppijaa löydy, jos oppijan tietoja ei löydy rekistereistä", async () => {
    await hakuLogin(
      "valpas-helsinki",
      createKunnanHetuhakuPath("/virkailija"),
      "article#kuntahetuhaku"
    )
    await fillQueryField("040392-530U")
    await submit()
    await expectResultToBe(
      "Hakuehdolla ei löytynyt oppijaa tai sinulla ei ole oikeuksia nähdä kyseisen oppijan tietoja"
    )
  })

  it("Maksuttomuus: Haku näyttää ilmoituksen maksuttomuusoikeudettomuudesta, jos oppija ei ole enää oppivelvollinen, koska on jo valmistunut ammattikoulusta", async () => {
    allowNetworkError(
      "api/henkilohaku/maksuttomuus/180304A082P",
      "403 (Forbidden)"
    )
    await hakuLogin("valpas-maksuttomuus-hki")
    await fillQueryField("180304A082P", "maksuttomuusoppijasearch")
    await submit("maksuttomuusoppijasearch")
    await expectResultToBe(
      "Henkilö ei ole laajennetun oppivelvollisuuden piirissä, tai hän on suorittanut oppivelvollisuutensa eikä hänellä ole oikeutta maksuttomaan koulutukseen.",
      undefined,
      "maksuttomuusoppijasearch"
    )
  })

  it("Suorittaminen: Haku näyttää virheilmoituksen, jos oppija ei ole enää oppivelvollinen, koska on jo valmistunut ammattikoulusta", async () => {
    allowNetworkError(
      "api/henkilohaku/suorittaminen/180304A082P",
      "403 (Forbidden)"
    )
    await hakuLogin(
      "valpas-pelkkä-suorittaminen",
      createSuorittaminenHetuhakuPath("/virkailija"),
      "article#suorittaminenhetuhaku"
    )
    await fillQueryField("180304A082P")
    await submit()
    await expectResultToBe(
      "Hakuehdolla ei löytynyt oppijaa tai sinulla ei ole oikeuksia nähdä kyseisen oppijan tietoja"
    )
  })

  it("Kunta: Haku näyttää virheilmoituksen, jos oppija ei ole enää oppivelvollinen, koska on jo valmistunut ammattikoulusta", async () => {
    allowNetworkError("api/henkilohaku/kunta/180304A082P", "403 (Forbidden)")
    await hakuLogin(
      "valpas-helsinki",
      createKunnanHetuhakuPath("/virkailija"),
      "article#kuntahetuhaku"
    )
    await fillQueryField("180304A082P")
    await submit()
    await expectResultToBe(
      "Hakuehdolla ei löytynyt oppijaa tai sinulla ei ole oikeuksia nähdä kyseisen oppijan tietoja"
    )
  })

  it("Maksuttomuus: Haku näyttää ilmoituksen maksuttomuusoikeudettomuudesta, jos oppija ei ole oppivelvollinen, koska on valmistunut perukoulusta ennen lain voimaantuloa", async () => {
    allowNetworkError(
      "api/henkilohaku/maksuttomuus/080905A0798",
      "403 (Forbidden)"
    )
    await hakuLogin("valpas-jkl-normaali")
    await fillQueryField("080905A0798", "maksuttomuusoppijasearch")
    await submit("maksuttomuusoppijasearch")
    await expectResultToBe(
      "Henkilö ei ole laajennetun oppivelvollisuuden piirissä, tai hän on suorittanut oppivelvollisuutensa eikä hänellä ole oikeutta maksuttomaan koulutukseen.",
      undefined,
      "maksuttomuusoppijasearch"
    )
  })

  it("Maksuttomuus: Haku näyttää ilmoituksen maksuttomuusoikeudettomuudesta, jos oppija ei ole oppivelvollinen, koska on vahvistettu international schoolin 9. luokan suoritus ennen lain voimaantuloa", async () => {
    allowNetworkError(
      "api/henkilohaku/maksuttomuus/090605A517L",
      "403 (Forbidden)"
    )
    await hakuLogin("valpas-jkl-normaali")
    await fillQueryField("090605A517L", "maksuttomuusoppijasearch")
    await submit("maksuttomuusoppijasearch")
    await expectResultToBe(
      "Henkilö ei ole laajennetun oppivelvollisuuden piirissä, tai hän on suorittanut oppivelvollisuutensa eikä hänellä ole oikeutta maksuttomaan koulutukseen.",
      undefined,
      "maksuttomuusoppijasearch"
    )
  })

  it("Maksuttomuus: Hakulomake verifioi virheelliset hetut ja oidit", async () => {
    await hakuLogin("valpas-jkl-normaali")

    const invalidInputs = ["123456-7890", "1.2.3.4", "Kekkonen"]
    for (const invalidInput of invalidInputs) {
      await fillQueryField(invalidInput, "maksuttomuusoppijasearch")
      await expectFieldErrorToBe(
        "Syötetty arvo ei ole validi henkilötunnus tai oppijatunnus"
      )
    }

    const validInputs = ["221105-3023", "1.2.246.562.24.00000000001"]
    for (const validInput of validInputs) {
      await fillQueryField(validInput, "maksuttomuusoppijasearch")
      await expectFieldErrorToBe(null)
    }
  })

  it("Maksuttomuus: Haku löytää oppijan, vaikka hänellä ei ole oppivelvollisuuden suorittamiseen kelpaavia opintoja", async () => {
    await hakuLogin("valpas-maksuttomuus-hki")
    await fillQueryField("061005A671V", "maksuttomuusoppijasearch") // Ei-oppivelvollisuuden-suorittamiseen-kelpaavia-opiskeluoikeuksia Valpas
    await submit("maksuttomuusoppijasearch")
    await expectResultToBe(
      "Löytyi: Ei-oppivelvollisuuden-suorittamiseen-kelpaavia-opiskeluoikeuksia Valpas (061005A671V)",
      createOppijaPath("/virkailija", {
        oppijaOid: "1.2.246.562.24.00000000058",
        prev: createMaksuttomuusPath(),
      }),
      "maksuttomuusoppijasearch"
    )
  })

  it("Maksuttomuus: Haku löytää oppijan, vaikka hänellä ei ole oppivelvollisuuden suorittamiseen kelpaavia opintoja paitsi International school jota ei tällä hetkellä Valppaassa näytetä", async () => {
    await hakuLogin("valpas-maksuttomuus-hki")
    await fillQueryField("200405A780K", "maksuttomuusoppijasearch")
    await submit("maksuttomuusoppijasearch")
    await expectResultToBe(
      "Löytyi: Inter-valmistunut-9-2021 Valpas (200405A780K)",
      createOppijaPath("/virkailija", {
        oppijaOid: "1.2.246.562.24.00000000078",
        prev: createMaksuttomuusPath(),
      }),
      "maksuttomuusoppijasearch"
    )
  })

  it("Maksuttomuus: Haku löytää myös hetuttoman oppijan", async () => {
    const hetutonOppijaOid = "1.2.246.562.24.00000000059"
    await hakuLogin("valpas-jkl-normaali")
    await fillQueryField(hetutonOppijaOid, "maksuttomuusoppijasearch")
    await submit("maksuttomuusoppijasearch")
    await expectResultToBe(
      "Löytyi: Hetuton Valpas",
      createOppijaPath("/virkailija", {
        oppijaOid: "1.2.246.562.24.00000000059",
        prev: createMaksuttomuusPath(),
      }),
      "maksuttomuusoppijasearch"
    )
  })

  it("Maksuttomuus: Haku löytää slave-oidilla haetun oppijan, vaikka slavella ei olekaan hetua, kunhan masterilla on", async () => {
    const oppijaMasterOid = "1.2.246.562.24.00000000060"
    const oppijaSlaveOid = "1.2.246.562.24.00000000061"
    await hakuLogin("valpas-jkl-normaali")
    await fillQueryField(oppijaSlaveOid, "maksuttomuusoppijasearch")
    await submit("maksuttomuusoppijasearch")
    await expectResultToBe(
      "Löytyi: Oppivelvollinen-hetullinen Valpas (030105A7507)",
      createOppijaPath("/virkailija", {
        oppijaOid: oppijaMasterOid,
        prev: createMaksuttomuusPath(),
      }),
      "maksuttomuusoppijasearch"
    )
  })

  it("Kunta: Haku löytää oppijan, vaikka hänellä ei ole oppivelvollisuuden suorittamiseen kelpaavia opintoja", async () => {
    await hakuLogin(
      "valpas-helsinki",
      createKunnanHetuhakuPath("/virkailija"),
      "article#kuntahetuhaku"
    )
    await fillQueryField("061005A671V") // Ei-oppivelvollisuuden-suorittamiseen-kelpaavia-opiskeluoikeuksia Valpas
    await submit()
    await expectResultToBe(
      "Löytyi: Ei-oppivelvollisuuden-suorittamiseen-kelpaavia-opiskeluoikeuksia Valpas (061005A671V)",
      createOppijaPath("/virkailija", {
        oppijaOid: "1.2.246.562.24.00000000058",
        prev: createKunnanHetuhakuPath(),
      })
    )
  })
})

const hakuLogin = async (
  user: string = "valpas-jkl-normaali",
  path: string = createMaksuttomuusPath("/virkailija"),
  selector: string = "article#maksuttomuus"
) => {
  await loginAs(path, user)
  await expectElementEventuallyVisible(selector)
}

const fillQueryField = async (
  query: string,
  className: string = "oppijasearch"
) => {
  const inputSelector = `.${className} .textfield__input`
  await clearTextInput(inputSelector)
  await setTextInput(inputSelector, query)
}

const submit = async (className: string = "oppijasearch") => {
  const buttonSelector = `.${className}__submit`
  expect(
    await inputIsEnabled(buttonSelector),
    "Expect submit button to be enabled"
  ).toBeTruthy()
  await clickElement(buttonSelector)
  await expectElementEventuallyVisible(`.${className}__resultvalue`)
}

const expectResultToBe = async (
  text: string,
  linkTo?: string,
  className: string = "oppijasearch"
) => {
  const result = await $(
    `.${className}__resultvalue`,
    defaultTimeout
  ).then((e) => e.getText())
  expect(result).toBe(text)
  if (linkTo) {
    const href = await $(`.${className}__resultlink`).then((e) =>
      e.getAttribute("href")
    )
    expect(href).toBe(pathToUrl(linkTo))
  }
}

const expectFieldErrorToBe = async (text: string | null) => {
  const errorSelector = ".textfield__error"
  if (text) {
    expect(await $(errorSelector).then((e) => e.getText())).toBe(text)
  } else {
    await expectElementNotVisible(errorSelector)
  }
}
