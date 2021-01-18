import { User } from "../state/auth"
import { apiGet, apiPost } from "./apiFetch"

/**
 * Hello world
 * TODO: Tuuppaa mereen
 */
export const fetchHello = () => apiGet<"string">("api/hello")

/**
 * Login
 */
export const fetchLogin = async (username: string, password: string) =>
  apiPost<User>("login", {
    body: {
      username,
      password,
    },
  })

/**
 * Get current user
 */
export const fetchCurrentUser = async () => apiGet<User>("api/user")
