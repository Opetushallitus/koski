package fi.oph.common.koskiuser

trait UserWithPassword extends UserWithUsername {
  def password: String
}

trait UserWithUsername {
  def username: String
}

trait UserWithOid {
  def oid: String
}
