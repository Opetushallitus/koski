package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.schema._

case class Example(name: String, description: String, data: Oppija, statusCode: Int = 200)