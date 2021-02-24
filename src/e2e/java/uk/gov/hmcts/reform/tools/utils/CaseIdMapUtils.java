package uk.gov.hmcts.reform.tools.utils;

import java.io.*;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CaseIdMapUtils {

    private static final String ID_FILE_NAME = "ccdCaseId.tmp";

    private CaseIdMapUtils() {
    }

    public static void write(HashMap<String,String> map) throws IOException {
        try {
            File fileOne = new File(ID_FILE_NAME);
            FileOutputStream fos = new FileOutputStream(fileOne);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(map);
            oos.flush();
            oos.close();
            fos.close();
        } catch (IOException e) {
            log.error("Failed store case id : " + e.getMessage());
            throw e;
        }
    }

    public static HashMap<String,String> read() throws IOException, ClassNotFoundException {
        try {
            File toRead = new File(ID_FILE_NAME);
            FileInputStream fis = new FileInputStream(toRead);
            ObjectInputStream ois = new ObjectInputStream(fis);

            HashMap<String,String> mapInFile = (HashMap<String,String>)ois.readObject();

            ois.close();
            fis.close();
            return mapInFile;
        } catch (IOException | ClassNotFoundException e) {
            log.error("Failed read case id : " + e.getMessage());
            throw e;
        }
    }
}
