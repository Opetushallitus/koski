import React, { Suspense, useEffect, useState } from "react"
import {
  CurrentUser,
  getOppijaLogin,
  isLoggedIn,
  redirectToLoginReturnUrl,
  storeLoginReturnUrl,
} from "./auth"

export const useUserLogin = (
  getUser: () => Promise<CurrentUser>,
): CurrentUser | null => {
  const [user, setUser] = useState<CurrentUser | null>(null)

  useEffect(() => {
    getUser().then(setUser)
  }, [getUser])

  if (!user) {
    return null
  }

  if (isLoggedIn(user) && redirectToLoginReturnUrl()) {
    return null
  }

  return user
}

export type LoginProps = {
  redirectAfterLogin?: string
}

export const Login = (props: LoginProps) => {
  useEffect(() => {
    storeLoginReturnUrl(
      props.redirectAfterLogin
        ? `${location.origin}${props.redirectAfterLogin}`
        : location.href,
    )
  }, [props.redirectAfterLogin])

  const login = getOppijaLogin()

  if (login.type === "external") {
    login.redirectToExternalLogin()
    return null
  }

  const LocalLoginApp = React.lazy(() => import("../views/LoginApp"))

  return (
    <Suspense fallback={<></>}>
      <LocalLoginApp />
    </Suspense>
  )
}
