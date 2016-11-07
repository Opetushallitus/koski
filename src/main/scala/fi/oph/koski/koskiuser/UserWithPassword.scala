package fi.oph.koski.koskiuser

trait UserWithPassword extends UserWithUsername {
  def password: String
}

trait UserWithUsername {
  def username: String
}