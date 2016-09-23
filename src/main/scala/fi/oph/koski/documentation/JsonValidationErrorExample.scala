package fi.oph.koski.documentation

import fi.oph.koski.json.Json
import org.json4s._

object JsonValidationErrorExample {
  val example = Json.read[JValue]("""[
                                    |  {
                                    |    "key": "badRequest.validation.jsonSchema",
                                    |    "message": {
                                    |      "level": "error",
                                    |      "schema": {
                                    |        "loadingURI": "#",
                                    |        "pointer": ""
                                    |      },
                                    |      "instance": {
                                    |        "pointer": ""
                                    |      },
                                    |      "domain": "validation",
                                    |      "keyword": "anyOf",
                                    |      "message": "instance failed to match at least one required schema among 4",
                                    |      "nrSchemas": 4,
                                    |      "reports": {
                                    |        "/anyOf/0": [
                                    |          {
                                    |            "level": "error",
                                    |            "schema": {
                                    |              "loadingURI": "#",
                                    |              "pointer": "/definitions/henkilötiedotjaoid"
                                    |            },
                                    |            "instance": {
                                    |              "pointer": ""
                                    |            },
                                    |            "domain": "validation",
                                    |            "keyword": "additionalProperties",
                                    |            "message": "object instance has properties which are not allowed by the schema: [\"etunimedt\"]",
                                    |            "unwanted": [
                                    |              "etunimedt"
                                    |            ]
                                    |          },
                                    |          {
                                    |            "level": "error",
                                    |            "schema": {
                                    |              "loadingURI": "#",
                                    |              "pointer": "/definitions/henkilötiedotjaoid"
                                    |            },
                                    |            "instance": {
                                    |              "pointer": ""
                                    |            },
                                    |            "domain": "validation",
                                    |            "keyword": "required",
                                    |            "message": "object has missing required properties ([\"etunimet\",\"oid\"])",
                                    |            "required": [
                                    |              "etunimet",
                                    |              "hetu",
                                    |              "kutsumanimi",
                                    |              "oid",
                                    |              "sukunimi"
                                    |            ],
                                    |            "missing": [
                                    |              "etunimet",
                                    |              "oid"
                                    |            ]
                                    |          }
                                    |        ],
                                    |        "/anyOf/1": [
                                    |          {
                                    |            "level": "error",
                                    |            "schema": {
                                    |              "loadingURI": "#",
                                    |              "pointer": "/definitions/oidhenkilö"
                                    |            },
                                    |            "instance": {
                                    |              "pointer": ""
                                    |            },
                                    |            "domain": "validation",
                                    |            "keyword": "additionalProperties",
                                    |            "message": "object instance has properties which are not allowed by the schema: [\"etunimedt\",\"hetu\",\"kutsumanimi\",\"sukunimi\"]",
                                    |            "unwanted": [
                                    |              "etunimedt",
                                    |              "hetu",
                                    |              "kutsumanimi",
                                    |              "sukunimi"
                                    |            ]
                                    |          },
                                    |          {
                                    |            "level": "error",
                                    |            "schema": {
                                    |              "loadingURI": "#",
                                    |              "pointer": "/definitions/oidhenkilö"
                                    |            },
                                    |            "instance": {
                                    |              "pointer": ""
                                    |            },
                                    |            "domain": "validation",
                                    |            "keyword": "required",
                                    |            "message": "object has missing required properties ([\"oid\"])",
                                    |            "required": [
                                    |              "oid"
                                    |            ],
                                    |            "missing": [
                                    |              "oid"
                                    |            ]
                                    |          }
                                    |        ],
                                    |        "/anyOf/2": [
                                    |          {
                                    |            "level": "error",
                                    |            "schema": {
                                    |              "loadingURI": "#",
                                    |              "pointer": "/definitions/täydellisethenkilötiedot"
                                    |            },
                                    |            "instance": {
                                    |              "pointer": ""
                                    |            },
                                    |            "domain": "validation",
                                    |            "keyword": "additionalProperties",
                                    |            "message": "object instance has properties which are not allowed by the schema: [\"etunimedt\"]",
                                    |            "unwanted": [
                                    |              "etunimedt"
                                    |            ]
                                    |          },
                                    |          {
                                    |            "level": "error",
                                    |            "schema": {
                                    |              "loadingURI": "#",
                                    |              "pointer": "/definitions/täydellisethenkilötiedot"
                                    |            },
                                    |            "instance": {
                                    |              "pointer": ""
                                    |            },
                                    |            "domain": "validation",
                                    |            "keyword": "required",
                                    |            "message": "object has missing required properties ([\"etunimet\",\"oid\"])",
                                    |            "required": [
                                    |              "etunimet",
                                    |              "hetu",
                                    |              "kutsumanimi",
                                    |              "oid",
                                    |              "sukunimi"
                                    |            ],
                                    |            "missing": [
                                    |              "etunimet",
                                    |              "oid"
                                    |            ]
                                    |          }
                                    |        ],
                                    |        "/anyOf/3": [
                                    |          {
                                    |            "level": "error",
                                    |            "schema": {
                                    |              "loadingURI": "#",
                                    |              "pointer": "/definitions/uusihenkilö"
                                    |            },
                                    |            "instance": {
                                    |              "pointer": ""
                                    |            },
                                    |            "domain": "validation",
                                    |            "keyword": "additionalProperties",
                                    |            "message": "object instance has properties which are not allowed by the schema: [\"etunimedt\"]",
                                    |            "unwanted": [
                                    |              "etunimedt"
                                    |            ]
                                    |          },
                                    |          {
                                    |            "level": "error",
                                    |            "schema": {
                                    |              "loadingURI": "#",
                                    |              "pointer": "/definitions/uusihenkilö"
                                    |            },
                                    |            "instance": {
                                    |              "pointer": ""
                                    |            },
                                    |            "domain": "validation",
                                    |            "keyword": "required",
                                    |            "message": "object has missing required properties ([\"etunimet\"])",
                                    |            "required": [
                                    |              "etunimet",
                                    |              "hetu",
                                    |              "kutsumanimi",
                                    |              "sukunimi"
                                    |            ],
                                    |            "missing": [
                                    |              "etunimet"
                                    |            ]
                                    |          }
                                    |        ]
                                    |      }
                                    |    }
                                    |  }
                                    |]""".stripMargin)
}