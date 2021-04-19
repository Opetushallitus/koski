import { Hakutoive, HakutoiveValintatilakoodiarvo } from "./oppijat"

describe("Hakutoive", () => {
  const testHakutoivePattern = (koodiarvo?: HakutoiveValintatilakoodiarvo) => (
    hyväksytty: boolean,
    varasijalla: boolean,
    eiPaikkaa: boolean
  ) => {
    const hakutoive: Hakutoive = {
      valintatila: koodiarvo && {
        koodistoUri: "valpashaunvalintatila",
        koodiarvo,
      },
    }
    expect(Hakutoive.isHyväksytty(hakutoive)).toEqual(hyväksytty)
    expect(Hakutoive.isVarasijalla(hakutoive)).toEqual(varasijalla)
    expect(Hakutoive.isEiPaikkaa(hakutoive)).toEqual(eiPaikkaa)
  }

  it("hyvaksytty", () => testHakutoivePattern("hyvaksytty")(true, false, false))
  it("hylatty", () => testHakutoivePattern("hylatty")(false, false, true))
  it("varasijalla", () =>
    testHakutoivePattern("varasijalla")(false, true, false))
  it("peruuntunut", () =>
    testHakutoivePattern("peruuntunut")(false, false, true))
  it("peruttu", () => testHakutoivePattern("peruttu")(false, false, true))
  it("peruutettu", () => testHakutoivePattern("peruutettu")(false, false, true))
  it("kesken", () => testHakutoivePattern("kesken")(false, false, false))
  it("undefined", () => testHakutoivePattern()(false, false, false))
})
