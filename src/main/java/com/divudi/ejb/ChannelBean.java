/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.ejb;

import com.divudi.bean.channel.BookingController;
import com.divudi.bean.common.SessionController;
import com.divudi.core.data.BillType;
import com.divudi.core.data.FeeType;
import com.divudi.core.data.dataStructure.ChannelFee;
import com.divudi.core.entity.BillFee;
import com.divudi.core.entity.BillSession;
import com.divudi.core.entity.BilledBill;
import com.divudi.core.entity.ServiceSession;
import com.divudi.core.entity.ServiceSessionLeave;
import com.divudi.core.entity.SessionNumberGenerator;
import com.divudi.core.entity.Staff;
import com.divudi.core.entity.channel.SessionInstance;
import com.divudi.core.facade.BillFeeFacade;
import com.divudi.core.facade.BillSessionFacade;
import com.divudi.core.facade.ServiceSessionFacade;
import com.divudi.core.facade.ServiceSessionLeaveFacade;
import com.divudi.core.facade.SessionInstanceFacade;
import com.divudi.core.facade.SessionNumberGeneratorFacade;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.TemporalType;

/**
 *
 * @author safrin
 */
@Stateless
public class ChannelBean {

    @EJB
    private FinalVariables finalVariables;
    @EJB
    private BillSessionFacade billSessionFacade;
    @EJB
    private BillFeeFacade billFeeFacade;
    @EJB
    private ServiceSessionLeaveFacade serviceSessionLeaveFacade;
    @EJB
    private ServiceSessionFacade serviceSessionFacade;
    @EJB
    SessionNumberGeneratorFacade sessionNumberGeneratorFacade;
    @EJB
    SessionInstanceFacade sessionInstanceFacade;
    @Inject
    SessionController sessionController;
    @Inject
    BookingController bookingController;

    public ChannelFee getChannelFee(BillSession bs, FeeType feeType) {
        ChannelFee doctorFee = new ChannelFee();

        for (BillFee bf : getBillFee(bs)) {
            if (bf.getFee().getFeeType() == feeType) {
                if (bf.getBill().getBillType() == BillType.ChannelPaid) {
                    doctorFee.setBilledFee(bf);
                } else {
                    doctorFee.setBilledFee(new BillFee());
                }
            }
        }

        for (BillFee bf : getRefundBillFee(bs)) {
            if (bf.getFee().getFeeType() == feeType) {
                if (bf.getBill().getBillType() == BillType.ChannelPaid) {
                    doctorFee.setPrevFee(bf);
                } else {
                    doctorFee.setPrevFee(new BillFee());
                }
            }
        }

        doctorFee.setRepayment(new BillFee());

        return doctorFee;
    }

    public List<BillFee> getRefundBillFee(BillSession bs) {
        List<BillFee> refundBillFee = new ArrayList<>();
        if (bs != null) {
            String sql = "Select s From BillFee s where s.retired=false and s.bill.billedBill.id=" + bs.getBill().getId();
            refundBillFee = getBillFeeFacade().findByJpql(sql);
        }

        return refundBillFee;
    }

    public List<BillFee> getBillFee(BillSession bs) {
        List<BillFee> billFee = new ArrayList<>();
        if (bs != null) {
            String sql = "Select s From BillFee s where s.retired=false and s.bill.id=" + bs.getBill().getId();
            billFee = getBillFeeFacade().findByJpql(sql);
        }

        return billFee;
    }

    @Deprecated
    public int getBillSessionsCount(ServiceSession ss, Date date) {

        BillType[] billTypes = {BillType.ChannelAgent, BillType.ChannelCash, BillType.ChannelOnCall, BillType.ChannelStaff};
        List<BillType> bts = Arrays.asList(billTypes);
        String sql = "Select count(bs) From BillSession bs "
                + " where bs.retired=false"
                + " and bs.serviceSession =:ser "
                + " and bs.bill.billType in :bt"
                + " and type(bs.bill)=:class "
                + " and bs.sessionDate= :ssDate";
        HashMap hh = new HashMap();
        hh.put("ssDate", date);
        hh.put("ser", ss);
        hh.put("bt", bts);
        hh.put("class", BilledBill.class);
        Long lg = getBillSessionFacade().findAggregateLong(sql, hh, TemporalType.DATE);
        return lg.intValue();
    }

    public int getBillSessionsCount(SessionInstance si) {
        BillType[] billTypes = {BillType.ChannelAgent, BillType.ChannelCash, BillType.ChannelOnCall, BillType.ChannelStaff};
        List<BillType> bts = Arrays.asList(billTypes);
        String sql = "Select count(bs) From BillSession bs "
                + " where bs.retired=false"
                + " and bs.sessionInstance=:si "
                + " and bs.bill.billType in :bt"
                + " and type(bs.bill)=:class ";
        HashMap hh = new HashMap();
        hh.put("si", si);
        hh.put("bt", bts);
        hh.put("class", BilledBill.class);
        Long lg = getBillSessionFacade().findAggregateLong(sql, hh, TemporalType.DATE);
        return lg.intValue();
    }

    @Deprecated
    public int getBillSessionsCountWithOutCancelRefund(ServiceSession ss, Date date) {
        BillType[] billTypes = {BillType.ChannelAgent, BillType.ChannelCash, BillType.ChannelOnCall, BillType.ChannelStaff};
        List<BillType> bts = Arrays.asList(billTypes);
        String sql = "Select count(bs) From BillSession bs "
                + " where bs.retired=false"
                + " and bs.serviceSession =:ser "
                + " and bs.bill.billType in :bt"
                + " and type(bs.bill)=:class "
                + " and bs.sessionDate= :ssDate "
                + " and bs.bill.cancelled=false "
                + " and bs.bill.refunded=false ";
        HashMap hh = new HashMap();
        hh.put("ssDate", date);
        hh.put("ser", ss);
        hh.put("bt", bts);
        hh.put("class", BilledBill.class);
        Long lg = getBillSessionFacade().findAggregateLong(sql, hh, TemporalType.DATE);

        return lg.intValue();
    }

    public int getBillSessionsCountWithOutCancelRefund(SessionInstance si) {
        BillType[] billTypes = {BillType.ChannelAgent, BillType.ChannelCash, BillType.ChannelOnCall, BillType.ChannelStaff};
        List<BillType> bts = Arrays.asList(billTypes);
        String sql = "Select count(bs) From BillSession bs "
                + " where bs.retired=false"
                + " and bs.sessionInstance =:si "
                + " and bs.bill.billType in :bt"
                + " and type(bs.bill)=:class "
                + " and bs.bill.cancelled=false "
                + " and bs.bill.refunded=false ";
        HashMap hh = new HashMap();
        hh.put("si", si);
        hh.put("bt", bts);
        hh.put("class", BilledBill.class);
        Long lg = getBillSessionFacade().findAggregateLong(sql, hh, TemporalType.DATE);
        return lg.intValue();
    }

    @Deprecated
    public int getBillSessionsCountCrditBill(ServiceSession ss, Date date) {
        BillType[] billTypes = {BillType.ChannelAgent, BillType.ChannelCash, BillType.ChannelOnCall, BillType.ChannelStaff};
        List<BillType> bts = Arrays.asList(billTypes);
        String sql = "Select count(bs) From BillSession bs "
                + " where bs.retired=false"
                + " and bs.serviceSession =:ser "
                + " and bs.bill.billType in :bt"
                + " and type(bs.bill)=:class "
                + " and bs.sessionDate= :ssDate "
                + " and bs.bill.paidAmount=:pa ";
        HashMap hh = new HashMap();
        hh.put("ssDate", date);
        hh.put("ser", ss);
        hh.put("bt", bts);
        hh.put("class", BilledBill.class);
        hh.put("pa", 0.0);
        Long lg = getBillSessionFacade().findAggregateLong(sql, hh, TemporalType.DATE);

        return lg.intValue();
    }

    public int getBillSessionsCountCrditBill(SessionInstance si) {
        BillType[] billTypes = {BillType.ChannelAgent, BillType.ChannelCash, BillType.ChannelOnCall, BillType.ChannelStaff};
        List<BillType> bts = Arrays.asList(billTypes);
        String sql = "Select count(bs) From BillSession bs "
                + " where bs.retired=false"
                + " and bs.sessionInstance =:si "
                + " and bs.bill.billType in :bt"
                + " and type(bs.bill)=:class "
                + " and bs.bill.paidAmount=:pa ";
        HashMap hh = new HashMap();
        hh.put("si", si);
        hh.put("bt", bts);
        hh.put("class", BilledBill.class);
        hh.put("pa", 0.0);
        Long lg = getBillSessionFacade().findAggregateLong(sql, hh, TemporalType.DATE);

        return lg.intValue();
    }

    public List<ServiceSession> setSessionAt(List<ServiceSession> sessions) {
        int sessionDayCount = 0;
        List<ServiceSession> serviceSessions = new ArrayList<>();
        Date nowDate = Calendar.getInstance().getTime();

        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, 1);
        Date toDate = c.getTime();
        Integer tmp = 0;
        int rowIndex = 0;

        while (toDate.after(nowDate) && sessionDayCount < getFinalVariables().getSessionSessionDayCounter()) {
            boolean hasSpecificDateSession = false;

            if (checkLeaveDate(nowDate, sessions.get(0).getStaff())) {
                continue;
            }

            for (ServiceSession ss : sessions) {
                if (ss.getSessionDate() != null) {
                    Calendar sessionDate = Calendar.getInstance();
                    sessionDate.setTime(ss.getSessionDate());
                    Calendar nDate = Calendar.getInstance();
                    nDate.setTime(nowDate);

                    if (sessionDate.get(Calendar.DATE) == nDate.get(Calendar.DATE)) {
                        hasSpecificDateSession = true;
                        ServiceSession newSs = new ServiceSession();
                        newSs.setName(ss.getName());
                        newSs.setMaxNo(ss.getMaxNo());
                        newSs.setStartingTime(ss.getStartingTime());
                        newSs.setSessionWeekday(ss.getSessionWeekday());
                        newSs.setHospitalFee(ss.getHospitalFee());
                        newSs.setProfessionalFee(ss.getProfessionalFee());
                        newSs.setId(ss.getId());
                        newSs.setSessionAt(nowDate);
                        newSs.setStaff(ss.getStaff());
                        newSs.setRoomNo(rowIndex++);
                        serviceSessions.add(newSs);

                        if (!tmp.equals(ss.getSessionWeekday())) {
                            sessionDayCount++;
                        }
                    }
                }
            }

            if (!hasSpecificDateSession) {
                for (ServiceSession ss : sessions) {
                    Calendar wdc = Calendar.getInstance();
                    wdc.setTime(nowDate);
                    if (ss.getSessionWeekday() == wdc.get(Calendar.DAY_OF_WEEK)) {
                        ServiceSession newSs = new ServiceSession();
                        newSs.setName(ss.getName());
                        newSs.setMaxNo(ss.getMaxNo());
                        newSs.setStartingTime(ss.getStartingTime());
                        newSs.setSessionWeekday(ss.getSessionWeekday());
                        newSs.setHospitalFee(ss.getHospitalFee());
                        newSs.setProfessionalFee(ss.getProfessionalFee());
                        newSs.setId(ss.getId());
                        newSs.setSessionAt(nowDate);
                        newSs.setStaff(ss.getStaff());
                        newSs.setRoomNo(rowIndex++);

                        serviceSessions.add(newSs);

                        if (!tmp.equals(ss.getSessionWeekday())) {
                            sessionDayCount++;
                        }
                    }

                }
            }

            Calendar nc = Calendar.getInstance();
            nc.setTime(nowDate);
            nc.add(Calendar.DATE, 1);
            nowDate = nc.getTime();

        }
        return serviceSessions;
    }

    private boolean checkLeaveDate(Date date, Staff staff) {
        String slq = "Select s From ServiceSessionLeave s"
                + "  Where s.sessionDate=:dt"
                + "  and s.staff=:st"
                + " and s.retired=false ";
        HashMap hm = new HashMap();
        hm.put("dt", date);
        hm.put("st", staff);
        ServiceSessionLeave tmp = getServiceSessionLeaveFacade().findFirstByJpql(slq, hm, TemporalType.DATE);

        return tmp != null;
    }

    public List<SessionInstance> generateSesionInstancesFromServiceSessions(List<ServiceSession> inputSessions, Date d) {
        int sessionDayCount = 0;
        List<SessionInstance> sessionInstances = new ArrayList<>();
        if (inputSessions == null || inputSessions.isEmpty()) {
            return sessionInstances;
        }

        for (ServiceSession ss : inputSessions) {
            Date startDate = new Date();
            Calendar cToDate = Calendar.getInstance();
            int numberOfDaysInAdvance;
            if (ss.getNumberOfDaysForAutomaticInstanceCreation() == null) {
                numberOfDaysInAdvance = 30;
            } else {
                numberOfDaysInAdvance = ss.getNumberOfDaysForAutomaticInstanceCreation();
            }
            cToDate.add(Calendar.DATE, numberOfDaysInAdvance);
            Date endDate = cToDate.getTime();

            Calendar cWorkingDate = Calendar.getInstance();
            cWorkingDate.setTime(startDate);

            // Reset time components for startDate
            cWorkingDate.set(Calendar.HOUR_OF_DAY, 0);
            cWorkingDate.set(Calendar.MINUTE, 0);
            cWorkingDate.set(Calendar.SECOND, 0);
            cWorkingDate.set(Calendar.MILLISECOND, 0);

            int rowIndex = 0;

            while (cWorkingDate.getTime().before(endDate) || cWorkingDate.getTime().equals(endDate)) {
                Date workingDate = cWorkingDate.getTime();
                boolean eligibleDate = false;
                // Reset time components for sessionDate

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(workingDate);

                if (ss.getSessionDate() == null) {
                    int workingDateWeekday = calendar.get(Calendar.DAY_OF_WEEK);
                    if (workingDateWeekday == ss.getSessionWeekday()) {
                        eligibleDate = true;
                    }
                } else {
                    Calendar cSessionDate = Calendar.getInstance();
                    cSessionDate.setTime(ss.getSessionDate());
                    cSessionDate.set(Calendar.HOUR_OF_DAY, 0);
                    cSessionDate.set(Calendar.MINUTE, 0);
                    cSessionDate.set(Calendar.SECOND, 0);
                    cSessionDate.set(Calendar.MILLISECOND, 0);
                    if (cSessionDate.getTime().equals(workingDate)) {
                        eligibleDate = true;
                    }
                }

                if (eligibleDate) {
                    String jpql = "select i "
                            + " from SessionInstance i "
                            + " where i.originatingSession=:os "
                            + " and i.retired=:ret "
                            + " and i.sessionDate=:sd";

                    Map m = new HashMap();
                    m.put("ret", false);
                    m.put("os", ss);
                    m.put("sd", workingDate);

                    SessionInstance si = sessionInstanceFacade.findFirstByJpql(jpql, m, TemporalType.DATE);

                    if (si == null) {
                        si = createSessionInstancesForServiceSession(ss, workingDate);
                        if (si.getId() == null) {
                            sessionInstanceFacade.create(si);
                        } else {
                            sessionInstanceFacade.edit(si);
                        }
                    }

                    si.setStaff(ss.getStaff());
                    si.setTransRowNumber(rowIndex++);

                    sessionInstances.add(si);
                }

                cWorkingDate.add(Calendar.DATE, 1); // Increment the date
            }
        }
        Collections.sort(sessionInstances, new Comparator<SessionInstance>() {
            @Override
            public int compare(SessionInstance s1, SessionInstance s2) {
                int dateCompare = compareNullableDates(s1.getSessionDate(), s2.getSessionDate());
                if (dateCompare != 0) {
                    return dateCompare;
                } else {
                    // Assuming ServiceSession has a method to get a navigateToSessionView identifier or name for comparison
                    return compareNullableStrings(
                            s1.getOriginatingSession() != null ? s1.getOriginatingSession().getName() : null,
                            s2.getOriginatingSession() != null ? s2.getOriginatingSession().getName() : null
                    );
                }
            }
        });
        return sessionInstances;
    }

    public List<SessionInstance> generateSesionInstancesFromServiceSessions(List<ServiceSession> inputSessions) {
        int sessionDayCount = 0;
        List<SessionInstance> sessionInstances = new ArrayList<>();
        if (inputSessions == null || inputSessions.isEmpty()) {
            return sessionInstances;
        }

        for (ServiceSession ss : inputSessions) {
            Date startDate = new Date();
            Calendar cToDate = Calendar.getInstance();
            int numberOfDaysInAdvance;
            if (ss.getNumberOfDaysForAutomaticInstanceCreation() == null) {
                numberOfDaysInAdvance = 30;
            } else {
                numberOfDaysInAdvance = ss.getNumberOfDaysForAutomaticInstanceCreation();
            }
            cToDate.add(Calendar.DATE, numberOfDaysInAdvance);
            Date endDate = cToDate.getTime();

            Calendar cWorkingDate = Calendar.getInstance();
            cWorkingDate.setTime(startDate);

            // Reset time components for startDate
            cWorkingDate.set(Calendar.HOUR_OF_DAY, 0);
            cWorkingDate.set(Calendar.MINUTE, 0);
            cWorkingDate.set(Calendar.SECOND, 0);
            cWorkingDate.set(Calendar.MILLISECOND, 0);

            int rowIndex = 0;

            while (cWorkingDate.getTime().before(endDate) || cWorkingDate.getTime().equals(endDate)) {
                Date workingDate = cWorkingDate.getTime();
                boolean eligibleDate = false;
                // Reset time components for sessionDate

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(workingDate);

                if (ss.getSessionDate() == null) {
                    int workingDateWeekday = calendar.get(Calendar.DAY_OF_WEEK);
                    if (ss.getSessionWeekday() != null) {
                        if (workingDateWeekday == ss.getSessionWeekday()) {
                            eligibleDate = true;
                        }
                    }

                } else {
                    Calendar cSessionDate = Calendar.getInstance();
                    cSessionDate.setTime(ss.getSessionDate());
                    cSessionDate.set(Calendar.HOUR_OF_DAY, 0);
                    cSessionDate.set(Calendar.MINUTE, 0);
                    cSessionDate.set(Calendar.SECOND, 0);
                    cSessionDate.set(Calendar.MILLISECOND, 0);
                    if (cSessionDate.getTime().equals(workingDate)) {
                        eligibleDate = true;
                    }
                }

                if (eligibleDate) {
                    String jpql = "select i "
                            + " from SessionInstance i "
                            + " where i.originatingSession=:os "
                            + " and i.retired=:ret "
                            + " and i.sessionDate=:sd";

                    Map m = new HashMap();
                    m.put("ret", false);
                    m.put("os", ss);
                    m.put("sd", workingDate);

                    SessionInstance si = sessionInstanceFacade.findFirstByJpql(jpql, m, TemporalType.DATE);

                    if (si == null) {
                        si = createSessionInstancesForServiceSession(ss, workingDate);
                        if (si.getId() == null) {
                            sessionInstanceFacade.create(si);
                        } else {
                            sessionInstanceFacade.edit(si);
                        }
                    }

                    si.setStaff(ss.getStaff());
                    si.setTransRowNumber(rowIndex++);

                    sessionInstances.add(si);
                }

                cWorkingDate.add(Calendar.DATE, 1); // Increment the date
            }
        }
        Collections.sort(sessionInstances, new Comparator<SessionInstance>() {
            @Override
            public int compare(SessionInstance s1, SessionInstance s2) {
                int dateCompare = compareNullableDates(s1.getSessionDate(), s2.getSessionDate());
                if (dateCompare != 0) {
                    return dateCompare;
                } else {
                    // Assuming ServiceSession has a method to get a navigateToSessionView identifier or name for comparison
                    return compareNullableStrings(
                            s1.getOriginatingSession() != null ? s1.getOriginatingSession().getName() : null,
                            s2.getOriginatingSession() != null ? s2.getOriginatingSession().getName() : null
                    );
                }
            }
        });
        return sessionInstances;
    }

    public List<SessionInstance> listTodaysSesionInstances() {
        return listTodaysSessionInstances(null, null, null);
    }

    public List<SessionInstance> listTodaysSessionInstances(Boolean ongoing, Boolean completed, Boolean pending) {
        List<SessionInstance> sessionInstances;
        StringBuilder jpql = new StringBuilder("select i from SessionInstance i where i.retired=:ret and i.sessionDate=:sd");

        // Initializing the parameters map
        Map<String, Object> params = new HashMap<>();
        params.put("ret", false);
        params.put("sd", new Date());

        // Dynamically appending conditions based on parameters
        List<String> conditions = new ArrayList<>();
        if (ongoing != null && ongoing) {
            conditions.add("(i.started = true and i.completed = false)");
        }
        if (completed != null && completed) {
            conditions.add("i.completed = true");
        }
        if (pending != null && pending) {
            conditions.add("(i.started = false and i.completed = false)");
        }

        // Adding the conditions to the JPQL query
        if (!conditions.isEmpty()) {
            jpql.append(" and (").append(String.join(" or ", conditions)).append(")");
        }

        sessionInstances = sessionInstanceFacade.findByJpql(jpql.toString(), params, TemporalType.DATE);

        // Sorting logic remains unchanged
        return sessionInstances;
    }

    public List<SessionInstance> listSessionInstancesByDate(Date sessionDate, Boolean ongoing, Boolean completed, Boolean pending) {
        List<SessionInstance> sessionInstances = new ArrayList<>();
        StringBuilder jpql = new StringBuilder("select i from SessionInstance i where i.retired=:ret and i.sessionDate between :startOfDay and :endOfDay");

        // Initializing the parameters map
        Calendar cal = Calendar.getInstance();
        cal.setTime(sessionDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfDay = cal.getTime();

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date endOfDay = cal.getTime();

        Map<String, Object> params = new HashMap<>();
        params.put("ret", false);
        params.put("startOfDay", startOfDay);
        params.put("endOfDay", endOfDay);

        // Dynamically appending conditions based on parameters
        List<String> conditions = new ArrayList<>();
        if (ongoing != null && ongoing) {
            conditions.add("(i.started = true and i.completed = false)");
        }
        if (completed != null && completed) {
            conditions.add("i.completed = true");
        }
        if (pending != null && pending) {
            conditions.add("(i.started = false and i.completed = false)");
        }

        // Adding the conditions to the JPQL query
        if (!conditions.isEmpty()) {
            jpql.append(" and (").append(String.join(" or ", conditions)).append(")");
        }

        // Fetching the session instances based on the constructed JPQL query and parameters
        sessionInstances = sessionInstanceFacade.findByJpql(jpql.toString(), params, TemporalType.TIMESTAMP);

        // Sorting logic remains unchanged
        Collections.sort(sessionInstances, new Comparator<SessionInstance>() {
            @Override
            public int compare(SessionInstance s1, SessionInstance s2) {
                int dateCompare = compareNullableDates(s1.getSessionDate(), s2.getSessionDate());
                if (dateCompare != 0) {
                    return dateCompare;
                } else {
                    return compareNullableStrings(
                            s1.getOriginatingSession() != null ? s1.getOriginatingSession().getName() : null,
                            s2.getOriginatingSession() != null ? s2.getOriginatingSession().getName() : null
                    );
                }
            }
        });
        return sessionInstances;
    }

    private int compareNullableDates(Date d1, Date d2) {
        return Comparator.nullsLast(Date::compareTo).compare(d1, d2);
    }

    private int compareNullableStrings(String s1, String s2) {
        return Comparator.nullsLast(String::compareTo).compare(s1, s2);
    }

    public List<SessionInstance> listSessionInstances(Date fromDate, Date toDate, Boolean ongoing, Boolean completed, Boolean pending) {
        return listSessionInstances(fromDate, toDate, ongoing, completed, pending, null);
    }

    public List<SessionInstance> listSessionInstances(Date fromDate, Date toDate, Boolean ongoing, Boolean completed, Boolean pending, Boolean cancelled) {
        List<SessionInstance> sessionInstances = new ArrayList<>();
        StringBuilder jpql = new StringBuilder("select i from SessionInstance i where i.retired=:ret and i.originatingSession.retired=:ret");

        // Initializing the parameters map
        Map<String, Object> params = new HashMap<>();
        params.put("ret", false);

        // Adding date conditions
        if (fromDate != null && toDate != null) {
            jpql.append(" and i.sessionDate between :fromDate and :toDate");
            params.put("fromDate", fromDate);
            params.put("toDate", toDate);
        } else if (fromDate != null) {
            jpql.append(" and i.sessionDate >= :fromDate");
            params.put("fromDate", fromDate);
        } else if (toDate != null) {
            jpql.append(" and i.sessionDate <= :toDate");
            params.put("toDate", toDate);
        } else {
            jpql.append(" and i.sessionDate = :sd");
            params.put("sd", new Date());
        }

        // Dynamically appending conditions based on parameters
        List<String> conditions = new ArrayList<>();
        if (ongoing != null && ongoing) {
            conditions.add("(i.started = true and i.completed = false)");
        }
        if (completed != null && completed) {
            conditions.add("i.completed = true");
        }
        if (pending != null && pending) {
            conditions.add("(i.started = false and i.completed = false)");
        }
        if (cancelled != null && cancelled) {
            conditions.add("i.cancelled = true");
        }

        // Adding the conditions to the JPQL query
        if (!conditions.isEmpty()) {
            jpql.append(" and (").append(String.join(" or ", conditions)).append(")");
        }

        // Adding sorting to JPQL with custom order
        jpql.append(" order by case when i.completed = true then 1 else 0 end, i.completed asc, i.started desc, i.sessionDate asc, i.startingTime asc");

        sessionInstances = sessionInstanceFacade.findByJpql(jpql.toString(), params, TemporalType.DATE);

        return sessionInstances;
    }

    public void createDocLeaveSession(List<ServiceSession> createdSessions, Date nDate, int rIndex) {
        ServiceSession newSs = new ServiceSession();
        newSs.setId(1L);
        newSs.setName("Leave");
        newSs.setSessionAt(nDate);
        newSs.setMaxNo(0);
        newSs.setTransDisplayCountWithoutCancelRefund(0);
        newSs.setTransCreditBillCount(0);
        newSs.setDeactivated(true);
        Calendar e = Calendar.getInstance();
        e.setTime(new Date());
        e.set(Calendar.HOUR, 0);
        e.set(Calendar.MINUTE, 0);
        e.set(Calendar.SECOND, 00);
        newSs.setStartingTime(e.getTime());
        e.add(Calendar.HOUR, 2);
        newSs.setEndingTime(e.getTime());
        newSs.setRoomNo(rIndex);
        createdSessions.add(newSs);

    }

    public SessionInstance createSessionInstancesForServiceSession(ServiceSession ss, Date d) {
        SessionInstance newSs = new SessionInstance();
        newSs.setOriginatingSession(ss);
        newSs.setName(ss.getName());
        newSs.setAcceptOnlineBookings(ss.isAcceptOnlineBookings());
        newSs.setMaxNo(ss.getMaxNo());
        newSs.setStartingTime(ss.getStartingTime());
        newSs.setSessionWeekday(ss.getSessionWeekday());
        newSs.setHospitalFee(ss.getHospitalFee());
        newSs.setProfessionalFee(ss.getProfessionalFee());
        newSs.setDepartment(ss.getDepartment());
        newSs.setInstitution(ss.getInstitution());
        newSs.setSessionAt(d);//what is this feild
        newSs.setSessionDate(d);
        newSs.setSessionTime(ss.getStartingTime());
        newSs.setStartingTime(ss.getStartingTime());
        newSs.setEndingTime(ss.getEndingTime());
        newSs.setCreatedAt(new Date());
        newSs.setCreater(getSessionController().getLoggedUser());
        newSs.setStaff(ss.getStaff());
        newSs.setRoomNo(ss.getRoomNo());
        newSs.setSessionNumberGenerator(saveSessionNumber(ss));
        newSs.setReserveNumbers(ss.getReserveNumbers());
        try {
            sessionInstanceFacade.create(newSs);
        } catch (Exception e) {
            sessionInstanceFacade.edit(newSs);
        }
        return newSs;
    }

    @Deprecated
    public ServiceSession fetchCreatedServiceSession(Staff s, Date d, ServiceSession ss) {
        String sql;
        Map m = new HashMap();
        sql = "Select s From ServiceSession s where s.retired=false "
                + " and s.staff=:staff "
                + " and s.originatingSession=:os "
                + " and s.sessionDate=:d "
                + " and type(s)=:class "
                + " order by s.sessionWeekday,s.startingTime ";
        m.put("d", d);
        m.put("staff", s);
        m.put("os", ss);
        m.put("class", ServiceSession.class);
        ServiceSession tmp = getServiceSessionFacade().findFirstByJpql(sql, m, TemporalType.TIMESTAMP);
        return tmp;
    }

    public SessionNumberGenerator saveSessionNumber(ServiceSession ss) {
        SessionNumberGenerator sessionNumberGenerator;
        sessionNumberGenerator = ss.getSessionNumberGenerator();
        if (sessionNumberGenerator == null) {
            sessionNumberGenerator = new SessionNumberGenerator();
            sessionNumberGenerator.setSpeciality(ss.getStaff().getSpeciality());
            sessionNumberGenerator.setStaff(ss.getStaff());
            sessionNumberGenerator.setName(ss.getStaff().getPerson().getName() + " " + ss.getName());
            sessionNumberGeneratorFacade.create(sessionNumberGenerator);
        }
        return sessionNumberGenerator;
    }

    public Date calSessionTime(ServiceSession serviceSession) {
        Calendar starting = Calendar.getInstance();
        starting.setTime(serviceSession.getStartingTime());
        int count = getBillSessionsCount(serviceSession, serviceSession.getSessionAt());

        if (count == 0) {
            return starting.getTime();
        }

        if (serviceSession.getDuration() != 0.0) {
            starting.add(Calendar.MINUTE, (int) (count * serviceSession.getDuration()));
        } else {
            starting.add(Calendar.MINUTE, (int) (count * getFinalVariables().getCahnnelingDurationMinute()));
        }

        return starting.getTime();

    }

    public FinalVariables getFinalVariables() {
        return finalVariables;
    }

    public void setFinalVariables(FinalVariables finalVariables) {
        this.finalVariables = finalVariables;
    }

    public BillSessionFacade getBillSessionFacade() {
        return billSessionFacade;
    }

    public void setBillSessionFacade(BillSessionFacade billSessionFacade) {
        this.billSessionFacade = billSessionFacade;
    }

    public BillFeeFacade getBillFeeFacade() {
        return billFeeFacade;
    }

    public void setBillFeeFacade(BillFeeFacade billFeeFacade) {
        this.billFeeFacade = billFeeFacade;
    }

    public ServiceSessionLeaveFacade getServiceSessionLeaveFacade() {
        return serviceSessionLeaveFacade;
    }

    public void setServiceSessionLeaveFacade(ServiceSessionLeaveFacade serviceSessionLeaveFacade) {
        this.serviceSessionLeaveFacade = serviceSessionLeaveFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public ServiceSessionFacade getServiceSessionFacade() {
        return serviceSessionFacade;
    }

    public void setServiceSessionFacade(ServiceSessionFacade serviceSessionFacade) {
        this.serviceSessionFacade = serviceSessionFacade;
    }

    public BookingController getBookingController() {
        return bookingController;
    }

    public void setBookingController(BookingController bookingController) {
        this.bookingController = bookingController;
    }
}
