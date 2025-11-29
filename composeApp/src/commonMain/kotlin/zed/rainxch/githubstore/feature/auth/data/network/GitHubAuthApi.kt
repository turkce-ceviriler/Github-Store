package zed.rainxch.githubstore.feature.auth.data.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.timeout
import io.ktor.client.request.accept
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import zed.rainxch.githubstore.core.domain.model.DeviceStart
import zed.rainxch.githubstore.core.domain.model.DeviceTokenError
import zed.rainxch.githubstore.core.domain.model.DeviceTokenSuccess

object GitHubAuthApi {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    val http by lazy {
        HttpClient {
            install(ContentNegotiation) { json(json) }

            install(HttpTimeout) {
                requestTimeoutMillis = 60_000
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 30_000
            }
        }
    }

    suspend fun startDeviceFlow(clientId: String, scope: String): DeviceStart {
        val res = http.post("https://github.com/login/device/code") {
            accept(ContentType.Application.Json)
            headers.append(HttpHeaders.UserAgent, "GithubStore/1.0 (DeviceFlow)")
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(
                FormDataContent(
                    Parameters.Companion.build {
                        append("client_id", clientId)
                        append("scope", scope)
                    }
                )
            )
        }
        val status = res.status
        val text = res.bodyAsText()
        if (status !in HttpStatusCode.Companion.OK..HttpStatusCode.Companion.MultipleChoices) {
            error(
                buildString {
                    append("GitHub device/code HTTP ")
                    append(status.value)
                    append(" ")
                    append(status.description)
                    append(". Body: ")
                    append(text.take(300))
                    append(". Hint: Ensure you are using an OAuth App Client ID (not a GitHub App) and that CLIENT_ID is set correctly in GitHubAuthConfig.")
                }
            )
        }

        return try {
            json.decodeFromString(DeviceStart.serializer(), text)
        } catch (_: Throwable) {
            try {
                val err = json.decodeFromString(DeviceTokenError.serializer(), text)
                error("${err.error}: ${err.errorDescription ?: ""}".trim())
            } catch (_: Throwable) {
                error("Unexpected response from GitHub device/code: ${text}")
            }
        }
    }

    suspend fun pollDeviceToken(
        clientId: String,
        deviceCode: String
    ): Result<DeviceTokenSuccess> {
        val res = http.post("https://github.com/login/oauth/access_token") {
            accept(ContentType.Application.Json)
            headers.append(HttpHeaders.UserAgent, "GithubStore/1.0 (DeviceFlow)")
            contentType(ContentType.Application.FormUrlEncoded)

            timeout {
                socketTimeoutMillis = 60_000
            }

            setBody(
                FormDataContent(
                    Parameters.Companion.build {
                        append("client_id", clientId)
                        append("device_code", deviceCode)
                        append("grant_type", "urn:ietf:params:oauth:grant-type:device_code")
                    }
                )
            )
        }
        val status = res.status
        val text = res.body<String>()
        if (status !in HttpStatusCode.Companion.OK..HttpStatusCode.Companion.MultipleChoices) {
            return Result.failure(IllegalStateException("GitHub access_token HTTP ${'$'}{status.value} ${'$'}{status.description}. Body: ${'$'}{text.take(300)}"))
        }
        return try {
            val ok = json.decodeFromString(DeviceTokenSuccess.serializer(), text)
            Result.success(ok)
        } catch (_: Throwable) {
            val err = json.decodeFromString(DeviceTokenError.serializer(), text)
            val message = buildString {
                append(err.error)
                val desc = err.errorDescription
                if (!desc.isNullOrBlank()) {
                    append(": ")
                    append(desc)
                }
            }
            Result.failure(IllegalStateException(message))
        }
    }
}