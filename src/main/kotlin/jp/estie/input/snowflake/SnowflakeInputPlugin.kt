package jp.estie.input.snowflake

import org.embulk.input.jdbc.AbstractJdbcInputPlugin
import org.embulk.input.jdbc.JdbcInputConnection
import org.embulk.util.config.Config
import org.embulk.util.config.ConfigDefault
import java.sql.DriverManager
import java.util.*

open class SnowflakeInputPlugin : AbstractJdbcInputPlugin() {
    override fun newConnection(task: PluginTask): JdbcInputConnection {
        Class.forName("net.snowflake.client.jdbc.SnowflakeDriver")

        val t = task as SnowflakePluginTask
        val props = Properties()
        props["user"] = t.getUser()
        props["db"] = t.getDatabase()
        props["schema"] = t.getSchema()
        if (t.getRole().isNotEmpty()) {
            props["role"] = t.getRole()
        }
        if (t.getWarehouse().isNotEmpty()) {
            props["warehouse"] = t.getWarehouse()
        }

        if (t.getPassword().isNotEmpty()) {
            props["password"] = t.getPassword()
        }
        if (t.getPrivateKey().isNotEmpty()) {
            val passphrase = t.getPrivateKeyPassphrase().ifEmpty { null }
            val privateKey = PrivateKeyReader.loadPrivateKey(t.getPrivateKey(), passphrase).getOrThrow()
            props["privateKey"] = privateKey
        }
        if (t.getPrivateKeyPath().isNotEmpty()) {
            val passphrase = t.getPrivateKeyPassphrase().ifEmpty { null }
            val privateKey = PrivateKeyReader.loadPrivateKeyFromFile(t.getPrivateKeyPath(), passphrase).getOrThrow()
            props["privateKey"] = privateKey
        }

        val con = DriverManager.getConnection("jdbc:snowflake://${t.getHost()}/", props)
        return JdbcInputConnection(con, null)
    }

    interface SnowflakePluginTask : PluginTask {
        @Config("host")
        fun getHost(): String

        @Config("user")
        fun getUser(): String

        @Config("password")
        @ConfigDefault("\"\"")
        fun getPassword(): String

        @Config("database")
        fun getDatabase(): String

        @Config("schema")
        fun getSchema(): String

        @Config("role")
        @ConfigDefault("\"\"")
        fun getRole(): String

        @Config("warehouse")
        @ConfigDefault("\"\"")
        fun getWarehouse(): String

        @Config("privateKey")
        @ConfigDefault("\"\"")
        fun getPrivateKey(): String

        @Config("privateKeyPath")
        @ConfigDefault("\"\"")
        fun getPrivateKeyPath(): String

        @Config("privateKeyPassphrase")
        @ConfigDefault("\"\"")
        fun getPrivateKeyPassphrase(): String
    }

    override fun getTaskClass(): Class<out PluginTask> {
        return SnowflakePluginTask::class.java
    }
}