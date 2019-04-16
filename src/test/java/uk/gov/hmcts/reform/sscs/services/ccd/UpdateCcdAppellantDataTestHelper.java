package uk.gov.hmcts.reform.sscs.services.ccd;

import uk.gov.hmcts.reform.sscs.ccd.domain.*;

final class UpdateCcdAppellantDataTestHelper {

    private UpdateCcdAppellantDataTestHelper() {
    }

    static GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataUpdatesHappyPaths() {
        GapsAppellantData gapsAppellantData = new GapsAppellantData(
            "first-name", "last-name", "email@email.com", "AB46575S");

        ExpectedExistingCcdAppellantName expectedExistingCcdAppellantName =
            new ExpectedExistingCcdAppellantName("first-name", "last-name",
                "email@email.com", "AB46575S");

        ExistingCcdAppellantData existingCcdAppellantData = new ExistingCcdAppellantData(
            "existingFirstName", "existingLastName", "existingCaseEmail@email.com",
            "CA 36 98 74 A");
        return new GapsAndCcdDataUpdateScenario(
            gapsAppellantData, expectedExistingCcdAppellantName, existingCcdAppellantData);
    }

    static GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataUpdatesWithEmptyFields() {
        GapsAppellantData gapsAppellantData = new GapsAppellantData(
            "", "", "", "");

        ExpectedExistingCcdAppellantName expectedExistingCcdAppellantName =
            new ExpectedExistingCcdAppellantName("existingFirstName", "existingLastName",
                "existingCaseEmail@email.com", "CA 36 98 74 A");

        ExistingCcdAppellantData existingCcdAppellantData = new ExistingCcdAppellantData(
            "existingFirstName", "existingLastName", "existingCaseEmail@email.com",
            "CA 36 98 74 A");
        return new GapsAndCcdDataUpdateScenario(
            gapsAppellantData, expectedExistingCcdAppellantName, existingCcdAppellantData);
    }

    static GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataUpdatesWithNullFields() {
        GapsAppellantData gapsAppellantData = new GapsAppellantData(
            null, null, null, null);

        ExpectedExistingCcdAppellantName expectedExistingCcdAppellantName =
            new ExpectedExistingCcdAppellantName("existingFirstName", "existingLastName",
                "existingCaseEmail@email.com", "CA 36 98 74 A");

        ExistingCcdAppellantData existingCcdAppellantData = new ExistingCcdAppellantData(
            "existingFirstName", "existingLastName", "existingCaseEmail@email.com",
            "CA 36 98 74 A");
        return new GapsAndCcdDataUpdateScenario(
            gapsAppellantData, expectedExistingCcdAppellantName, existingCcdAppellantData);
    }

    static GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreExistingCcdDataUpdatesWithEmptyFields() {
        GapsAppellantData gapsAppellantData = new GapsAppellantData(
            "first-name", "last-name", "email@email.com", "AB46575S");

        ExpectedExistingCcdAppellantName expectedExistingCcdAppellantName =
            new ExpectedExistingCcdAppellantName("first-name", "last-name",
                "email@email.com", "AB46575S");

        ExistingCcdAppellantData existingCcdAppellantData = new ExistingCcdAppellantData(
            "", "", "", "");
        return new GapsAndCcdDataUpdateScenario(
            gapsAppellantData, expectedExistingCcdAppellantName, existingCcdAppellantData);
    }

    static GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreExistingCcdDataUpdatesWithNullFields() {
        GapsAppellantData gapsAppellantData = new GapsAppellantData(
            "first-name", "last-name", "email@email.com", "AB46575S");

        ExpectedExistingCcdAppellantName expectedExistingCcdAppellantName =
            new ExpectedExistingCcdAppellantName("first-name", "last-name",
                "email@email.com", "AB46575S");

        ExistingCcdAppellantData existingCcdAppellantData = new ExistingCcdAppellantData(
            "null", "null", "null", "null");
        return new GapsAndCcdDataUpdateScenario(
            gapsAppellantData, expectedExistingCcdAppellantName, existingCcdAppellantData);
    }

    static GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataUpdatesWithNewAppointeeHappyPaths() {
        Appointee appointeeNewData = Appointee.builder()
            .name(Name.builder().firstName("Ap").lastName("Pointee").build())
            .address(Address.builder().line1("1 Appointee St").postcode("TS1 1ST").build())
            .contact(Contact.builder().email("appointee@test.com").mobile("07000000001").phone("01000000001").build())
            .identity(Identity.builder().dob("01/01/1998").nino("AB999999C").build())
            .build();

        GapsAppellantData gapsAppellantData = new GapsAppellantData(
            "first-name", "last-name", "email@email.com", "AB46575S", appointeeNewData);

        ExpectedExistingCcdAppellantName expectedExistingCcdAppellantName =
            new ExpectedExistingCcdAppellantName("first-name", "last-name",
                "email@email.com", "AB46575S", appointeeNewData);

        ExistingCcdAppellantData existingCcdAppellantData = new ExistingCcdAppellantData(
            "existingFirstName", "existingLastName", "existingCaseEmail@email.com",
            "CA 36 98 74 A", null);
        return new GapsAndCcdDataUpdateScenario(
            gapsAppellantData, expectedExistingCcdAppellantName, existingCcdAppellantData);
    }

    static GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataWithUpdatedAppointeeContactHappyPaths() {
        Appointee appointeeNewData = Appointee.builder()
            .name(Name.builder().firstName("Ap").lastName("Pointee").build())
            .address(Address.builder().line1("1 Appointee St").postcode("TS1 1ST").build())
            .contact(Contact.builder().email("appointee@test.com").mobile("07000000001").phone("01000000001").build())
            .identity(Identity.builder().dob("01/01/1998").nino("AB999999C").build())
            .build();

        GapsAppellantData gapsAppellantData = new GapsAppellantData(
            "first-name", "last-name", "email@email.com", "AB46575S", appointeeNewData);

        ExpectedExistingCcdAppellantName expectedExistingCcdAppellantName =
            new ExpectedExistingCcdAppellantName("first-name", "last-name",
                "email@email.com", "AB46575S", appointeeNewData);

        Appointee appointeeExistingData = Appointee.builder()
            .name(Name.builder().firstName("Ap").lastName("Pointee").build())
            .address(Address.builder().line1("1 Appointee St").postcode("TS1 1ST").build())
            .identity(Identity.builder().dob("01/01/1998").nino("AB999999C").build())
            .build();

        ExistingCcdAppellantData existingCcdAppellantData = new ExistingCcdAppellantData(
            "existingFirstName", "existingLastName", "existingCaseEmail@email.com",
            "CA 36 98 74 A", appointeeExistingData);
        return new GapsAndCcdDataUpdateScenario(
            gapsAppellantData, expectedExistingCcdAppellantName, existingCcdAppellantData);
    }

    static GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataWithUpdatedAppointeeNameHappyPaths() {
        Appointee appointeeNewData = Appointee.builder()
            .name(Name.builder().firstName("Ap").lastName("Pointee").build())
            .address(Address.builder().line1("1 Appointee St").postcode("TS1 1ST").build())
            .contact(Contact.builder().email("appointee@test.com").mobile("07000000001").phone("01000000001").build())
            .identity(Identity.builder().dob("01/01/1998").nino("AB999999C").build())
            .build();

        GapsAppellantData gapsAppellantData = new GapsAppellantData(
            "first-name", "last-name", "email@email.com", "AB46575S", appointeeNewData);

        ExpectedExistingCcdAppellantName expectedExistingCcdAppellantName =
            new ExpectedExistingCcdAppellantName("first-name", "last-name",
                "email@email.com", "AB46575S", appointeeNewData);

        Appointee appointeeExistingData = Appointee.builder()
            .address(Address.builder().line1("1 Appointee St").postcode("TS1 1ST").build())
            .contact(Contact.builder().email("appointee@test.com").mobile("07000000001").phone("01000000001").build())
            .identity(Identity.builder().dob("01/01/1998").nino("AB999999C").build())
            .build();

        ExistingCcdAppellantData existingCcdAppellantData = new ExistingCcdAppellantData(
            "existingFirstName", "existingLastName", "existingCaseEmail@email.com",
            "CA 36 98 74 A", appointeeExistingData);
        return new GapsAndCcdDataUpdateScenario(
            gapsAppellantData, expectedExistingCcdAppellantName, existingCcdAppellantData);
    }

    static GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataWithUpdatedAppointeeIdentityHappyPaths() {
        Appointee appointeeNewData = Appointee.builder()
            .name(Name.builder().firstName("Ap").lastName("Pointee").build())
            .address(Address.builder().line1("1 Appointee St").postcode("TS1 1ST").build())
            .contact(Contact.builder().email("appointee@test.com").mobile("07000000001").phone("01000000001").build())
            .identity(Identity.builder().dob("01/01/1998").nino("AB999999C").build())
            .build();

        GapsAppellantData gapsAppellantData = new GapsAppellantData(
            "first-name", "last-name", "email@email.com", "AB46575S", appointeeNewData);

        ExpectedExistingCcdAppellantName expectedExistingCcdAppellantName =
            new ExpectedExistingCcdAppellantName("first-name", "last-name",
                "email@email.com", "AB46575S", appointeeNewData);

        Appointee appointeeExistingData = Appointee.builder()
            .name(Name.builder().firstName("Ap").lastName("Pointee").build())
            .address(Address.builder().line1("1 Appointee St").postcode("TS1 1ST").build())
            .contact(Contact.builder().email("appointee@test.com").mobile("07000000001").phone("01000000001").build())
            .build();

        ExistingCcdAppellantData existingCcdAppellantData = new ExistingCcdAppellantData(
            "existingFirstName", "existingLastName", "existingCaseEmail@email.com",
            "CA 36 98 74 A", appointeeExistingData);
        return new GapsAndCcdDataUpdateScenario(
            gapsAppellantData, expectedExistingCcdAppellantName, existingCcdAppellantData);
    }

    static class GapsAppellantData {
        String firstName;
        String lastName;
        String contactEmail;
        String nino;
        Appointee appointee;

        GapsAppellantData(String firstName, String lastName, String contactEmail, String nino) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.contactEmail = contactEmail;
            this.nino = nino;
        }

        GapsAppellantData(String firstName, String lastName, String contactEmail, String nino, Appointee appointee) {
            this(firstName, lastName, contactEmail, nino);
            this.appointee = appointee;
        }
    }

    static class ExpectedExistingCcdAppellantName {
        String firstName;
        String lastName;
        String contactEmail;
        String nino;
        Appointee appointee;

        ExpectedExistingCcdAppellantName(String firstName, String lastName, String contactEmail, String nino) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.contactEmail = contactEmail;
            this.nino = nino;
        }

        ExpectedExistingCcdAppellantName(String firstName, String lastName, String contactEmail, String nino,
                                         Appointee appointee) {
            this(firstName, lastName, contactEmail, nino);
            this.appointee = appointee;
        }
    }

    static class ExistingCcdAppellantData {
        String firstName;
        String lastName;
        String contactEmail;
        String nino;
        Appointee appointee;

        ExistingCcdAppellantData(String firstName, String lastName, String contactEmail, String nino) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.contactEmail = contactEmail;
            this.nino = nino;
        }

        ExistingCcdAppellantData(String firstName, String lastName, String contactEmail, String nino,
                                 Appointee appointee) {
            this(firstName, lastName, contactEmail, nino);
            this.appointee = appointee;
        }
    }

    static class GapsAndCcdDataUpdateScenario {
        GapsAppellantData gapsAppellantData;
        ExpectedExistingCcdAppellantName expectedExistingCcdAppellantName;
        ExistingCcdAppellantData existingCcdAppellantData;

        GapsAndCcdDataUpdateScenario(GapsAppellantData gapsAppellantData,
                                     ExpectedExistingCcdAppellantName expectedExistingCcdAppellantName,
                                     ExistingCcdAppellantData existingCcdAppellantData) {
            this.gapsAppellantData = gapsAppellantData;
            this.expectedExistingCcdAppellantName = expectedExistingCcdAppellantName;
            this.existingCcdAppellantData = existingCcdAppellantData;
        }
    }
}
