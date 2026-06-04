/* ============================================================================
 *  DISASTER RESPONSE & RELIEF MANAGEMENT SYSTEM   (Java edition)
 *  Object Oriented Programming Lab  (Code: 24ECSP203)  -  Team 01
 *
 *  Every class, attribute and method shown in the project UML is implemented.
 *
 *  OOP concepts demonstrated:
 *    Abstraction        : Person & Employee are abstract (abstract methods)
 *    Inheritance        : Person -> Employee -> {IncidentCommander, FieldResponder}
 *                         Person -> Victim
 *    Polymorphism       : displayRoleDetails() resolved at run time
 *    Encapsulation      : private/protected fields + public accessors
 *    Singleton pattern  : DisasterResponseSystem (one mission-control instance)
 *    Composition/Aggreg.: Shelter<>FacilityRoom, AffectedZone<>DisasterIncident,
 *                         MedicalRecord<>Diagnosis/Prescription/Treatment,
 *                         Laboratory<>Test, ReliefBatch<>SupplyItem
 *    Exception handling : custom checked SystemException (try/catch/throws)
 *
 *  Compile : javac DisasterResponseApp.java
 *  Run     : java  DisasterResponseApp
 * ========================================================================== */

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/* -------------------------------------------------------------------------- *
 *  Small shared utilities (timestamps, section banners)
 * -------------------------------------------------------------------------- */
final class Util {
    private Util() {}
    static String nowStamp() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    static void banner(String title) {
        System.out.println("\n========================================================");
        System.out.println("  " + title);
        System.out.println("========================================================");
    }
}

/* ==========================================================================
 *  SystemException  -  custom checked exception used across the system
 * ========================================================================== */
class SystemException extends Exception {
    private final int    structuralErrorCode;
    private final String standardExceptionMessage;
    private final String failureTimestamp;

    public SystemException(int code, String message) {
        super(message);
        this.structuralErrorCode      = code;
        this.standardExceptionMessage = message;
        this.failureTimestamp         = Util.nowStamp();
    }

    public void displaySystemFaultNotice() {
        System.out.printf("  [FAULT #%d] %s  (at %s)%n",
                structuralErrorCode, standardExceptionMessage, failureTimestamp);
    }
    public void logErrorToSecureFile() {
        System.out.printf("  [LOG] error %d written to secure file at %s.%n",
                structuralErrorCode, failureTimestamp);
    }
    public void triggerFailSafeShutdown() {
        System.out.println("  [FAIL-SAFE] persisting state before controlled shutdown.");
    }
    public int getStructuralErrorCode() { return structuralErrorCode; }
}

/* ==========================================================================
 *  MODULE 1 : Personnel, Emergency Command & Spatial Logistics
 * ========================================================================== */

/* ---------- Person  (Abstract base) -------------------------------------- */
abstract class Person {
    protected String id;
    protected String name;
    protected int    age;
    protected String gender;
    protected String phoneNumber;

    public Person(String id, String name, int age, String gender, String phoneNumber) {
        this.id = id; this.name = name; this.age = age;
        this.gender = gender; this.phoneNumber = phoneNumber;
    }

    public abstract void displayRoleDetails();          // makes Person abstract

    public void updateContactInfo(String newPhone) {
        this.phoneNumber = newPhone;
        System.out.println("  Contact for " + name + " updated to " + newPhone + ".");
    }
    public boolean verifyIdentityCredentials() {
        return id != null && !id.isEmpty() && age > 0 && name != null && !name.isEmpty();
    }
    public String getId()   { return id; }
    public String getName() { return name; }
}

/* ---------- Employee  (Abstract, inherits Person) ------------------------ */
abstract class Employee extends Person {
    protected String empId;
    protected double salary;
    protected String deploymentDate;
    protected String operationalStatus;                 // ACTIVE / STANDBY / OFF-DUTY

    public Employee(String id, String name, int age, String gender, String phoneNumber,
                    String empId, double salary, String operationalStatus) {
        super(id, name, age, gender, phoneNumber);
        this.empId = empId; this.salary = salary;
        this.deploymentDate = Util.nowStamp();
        this.operationalStatus = operationalStatus;
    }

    public abstract void displayRoleDetails();          // still abstract

    public double calculateHazardPayBonus() {
        double factor = operationalStatus.equals("ACTIVE") ? 0.30 : 0.10;
        return salary * factor;
    }
    public void updateOperationalStatus(String status) {
        this.operationalStatus = status;
        System.out.println("  " + name + " operational status -> " + status + ".");
    }
    public String getEmpId() { return empId; }
}

/* ---------- IncidentCommander  (inherits Employee) ----------------------- */
class IncidentCommander extends Employee {
    private int    commandTier;            // 1 = highest authority
    private int    managedIncidentsCount;
    private String authorizationToken;

    public IncidentCommander(String id, String name, int age, String gender, String phone,
                             String empId, double salary, int commandTier, String token) {
        super(id, name, age, gender, phone, empId, salary, "ACTIVE");
        this.commandTier = commandTier;
        this.managedIncidentsCount = 0;
        this.authorizationToken = token;
    }

    public void authorizeDeployment(String zoneId, String responderId) {
        System.out.println("  CMDR " + name + " authorizes responder " + responderId
                + " to zone " + zoneId + " [token " + authorizationToken + "].");
    }
    public int evaluateIncidentSeverity(String incidentId) {
        managedIncidentsCount++;
        int severity = 7;   // demo heuristic
        System.out.println("  CMDR " + name + " rates incident " + incidentId
                + " severity = " + severity + "/10.");
        return severity;
    }
    public boolean requestFederalBackup(String justification) throws SystemException {
        if (commandTier > 2)
            throw new SystemException(401,
                    "Command tier too low to request federal backup");
        System.out.println("  Federal backup requested. Justification: " + justification + ".");
        return true;
    }
    public void requestEmergencyRelief(String batchId) {
        System.out.println("  CMDR " + name + " files pull-request for relief batch "
                + batchId + ".");
    }
    @Override
    public void displayRoleDetails() {
        System.out.printf("  [IncidentCommander] %s (%s) tier %d, managing %d incident(s), "
                + "hazard bonus = %.2f.%n",
                name, empId, commandTier, managedIncidentsCount, calculateHazardPayBonus());
    }
}

/* ---------- FieldResponder  (inherits Employee) -------------------------- */
class FieldResponder extends Employee {
    private String tacticalSpecialty;
    private int    certificationLevel;
    private String currentGpsCoordinates;

    public FieldResponder(String id, String name, int age, String gender, String phone,
                          String empId, double salary, String specialty, int certLevel) {
        super(id, name, age, gender, phone, empId, salary, "ACTIVE");
        this.tacticalSpecialty = specialty;
        this.certificationLevel = certLevel;
        this.currentGpsCoordinates = "0.0,0.0";
    }

    public void reportStatus(String status) {
        System.out.println("  Responder " + name + " reports: " + status + ".");
    }
    public void updateLocationTelemetry(String coordinates) {
        this.currentGpsCoordinates = coordinates;
        System.out.println("  Responder " + name + " GPS -> " + coordinates + ".");
    }
    public void administerFirstAid(String victimId) {
        System.out.println("  Responder " + name + " administers first aid to victim "
                + victimId + ".");
    }
    @Override
    public void displayRoleDetails() {
        System.out.println("  [FieldResponder] " + name + " (" + empId + ") specialty="
                + tacticalSpecialty + ", cert L" + certificationLevel
                + ", GPS=" + currentGpsCoordinates + ".");
    }
}

/* ---------- Victim  (inherits Person) ------------------------------------ */
class Victim extends Person {
    private String  victimId;
    private int     triageStatus;          // 1 = critical, higher = less severe
    private boolean medicalAlertFlag;
    private String  specializedNeedsLog;

    public Victim(String id, String name, int age, String gender, String phone,
                  String victimId, int triageStatus) {
        super(id, name, age, gender, phone);
        this.victimId = victimId;
        this.triageStatus = triageStatus;
        this.medicalAlertFlag = false;
        this.specializedNeedsLog = "none";
    }

    public void updateTriage(int tier) throws SystemException {
        if (tier < 1) throw new SystemException(110, "Invalid triage tier (< 1)");
        this.triageStatus = tier;
        System.out.println("  Victim " + name + " triage -> tier " + tier + ".");
    }
    public void logSpecializedNeeds(String needs) {
        this.specializedNeedsLog = needs;
        System.out.println("  Victim " + name + " special needs: " + needs + ".");
    }
    public void flagCriticalMedicalAlert() {
        this.medicalAlertFlag = true;
        this.triageStatus = 1;
        System.out.println("  *** CRITICAL MEDICAL ALERT raised for " + name + " ***");
    }
    @Override
    public void displayRoleDetails() {
        System.out.println("  [Victim] " + name + " (" + victimId + ") triage="
                + triageStatus + (medicalAlertFlag ? " [ALERT]" : "")
                + ", needs=" + specializedNeedsLog + ".");
    }
    public String getVictimId() { return victimId; }
}

/* ---------- DisasterIncident --------------------------------------------- */
class DisasterIncident {
    private String  incidentId;
    private String  hazardType;
    private int     severityScale;          // 1 - 10
    private boolean isActive;

    public DisasterIncident(String incidentId, String hazardType, int severityScale) {
        this.incidentId = incidentId; this.hazardType = hazardType;
        this.severityScale = severityScale; this.isActive = false;
    }

    public void triggerAlert() {
        isActive = true;
        System.out.println("  Incident " + incidentId + " (" + hazardType
                + ") ALERT triggered, severity " + severityScale + ".");
    }
    public void resolveIncident() {
        isActive = false;
        System.out.println("  Incident " + incidentId
                + " resolved; nearby shelter capacity released.");
    }
    public double calculateImpactRadius() { return severityScale * 1.5; }  // km

    public String getIncidentId() { return incidentId; }
    public int    getSeverityScale() { return severityScale; }
}

/* ---------- AffectedZone  (aggregates DisasterIncident) ------------------ */
class AffectedZone {
    private String  zoneId;
    private String  quadrantGeocode;
    private boolean evacuationRequired;
    private int     casualtyCount;
    private final List<DisasterIncident> registeredIncidents = new ArrayList<>();  // 1..*

    public AffectedZone(String zoneId, String quadrantGeocode) {
        this.zoneId = zoneId; this.quadrantGeocode = quadrantGeocode;
        this.evacuationRequired = false; this.casualtyCount = 0;
    }

    public void addIncident(DisasterIncident i) throws SystemException {
        if (i == null) throw new SystemException(120, "Null incident cannot be added to zone");
        registeredIncidents.add(i);
        System.out.println("  Zone " + zoneId + " now tracks incident " + i.getIncidentId() + ".");
    }
    public String assessEvacuationUrgency() {
        int maxSev = 0;
        for (DisasterIncident inc : registeredIncidents)
            if (inc.getSeverityScale() > maxSev) maxSev = inc.getSeverityScale();
        evacuationRequired = maxSev >= 6;
        String verdict = evacuationRequired ? "EVACUATE" : "monitor";
        System.out.println("  Zone " + zoneId + " evacuation urgency = " + maxSev
                + " (" + verdict + ").");
        return verdict;
    }
    public void broadcastEmergencyNotification(String msg) {
        System.out.println("  [BROADCAST @" + quadrantGeocode + "] " + msg);
    }
    public String getZoneId() { return zoneId; }
}

/* ---------- FacilityRoom  (granular shelter capacity) -------------------- */
class FacilityRoom {
    private int     roomNo;
    private int     currentBedVacancyCount;
    private boolean specializedMedicalSetup;
    private boolean isolationProtocolActive;

    public FacilityRoom(int roomNo, int beds, boolean specializedMedicalSetup) {
        this.roomNo = roomNo; this.currentBedVacancyCount = beds;
        this.specializedMedicalSetup = specializedMedicalSetup;
        this.isolationProtocolActive = false;
    }

    public void occupySpace() throws SystemException {
        if (currentBedVacancyCount <= 0)
            throw new SystemException(201,
                    "Room " + roomNo + " is full - capacity violation");
        currentBedVacancyCount--;
        System.out.println("  Room " + roomNo + " occupied; "
                + currentBedVacancyCount + " bed(s) left.");
    }
    public void vacateSpace() {
        currentBedVacancyCount++;
        System.out.println("  Room " + roomNo + " vacated; "
                + currentBedVacancyCount + " bed(s) free.");
    }
    public void toggleIsolationProtocol() {
        isolationProtocolActive = !isolationProtocolActive;
        System.out.println("  Room " + roomNo + " isolation protocol "
                + (isolationProtocolActive ? "ON" : "OFF") + ".");
    }
    public int getVacancy() { return currentBedVacancyCount; }
    public int getRoomNo()  { return roomNo; }
}

/* ---------- Shelter  (composes FacilityRoom) ----------------------------- */
class Shelter {
    private String shelterId;
    private String structuralAddress;
    private int    maxCapacity;
    private String safetyRating;
    private final List<FacilityRoom> rooms = new ArrayList<>();   // 1..*

    public Shelter(String shelterId, String structuralAddress, int maxCapacity, String safetyRating) {
        this.shelterId = shelterId; this.structuralAddress = structuralAddress;
        this.maxCapacity = maxCapacity; this.safetyRating = safetyRating;
    }

    public void addRoom(FacilityRoom r) {
        rooms.add(r);
        System.out.println("  Shelter " + shelterId + " + room " + r.getRoomNo() + ".");
    }
    public double calculateCurrentOccupancyPercentage() {
        int freeBeds = 0;
        for (FacilityRoom r : rooms) freeBeds += r.getVacancy();
        int occupied = maxCapacity - freeBeds;
        if (maxCapacity == 0) return 0.0;
        return (occupied * 100.0) / maxCapacity;
    }
    public boolean verifyStructuralSafety() {
        return safetyRating.equals("A") || safetyRating.equals("B");
    }
    public FacilityRoom roomWithVacancy() throws SystemException {
        for (FacilityRoom r : rooms) if (r.getVacancy() > 0) return r;
        throw new SystemException(202, "Shelter " + shelterId
                + " full - no vacant bed available");
    }
    public String getShelterId() { return shelterId; }
}

/* ---------- EmergencyAssignment  (responder <-> incident) ---------------- */
class EmergencyAssignment {
    private String  assignmentId;
    private String  operationalShift;
    private String  activationTimestamp;
    private boolean isCompleted;
    private String  responderId;          // assigns FieldResponder
    private String  incidentId;           // references DisasterIncident

    public EmergencyAssignment(String assignmentId, String operationalShift,
                               String responderId, String incidentId) {
        this.assignmentId = assignmentId; this.operationalShift = operationalShift;
        this.responderId = responderId; this.incidentId = incidentId;
        this.activationTimestamp = "-"; this.isCompleted = false;
    }

    public void activateAssignment() {
        activationTimestamp = Util.nowStamp();
        System.out.println("  Assignment " + assignmentId + " ACTIVE (" + operationalShift
                + ") responder " + responderId + " -> incident " + incidentId
                + " @ " + activationTimestamp + ".");
    }
    public void terminateAssignment() {
        isCompleted = true;
        System.out.println("  Assignment " + assignmentId + " completed.");
    }
    public void logFieldIncidentNotes(String notes) {
        System.out.println("  [NOTE " + assignmentId + "] " + notes);
    }
    public String getAssignmentId() { return assignmentId; }
}

/* ==========================================================================
 *  MODULE 2 : Clinical, Supply Chain & Financial Logistics
 * ========================================================================== */

/* ---------- Diagnosis ----------------------------------------------------- */
class Diagnosis {
    private String diagnosisCodeId;
    private String symptomaticFindings;
    private int    acuityLevel;            // 1 (mild) - 5 (critical)
    private String assessmentDate;

    public Diagnosis() { this.acuityLevel = 1; this.assessmentDate = Util.nowStamp(); }

    public void addDiagnosis(String code, String findings) { addDiagnosis(code, findings, 3); }

    public void addDiagnosis(String code, String findings, int acuity) {
        this.diagnosisCodeId = code; this.symptomaticFindings = findings;
        this.acuityLevel = acuity; this.assessmentDate = Util.nowStamp();
        System.out.println("    + Diagnosis " + code + ": " + findings
                + " (acuity " + acuity + ").");
    }
    public boolean isLifeThreatening() { return acuityLevel >= 4; }
    public String getDiagnosisCodeId() { return diagnosisCodeId; }
}

/* ---------- Prescription -------------------------------------------------- */
class Prescription {
    private String  scriptId;
    private String  pharmaceuticalCompoundName;
    private String  dosageFrequencyInterval;
    private boolean fulfillmentStatus;

    public Prescription(String scriptId, String drug, String dosage) {
        this.scriptId = scriptId; this.pharmaceuticalCompoundName = drug;
        this.dosageFrequencyInterval = dosage; this.fulfillmentStatus = false;
    }

    public void generatePrescription() {
        System.out.println("    + Rx " + scriptId + ": " + pharmaceuticalCompoundName
                + " (" + dosageFrequencyInterval + ").");
    }
    public void markAsFulfilled() {
        fulfillmentStatus = true;
        System.out.println("    Rx " + scriptId + " fulfilled by logistics batch.");
    }
}

/* ---------- Treatment ----------------------------------------------------- */
class Treatment {
    private String therapeuticProcedureType;
    private double consumableResourceCost;
    private double successProbability;

    public Treatment() { this.consumableResourceCost = 0; this.successProbability = 0; }

    public void assignTreatment(String type, double cost) { assignTreatment(type, cost, 0.85); }

    public void assignTreatment(String type, double cost, double prob) {
        this.therapeuticProcedureType = type;
        this.consumableResourceCost = cost;
        this.successProbability = prob;
        System.out.printf("    + Treatment %s cost=%.2f p(success)=%.2f.%n", type, cost, prob);
    }
    public void executeEmergencyProcedure() {
        System.out.printf("    Executing %s (success probability %.2f).%n",
                therapeuticProcedureType, successProbability);
    }
    public double getConsumableResourceCost() { return consumableResourceCost; }
}

/* ---------- Test ---------------------------------------------------------- */
class Test {
    private String testId;
    private String testName;
    private double fee;

    public Test(String testId, String testName, double fee) {
        this.testId = testId; this.testName = testName; this.fee = fee;
    }

    public void displayTestDetails() {
        System.out.printf("    Test %s - %s (fee %.2f).%n", testId, testName, fee);
    }
    public double getFee()     { return fee; }
    public String getTestName(){ return testName; }
}

/* ---------- MedicalRecord  (composes Diagnosis/Prescription/Treatment) --- */
class MedicalRecord {
    private String chartRecordId;
    private String trackingVictimId;
    private String bloodType;
    private final List<Diagnosis>    diagnoses    = new ArrayList<>();   // 1..*
    private final List<Prescription> scripts      = new ArrayList<>();   // 1..*
    private final List<Treatment>    treatments   = new ArrayList<>();   // 1..*

    public MedicalRecord(String chartRecordId, String trackingVictimId, String bloodType) {
        this.chartRecordId = chartRecordId;
        this.trackingVictimId = trackingVictimId;
        this.bloodType = bloodType;
    }

    public void appendChartEntry(Diagnosis d, Prescription p, Treatment t) {
        if (d != null) diagnoses.add(d);
        if (p != null) scripts.add(p);
        if (t != null) treatments.add(t);
        System.out.println("    Chart " + chartRecordId + " updated for victim "
                + trackingVictimId + ".");
    }
    public String extractLongitudinalMedicalHistory() {
        return "Chart " + chartRecordId + ": " + diagnoses.size()
                + " diagnosis/-es, " + scripts.size() + " script(s), "
                + treatments.size() + " treatment(s).";
    }
    public void displayRecord() {
        System.out.println("    " + extractLongitudinalMedicalHistory()
                + " Blood type: " + bloodType + ".");
    }
}

/* ---------- Laboratory  (composes Test, updates MedicalRecord) ----------- */
class Laboratory {
    private String labId;
    private String labName;
    private final List<Test> testCatalog = new ArrayList<>();   // 1..*

    public Laboratory(String labId, String labName) {
        this.labId = labId; this.labName = labName;
    }

    public void addTest(Test t) { testCatalog.add(t); }

    public void performTest(Victim patient, Test t) throws SystemException {
        if (t == null) throw new SystemException(305, "Unknown test requested at lab " + labId);
        System.out.println("  Lab " + labName + " runs '" + t.getTestName()
                + "' for victim " + patient.getVictimId() + ".");
    }
    public void generateReport(Victim patient, MedicalRecord chart) {
        Diagnosis d = new Diagnosis();
        d.addDiagnosis("ICD-FLOOD-01", "hypothermia + dehydration", 4);
        chart.appendChartEntry(d, null, null);
        System.out.println("  Lab report for victim " + patient.getVictimId()
                + " auto-filed to medical chart.");
    }
}

/* ---------- SupplyItem ---------------------------------------------------- */
class SupplyItem {
    private String stockKeepingUnitSKU;
    private String itemCategoryLabel;
    private double unitCostValuation;
    private int    currentStockLevel;

    public SupplyItem(String sku, String category, double cost, int stock) {
        this.stockKeepingUnitSKU = sku; this.itemCategoryLabel = category;
        this.unitCostValuation = cost; this.currentStockLevel = stock;
    }

    public void displayItemMetrics() {
        System.out.printf("    SKU %s [%s] @%.2f x %d.%n",
                stockKeepingUnitSKU, itemCategoryLabel, unitCostValuation, currentStockLevel);
    }
    public void deductInventoryStock(int quantity) throws SystemException {
        if (quantity > currentStockLevel)
            throw new SystemException(310, "Stock shortage for SKU " + stockKeepingUnitSKU
                    + " (need " + quantity + ", have " + currentStockLevel + ")");
        currentStockLevel -= quantity;
        System.out.println("    Dispatched " + quantity + " x " + stockKeepingUnitSKU
                + "; " + currentStockLevel + " left.");
    }
    public boolean triggerLowStockAlert() {
        boolean low = currentStockLevel < 10;
        if (low) System.out.println("    [LOW STOCK] re-order " + stockKeepingUnitSKU + ".");
        return low;
    }
    public double getUnitCostValuation() { return unitCostValuation; }
}

/* ---------- DonationPayment ----------------------------------------------- */
class DonationPayment {
    private String  transactionalPaymentId;
    private double  clearFundsAmount;
    private String  routingChannelApproach;
    private boolean anonymousDonorFlag;

    public DonationPayment(String id, double amount, String channel, boolean anonymous) {
        this.transactionalPaymentId = id; this.clearFundsAmount = amount;
        this.routingChannelApproach = channel; this.anonymousDonorFlag = anonymous;
    }

    public boolean authorizationHandshake() throws SystemException {
        if (clearFundsAmount <= 0) throw new SystemException(420, "Invalid donation amount");
        System.out.printf("  Donation %s of %.2f authorized via %s.%n",
                transactionalPaymentId, clearFundsAmount, routingChannelApproach);
        return true;
    }
    public void refundTransaction() {
        System.out.println("  Donation " + transactionalPaymentId + " refunded.");
    }
    public void issueTaxExemptionReceipt() {
        System.out.println("  Tax-exemption receipt issued"
                + (anonymousDonorFlag ? " (donor kept anonymous)." : "."));
    }
    public double getClearFundsAmount() { return clearFundsAmount; }
}

/* ---------- AllocationInvoice  (balanced by DonationPayment) ------------- */
class AllocationInvoice {
    private String invoiceId;
    private String associatedTargetZoneId;
    private double aggregatedOperationalCost;
    private String approvalSignature;

    public AllocationInvoice(String invoiceId, String associatedTargetZoneId) {
        this.invoiceId = invoiceId; this.associatedTargetZoneId = associatedTargetZoneId;
        this.aggregatedOperationalCost = 0; this.approvalSignature = "PENDING";
    }

    public void addCost(double c) { aggregatedOperationalCost += c; }

    public void finaliseInvoiceCalculation() {
        approvalSignature = "SIGNED";
        System.out.printf("  Invoice %s (zone %s) total = %.2f.%n",
                invoiceId, associatedTargetZoneId, aggregatedOperationalCost);
    }
    public void applyForGovernmentSubsidy() {
        double subsidy = aggregatedOperationalCost * 0.40;
        aggregatedOperationalCost -= subsidy;
        System.out.printf("  Govt subsidy %.2f applied; net = %.2f.%n",
                subsidy, aggregatedOperationalCost);
    }
    public boolean auditExpenditures() {
        System.out.println("  Audit OK for invoice " + invoiceId + ".");
        return true;
    }
    public boolean settleWith(DonationPayment donation) {
        if (donation.getClearFundsAmount() + 1e-6 < aggregatedOperationalCost) {
            System.out.println("  Donation insufficient to clear invoice " + invoiceId + ".");
            return false;
        }
        System.out.println("  Invoice " + invoiceId + " cleared by donor network.");
        return true;
    }
    public double getAggregatedOperationalCost() { return aggregatedOperationalCost; }
}

/* ---------- ReliefBatch  (composes SupplyItem, generates AllocationInvoice) */
class ReliefBatch {
    private String cargoBatchId;
    private String logisticsSourceNode;
    private String deliveryStatus;
    private final List<SupplyItem> items = new ArrayList<>();   // *

    public ReliefBatch(String cargoBatchId, String logisticsSourceNode) {
        this.cargoBatchId = cargoBatchId; this.logisticsSourceNode = logisticsSourceNode;
        this.deliveryStatus = "PACKED";
    }

    public void bundleItem(SupplyItem s) {
        items.add(s);
        System.out.println("  Batch " + cargoBatchId + " bundles a supply item.");
    }
    public void dispatchBatch() {
        deliveryStatus = "IN-TRANSIT";
        System.out.println("  Batch " + cargoBatchId + " dispatched from "
                + logisticsSourceNode + ".");
    }
    public void updateDeliveryRoute(String checkpoint) {
        System.out.println("  Batch " + cargoBatchId + " reached " + checkpoint + ".");
    }
    public boolean verifyExpirationDates() {
        System.out.println("  Batch " + cargoBatchId + " expiry check passed.");
        return true;
    }
    public AllocationInvoice generateAllocationInvoice(String invId, String zoneId) {
        AllocationInvoice inv = new AllocationInvoice(invId, zoneId);
        for (SupplyItem s : items) inv.addCost(s.getUnitCostValuation());
        System.out.println("  Batch " + cargoBatchId + " generated invoice " + invId + ".");
        return inv;
    }
    public String getCargoBatchId() { return cargoBatchId; }
}

/* ==========================================================================
 *  DisasterResponseSystem  -  SINGLETON mission-control instance
 * ========================================================================== */
class DisasterResponseSystem {
    private String systemId;
    private String missionControlCenter;
    private final List<Employee>            employeeList   = new ArrayList<>();
    private final List<Victim>              victimList     = new ArrayList<>();
    private final List<EmergencyAssignment> assignmentList = new ArrayList<>();

    private static DisasterResponseSystem instance;        // the single instance

    private DisasterResponseSystem() {                     // private constructor
        this.systemId = "DRRMS-01";
        this.missionControlCenter = "Central Command";
    }

    public static DisasterResponseSystem getInstance() {
        if (instance == null) instance = new DisasterResponseSystem();
        return instance;
    }

    public void addEmployee(Employee e) throws SystemException {
        for (Employee ex : employeeList)
            if (ex.getEmpId().equals(e.getEmpId()))
                throw new SystemException(501, "Duplicate employee registration: " + e.getEmpId());
        employeeList.add(e);
        System.out.println("  Registered employee " + e.getName() + ".");
    }
    public void registerVictim(Victim v) throws SystemException {
        for (Victim ex : victimList)
            if (ex.getVictimId().equals(v.getVictimId()))
                throw new SystemException(502, "Duplicate victim registration: " + v.getVictimId());
        victimList.add(v);
        System.out.println("  Registered victim " + v.getName() + ".");
    }
    public void createAssignment(EmergencyAssignment a) {
        assignmentList.add(a);
        System.out.println("  Logged assignment " + a.getAssignmentId() + ".");
    }
    public void generateGlobalSituationReport() {
        Util.banner("GLOBAL SITUATION REPORT  (" + missionControlCenter + ")");
        System.out.println("  System: " + systemId);
        System.out.println("  Personnel on roster (" + employeeList.size() + "):");
        for (Employee e : employeeList) e.displayRoleDetails();      // polymorphism
        System.out.println("  Victims registered (" + victimList.size() + "):");
        for (Victim v : victimList) v.displayRoleDetails();          // polymorphism
        System.out.println("  Active assignments: " + assignmentList.size());
    }
}

/* ==========================================================================
 *  DRIVER  -  a complete disaster-response scenario, run end to end
 * ========================================================================== */
public class DisasterResponseApp {
    public static void main(String[] args) throws SystemException {

        Util.banner("DISASTER RESPONSE & RELIEF MANAGEMENT SYSTEM - Team 01");

        // single mission-control instance (Singleton)
        DisasterResponseSystem drs = DisasterResponseSystem.getInstance();

        /* ----- 1. Personnel & command ----------------------------------- */
        Util.banner("1. PERSONNEL REGISTRATION & COMMAND");
        IncidentCommander cmdr = new IncidentCommander(
                "P100", "Omkar", 38, "M", "99990001", "E-CMD-1", 95000, 1, "TOKEN-ALPHA");
        FieldResponder resp1 = new FieldResponder(
                "P101", "Santhosh", 29, "M", "99990002", "E-FR-1", 52000, "Water Rescue", 3);
        FieldResponder resp2 = new FieldResponder(
                "P102", "Subhash", 31, "M", "99990003", "E-FR-2", 54000, "Medical Evac", 4);

        drs.addEmployee(cmdr);
        drs.addEmployee(resp1);
        drs.addEmployee(resp2);

        System.out.println("  Identity check (cmdr): " + cmdr.verifyIdentityCredentials());
        cmdr.requestFederalBackup("Severity-8 flood, multiple zones");
        resp2.updateOperationalStatus("STANDBY");

        // demonstrate duplicate-registration exception (try/catch)
        try {
            drs.addEmployee(new FieldResponder(
                    "P103", "Clone", 30, "M", "0", "E-FR-1", 50000, "X", 1));
        } catch (SystemException ex) {
            ex.displaySystemFaultNotice();
            ex.logErrorToSecureFile();
        }

        /* ----- 2. Disaster, zone & incidents ---------------------------- */
        Util.banner("2. ZONE, INCIDENTS & EVACUATION");
        AffectedZone zone = new AffectedZone("Z-NORTH", "12.97N,77.59E");
        DisasterIncident flood = new DisasterIncident("INC-001", "Flood", 8);
        DisasterIncident fire  = new DisasterIncident("INC-002", "Wildfire", 5);

        flood.triggerAlert();
        fire.triggerAlert();
        zone.addIncident(flood);
        zone.addIncident(fire);
        cmdr.evaluateIncidentSeverity(flood.getIncidentId());
        System.out.printf("  Flood impact radius: %.2f km.%n", flood.calculateImpactRadius());
        zone.assessEvacuationUrgency();
        zone.broadcastEmergencyNotification("Evacuate low-lying areas immediately.");

        /* ----- 3. Deployment & assignment ------------------------------- */
        Util.banner("3. DEPLOYMENT & FIELD ASSIGNMENT");
        cmdr.authorizeDeployment(zone.getZoneId(), resp1.getEmpId());
        EmergencyAssignment asg = new EmergencyAssignment(
                "ASG-1", "Day", resp1.getEmpId(), flood.getIncidentId());
        drs.createAssignment(asg);
        asg.activateAssignment();
        resp1.updateLocationTelemetry("12.96N,77.60E");
        resp1.reportStatus("On-site, water level rising");
        resp1.updateContactInfo("99991111");
        asg.logFieldIncidentNotes("3 families stranded on rooftop, boat requested.");

        /* ----- 4. Victims, shelter & rooms (composition) ---------------- */
        Util.banner("4. VICTIM INTAKE & SHELTER ALLOCATION");
        Victim vic1 = new Victim("P200", "Rahul", 45, "M", "88880001", "V-001", 2);
        Victim vic2 = new Victim("P201", "Meena", 27, "F", "88880002", "V-002", 3);
        drs.registerVictim(vic1);
        drs.registerVictim(vic2);

        vic1.flagCriticalMedicalAlert();
        vic2.updateTriage(2);
        vic2.logSpecializedNeeds("insulin, special diet");
        resp2.administerFirstAid(vic1.getVictimId());

        Shelter shelter = new Shelter("SH-01", "Relief Camp Rd, North", 4, "A");
        FacilityRoom roomMed = new FacilityRoom(101, 2, true);
        FacilityRoom roomGen = new FacilityRoom(102, 2, false);
        shelter.addRoom(roomMed);
        shelter.addRoom(roomGen);
        roomMed.toggleIsolationProtocol();
        System.out.println("  Shelter structurally safe: " + shelter.verifyStructuralSafety() + ".");

        // allocate beds, then deliberately overflow to show the exception
        try {
            shelter.roomWithVacancy().occupySpace();   // bed 1
            shelter.roomWithVacancy().occupySpace();   // bed 2
            shelter.roomWithVacancy().occupySpace();   // bed 3
            shelter.roomWithVacancy().occupySpace();   // bed 4
            shelter.roomWithVacancy().occupySpace();   // 5th -> throws
        } catch (SystemException ex) {
            ex.displaySystemFaultNotice();
            ex.triggerFailSafeShutdown();
        }
        roomGen.vacateSpace();   // free a bed back up
        System.out.printf("  Shelter occupancy ~ %.2f%%.%n",
                shelter.calculateCurrentOccupancyPercentage());

        /* ----- 5. Clinical track ---------------------------------------- */
        Util.banner("5. CLINICAL: LAB, DIAGNOSIS, Rx & TREATMENT");
        MedicalRecord chart = new MedicalRecord("MR-001", vic1.getVictimId(), "O+");
        Laboratory lab = new Laboratory("LAB-1", "Field Pathology Unit");
        Test bloodTest = new Test("T-1", "Complete Blood Count", 350.0);
        lab.addTest(bloodTest);
        bloodTest.displayTestDetails();

        lab.performTest(vic1, bloodTest);
        lab.generateReport(vic1, chart);              // auto-files a diagnosis

        Diagnosis diag = new Diagnosis();
        diag.addDiagnosis("ICD-RESP-02", "respiratory distress", 5);
        System.out.println("    Life-threatening? " + diag.isLifeThreatening());

        Prescription rx = new Prescription("RX-1", "Antibiotic IV", "8h x 5d");
        Treatment tr = new Treatment();
        rx.generatePrescription();
        tr.assignTreatment("IV rehydration", 800.0, 0.92);
        tr.executeEmergencyProcedure();
        chart.appendChartEntry(diag, rx, tr);
        rx.markAsFulfilled();
        chart.displayRecord();

        /* ----- 6. Supply chain & finance -------------------------------- */
        Util.banner("6. SUPPLY CHAIN, INVOICE & DONATION");
        cmdr.requestEmergencyRelief("BATCH-1");
        ReliefBatch batch = new ReliefBatch("BATCH-1", "Central Warehouse");
        SupplyItem water = new SupplyItem("SKU-W", "Water 1L", 12.0, 500);
        SupplyItem meds  = new SupplyItem("SKU-M", "Med Kit", 450.0, 8);
        batch.bundleItem(water);
        batch.bundleItem(meds);
        water.displayItemMetrics();
        meds.displayItemMetrics();

        batch.verifyExpirationDates();
        batch.dispatchBatch();
        batch.updateDeliveryRoute("Checkpoint North-2");
        water.deductInventoryStock(120);
        meds.triggerLowStockAlert();

        // demonstrate supply-shortage exception (try/catch)
        try {
            meds.deductInventoryStock(50);            // only 8 in stock -> throws
        } catch (SystemException ex) {
            ex.displaySystemFaultNotice();
            ex.logErrorToSecureFile();
        }

        AllocationInvoice invoice = batch.generateAllocationInvoice("INV-1", zone.getZoneId());
        invoice.addCost(tr.getConsumableResourceCost());   // fold in treatment cost
        invoice.addCost(bloodTest.getFee());               // and lab fee
        invoice.finaliseInvoiceCalculation();
        invoice.applyForGovernmentSubsidy();
        invoice.auditExpenditures();

        DonationPayment donation = new DonationPayment("PAY-1", 5000.0, "SWIFT-WIRE", true);
        donation.authorizationHandshake();
        invoice.settleWith(donation);
        donation.issueTaxExemptionReceipt();

        // a refund path, just to exercise the method
        DonationPayment overpay = new DonationPayment("PAY-2", 200.0, "CARD", false);
        overpay.authorizationHandshake();
        overpay.refundTransaction();

        /* ----- 7. Global report & stand-down ---------------------------- */
        drs.generateGlobalSituationReport();

        Util.banner("7. STAND-DOWN");
        asg.terminateAssignment();
        flood.resolveIncident();
        fire.resolveIncident();

        System.out.println("\nAll modules executed successfully. System operating nominally.");
    }
}
