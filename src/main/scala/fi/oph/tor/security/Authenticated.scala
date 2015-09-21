package fi.oph.tor.security

trait Authenticated extends CurrentUser {
  before() {
    if(getAuthenticatedUser.isEmpty) {
      halt(401, "Not authenticated")
    }
  }
}
