import * as E from "fp-ts/lib/Either"
import { apiFetch, ApiResponse } from "./apiFetch"

/**
 * Hello world
 * TODO: Tuuppaa mereen
 */
export const fetchHello = () => apiFetch<"string">("hello")

/**
 * Login
 * TODO: Replace this mock with an API call
 */

export type Login = {
  session: string
}

export const fetchLogin = async (
  username: string,
  password: string
): Promise<ApiResponse<Login>> => {
  return username === "" || username !== password
    ? E.left({
        message: "Väärä käyttäjätunnus tai salasana",
      })
    : E.right({
        status: 200,
        data: {
          session: "mock",
        },
      })
}
