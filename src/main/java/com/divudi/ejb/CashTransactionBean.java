/*
 * Open Hospital Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package com.divudi.ejb;

import com.divudi.core.entity.Bill;
import com.divudi.core.entity.WebUser;
import com.divudi.core.entity.cashTransaction.CashTransaction;
import com.divudi.core.entity.cashTransaction.Drawer;
import com.divudi.core.facade.BillFacade;
import com.divudi.core.facade.CashTransactionFacade;
import com.divudi.core.facade.CashTransactionHistoryFacade;
import com.divudi.core.facade.DrawerFacade;
import com.divudi.core.facade.WebUserFacade;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 *
 * @author safrin
 */
@Stateless
public class CashTransactionBean {

    @EJB
    CashTransactionHistoryFacade cashTransactionHistoryFacade;
    @EJB
    private DrawerFacade drawerFacade;
    @EJB
    private CashTransactionFacade cashTransactionFacade;
    @EJB
    private WebUserFacade webUserFacade;

    public void updateDrawers() {
    }

    public List<WebUser> getUser(Drawer drawer) {

        return new ArrayList<>();
    }

    public CashTransaction setCashTransactionValue(CashTransaction cashTransaction, Bill bill) {

        return cashTransaction;
    }

    public CashTransaction saveCashInTransaction(CashTransaction ct, Bill bill, WebUser webUser) {

        return ct;
    }

    public CashTransaction saveCashOutTransaction(CashTransaction ct, Bill bill, WebUser webUser) {

        return ct;
    }

    public CashTransaction saveCashAdjustmentTransactionOut(CashTransaction ct, Bill bill, Drawer drawer, WebUser webUser) {

        return ct;
    }

    public CashTransaction saveCashAdjustmentTransactionIn(CashTransaction ct, Bill bill, Drawer drawer, WebUser webUser) {

        return ct;
    }

    public CashTransactionHistoryFacade getCashTransactionHistoryFacade() {
        return cashTransactionHistoryFacade;
    }

    public void setCashTransactionHistoryFacade(CashTransactionHistoryFacade cashTransactionHistoryFacade) {
        this.cashTransactionHistoryFacade = cashTransactionHistoryFacade;
    }

    public double calTotal(CashTransaction cashTransaction) {

        return 0;
    }

    public Drawer getDrawer(WebUser webUser) {

        return new Drawer();
    }

    public void addToTransactionHistory(CashTransaction cashTransaction, Drawer drawer) {

    }

    @EJB
    BillFacade billFacade;

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    @Deprecated
    public WebUser saveBillCashInTransaction(Bill bill, WebUser webUser) {

        return webUser;
    }

    @Deprecated
    public WebUser saveBillCashOutTransaction(Bill bill, WebUser webUser) {

        return webUser;
    }

    public boolean addToBallance(Drawer drawer, CashTransaction cashTransaction) {

        return true;
    }

    public boolean deductFromBallance(Drawer drawer, CashTransaction cashTransaction) {
        return true;
    }

    public boolean resetBallance(Drawer drawer, CashTransaction cashTransaction) {

        return true;
    }

    public CashTransactionFacade getCashTransactionFacade() {
        return cashTransactionFacade;
    }

    public void setCashTransactionFacade(CashTransactionFacade cashTransactionFacade) {
        this.cashTransactionFacade = cashTransactionFacade;
    }

    public DrawerFacade getDrawerFacade() {
        return drawerFacade;
    }

    public void setDrawerFacade(DrawerFacade drawerFacade) {
        this.drawerFacade = drawerFacade;
    }

    public WebUserFacade getWebUserFacade() {
        return webUserFacade;
    }

    public void setWebUserFacade(WebUserFacade webUserFacade) {
        this.webUserFacade = webUserFacade;
    }
}
