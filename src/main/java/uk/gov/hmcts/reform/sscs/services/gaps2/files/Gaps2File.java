package uk.gov.hmcts.reform.sscs.services.gaps2.files;

import java.text.ParsePosition;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Gaps2File implements Comparable<Gaps2File> {

    private static DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");

    private final String name;
    private final LocalDateTime date;

    public Gaps2File(String name) {
        this.name = name;
        this.date = LocalDateTime.from(format.parse(name, new ParsePosition(name.lastIndexOf('_') + 1)));
    }

    public String getName() {
        return name;
    }

    public boolean isDelta() {
        return name.contains("Delta");
    }

    public LocalDateTime getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "Gaps2File{"
            + "name='" + name + '\''
            + ", date=" + date
            + '}';
    }

    @Override
    public int compareTo(Gaps2File o) {
        if (this.equals(o)) {
            return 0;
        }
        int result = date.compareTo(o.getDate());
        if (result != 0 || isDelta() == o.isDelta()) {
            return result;
        }
        return isDelta() ? 1 : -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return name.equals(((Gaps2File) o).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
