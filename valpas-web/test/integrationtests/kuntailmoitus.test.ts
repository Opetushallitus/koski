import { hakeutumisvalvonnanKunnalleIlmoitetutPathWithoutOrg } from "../../src/state/paths"
import { dataTableEventuallyEquals } from "../integrationtests-env/browser/datatable"
import { loginAs } from "../integrationtests-env/browser/reset"
import {
  opo,
  teeKuntailmoitusHakutilannenäkymästä,
} from "./kuntailmoitus.shared"

const ilmoitusPath =
  hakeutumisvalvonnanKunnalleIlmoitetutPathWithoutOrg.href("/virkailija")

describe("Kuntailmoituksen tekeminen 1/7", () => {
  it("happy path hakeutumisen valvojana", async () => {
    await teeKuntailmoitusHakutilannenäkymästä("valpas-jkl-normaali", opo)
  })

  it("Kaikki hakeutumisvalvojana tehdyt ilmoitukset näytetään, vaikka oppijan tietoja ei enää olisikaan mahdollista tarkastella", async () => {
    await loginAs(
      ilmoitusPath,
      "valpas-jkl-normaali-perus",
      false,
      "2021-12-05"
    )

    await dataTableEventuallyEquals(
      ".kuntailmoitukset",
      `
        Ilmoituksen-lisätiedot–poistettu Valpas                   | 19.5.2005  | 1.2.246.562.10.69417312936   | 5.12.2021	  | –
        Kahdella-oppija-oidilla-ilmo Valpas	                      | –          | Pyhtään kunta                | 5.12.2021	  | Ei
        Kahdella-oppija-oidilla-ilmo-2 Valpas	                    | –	         | Pyhtään kunta	              | 5.12.2021	  | Ei
        KahdenKoulunYsi-ilmo Valpas	                              | –	         | Pyhtään kunta	              | 5.12.2021	  | Ei
        KasiinAstiToisessaKoulussaOllut-ilmo Valpas	              | 2.5.2005	 | Pyhtään kunta	              | 5.12.2021	  | Ei
        LukionAloittanut-ilmo Valpas	                            | –	         | Pyhtään kunta	              | 15.6.2021	  | Ei
        LukionAloittanutJaLopettanut-ilmo Valpas	                | –	         | Helsingin kaupunki	          | 30.11.2021  | Ei
        LukionAloittanutJaLopettanut-ilmo Valpas	                | –	         | Pyhtään kunta	              | 20.9.2021	  | Ei
        LukionAloittanutJaLopettanut-ilmo Valpas  	              | –	         | Helsingin kaupunki	          | 15.9.2021	  | Ei
        LukionAloittanutJaLopettanut-ilmo Valpas	                | –	         | Pyhtään kunta	              | 15.6.2021	  | Ei
        Oppivelvollisuus-keskeytetty-ei-opiskele Valpas	          | –	         | Pyhtään kunta	              | 20.5.2021	  | Ei
        Oppivelvollisuus-keskeytetty-ei-opiskele Valpas	          | –	         | Helsingin kaupunki	          | 19.5.2021	  | Ei
        Turvakielto Valpas	                                      | 29.9.2004	 | Pyhtään kunta	              | 5.12.2021	  | Ei
        Ysiluokka-valmis-ja-ilmoitettu-ja-uusi-nivelvaihe Valpas	| –	         | Pyhtään kunta	              | 27.6.2021	  | Ei
        Ysiluokka-valmis-keväällä-2021-ilmo Valpas	              | –	         | Pyhtään kunta	              | 5.12.2021	  | Ei
      `,
      "|"
    )
  })
})
