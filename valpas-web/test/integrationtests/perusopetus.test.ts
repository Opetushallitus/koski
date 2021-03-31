import {
  dataTableEventuallyEquals,
  dropdownSelect,
  loginAs,
  textEventuallyEquals,
} from "../integrationtests-env/browser"

const selectOrganisaatio = (index: number) =>
  dropdownSelect("#organisaatiovalitsin", index)

// HUOM: Tämä on sarkaimilla (\t) erotettu taulukko:
const jklNormaalikouluTableContent = `
  Epäonninen Valpas	30.10.2005	9C	Ei hakemusta	Ei toteutettu	Ei toteutettu	Ei toteutettu
  Eroaja-myöhemmin Valpas	29.9.2005	9C	Ei hakemusta	Ei toteutettu	Ei toteutettu	Ei toteutettu
  Kahdella-oppija-oidilla Valpas	15.2.2005	9C	Ei hakemusta	Ei toteutettu	Ei toteutettu	Ei toteutettu
  KasiinAstiToisessaKoulussaOllut Valpas	17.8.2005	9C	Ei hakemusta	Ei toteutettu	Ei toteutettu	Ei toteutettu
  Kotiopetus-menneisyydessä Valpas	6.2.2005	9C	Ei hakemusta	Ei toteutettu	Ei toteutettu	Ei toteutettu
  LukionAloittanut Valpas	29.4.2005	9C	Ei hakemusta	Ei toteutettu	Ei toteutettu	Ei toteutettu
  LukionLokakuussaAloittanut Valpas	18.4.2005	9C	Ei hakemusta	Ei toteutettu	Ei toteutettu	Ei toteutettu
  LuokallejäänytYsiluokkalainen Valpas	2.8.2005	9A	2 hakua	Ei toteutettu	Ei toteutettu	Ei toteutettu
  LuokallejäänytYsiluokkalainenJatkaa Valpas	6.2.2005	9B	Ei hakemusta	Ei toteutettu	Ei toteutettu	Ei toteutettu
  LuokallejäänytYsiluokkalainenKouluvaihtoMuualta Valpas	2.11.2005	9B	Ei hakemusta	Ei toteutettu	Ei toteutettu	Ei toteutettu
  Oppivelvollinen-ysiluokka-kesken-keväällä-2021 Valpas	22.11.2005	9C	Hakenut open_in_new	Ei toteutettu	Ei toteutettu	Ei toteutettu
  Päällekkäisiä Oppivelvollisuuksia	6.6.2005	9B	Ei hakemusta	Ei toteutettu	Ei toteutettu	Ei toteutettu
  Turvakielto Valpas	29.9.2004	9C	Hakenut open_in_new	Ei toteutettu	Ei toteutettu	Ei toteutettu
  UseampiYsiluokkaSamassaKoulussa Valpas	25.8.2005	9D	Ei hakemusta	Ei toteutettu	Ei toteutettu	Ei toteutettu
  UseampiYsiluokkaSamassaKoulussa Valpas	25.8.2005	9C	Ei hakemusta	Ei toteutettu	Ei toteutettu	Ei toteutettu
  Ysiluokka-valmis-keväällä-2021 Valpas	19.6.2005	9C	Ei hakemusta	Ei toteutettu	Ei toteutettu	Ei toteutettu
`

describe("Perusopetuksen näkymä", () => {
  it("Näyttää listan oppijoista", async () => {
    await loginAs("/virkailija/oppijat", "valpas-jkl-normaali")
    await textEventuallyEquals(
      ".card__header",
      "Perusopetuksen päättävät 2021 (16)"
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
      "Perusopetuksen päättävät 2021 (16)"
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
