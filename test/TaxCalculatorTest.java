import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

/**
 * Three seed tests. Coverage is intentionally light:
 *  - computeIncomeTax: only the middle slab branch is hit
 *  - computeVAT: only the 18% rate is hit
 *  - applyExemption: only the "under cap" branch is hit
 *  - roundToPaise: not exercised
 *  - isEligibleForReturn: not exercised at all
 */
public class TaxCalculatorTest {

    @Test
    public void incomeTax_middleSlab() {
        TaxCalculator c = new TaxCalculator();
        BigDecimal tax = c.computeIncomeTax(new BigDecimal("700000"));
        // 12500 (5% on 250k-500k) + 40000 (20% on 500k-700k) = 52500
        assertEquals(0, tax.compareTo(new BigDecimal("52500.00")));
    }

    @Test
    public void vat_eighteenPercent() {
        TaxCalculator c = new TaxCalculator();
        BigDecimal gst = c.computeVAT(new BigDecimal("1000"), 18);
        assertEquals(0, gst.compareTo(new BigDecimal("180.00")));
    }

    @Test
    public void exemption_underCap() {
        TaxCalculator c = new TaxCalculator();
        BigDecimal net = c.applyExemption(
                new BigDecimal("600000"), new BigDecimal("120000"));
        assertEquals(0, net.compareTo(new BigDecimal("480000")));
    }

    // ------------------------------------------------------------------
    // isEligibleForReturn -- AI-generated (Session 5A, Part B), reviewed
    // in Part C. Contract: a return is required iff gross income strictly
    // exceeds the basic exemption limit for the age band
    // (<60: 2,50,000; 60-79: 3,00,000; 80+: 5,00,000).
    // ------------------------------------------------------------------

    @Test
    public void isEligibleForReturn_nullIncome_returnsFalse() {
        // Arrange
        TaxCalculator c = new TaxCalculator();
        // Act
        boolean eligible = c.isEligibleForReturn(null, 30);
        // Assert
        assertFalse(eligible);
    }

    @Test
    public void isEligibleForReturn_negativeAge_returnsFalse() {
        // Arrange
        TaxCalculator c = new TaxCalculator();
        // Act
        boolean eligible = c.isEligibleForReturn(new BigDecimal("1000000"), -1);
        // Assert
        assertFalse(eligible);
    }

    @Test
    public void isEligibleForReturn_zeroIncome_returnsFalse() {
        // Arrange
        TaxCalculator c = new TaxCalculator();
        // Act
        boolean eligible = c.isEligibleForReturn(BigDecimal.ZERO, 30);
        // Assert
        assertFalse(eligible);
    }

    @Test
    public void isEligibleForReturn_under60AtExemptionLimit_returnsFalse() {
        // Arrange
        TaxCalculator c = new TaxCalculator();
        // Act
        boolean eligible = c.isEligibleForReturn(new BigDecimal("250000"), 30);
        // Assert
        assertFalse(eligible);
    }

    @Test
    public void isEligibleForReturn_under60OnePaiseAboveLimit_returnsTrue() {
        // Arrange
        TaxCalculator c = new TaxCalculator();
        // Act
        boolean eligible = c.isEligibleForReturn(new BigDecimal("250000.01"), 30);
        // Assert
        assertTrue(eligible);
    }

    @Test
    public void isEligibleForReturn_age59At300000_returnsTrue() {
        // Arrange: 59 is still in the general band (limit 2,50,000)
        TaxCalculator c = new TaxCalculator();
        // Act
        boolean eligible = c.isEligibleForReturn(new BigDecimal("300000"), 59);
        // Assert
        assertTrue(eligible);
    }

    @Test
    public void isEligibleForReturn_age60AtSeniorLimit_returnsFalse() {
        // Arrange: 60 enters the senior band (limit 3,00,000)
        TaxCalculator c = new TaxCalculator();
        // Act
        boolean eligible = c.isEligibleForReturn(new BigDecimal("300000"), 60);
        // Assert
        assertFalse(eligible);
    }

    @Test
    public void isEligibleForReturn_age60AboveSeniorLimit_returnsTrue() {
        // Arrange
        TaxCalculator c = new TaxCalculator();
        // Act
        boolean eligible = c.isEligibleForReturn(new BigDecimal("300000.01"), 60);
        // Assert
        assertTrue(eligible);
    }

    @Test
    public void isEligibleForReturn_age79At500000_returnsTrue() {
        // Arrange: 79 is still a senior (limit 3,00,000), not super-senior
        TaxCalculator c = new TaxCalculator();
        // Act
        boolean eligible = c.isEligibleForReturn(new BigDecimal("500000"), 79);
        // Assert
        assertTrue(eligible);
    }

    @Test
    public void isEligibleForReturn_age80AtSuperSeniorLimit_returnsFalse() {
        // Arrange: 80 enters the super-senior band (limit 5,00,000)
        TaxCalculator c = new TaxCalculator();
        // Act
        boolean eligible = c.isEligibleForReturn(new BigDecimal("500000"), 80);
        // Assert
        assertFalse(eligible);
    }

    @Test
    public void isEligibleForReturn_age80AboveSuperSeniorLimit_returnsTrue() {
        // Arrange
        TaxCalculator c = new TaxCalculator();
        // Act
        boolean eligible = c.isEligibleForReturn(new BigDecimal("500000.01"), 80);
        // Assert
        assertTrue(eligible);
    }

    @Test
    public void isEligibleForReturn_salaried35With12Lakh_returnsTrue() {
        // Arrange: representative happy path
        TaxCalculator c = new TaxCalculator();
        // Act
        boolean eligible = c.isEligibleForReturn(new BigDecimal("1200000"), 35);
        // Assert
        assertTrue(eligible);
    }

    // Part D: kills the surviving mutant at line 70 (`ageYears < 0` -> `ageYears <= 0`).
    // Age 0 is a valid age, so a minor above the general limit must still be eligible.
    @Test
    public void isEligibleForReturn_ageZeroAboveGeneralLimit_returnsTrue() {
        // Arrange
        TaxCalculator c = new TaxCalculator();
        // Act
        boolean eligible = c.isEligibleForReturn(new BigDecimal("300000"), 0);
        // Assert
        assertTrue(eligible);
    }
}
