import { User } from "../state/auth"
import { apiGet, apiPost } from "./apiFetch"

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
