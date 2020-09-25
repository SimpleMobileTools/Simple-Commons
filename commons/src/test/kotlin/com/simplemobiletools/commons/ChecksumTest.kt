package com.simplemobiletools.commons

import com.simplemobiletools.commons.extensions.md5
import com.simplemobiletools.commons.extensions.sha1
import com.simplemobiletools.commons.extensions.sha256
import com.simplemobiletools.commons.extensions.sha512
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File


class ChecksumTest {

    private var file: File? = File.createTempFile("test-", ".tmp").also { file ->
                file.writer().use { it.write("This is the content of a file." + 0x0a.toChar()) }
            }

    @Test
    fun md5() {
        assertEquals("a149f5161e873921d84636b2a1b3aad2", this.file?.md5())
    }

    @Test
    fun sha1() {
        assertEquals("79a6c02870843170c2e4becba621a645e03c1b19", this.file?.sha1())
    }

    @Test
    fun sha256() {
        assertEquals("50162bebb1a3bc846389f62db73851f932ebbb8e5a4d31a7805d79399c30663d", this.file?.sha256())
    }

    @Test
    fun sha512() {
        assertEquals("4ed35f5ed969568d42b26160d5303fcde1742176ab92d4a0010b938b7d15be94e6e14220f9ad09bd32abeeae97890d4e3823d96493e580695c8544f3bf18960a", this.file?.sha512())
    }

}
