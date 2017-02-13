
/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) <2015>  eGovernments Foundation
 *
 *     The updated version of eGov suite of products as by eGovernments Foundation
 *     is available at http://www.egovernments.org
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see http://www.gnu.org/licenses/ or
 *     http://www.gnu.org/licenses/gpl.html .
 *
 *     In addition to the terms of the GPL license to be adhered to in using this
 *     program, the following additional terms are to be complied with:
 *
 *         1) All versions of this program, verbatim or modified must carry this
 *            Legal Notice.
 *
 *         2) Any misrepresentation of the origin of the material is prohibited. It
 *            is required that all modified versions of this material be marked in
 *            reasonable ways as different from the original version.
 *
 *         3) This license does not grant any rights to any user of the program
 *            with regards to rights under trademark law for use of the trade names
 *            or trademarks of eGovernments Foundation.
 *
 *   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */
package org.egov.lcms.transactions.service;

import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.egov.eis.entity.Employee;
import org.egov.lcms.transactions.entity.CounterAffidavit;
import org.egov.lcms.transactions.entity.EmployeeHearing;
import org.egov.lcms.transactions.entity.Hearings;
import org.egov.lcms.transactions.entity.Judgment;
import org.egov.lcms.transactions.entity.JudgmentImpl;
import org.egov.lcms.transactions.entity.LegalCase;
import org.egov.lcms.transactions.entity.LegalCaseDisposal;
import org.egov.lcms.transactions.entity.LegalCaseInterimOrder;
import org.egov.lcms.transactions.entity.Pwr;
import org.egov.lcms.utils.LegalCaseUtil;
import org.egov.lcms.utils.constants.LcmsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class LegalCaseSmsService {

    @Autowired
    @Qualifier("parentMessageSource")
    private MessageSource messageSource;

    @Autowired
    private LegalCaseUtil legalCaseUtil;

    public void sendSmsToOfficerInchargeForLegalCase(final LegalCase legalcase) {
        if (legalcase.getOfficerIncharge() != null && legalcase.getOfficerIncharge().getName() != null) {
            final String mobileNumber = legalCaseUtil.getOfficerInchargeMobileNumber(legalcase);
            // SMS for legalcase
            if (LcmsConstants.LEGALCASE_STATUS_CREATED.equalsIgnoreCase(legalcase.getStatus().getCode()))
                getSmsForLegalCase(legalcase, mobileNumber);

        }
    }

    public void getSmsForLegalCase(final LegalCase legalcase, final String mobileNo) {
        String smsMsg = null;
        smsMsg = SmsBodyByCodeAndArgsWithTypeForLegalcase("msg.createlegalcase.sms", legalcase);

        if (StringUtils.isNotBlank(mobileNo) && StringUtils.isNotBlank(smsMsg))
            legalCaseUtil.sendSMSOnLegalCase(mobileNo, smsMsg);

    }

    public String SmsBodyByCodeAndArgsWithTypeForLegalcase(final String code, final LegalCase legalcase) {
        String smsMsg = "";
        smsMsg = messageSource.getMessage(code, new String[] { legalCaseUtil.getOfficerInchargeName(legalcase),
                legalcase.getCaseNumber(), legalcase.getPetitionersNames(), legalcase.getRespondantNames() }, null);
        return smsMsg;
    }

    public void sendSmsToHearingEmployee(final Hearings hearings) {
        if (hearings.getTempEmplyeeHearing() != null && !hearings.getTempEmplyeeHearing().isEmpty())
            for (final EmployeeHearing hearingEmp : hearings.getTempEmplyeeHearing())
                getSmsForHearingsEmployee(hearings, hearingEmp.getEmployee());

    }

    public void getSmsForHearingsEmployee(final Hearings hearings, final Employee employee) {
        final String mobileNo = employee.getMobileNumber();
        String smsMsg = null;
        if (LcmsConstants.LEGALCASE_HEARING_STATUS.equalsIgnoreCase(hearings.getLegalCase().getStatus().getCode()))
            smsMsg = SmsBodyByCodeAndArgsWithTypeForHearings("msg.hearingemployee.sms", hearings, employee);

        if (StringUtils.isNotBlank(mobileNo) && StringUtils.isNotBlank(smsMsg))
            legalCaseUtil.sendSMSOnLegalCase(mobileNo, smsMsg);

    }

    public String SmsBodyByCodeAndArgsWithTypeForHearings(final String code, final Hearings hearings,
            final Employee employee) {
        String smsMsg = "";
        final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        smsMsg = messageSource.getMessage(code,
                new String[] { employee.getName(), hearings.getLegalCase().getCaseNumber(),
                        formatter.format(hearings.getHearingDate()).toString(), hearings.getPurposeofHearings() },
                null);
        return smsMsg;
    }

    public void sendSmsToOfficerInchargeInterimOrder(final LegalCaseInterimOrder legalCaseInterimOrder) {
        if (legalCaseInterimOrder.getLegalCase().getOfficerIncharge() != null
                && legalCaseInterimOrder.getLegalCase().getOfficerIncharge().getName() != null) {
            final String mobileNumber = legalCaseUtil
                    .getOfficerInchargeMobileNumber(legalCaseInterimOrder.getLegalCase());

            if (LcmsConstants.LEGALCASE_INTERIMSTAY_STATUS
                    .equalsIgnoreCase(legalCaseInterimOrder.getLegalCase().getStatus().getCode()))
                getSmsForInterimOrder(legalCaseInterimOrder, mobileNumber);
        }

    }

    public void getSmsForInterimOrder(final LegalCaseInterimOrder legalCaseInterimOrder, final String mobileNo) {
        String smsMsg = null;
        smsMsg = SmsBodyByCodeAndArgsWithTypeForInterimOrder("msg.interimorder.sms", legalCaseInterimOrder);

        if (StringUtils.isNotBlank(mobileNo) && StringUtils.isNotBlank(smsMsg))
            legalCaseUtil.sendSMSOnLegalCase(mobileNo, smsMsg);

    }

    public String SmsBodyByCodeAndArgsWithTypeForInterimOrder(final String code,
            final LegalCaseInterimOrder legalCaseInterimOrder) {
        String smsMsg = "";
        final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        smsMsg = messageSource.getMessage(code,
                new String[] { legalCaseUtil.getOfficerInchargeName(legalCaseInterimOrder.getLegalCase()),
                        legalCaseInterimOrder.getLegalCase().getCaseNumber(),
                        legalCaseInterimOrder.getInterimOrder().getInterimOrderType(),
                        formatter.format(legalCaseInterimOrder.getIoDate()).toString() },
                null);
        return smsMsg;
    }

    public void sendSmsToOfficerInchargeForHearings(final Hearings hearings) {
        if (hearings.getLegalCase().getOfficerIncharge() != null
                && hearings.getLegalCase().getOfficerIncharge().getName() != null) {
            final String mobileNumber = legalCaseUtil.getOfficerInchargeMobileNumber(hearings.getLegalCase());

            if (LcmsConstants.LEGALCASE_HEARING_STATUS.equalsIgnoreCase(hearings.getLegalCase().getStatus().getCode()))
                getSmsForHearings(hearings, mobileNumber);
        }

    }

    public void getSmsForHearings(final Hearings hearings, final String mobileNo) {
        String smsMsg = null;
        smsMsg = SmsBodyByCodeAndArgsWithTypeForHearings("msg.hearing.sms", hearings);

        if (StringUtils.isNotBlank(mobileNo) && StringUtils.isNotBlank(smsMsg))
            legalCaseUtil.sendSMSOnLegalCase(mobileNo, smsMsg);

    }

    public String SmsBodyByCodeAndArgsWithTypeForHearings(final String code, final Hearings hearings) {
        String smsMsg = "";
        final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        smsMsg = messageSource.getMessage(code,
                new String[] { legalCaseUtil.getOfficerInchargeName(hearings.getLegalCase()),
                        hearings.getLegalCase().getCaseNumber(), formatter.format(hearings.getHearingDate()).toString(),
                        hearings.getPurposeofHearings() },
                null);
        return smsMsg;
    }

    public void sendSmsToOfficerInchargeForJudgment(final Judgment judgment) {
        if (judgment.getLegalCase().getOfficerIncharge() != null
                && judgment.getLegalCase().getOfficerIncharge().getName() != null) {
            final String mobileNumber = legalCaseUtil.getOfficerInchargeMobileNumber(judgment.getLegalCase());

            if (LcmsConstants.LEGALCASE_STATUS_JUDGMENT.equalsIgnoreCase(judgment.getLegalCase().getStatus().getCode()))
                getSmsForJudgment(judgment, mobileNumber);
        }

    }

    public void getSmsForJudgment(final Judgment judgment, final String mobileNo) {
        String smsMsg = null;
        smsMsg = SmsBodyByCodeAndArgsWithTypeForJudgment("msg.judgment.sms", judgment);

        if (StringUtils.isNotBlank(mobileNo) && StringUtils.isNotBlank(smsMsg))
            legalCaseUtil.sendSMSOnLegalCase(mobileNo, smsMsg);

    }

    public String SmsBodyByCodeAndArgsWithTypeForJudgment(final String code, final Judgment judgment) {
        String smsMsg = "";
        final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        smsMsg = messageSource.getMessage(code,
                new String[] { legalCaseUtil.getOfficerInchargeName(judgment.getLegalCase()),
                        judgment.getLegalCase().getCaseNumber(), judgment.getJudgmentType().getName(),
                        judgment.getImplementByDate() == null ? ""
                                : formatter.format(judgment.getImplementByDate()).toString() },
                null);
        return smsMsg;
    }

    public void sendSmsToOfficerInchargeForJudgmentImpl(final JudgmentImpl judgmentImpl) {
        if (judgmentImpl.getJudgment().getLegalCase().getOfficerIncharge() != null
                && judgmentImpl.getJudgment().getLegalCase().getOfficerIncharge().getName() != null) {
            final String mobileNumber = legalCaseUtil
                    .getOfficerInchargeMobileNumber(judgmentImpl.getJudgment().getLegalCase());

            if (LcmsConstants.LEGALCASE_STATUS_JUDGMENT_IMPLIMENTED
                    .equalsIgnoreCase(judgmentImpl.getJudgment().getLegalCase().getStatus().getCode()))
                getSmsForJudgmentImpl(judgmentImpl, mobileNumber);
        }

    }

    public void getSmsForJudgmentImpl(final JudgmentImpl judgmentImpl, final String mobileNo) {
        String smsMsg = null;
        if (judgmentImpl.getJudgmentImplIsComplied().toString().equals("YES"))
            smsMsg = SmsBodyByCodeAndArgsWithTypeForJudgmentImplIsCompliedYes("msg.judgmentimpliscompliedyes.sms",
                    judgmentImpl);
        else if (judgmentImpl.getJudgmentImplIsComplied().toString().equals("NO"))
            smsMsg = SmsBodyByCodeAndArgsWithTypeForJudgmentImplIsCompliedNo("msg.judgmentimpliscompliedno.sms",
                    judgmentImpl);

        if (StringUtils.isNotBlank(mobileNo) && StringUtils.isNotBlank(smsMsg))
            legalCaseUtil.sendSMSOnLegalCase(mobileNo, smsMsg);

    }

    public String SmsBodyByCodeAndArgsWithTypeForJudgmentImplIsCompliedYes(final String code,
            final JudgmentImpl judgmentImpl) {
        String smsMsg = "";
        final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        smsMsg = messageSource.getMessage(code,
                new String[] { legalCaseUtil.getOfficerInchargeName(judgmentImpl.getJudgment().getLegalCase()),
                        judgmentImpl.getJudgment().getLegalCase().getCaseNumber(),
                        judgmentImpl.getJudgmentImplIsComplied().toString(),
                        formatter.format(judgmentImpl.getDateOfCompliance()).toString() },
                null);
        return smsMsg;
    }

    public String SmsBodyByCodeAndArgsWithTypeForJudgmentImplIsCompliedNo(final String code,
            final JudgmentImpl judgmentImpl) {
        String smsMsg = "";
        smsMsg = messageSource.getMessage(code,
                new String[] { legalCaseUtil.getOfficerInchargeName(judgmentImpl.getJudgment().getLegalCase()),
                        judgmentImpl.getJudgment().getLegalCase().getCaseNumber(),
                        judgmentImpl.getJudgmentImplIsComplied().toString(),
                        judgmentImpl.getImplementationFailure().toString() },
                null);
        return smsMsg;
    }

    public void sendSmsToOfficerInchargeForCloseCase(final LegalCaseDisposal legalCaseDisposal) {
        if (legalCaseDisposal.getLegalCase().getOfficerIncharge() != null
                && legalCaseDisposal.getLegalCase().getOfficerIncharge().getName() != null) {
            final String mobileNumber = legalCaseUtil.getOfficerInchargeMobileNumber(legalCaseDisposal.getLegalCase());

            if (LcmsConstants.LEGALCASE_STATUS_CLOSED
                    .equalsIgnoreCase(legalCaseDisposal.getLegalCase().getStatus().getCode()))
                getSmsForCloseCase(legalCaseDisposal, mobileNumber);
        }

    }

    public void getSmsForCloseCase(final LegalCaseDisposal legalCaseDisposal, final String mobileNo) {
        String smsMsg = null;
        smsMsg = SmsBodyByCodeAndArgsWithTypeForCloseCase("msg.closecase.sms", legalCaseDisposal);

        if (StringUtils.isNotBlank(mobileNo) && StringUtils.isNotBlank(smsMsg))
            legalCaseUtil.sendSMSOnLegalCase(mobileNo, smsMsg);

    }

    public String SmsBodyByCodeAndArgsWithTypeForCloseCase(final String code,
            final LegalCaseDisposal legalCaseDisposal) {
        String smsMsg = "";
        final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        smsMsg = messageSource.getMessage(code,
                new String[] { legalCaseUtil.getOfficerInchargeName(legalCaseDisposal.getLegalCase()),
                        legalCaseDisposal.getLegalCase().getCaseNumber(),
                        formatter.format(legalCaseDisposal.getDisposalDate()).toString() },
                null);
        return smsMsg;
    }

    public void sendSmsToOfficerInchargeForCounterAffidavit(final List<CounterAffidavit> counterAffidavit) {
        if (counterAffidavit.get(0).getLegalCase().getOfficerIncharge() != null
                && counterAffidavit.get(0).getLegalCase().getOfficerIncharge().getName() != null) {
            final String mobileNumber = legalCaseUtil
                    .getOfficerInchargeMobileNumber(counterAffidavit.get(0).getLegalCase());
            getSmsForCounterAffidavit(counterAffidavit, mobileNumber);
        }

    }

    public void getSmsForCounterAffidavit(final List<CounterAffidavit> counterAffidavit, final String mobileNo) {
        String smsMsg = null;
        if (counterAffidavit.get(0).getCounterAffidavitDueDate() != null)
            smsMsg = SmsBodyByCodeAndArgsWithTypeForCounterAffidavitwithSubmissionDate(
                    "msg.counteraffidavitsubmissiondate.sms", counterAffidavit);
        if (counterAffidavit.get(0).getCounterAffidavitApprovalDate() != null)
            smsMsg = SmsBodyByCodeAndArgsWithTypeForCounterAffidavitwithApprovalDate(
                    "msg.counteraffidavitapprovaldate.sms", counterAffidavit);

        if (StringUtils.isNotBlank(mobileNo) && StringUtils.isNotBlank(smsMsg))
            legalCaseUtil.sendSMSOnLegalCase(mobileNo, smsMsg);

    }

    public String SmsBodyByCodeAndArgsWithTypeForCounterAffidavitwithSubmissionDate(final String code,
            final List<CounterAffidavit> counterAffidavit) {
        String smsMsg = "";
        final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        smsMsg = messageSource.getMessage(code,
                new String[] { legalCaseUtil.getOfficerInchargeName(counterAffidavit.get(0).getLegalCase()),
                        counterAffidavit.get(0).getLegalCase().getCaseNumber(),
                        formatter.format(counterAffidavit.get(0).getCounterAffidavitDueDate()).toString() },
                null);
        return smsMsg;
    }

    public String SmsBodyByCodeAndArgsWithTypeForCounterAffidavitwithApprovalDate(final String code,
            final List<CounterAffidavit> counterAffidavit) {
        String smsMsg = "";
        final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        smsMsg = messageSource
                .getMessage(code,
                        new String[] { legalCaseUtil.getOfficerInchargeName(counterAffidavit.get(0).getLegalCase()),
                                counterAffidavit.get(0).getLegalCase().getCaseNumber(), formatter
                                        .format(counterAffidavit.get(0).getCounterAffidavitApprovalDate()).toString() },
                        null);
        return smsMsg;
    }

    public void sendSmsToOfficerInchargeForPWR(final List<Pwr> pwr) {
        if (pwr.get(0).getLegalCase().getOfficerIncharge() != null
                && pwr.get(0).getLegalCase().getOfficerIncharge().getName() != null) {
            final String mobileNumber = legalCaseUtil.getOfficerInchargeMobileNumber(pwr.get(0).getLegalCase());
            getSmsForPWR(pwr, mobileNumber);
        }

    }

    public void getSmsForPWR(final List<Pwr> pwr, final String mobileNo) {
        String smsMsg = null;
        if (pwr.get(0).getPwrDueDate() != null)
            smsMsg = SmsBodyByCodeAndArgsWithTypeForPwrWithSubmissionDate("msg.pwrwithsubmissiondate.sms", pwr);
        if (pwr.get(0).getPwrApprovalDate() != null)
            smsMsg = SmsBodyByCodeAndArgsWithTypeForPwrwithApprovalDate("msg.pwrwithapprovaldate.sms", pwr);

        if (StringUtils.isNotBlank(mobileNo) && StringUtils.isNotBlank(smsMsg))
            legalCaseUtil.sendSMSOnLegalCase(mobileNo, smsMsg);

    }

    public String SmsBodyByCodeAndArgsWithTypeForPwrWithSubmissionDate(final String code, final List<Pwr> pwr) {
        String smsMsg = "";
        final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        smsMsg = messageSource.getMessage(code,
                new String[] { legalCaseUtil.getOfficerInchargeName(pwr.get(0).getLegalCase()),
                        pwr.get(0).getLegalCase().getCaseNumber(),
                        formatter.format(pwr.get(0).getPwrDueDate()).toString() },
                null);
        return smsMsg;
    }

    public String SmsBodyByCodeAndArgsWithTypeForPwrwithApprovalDate(final String code, final List<Pwr> pwr) {
        String smsMsg = "";
        final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        smsMsg = messageSource.getMessage(code,
                new String[] { legalCaseUtil.getOfficerInchargeName(pwr.get(0).getLegalCase()),
                        pwr.get(0).getLegalCase().getCaseNumber(),
                        formatter.format(pwr.get(0).getPwrApprovalDate()).toString() },
                null);
        return smsMsg;
    }

}