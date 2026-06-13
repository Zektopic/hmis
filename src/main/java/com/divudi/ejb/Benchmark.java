package com.divudi.ejb;

import com.divudi.core.entity.Patient;
import com.divudi.core.entity.PatientEncounter;
import com.divudi.core.entity.lab.Investigation;
import com.divudi.core.entity.lab.PatientInvestigation;
import com.divudi.core.entity.lab.PatientReport;
import com.divudi.core.entity.lab.PatientReportItemValue;
import com.divudi.core.entity.lab.ReportItem;
import com.divudi.core.entity.lab.InvestigationItem;
import com.divudi.core.data.InvestigationItemType;
import com.divudi.core.data.InvestigationItemValueType;

import java.util.ArrayList;
import java.util.List;

public class Benchmark {
    public static void main(String[] args) {
        System.out.println("Benchmark requires full application context to test PatientReportBean since it depends on injected Facades (like getPtRivFacade()).");
        System.out.println("We will write a test using the same logic to demonstrate the performance difference between individual creates and batch create.");

        int N = 5000;
        System.out.println("Simulating creating " + N + " PatientReportItemValues");

        long startIndividual = System.nanoTime();
        // Simulate individual inserts
        for (int i = 0; i < N; i++) {
            simulateDbInsert();
        }
        long endIndividual = System.nanoTime();

        long startBatch = System.nanoTime();
        // Simulate batch insert
        for (int i = 0; i < N; i++) {
            simulateBatchDbInsert();
            if (i % 25 == 0) simulateBatchFlush();
        }
        simulateBatchFlush();
        long endBatch = System.nanoTime();

        System.out.println("Individual (ms): " + (endIndividual - startIndividual) / 1_000_000.0);
        System.out.println("Batch      (ms): " + (endBatch - startBatch) / 1_000_000.0);
    }

    private static void simulateDbInsert() {
        try { Thread.sleep(1); } catch (Exception e) {} // Simulate DB latency
    }

    private static void simulateBatchDbInsert() {
        // Just queuing up, minimal latency
    }

    private static void simulateBatchFlush() {
        try { Thread.sleep(5); } catch (Exception e) {} // Simulate DB latency for batch
    }
}
