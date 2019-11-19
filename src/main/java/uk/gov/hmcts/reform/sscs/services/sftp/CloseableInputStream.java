package uk.gov.hmcts.reform.sscs.services.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CloseableInputStream extends BufferedInputStream {
    Session session;
    ChannelSftp sftp;
    boolean closeSession = true;

    public CloseableInputStream(InputStream is, Session session, ChannelSftp sftp, boolean closeSession) {
        super(is);
        this.session = session;
        this.sftp = sftp;
        this.closeSession = closeSession;
    }

    @Override
    public void close() throws IOException {
        if (closeSession) {
            try {
                super.close();
            } catch (Exception ex) {
                log.error("Error closing a input stream");
            } finally {
                super.close();
            }
        }
    }
}

