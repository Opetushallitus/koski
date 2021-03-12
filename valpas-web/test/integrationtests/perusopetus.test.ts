import {
  dataTableEventuallyEquals,
  dropdownSelect,
  loginAs,
  textEventuallyEquals,
} from "../integrationtests-env/browser"

const selectOrganisaatio = (index: number) =>
  dropdownSelect("#organisaatiovalitsin", index)

const jklNormaalikouluTableContent = `
  Kahdella-oppija-oidilla Valpas	Jyväskylän normaalikoulu	15.2.2005		Ei hakemusta	-	-	-
  KasiinAstiToisessaKoulussaOllut Valpas	Jyväskylän normaalikoulu	17.8.2005	9C	Ei hakemusta	-	-	-
  Kotiopetus-menneisyydessä Valpas	Jyväskylän normaalikoulu	6.2.2005	9C	Ei hakemusta	-	-	-
  LukionAloittanut Valpas	Jyväskylän normaalikoulu	29.4.2005		Ei hakemusta	-	-	-
  LuokallejäänytYsiluokkalainen Valpas	Jyväskylän normaalikoulu	2.8.2005	9A	Ei hakemusta	-	-	-
  LuokallejäänytYsiluokkalainenJatkaa Valpas	Jyväskylän normaalikoulu	6.2.2005	9B	Ei hakemusta	-	-	-
  LuokallejäänytYsiluokkalainenKouluvaihtoMuualta Valpas	Jyväskylän normaalikoulu	2.11.2005	9B	Ei hakemusta	-	-	-
  Oppivelvollinen-ysiluokka-kesken-keväällä-2021 Valpas	Jyväskylän normaalikoulu	22.11.2005	9C	Aktiivinen	-	-	-
  Päällekkäisiä Oppivelvollisuuksia	Jyväskylän normaalikoulu	6.6.2005	9B	Ei hakemusta	-	-	-
  Yli-2-kk-aiemmin-peruskoulusta-valmistunut Valpas	Jyväskylän normaalikoulu	1.2.2004	9C	Ei hakemusta	-	-	-
  Ysiluokka-valmis-keväällä-2021 Valpas	Jyväskylän normaalikoulu	19.6.2005	9C	Ei hakemusta	-	-	-
`

describe("Perusopetuksen näkymä", () => {
  it("Näyttää listan oppijoista", async () => {
    await loginAs("/virkailija/oppijat", "valpas-jkl-normaali")
    await textEventuallyEquals(
      ".card__header",
      "Perusopetuksen päättävät 2021 (11)"
    )
    await dataTableEventuallyEquals(
      ".hakutilanne",
      jklNormaalikouluTableContent
    )
  })

  it("Näyttää tyhjän listan virheittä, jos ei oppijoita", async () => {
    await loginAs("/virkailija/oppijat", "valpas-kulosaari")
    await textEventuallyEquals(
      ".card__header",
      "Perusopetuksen päättävät 2021 (0)"
    )
  })

  it("Vaihtaa taulun sisällön organisaatiovalitsimesta", async () => {
    await loginAs("/virkailija/oppijat", "valpas-useampi-peruskoulu")

    await selectOrganisaatio(0)
    await textEventuallyEquals(
      ".card__header",
      "Perusopetuksen päättävät 2021 (11)"
    )
    await dataTableEventuallyEquals(
      ".hakutilanne",
      jklNormaalikouluTableContent
    )

    await selectOrganisaatio(1)
    await textEventuallyEquals(
      ".card__header",
      "Perusopetuksen päättävät 2021 (0)"
    )
  })
})
