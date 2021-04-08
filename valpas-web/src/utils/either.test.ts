import * as E from "fp-ts/Either"
import { tapLeft, tapLeftP } from "./either"

describe("either", () => {
  it("tapLeft suorittaa sivuvaikutuksen left-arvolle", () => {
    const e = E.left("ok")
    let value
    const e2 = tapLeft((left) => {
      value = left
    })(e)

    expect(e2).toEqual(E.left("ok"))
    expect(value).toEqual("ok")
  })

  it("tapLeftP suorittaa sivuvaikutuksen left-arvolle", async () => {
    const ep = Promise.resolve(E.left("ok"))
    let value
    const ep2 = tapLeftP((left) => {
      value = left
    })(ep)

    expect(await ep2).toEqual(E.left("ok"))
    expect(value).toEqual("ok")
  })
})
