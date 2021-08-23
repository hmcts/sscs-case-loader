package uk.gov.hmcts.reform.sscs.services;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.sscs.ccd.domain.EventType.SENT_TO_DWP;
import static uk.gov.hmcts.reform.sscs.ccd.domain.EventType.VALID_APPEAL_CREATED;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.ccd.service.SearchCcdCaseService;
import uk.gov.hmcts.reform.sscs.ccd.service.UpdateCcdCaseService;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.refdata.RefDataFactory;
import uk.gov.hmcts.reform.sscs.services.ccd.CcdCasesSender;
import uk.gov.hmcts.reform.sscs.services.gaps2.files.Gaps2File;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpSshService;
import uk.gov.hmcts.reform.sscs.services.xml.XmlValidator;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class CaseLoaderServiceTest {

    @MockBean
    private SftpSshService sftpSshService;

    @MockBean
    private SftpChannelAdapter channelAdapter;

    @MockBean
    private XmlValidator xmlValidator;
    @MockBean
    private TransformationService transformService;
    @MockBean
    private CcdService ccdService;
    @MockBean
    private RefDataFactory refDataFactory;
    @Mock
    private Gaps2File file;
    @Mock
    private InputStream inputStream;
    @MockBean
    private SearchCcdCaseService searchCcdCaseService;
    @MockBean
    private UpdateCcdCaseService updateCcdCaseService;
    @MockBean
    private IdamService idamService;

    private SscsCaseData caseData;

    @Autowired
    private CaseLoaderService caseLoaderService;

    @Autowired
    private CcdCasesSender ccdCasesSender;

    @Before
    public void setUp() {
        when(sftpSshService.getFiles()).thenReturn(newArrayList(file, file));
        when(sftpSshService.readExtractFile(file)).thenReturn(inputStream);
        when(file.isDelta()).thenReturn(false).thenReturn(true);

        Appeal appeal = Appeal.builder()
            .benefitType(BenefitType.builder()
                .code("PIP")
                .build())
            .build();
        caseData = SscsCaseData.builder()
            .caseReference("caseRef")
            .appeal(appeal)
            .build();
    }

    @Test
    public void givenTwoDeltas_shouldRenewIdamTokenForEachDelta() {
        when(sftpSshService.getFiles()).thenReturn(newArrayList(file, file, file));
        when(sftpSshService.readExtractFile(file)).thenReturn(inputStream);
        when(file.isDelta()).thenReturn(false).thenReturn(true).thenReturn(true);

        given(transformService.transform(inputStream)).willReturn(newArrayList(caseData));

        given(ccdService.createCase(eq(caseData), eq(VALID_APPEAL_CREATED.getCcdType()), any(), any(),
            any(IdamTokens.class)))
            .willReturn(SscsCaseDetails.builder().id(1234L).build());

        IdamTokens idamTokens = IdamTokens.builder()
            .idamOauth2Token("oAuth2Token")
            .serviceAuthorization("s2sToken")
            .userId("16")
            .build();

        IdamTokens idamTokensRenewed = IdamTokens.builder()
            .idamOauth2Token("oAuth2Token2")
            .serviceAuthorization("s2sToken")
            .userId("16")
            .build();

        given(idamService.getIdamTokens())
            .willReturn(idamTokens)
            .willReturn(idamTokensRenewed);

        caseLoaderService.process();

        then(idamService).should(times(2)).getIdamTokens();

        ArgumentCaptor<IdamTokens> idamTokensArgumentCaptor = ArgumentCaptor.forClass(IdamTokens.class);
        then(searchCcdCaseService).should(times(2))
            .findListOfCasesByCaseRefOrCaseId(eq(caseData), idamTokensArgumentCaptor.capture());

        List<IdamTokens> values = idamTokensArgumentCaptor.getAllValues();
        assertThat(values.get(0).getIdamOauth2Token(), is(equalTo("oAuth2Token")));
        assertThat(values.get(1).getIdamOauth2Token(), is(equalTo("oAuth2Token2")));
    }

    @Test
    public void givenNumberFormatExceptionIsThrown_shouldCarryOnProcessingNextCasesAndSendCaseToDwp() {
        Appeal appeal = Appeal.builder()
            .benefitType(BenefitType.builder()
                .code("PIP")
                .build())
            .build();
        SscsCaseData caseDataWithInvalidScNumber = SscsCaseData.builder()
            .caseReference("invalidCaseRef")
            .appeal(appeal)
            .build();

        List<Event> events = new ArrayList<>();
        events.add(Event.builder().value(EventDetails.builder().type(SENT_TO_DWP.getCcdType()).build()).build());

        SscsCaseData updatedCaseData = SscsCaseData.builder()
            .events(events)
            .caseReference("caseRef")
            .appeal(appeal)
            .build();

        List<SscsCaseData> cases = Arrays.asList(caseDataWithInvalidScNumber, updatedCaseData);
        given(transformService.transform(inputStream)).willReturn(cases);

        given(idamService.getIdamTokens()).willReturn(IdamTokens.builder().build());

        given(searchCcdCaseService.findListOfCasesByCaseRefOrCaseId(eq(caseDataWithInvalidScNumber),
            any(IdamTokens.class))).willThrow(NumberFormatException.class);

        given(searchCcdCaseService.findListOfCasesByCaseRefOrCaseId(eq(updatedCaseData), any(IdamTokens.class)))
            .willReturn(singletonList(SscsCaseDetails.builder().data(caseData).id(123L).build()));

        caseLoaderService.process();

        verify(updateCcdCaseService).updateCase(eq(updatedCaseData), any(), eq(SENT_TO_DWP.getCcdType()), any(), any(),
            any(IdamTokens.class));
        verifyNoMoreInteractions(ccdService);
    }

    @Test
    public void givenCaseReferenceIfFoundMoreThanOne_shouldCarryOnProcessingNextCases() {
        final SscsCaseDetails caseDetails1 = SscsCaseDetails.builder().data(caseData).id(123L).build();
        final SscsCaseDetails caseDetails2 = SscsCaseDetails.builder().data(caseData).id(234L).build();
        Appeal appeal = Appeal.builder()
            .benefitType(BenefitType.builder()
                .code("PIP")
                .build())
            .build();
        SscsCaseData caseDataScNumber = SscsCaseData.builder()
            .caseReference("SC001/19/00365123")
            .appeal(appeal)
            .build();

        List<Event> events = new ArrayList<>();
        events.add(Event.builder().value(EventDetails.builder().type(SENT_TO_DWP.getCcdType()).build()).build());

        SscsCaseData updatedCaseData = SscsCaseData.builder()
            .events(events)
            .caseReference("caseRef")
            .appeal(appeal)
            .build();

        List<SscsCaseData> cases = Arrays.asList(caseDataScNumber, updatedCaseData);
        given(transformService.transform(inputStream)).willReturn(cases);

        given(idamService.getIdamTokens()).willReturn(IdamTokens.builder().build());

        given(searchCcdCaseService.findListOfCasesByCaseRefOrCaseId(eq(updatedCaseData), any(IdamTokens.class)))
            .willReturn(Lists.list(caseDetails1, caseDetails2));

        given(searchCcdCaseService.findListOfCasesByCaseRefOrCaseId(eq(updatedCaseData), any(IdamTokens.class)))
            .willReturn(singletonList(SscsCaseDetails.builder().data(caseData).id(123L).build()));

        caseLoaderService.process();

        verify(updateCcdCaseService).updateCase(eq(updatedCaseData), any(), eq(SENT_TO_DWP.getCcdType()), any(), any(),
            any(IdamTokens.class));
        verifyNoMoreInteractions(ccdService);
    }

    @Test
    public void givenCaseReferenceFoundVoidState_shouldNotProcess() {
        final SscsCaseDetails caseDetails1 = SscsCaseDetails.builder().data(caseData).id(123L).build();
        Appeal appeal = Appeal.builder()
            .benefitType(BenefitType.builder()
                .code("PIP")
                .build())
            .build();
        SscsCaseData caseDataScNumber = SscsCaseData.builder()
            .caseReference("SC001/19/00365123")
            .appeal(appeal)
            .build();

        List<Event> events = new ArrayList<>();
        events.add(Event.builder().value(EventDetails.builder().type(SENT_TO_DWP.getCcdType()).build()).build());

        SscsCaseData updatedCaseData = SscsCaseData.builder()
            .events(events)
            .caseReference("caseRef")
            .appeal(appeal)
            .build();

        List<SscsCaseData> cases = Arrays.asList(caseDataScNumber, updatedCaseData);
        given(transformService.transform(inputStream)).willReturn(cases);

        given(idamService.getIdamTokens()).willReturn(IdamTokens.builder().build());

        given(searchCcdCaseService.findListOfCasesByCaseRefOrCaseId(eq(updatedCaseData), any(IdamTokens.class)))
            .willReturn(Lists.list(caseDetails1));

        given(searchCcdCaseService.findListOfCasesByCaseRefOrCaseId(eq(updatedCaseData), any(IdamTokens.class)))
            .willReturn(singletonList(SscsCaseDetails.builder().data(caseData).id(123L).state("voidState").build()));

        caseLoaderService.process();

        verifyNoMoreInteractions(updateCcdCaseService);
    }
}
