package uk.gov.hmcts.reform.sscs.services.idam;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.exceptions.JwtDecodingException;

@Service
public class AuthTokenSubjectExtractor {

    public String extract(String token) {
        try {

            DecodedJWT jwt = JWT.decode(token);
            return jwt.getSubject();

        } catch (JWTDecodeException e) {
            throw new JwtDecodingException(e.getMessage(), e);
        }
    }
}
