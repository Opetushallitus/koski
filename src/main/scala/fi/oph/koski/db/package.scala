package fi.oph.koski

import slick.jdbc.PostgresProfile

package object db {
  type DB = PostgresProfile.backend.Database
}
