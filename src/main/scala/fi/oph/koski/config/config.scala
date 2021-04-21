package fi.oph.koski

import com.typesafe.config.Config

package object config {
  implicit class FluentConfig(c: Config) {
    def andThen(fn: Config => Config): Config = fn(c)
  }
}
