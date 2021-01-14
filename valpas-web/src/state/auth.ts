export type UnauthorizedState = {
  loggedIn: false
}

export type AuthorizedState = {
  loggedIn: true
  user: string
}

export type AuthState = UnauthorizedState | AuthorizedState

const VALPAS_USER_COOKIE = "valpasUser"

export function getAuthState(): AuthState {
  const userCookie = getCookies()[VALPAS_USER_COOKIE]
  return userCookie
    ? {
        loggedIn: true,
        user: userCookie,
      }
    : {
        loggedIn: false,
      }
}

export function login(user: string) {
  setCookie(VALPAS_USER_COOKIE, user)
}

export function logout() {
  setCookie(VALPAS_USER_COOKIE, "")
}

function setCookie(key: string, value: string) {
  document.cookie = `${key}=${value}`
}

function getCookies(): Record<string, string> {
  return document.cookie
    .split(";")
    .map((s) => s.trim())
    .reduce((obj, item) => {
      const [key, value] = item.split("=")
      return key && value
        ? {
            ...obj,
            [key]: value,
          }
        : obj
    }, {})
}
