package jp.estie.input.snowflake

import kotlin.test.Test
import kotlin.test.assertEquals

internal class PrivateKeyReaderTest {
    @Test
    fun testLoadPrivateKeyFromFile() {
        // Arrange
        val keyName = javaClass.getResource("/snowflake_key")!!.path
        val encryptedKeyName = javaClass.getResource("/snowflake_key.p8")!!.path

        // Act
        val key = PrivateKeyReader.loadPrivateKeyFromFile(keyName)
        val decrypted = PrivateKeyReader.loadPrivateKeyFromFile(encryptedKeyName, "12345")

        // Assert
        assertEquals(key.getOrThrow(), decrypted.getOrThrow())
    }
}