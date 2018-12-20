package io.ktor.network.sockets

import io.ktor.util.*

/**
 * Socket options builder
 */
@UseExperimental(ExperimentalUnsignedTypes::class)
sealed class SocketOptions(
    @Suppress("KDocMissingDocumentation") protected val customOptions: MutableMap<Any, Any?>
) {
    /**
     * Copy options
     */
    abstract fun copy(): SocketOptions

    @Suppress("KDocMissingDocumentation")
    protected open fun copyCommon(from: SocketOptions) {
        typeOfService = from.typeOfService
        reuseAddress = from.reuseAddress
        reusePort = from.reusePort
    }

    private class GeneralSocketOptions constructor(customOptions: MutableMap<Any, Any?>) :
        SocketOptions(customOptions) {
        override fun copy(): GeneralSocketOptions = GeneralSocketOptions(HashMap(customOptions)).apply {
            copyCommon(this@GeneralSocketOptions)
        }
    }

    /**
     * ToS value, [TypeOfService.UNDEFINED] by default
     */
    var typeOfService: TypeOfService = TypeOfService.UNDEFINED

    /**
     * SO_REUSEADDR option
     */
    var reuseAddress: Boolean = false

    /**
     * SO_REUSEPORT option
     */
    var reusePort: Boolean = false

    /**
     * TCP server socket options
     */
    class TCPServerOptions private constructor(customOptions: MutableMap<Any, Any?>) : SocketOptions(customOptions) {
        internal constructor() : this(HashMap())

        override fun copy(): TCPServerOptions {
            return TCPServerOptions(HashMap(customOptions)).apply {
                copyCommon(this@TCPServerOptions)
            }
        }
    }

    /**
     * Represents TCP client or UDP socket options
     */
    open class ClientSocketOptions internal constructor(customOptions: MutableMap<Any, Any?>) :
        SocketOptions(customOptions) {
        internal constructor() : this(HashMap())

        /**
         * Socket ougoing buffer size (SO_SNDBUF)
         */
        var sendBufferSize: Int = -1

        /**
         * Socket incoming buffer size (SO_RCVBUF)
         */
        var receiveBufferSize: Int = -1

        @Suppress("KDocMissingDocumentation")
        override fun copyCommon(from: SocketOptions) {
            super.copyCommon(from)
            if (from is ClientSocketOptions) {
                sendBufferSize = from.sendBufferSize
                receiveBufferSize = from.receiveBufferSize
            }
        }

        override fun copy(): ClientSocketOptions {
            return ClientSocketOptions(HashMap(customOptions)).apply {
                copyCommon(this@ClientSocketOptions)
            }
        }
    }

    /**
     * Represents TCP client socket options
     */
    class TCPClientSocketOptions private constructor(customOptions: MutableMap<Any, Any?>) :
        ClientSocketOptions(customOptions) {
        /**
         * TCP_NODELAY socket option, useful to disable Nagle
         */
        var noDelay: Boolean = false

        /**
         * SO_LINGER option applied at socket close, not recommended to set to 0 however useful for debugging
         * Value of `-1` is the default and means that it is not set and system-dependant
         */
        var lingerSeconds: Int = -1

        /**
         * SO_KEEPALIVE option is to enable/disable TCP keep-alive
         */
        var keepAlive: Boolean? = null

        @Suppress("KDocMissingDocumentation")
        override fun copyCommon(from: SocketOptions) {
            super.copyCommon(from)
            if (from is TCPClientSocketOptions) {
                noDelay = from.noDelay
                lingerSeconds = from.lingerSeconds
                keepAlive = from.keepAlive
            }
        }

        override fun copy(): TCPClientSocketOptions {
            return TCPClientSocketOptions(HashMap(customOptions)).apply {
                copyCommon(this@TCPClientSocketOptions)
            }
        }
    }

    companion object {
        /**
         * Default socket options
         */
        @KtorExperimentalAPI
        @Deprecated("Not supported anymore", level = DeprecationLevel.ERROR)
        val Empty: SocketOptions
            get() = TODO("Not supported anymore")

        internal fun create(): SocketOptions = GeneralSocketOptions(HashMap())
    }
}
