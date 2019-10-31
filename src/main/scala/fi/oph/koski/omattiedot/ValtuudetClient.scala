package fi.oph.koski.omattiedot

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.typesafe.config.Config
import fi.oph.koski.http.OpintopolkuCallerId
import fi.vm.sade.javautils.httpclient.apache.ApacheOphHttpClient.{createDefaultOphClient => ophClient}
import fi.vm.sade.suomifi.valtuudet.ValtuudetPropertiesImpl.{builder => valtuudetProps}
import fi.vm.sade.suomifi.valtuudet.{JsonDeserializer, ValtuudetClientImpl}
import fi.vm.sade.suomifi.valtuudet.{ValtuudetClient => SuomifiValtuudetClient}

object ValtuudetClient {
  private val deserializer = new JsonDeserializer {
    private val objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    override def deserialize[T](json: String, `type`: Class[T]): T = objectMapper.readValue(json, `type`)
  }

  def apply(config: Config): SuomifiValtuudetClient = if (config.getString("suomifi.valtuudet.host") == "mock") {
    MockValtuudetClient
  } else {
    new ValtuudetClientImpl(ophClient(OpintopolkuCallerId.koski, null), deserializer, mkProperties(config))
  }

  private def mkProperties(config: Config) = {
    val host = config.getString("suomifi.valtuudet.host")
    val clientId = config.getString("suomifi.valtuudet.clientId")
    val apiKey = config.getString("suomifi.valtuudet.apiKey")
    val oauthPassword = config.getString("suomifi.valtuudet.oauthPassword")
    valtuudetProps.host(host).clientId(clientId).apiKey(apiKey).oauthPassword(oauthPassword).build
  }
}

