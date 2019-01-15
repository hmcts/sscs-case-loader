package uk.gov.hmcts.reform.sscs.util;

import org.junit.Assert;
import org.junit.Test;

public class UkMobileTest {

    @Test
    public void shouldAcceptValidUkMobileNumber() {

        Assert.assertTrue(UkMobile.validate("07123456789"));
        Assert.assertTrue(UkMobile.validate("07123 456789"));
        Assert.assertTrue(UkMobile.validate("07123 456 789"));
        Assert.assertTrue(UkMobile.validate("+447123456789"));
        Assert.assertTrue(UkMobile.validate("+44 7123 456 789"));
    }

    @Test
    public void shouldNotAcceptIncompleteNumbers() {

        Assert.assertFalse(UkMobile.validate("7123456789"));
        Assert.assertFalse(UkMobile.validate("447123456789"));
        Assert.assertFalse(UkMobile.validate("4407123456789"));
    }

    @Test
    public void shouldNotAcceptNonSpaces() {

        Assert.assertFalse(UkMobile.validate("07-123-456789"));
        Assert.assertFalse(UkMobile.validate("07.123.456789"));
        Assert.assertFalse(UkMobile.validate("(0)7 123 456789"));
    }

    @Test
    public void shouldAcceptLandLineNumbers() {

        Assert.assertTrue(UkMobile.validate("01234567890"));
        Assert.assertTrue(UkMobile.validate("08001234567"));
    }
}
