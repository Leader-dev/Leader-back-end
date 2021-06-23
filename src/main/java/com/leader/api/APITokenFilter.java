package com.leader.api;

import com.leader.api.data.token.TokenRecord;
import com.leader.api.data.token.TokenRecordRepository;
import com.leader.api.util.component.ClientDataUtil;
import com.leader.api.util.component.DateUtil;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import static com.leader.api.util.ExceptionUtil.ignoreExceptionSilent;

@Component
public class APITokenFilter implements Filter {

    public static final String TOKEN_HEADER_KEY = "API-Token";
    public static final String SET_TOKEN_HEADER_KEY = "Set-API-Token";

    private static final long TOKEN_EXPIRE_MILLISECONDS = 2592000000L;

    private final TokenRecordRepository recordRepository;
    private final DateUtil dateUtil;
    private final ClientDataUtil clientDataUtil;

    @Autowired
    public APITokenFilter(TokenRecordRepository recordRepository, DateUtil dateUtil, ClientDataUtil clientDataUtil) {
        this.recordRepository = recordRepository;
        this.dateUtil = dateUtil;
        this.clientDataUtil = clientDataUtil;
    }

    private TokenRecord createNewRecord() {
        // generate a new token record
        TokenRecord record = new TokenRecord();

        // generate new id
        UUID id;
        do {  // random UUID is almost impossible to collide but write a loop anyway
            id = UUID.randomUUID();
        } while (recordRepository.existsById(id));
        record.id = id;

        // generate create and accessed date
        Date newDate = dateUtil.getCurrentDate();
        record.created = newDate;
        record.accessed = newDate;

        // create clientData
        record.data = new Document();

        return record;
    }

    private TokenRecord getValidExistingRecord(String stringId) {
        return ignoreExceptionSilent(() -> {
            UUID id = UUID.fromString(stringId);
            TokenRecord record = recordRepository.findFirstById(id);
            long timePassedSinceLastAccess = dateUtil.getCurrentTime() - record.accessed.getTime();
            if (timePassedSinceLastAccess > TOKEN_EXPIRE_MILLISECONDS) {  // check if expired
                return null;
            }
            // update access date
            record.accessed = dateUtil.getCurrentDate();
            return record;
        });
    }

    private void saveRecord(TokenRecord record) {
        recordRepository.save(record);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        // CAST TYPES

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        // BEFORE SERVLET

        String tokenHeader = httpServletRequest.getHeader(TOKEN_HEADER_KEY);
        TokenRecord record = getValidExistingRecord(tokenHeader);
        if (record == null) {  // no existing record
            record = createNewRecord();

            // set token header into response
            httpServletResponse.setHeader(SET_TOKEN_HEADER_KEY, record.id.toString());
        }
        // set clientData into request attribute so that other components can access it
        clientDataUtil.setClientData(record.data);

        // CALL SERVLET

        filterChain.doFilter(request, response);

        // AFTER SERVLET

        // get (modified) clientData and save record to repository
        record.data = clientDataUtil.popClientData();
        saveRecord(record);
    }
}

