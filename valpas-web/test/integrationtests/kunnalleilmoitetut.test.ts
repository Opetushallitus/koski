import { kunnalleIlmoitetutPathWithOrg } from "../../src/state/paths"
import {
  clickElement,
  expectElementEventuallyVisible,
  expectElementNotVisible,
} from "../integrationtests-env/browser/content"
import { $ } from "../integrationtests-env/browser/core"
import { dataTableEventuallyEquals } from "../integrationtests-env/browser/datatable"
import { loginAs } from "../integrationtests-env/browser/reset"
import { kuntailmoitusRowSelector } from "./hakutilanne.shared"
import { mainHeadingEquals } from "./oppija.shared"

const jklNormaalikouluKunnalleIlmoitetutPath =
  kunnalleIlmoitetutPathWithOrg.href("/virkailija", {
    organisaatioOid: "1.2.246.562.10.14613773812",
  })

describe("Kunnalle tehdyt ilmoitukset -näkymä", () => {
  it("Kaikki hakeutumisvalvojana tehdyt ilmoitukset näytetään, vaikka oppijan tietoja ei enää olisikaan mahdollista tarkastella", async () => {
    await loginAs(
      jklNormaalikouluKunnalleIlmoitetutPath,
      "valpas-jkl-normaali-perus",
      false,
      "2021-12-05",
    )

    await dataTableEventuallyEquals(
      ".kuntailmoitukset",
      `
        Ilmoituksen-lisätiedot–poistettu Valpas                   | 19.5.2005   | 1.2.246.562.10.69417312936    | 5.12.2021	  | –
        Kahdella-oppija-oidilla-ilmo Valpas	                      | –           | Pyhtään kunta                 | 5.12.2021	  | Ei
        Kahdella-oppija-oidilla-ilmo-2 Valpas	                  | –	        | Pyhtään kunta	                | 5.12.2021	  | Ei
        KahdenKoulunYsi-ilmo Valpas	                              | –	        | Pyhtään kunta	                | 5.12.2021	  | Ei
        KasiinAstiToisessaKoulussaOllut-ilmo Valpas	              | 2.5.2005    | Pyhtään kunta	                | 5.12.2021	  | Ei
        LukionAloittanut-ilmo Valpas	                          | –	        | Pyhtään kunta	                | 15.6.2021	  | Ei
        LukionAloittanutJaLopettanut-ilmo Valpas	              | –	        | Helsingin kaupunki	        | 30.11.2021  | Ei
        LukionAloittanutJaLopettanut-ilmo Valpas	              | –	        | Pyhtään kunta	                | 20.9.2021	  | Ei
        LukionAloittanutJaLopettanut-ilmo Valpas  	              | –	        | Helsingin kaupunki	        | 15.9.2021	  | Ei
        LukionAloittanutJaLopettanut-ilmo Valpas	              | –	        | Pyhtään kunta	                | 15.6.2021	  | Ei
        Oppivelvollisuus-keskeytetty-ei-opiskele Valpas	          | –	        | Pyhtään kunta	                | 20.5.2021	  | Ei
        Oppivelvollisuus-keskeytetty-ei-opiskele Valpas	          | –	        | Helsingin kaupunki	        | 19.5.2021	  | Ei
        Turvakielto Valpas	                                      | 29.9.2004   | Pyhtään kunta	                | 5.12.2021	  | Ei
        Ysiluokka-valmis-ja-ilmoitettu-ja-uusi-nivelvaihe Valpas  | –	        | Pyhtään kunta	                | 27.6.2021	  | Ei
        Ysiluokka-valmis-keväällä-2021-ilmo Valpas	              | –	        | Pyhtään kunta	                | 5.12.2021	  | Ei
        `,
      "|",
    )
  })

  it("Oppijan nimeä klikkaamalla aukeaa ilmoitus ja linkki oppijasivulle", async () => {
    await loginAs(
      jklNormaalikouluKunnalleIlmoitetutPath,
      "valpas-jkl-normaali-perus",
      false,
      "2021-12-05",
    )

    const oppijaOid = "1.2.246.562.24.00000000035"
    const ilmoitusSelector = kuntailmoitusRowSelector(oppijaOid)

    await expectElementEventuallyVisible(ilmoitusSelector)

    await clickElement(ilmoitusSelector)
    expect(await (await $(".kuntailmoitus__title")).getText()).toEqual(
      "Vastuuilmoitus – 5.12.2021: Oppivelvollinen ilman opiskelupaikkaa",
    )

    const linkSelector = ".modalbuttongroup a"
    await expectElementEventuallyVisible(linkSelector)
    await clickElement(linkSelector)

    await mainHeadingEquals(
      "KasiinAstiToisessaKoulussaOllut-ilmo Valpas (020505A164W)",
    )
  })

  it("Oppijan, jonka tietoja ei enää voi katsella, nimen klikkaaminen aukeaisee vain ilmoituksen, mutta linkki oppijasivulle on piilotettu", async () => {
    await loginAs(
      jklNormaalikouluKunnalleIlmoitetutPath,
      "valpas-jkl-normaali-perus",
      false,
      "2021-12-05",
    )

    const oppijaOid = "nimi: Kahdella-oppija-oidilla-ilmo Valpas"
    const ilmoitusSelector = kuntailmoitusRowSelector(oppijaOid)

    await expectElementEventuallyVisible(ilmoitusSelector)

    await clickElement(ilmoitusSelector)
    expect(await (await $(".kuntailmoitus__title")).getText()).toEqual(
      "Vastuuilmoitus – 5.12.2021: Oppivelvollinen ilman opiskelupaikkaa",
    )

    const linkSelector = ".modalbuttongroup a"
    await expectElementNotVisible(linkSelector)
  })
})
