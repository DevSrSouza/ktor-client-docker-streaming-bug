import unixsocket.UnixSocketFactory
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.get
import io.ktor.client.statement.*
import io.ktor.utils.io.jvm.javaio.*
import io.ktor.utils.io.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

internal val unixFactory = UnixSocketFactory()
internal val eventsUrl = UnixSocketFactory.urlForUnixSocketPath("/var/run/docker.sock", "/v1.41/events")

enum class ClientType { KTOR, OKHTTP }



suspend fun main(args: Array<String>) {
    val clientType = args.getOrNull(0)?.let { arg ->
        ClientType.values().firstOrNull { it.name.equals(arg, ignoreCase = true) }
    }

    requireNotNull(clientType) { "You should specify a Client Type as a argument!" }

    when(clientType) {
        ClientType.KTOR -> ktorStreaming()
        ClientType.OKHTTP -> okhttpStreaming()
    }
}

suspend fun ktorStreaming() {
    val client = HttpClient(ktorEngine())

    client.get<HttpStatement>(eventsUrl.toUrl()).execute { response ->
        val content = response.content

        while (true) {
            println(content.readUTF8Line())
        }
    }
}

suspend fun okhttpStreaming() {
    val client = okhttpClient()

    val call = client.newCall(
        Request.Builder()
            .url(eventsUrl)
            .build()
    )

    val input = call.execute().body!!.source().inputStream().toByteReadChannel()

    while (true) {
        println(input.readUTF8Line())
    }
}

fun ktorEngine() = OkHttp.create {
    config {
        configureOkhttp()
    }
}

fun okhttpClient() = OkHttpClient.Builder()
    .configureOkhttp()
    .build()

fun OkHttpClient.Builder.configureOkhttp() = apply {
    socketFactory(unixFactory)
    dns(unixFactory)
    readTimeout(0, TimeUnit.MILLISECONDS)
    connectTimeout(0, TimeUnit.MILLISECONDS)
    callTimeout(0, TimeUnit.MILLISECONDS)
}
