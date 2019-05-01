package uk.gov.hmcts.reform.sscs.services.ccd;

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


    static class GapsAppellantData {
        String firstName;
        String lastName;
        String contactEmail;
        String nino;

        GapsAppellantData(String firstName, String lastName, String contactEmail, String nino) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.contactEmail = contactEmail;
            this.nino = nino;
        }
    }

    static class ExpectedExistingCcdAppellantName {
        String firstName;
        String lastName;
        String contactEmail;
        String nino;

        ExpectedExistingCcdAppellantName(String firstName, String lastName, String contactEmail, String nino) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.contactEmail = contactEmail;
            this.nino = nino;
        }
    }

    static class ExistingCcdAppellantData {
        String firstName;
        String lastName;
        String contactEmail;
        String nino;

        ExistingCcdAppellantData(String firstName, String lastName, String contactEmail, String nino) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.contactEmail = contactEmail;
            this.nino = nino;
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
