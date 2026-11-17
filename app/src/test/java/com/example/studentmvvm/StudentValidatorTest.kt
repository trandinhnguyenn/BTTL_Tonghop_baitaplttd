package com.example.studentmvvm

import com.example.studentmvvm.model.StudentValidator
import org.junit.Assert.*
import org.junit.Test

class StudentValidatorTest {
    @Test fun acceptsAuthorCodeAndVietnameseName() {
        assertNull(StudentValidator.validate("Trần Đình Nguyên", "2415053122225", "nguyen.demo@example.com", 3.2))
    }
    @Test fun rejectsMalformedInput() {
        assertNotNull(StudentValidator.validate("", "2415", "a@example.com", 3.0))
        assertNotNull(StudentValidator.validate("Nguyên", "", "a@example.com", 3.0))
        assertNotNull(StudentValidator.validate("Nguyên", "2415", "invalid", 3.0))
        assertNotNull(StudentValidator.validate("Nguyên", "2415", "a @example.com", 3.0))
        assertNotNull(StudentValidator.validate("Nguyên", "2415", "a@example.com", -0.1))
        assertNotNull(StudentValidator.validate("Nguyên", "2415", "a@example.com", 4.1))
        assertNotNull(StudentValidator.validate("Nguyên", "2415", "a@example.com", Double.NaN))
        assertNotNull(StudentValidator.validate("Nguyên", "2415", "a@example.com", null))
    }
    @Test fun parsesBothDecimalSeparators() {
        assertEquals(3.25, StudentValidator.parseGpa("3,25")!!, 0.001)
        assertEquals(3.25, StudentValidator.parseGpa("3.25")!!, 0.001)
        assertNull(StudentValidator.parseGpa("abc"))
    }
}
