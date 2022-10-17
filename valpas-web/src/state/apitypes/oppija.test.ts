import {
  HakutoiveValintatilakoodiarvo,
  isEiPaikkaa,
  isHyväksytty,
  isVarasijalla,
  SuppeaHakutoive,
} from "./hakutoive"

describe("Hakutoive", () => {
  const testHakutoivePattern =
    (koodiarvo?: HakutoiveValintatilakoodiarvo) =>
    (hyväksytty: boolean, varasijalla: boolean, eiPaikkaa: boolean) => {
      const hakutoive: SuppeaHakutoive = {
        valintatila: koodiarvo && {
          koodistoUri: "valpashaunvalintatila",
          koodiarvo,
        },
      }
      expect(isHyväksytty(hakutoive)).toEqual(hyväksytty)
      expect(isVarasijalla(hakutoive)).toEqual(varasijalla)
      expect(isEiPaikkaa(hakutoive)).toEqual(eiPaikkaa)
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
