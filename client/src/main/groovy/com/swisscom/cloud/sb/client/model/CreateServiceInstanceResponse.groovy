package com.swisscom.cloud.sb.client.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import groovy.transform.CompileStatic

@CompileStatic
@JsonIgnoreProperties(ignoreUnknown = true)
class CreateServiceInstanceResponse implements Serializable {
    @JsonSerialize
    @JsonProperty("dashboard_url")
    String dashboardUrl

    @JsonSerialize
    String operation

    @JsonSerialize
    Boolean async
}
