package com.divudi.ejb;

import com.divudi.core.entity.Institution;
import com.divudi.core.entity.Person;
import com.divudi.core.entity.Staff;
import com.divudi.core.entity.channel.SessionInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SmsManagerEjbTest {

    private SmsManagerEjb smsManagerEjb;

    @BeforeEach
    public void setUp() {
        smsManagerEjb = new SmsManagerEjb();
    }

    @Test
    public void createSmsForCDoctorRemider_NullSessionInstance_ReturnsEmptyString() {
        String template = "Hello {doctor}";
        String result = smsManagerEjb.createSmsForCDoctorRemider(null, template);
        assertEquals("", result, "Should return empty string for null SessionInstance");
    }

    @Test
    public void createSmsForCDoctorRemider_ValidSessionInstance_ReplacesAllPlaceholders() {
        // Arrange
        SessionInstance sessionInstance = new SessionInstance();

        Calendar cal = Calendar.getInstance();

        // Starting time: e.g. 14:30
        cal.set(Calendar.HOUR_OF_DAY, 14);
        cal.set(Calendar.MINUTE, 30);
        Date startingTime = cal.getTime();
        sessionInstance.setStartingTime(startingTime);

        // Session date: e.g. 15 August 2023
        cal.set(Calendar.YEAR, 2023);
        cal.set(Calendar.MONTH, Calendar.AUGUST);
        cal.set(Calendar.DAY_OF_MONTH, 15);
        Date sessionDate = cal.getTime();
        sessionInstance.setSessionDate(sessionDate);

        Person person = new Person();
        person.setTitle(com.divudi.core.data.Title.Dr);
        person.setName("John Doe");

        Staff staff = new Staff();
        staff.setPerson(person);
        sessionInstance.setStaff(staff);

        sessionInstance.setBookedPatientCount(5L);
        sessionInstance.setPaidPatientCount(3L);

        Institution institution = new Institution();
        institution.setName("City Hospital");
        sessionInstance.setInstitution(institution);

        String template = "Reminder: Dr {doctor} at {appointment_time} on {appointment_date} at {ins_name}. Booked: {booked}, Paid: {paid}";

        // Act
        String result = smsManagerEjb.createSmsForCDoctorRemider(sessionInstance, template);

        // Assert
        // Expected format for time: "HH:mm" -> "14:30"
        // Expected format for date: "dd MMMMM yyyy" -> "15 August 2023"
        // Title formatting might be "Dr. John Doe" or "Dr John Doe" - let's check getNameWithTitle logic, or assume Dr John Doe based on title enum.
        // Let's actually test what comes out by manually building the expected string.

        String expectedDocName = person.getNameWithTitle();
        // We might just use the actual method result to avoid hardcoding the exact formatting of getNameWithTitle if it depends on implementation.
        // Wait, getNameWithTitle() from Person.java might be:
        // getTitle() + " " + getName() if title != null -> "Dr John Doe"
        // Let's assume the Person's getNameWithTitle() returns "Dr John Doe" if there is no period in enum.

        String expectedResult = "Reminder: Dr " + expectedDocName + " at 14:30 on 15 August 2023 at City Hospital. Booked: 5, Paid: 3";
        assertEquals(expectedResult, result, "All placeholders should be correctly replaced");
    }
}
