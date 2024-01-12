package jp.estie.input.snowflake

import net.snowflake.client.jdbc.internal.org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import net.snowflake.client.jdbc.internal.org.bouncycastle.jce.provider.BouncyCastleProvider
import net.snowflake.client.jdbc.internal.org.bouncycastle.openssl.PEMParser
import net.snowflake.client.jdbc.internal.org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import net.snowflake.client.jdbc.internal.org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder
import net.snowflake.client.jdbc.internal.org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo
import java.io.FileReader
import java.io.Reader
import java.nio.file.Paths
import java.security.PrivateKey
import java.security.Security


/**
 * A utility class to read a private key from a file.
 *
 * It is used to authenticate with Snowflake using a private key.
 *
 * This class is implemented based on the official example:
 * https://docs.snowflake.com/en/developer-guide/jdbc/jdbc-configure#label-jdbc-private-key-auth-example
 */
object PrivateKeyReader {
    fun loadPrivateKeyFromFile(filename: String, passphrase: String? = null): Result<PrivateKey> {
        return FileReader(Paths.get(filename).toFile()).use { reader -> loadPrivateKeyFromReader(reader, passphrase) }
    }

    fun loadPrivateKey(privateKey: String, passphrase: String? = null): Result<PrivateKey> {
        return privateKey.reader().use { reader -> loadPrivateKeyFromReader(reader, passphrase) }
    }
}

private fun loadPrivateKeyFromReader(reader: Reader, passphrase: String?): Result<PrivateKey> {
    Security.addProvider(BouncyCastleProvider())

    val pemObj = PEMParser(reader).use { it -> runCatching { it.readObject() } }
    val privateKeyInfo = pemObj.andThen {
        if (it is PKCS8EncryptedPrivateKeyInfo && passphrase != null) {
            return@andThen decrypt(it, passphrase)
        } else if (it is PrivateKeyInfo) {
            return@andThen Result.success(it)
        } else {
            return@andThen Result.failure(RuntimeException("Unsupported PEM object: $it"))
        }
    }

    val converter = JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME)
    return privateKeyInfo.mapCatching { converter.getPrivateKey(it) }
}

private fun decrypt(keyInfo: PKCS8EncryptedPrivateKeyInfo, passphrase: String): Result<PrivateKeyInfo> {
    val provider = runCatching { JceOpenSSLPKCS8DecryptorProviderBuilder().build(passphrase.toCharArray()) }
    return provider.mapCatching { keyInfo.decryptPrivateKeyInfo(it) }
}

private fun <V, U> Result<V>.andThen(transform: (V) -> Result<U>): Result<U> {
    return try {
        transform(getOrThrow())
    } catch (e: Throwable) {
        Result.failure(e)
    }
}