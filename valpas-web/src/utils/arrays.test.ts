import { update } from "./arrays"

describe("arrays", () => {
  it("update päivittää listan oikean alkion", () => {
    const source = [0, 1, 2, 3, 4, 5]
    const result = update(source, 2, 666)

    expect(result).toEqual([0, 1, 666, 3, 4, 5])
    expect(source).not.toBe(result)
  })

  it("update ei päivitä listaa, jos indeksi menee yli", () => {
    const source = [0, 1, 2, 3, 4, 5]
    const result = update(source, 20, 666)

    expect(result).toEqual([0, 1, 2, 3, 4, 5])
    expect(source).toBe(result)
  })
})
