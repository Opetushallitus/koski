package fi.oph.koski.db

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.regions.DefaultAwsRegionProviderChain
import com.amazonaws.services.rds.auth.{GetIamAuthTokenRequest, RdsIamAuthTokenGenerator}
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import fi.oph.koski.log.Logging

import java.net.URI
import java.sql.Connection

class DataSourceWithRdsIamSupport extends HikariDataSource with Logging {
  override def getConnection(username: String, password: String): Connection = {
      super.getConnection(username, getPassword())
  }

  override def getPassword: String = getToken

  private def getToken = {
    val region = new DefaultAwsRegionProviderChain().getRegion()
    logger.info(s"Generating IAM authentication token for user ${getUsername} in region ${region}")

    // TODO: Miki tämä on null?
    val jdbcUri = parseJdbcURL(getJdbcUrl)

    val generator = RdsIamAuthTokenGenerator.builder.credentials(new DefaultAWSCredentialsProviderChain()).region(region).build
    val request = GetIamAuthTokenRequest.builder.hostname(jdbcUri.getHost()).port(jdbcUri.getPort()).userName(getUsername).build
    generator.getAuthToken(request)
  }

  private def parseJdbcURL(jdbcUrl: String) = {
    val uri = jdbcUrl.substring(5)
    URI.create(uri)
  }
}
