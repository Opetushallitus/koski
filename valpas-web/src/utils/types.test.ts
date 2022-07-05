import { filterFalsy } from "./types"

describe("types", () => {
  it("filterFalsy palauttaa false väärällä syötteellä", () => {
    expect(filterFalsy(false)).toEqual(false)
    expect(filterFalsy(null)).toEqual(false)
    expect(filterFalsy(undefined)).toEqual(false)
    expect(filterFalsy("")).toEqual(false)
  })
  it("filterFalsy palauttaa true oikealla syötteellä", () => {
    expect(filterFalsy("Something")).toEqual(true)
    expect(filterFalsy({ value: 1 })).toEqual(true)
    expect(filterFalsy(10)).toEqual(true)
    expect(filterFalsy(true)).toEqual(true)
  })
})
