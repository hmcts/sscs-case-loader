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

    static GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataWithNewAppointeeContactHappyPaths() {
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

    static GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataWithNewAppointeeNameHappyPaths() {
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

    static GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataWithNewAppointeeIdentityHappyPaths() {
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

    static GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataWithUpdatedMobileHappyPaths() {
        GapsAppellantData gapsAppellantData = new GapsAppellantData(
            "first-name", "last-name", "email@email.com", "AB46575S",
            "07000000001", null);

        ExpectedExistingCcdAppellantName expectedExistingCcdAppellantName =
            new ExpectedExistingCcdAppellantName("first-name", "last-name",
                "email@email.com", "AB46575S", "07000000002", null);

        ExistingCcdAppellantData existingCcdAppellantData = new ExistingCcdAppellantData(
            "first-name", "last-name", "email@email.com",
            "AB46575S", "07000000002", null);
        return new GapsAndCcdDataUpdateScenario(
            gapsAppellantData, expectedExistingCcdAppellantName, existingCcdAppellantData);
    }

    static GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataWithUpdatedPhoneHappyPaths() {
        GapsAppellantData gapsAppellantData = new GapsAppellantData(
            "first-name", "last-name", "email@email.com", "AB46575S",
            null, "01000000001");

        ExpectedExistingCcdAppellantName expectedExistingCcdAppellantName =
            new ExpectedExistingCcdAppellantName("first-name", "last-name",
                "email@email.com", "AB46575S", null, "01000000001");

        ExistingCcdAppellantData existingCcdAppellantData = new ExistingCcdAppellantData(
            "first-name", "last-name", "email@email.com",
            "AB46575S", null, "01000000001");
        return new GapsAndCcdDataUpdateScenario(
            gapsAppellantData, expectedExistingCcdAppellantName, existingCcdAppellantData);
    }

    static GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataWithUpdatedAppointeeNinoHappyPaths() {
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
            .identity(Identity.builder().dob("01/01/1998").nino("AB000000C").build())
            .build();

        ExistingCcdAppellantData existingCcdAppellantData = new ExistingCcdAppellantData(
            "existingFirstName", "existingLastName", "existingCaseEmail@email.com",
            "CA 36 98 74 A", appointeeExistingData);
        return new GapsAndCcdDataUpdateScenario(
            gapsAppellantData, expectedExistingCcdAppellantName, existingCcdAppellantData);
    }

    static GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataWithUpdatedAppointeeEmailHappyPaths() {
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
            .contact(Contact.builder().email("test@test.com").mobile("07000000001").phone("01000000001").build())
            .identity(Identity.builder().dob("01/01/1998").nino("AB999999C").build())
            .build();

        ExistingCcdAppellantData existingCcdAppellantData = new ExistingCcdAppellantData(
            "existingFirstName", "existingLastName", "existingCaseEmail@email.com",
            "CA 36 98 74 A", appointeeExistingData);
        return new GapsAndCcdDataUpdateScenario(
            gapsAppellantData, expectedExistingCcdAppellantName, existingCcdAppellantData);
    }

    static GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataWithUpdatedAppointeeMobileHappyPaths() {
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
            .contact(Contact.builder().email("appointee@test.com").mobile("07000000000").phone("01000000001").build())
            .identity(Identity.builder().dob("01/01/1998").nino("AB999999C").build())
            .build();

        ExistingCcdAppellantData existingCcdAppellantData = new ExistingCcdAppellantData(
            "existingFirstName", "existingLastName", "existingCaseEmail@email.com",
            "CA 36 98 74 A", appointeeExistingData);
        return new GapsAndCcdDataUpdateScenario(
            gapsAppellantData, expectedExistingCcdAppellantName, existingCcdAppellantData);
    }

    static GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataWithUpdatedAppointeePhoneHappyPaths() {
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
            .contact(Contact.builder().email("appointee@test.com").mobile("07000000001").phone("01000000000").build())
            .identity(Identity.builder().dob("01/01/1998").nino("AB999999C").build())
            .build();

        ExistingCcdAppellantData existingCcdAppellantData = new ExistingCcdAppellantData(
            "existingFirstName", "existingLastName", "existingCaseEmail@email.com",
            "CA 36 98 74 A", appointeeExistingData);
        return new GapsAndCcdDataUpdateScenario(
            gapsAppellantData, expectedExistingCcdAppellantName, existingCcdAppellantData);
    }

    static GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataWithUpdatedAppointeeFirstNameHappyPaths() {
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
            .name(Name.builder().firstName("Zz").lastName("Pointee").build())
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

    static GapsAndCcdDataUpdateScenario updateCcdDataWhenThereAreGapsDataWithUpdatedAppointeeLastNameHappyPaths() {
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
            .name(Name.builder().firstName("Ap").lastName("Zzzzzzz").build())
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

    static class GapsAppellantData {
        String firstName;
        String lastName;
        String email;
        String mobile;
        String phone;
        String nino;
        Appointee appointee;

        GapsAppellantData(String firstName, String lastName, String email, String nino) {
            this(firstName, lastName, email, nino, null, null, null);
        }

        GapsAppellantData(String firstName, String lastName, String email, String nino, String mobile, String phone) {
            this(firstName, lastName, email, nino, null, mobile, phone);
        }

        GapsAppellantData(String firstName, String lastName, String email, String nino, Appointee appointee) {
            this(firstName, lastName, email, nino, appointee, null, null);
        }

        GapsAppellantData(String firstName, String lastName, String email, String nino, Appointee appointee,
                          String mobile, String phone) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.mobile = mobile;
            this.phone = phone;
            this.nino = nino;
            this.appointee = appointee;
        }
    }

    static class ExpectedExistingCcdAppellantName {
        String firstName;
        String lastName;
        String email;
        String mobile;
        String phone;
        String nino;
        Appointee appointee;

        ExpectedExistingCcdAppellantName(String firstName, String lastName, String email, String nino) {
            this(firstName, lastName, email, nino, null, null, null);
        }

        ExpectedExistingCcdAppellantName(
            String firstName, String lastName, String email, String nino, String mobile, String phone
        ) {
            this(firstName, lastName, email, nino, null, mobile, phone);
        }

        ExpectedExistingCcdAppellantName(String firstName, String lastName, String email, String nino,
                                         Appointee appointee) {
            this(firstName, lastName, email, nino, appointee, null, null);
        }

        ExpectedExistingCcdAppellantName(
            String firstName, String lastName, String email, String nino,
            Appointee appointee, String mobile, String phone
        ) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.nino = nino;
            this.mobile = mobile;
            this.phone = phone;
            this.appointee = appointee;
        }
    }

    static class ExistingCcdAppellantData {
        String firstName;
        String lastName;
        String email;
        String mobile;
        String phone;
        String nino;
        Appointee appointee;

        ExistingCcdAppellantData(String firstName, String lastName, String email, String nino) {
            this(firstName, lastName, email, nino, null, null, null);
        }

        ExistingCcdAppellantData(
            String firstName, String lastName, String email, String nino, String mobile, String phone
        ) {
            this(firstName, lastName, email, nino, null, mobile, phone);
        }

        ExistingCcdAppellantData(String firstName, String lastName, String email, String nino, Appointee appointee) {
            this(firstName, lastName, email, nino, appointee, null, null);
        }

        ExistingCcdAppellantData(
            String firstName, String lastName, String email, String nino,
            Appointee appointee, String mobile, String phone
        ) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.mobile = mobile;
            this.phone = phone;
            this.nino = nino;
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
