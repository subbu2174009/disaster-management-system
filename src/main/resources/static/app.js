// Global variables
let map;
let incidents = [];
let shelters = [];
let victims = [];
let batches = [];
let donations = [];
let invoices = [];
let responders = [];
let assignments = [];
let zones = [];
let markers = [];
let tempReportMarker = null;

// API Endpoints
const INCIDENTS_API = '/api/incidents';
const SHELTERS_API = '/api/shelters';
const VICTIMS_API = '/api/victims';
const BATCHES_API = '/api/logistics/batches';
const SUPPLIES_API = '/api/logistics/supplies';
const FINANCE_API = '/api/finance';
const PERSONNEL_API = '/api/personnel';
const ZONES_API = '/api/zones';

// Initialize the Application
document.addEventListener('DOMContentLoaded', () => {
    initClock();
    initMap();
    fetchData();
});

// Live Clock Widget
function initClock() {
    const clockEl = document.getElementById('live-clock');
    setInterval(() => {
        const now = new Date();
        clockEl.innerHTML = now.toLocaleTimeString() + ' | ' + now.toLocaleDateString() + ' (IST)';
    }, 1000);
}

// Map Initialization
function initMap() {
    map = L.map('map-container').setView([40.7306, -73.9352], 11);

    // Premium Dark Theme Map Layer
    L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> &copy; <a href="https://carto.com/attributions">CARTO</a>',
        subdomains: 'abcd',
        maxZoom: 20
    }).addTo(map);

    map.on('click', (e) => {
        const lat = e.latlng.lat;
        const lng = e.latlng.lng;
        setTempReportMarker(lat, lng);
        openReportModal(lat, lng);
    });
}

function setTempReportMarker(lat, lng) {
    if (tempReportMarker) {
        map.removeLayer(tempReportMarker);
    }
    const pulsingIcon = L.divIcon({
        className: 'temp-report-icon-container',
        html: `<div class="temp-pulse-ring"></div><div class="temp-dot"></div>`,
        iconSize: [20, 20],
        iconAnchor: [10, 10]
    });
    tempReportMarker = L.marker([lat, lng], { icon: pulsingIcon }).addTo(map);
}

// Switch tabs inside Bottom sheet
function switchTab(tabId) {
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));
    
    // Highlight button and show content pane
    const targetBtn = document.querySelector(`[onclick="switchTab('${tabId}')"]`);
    if (targetBtn) targetBtn.classList.add('active');
    
    const targetContent = document.getElementById(tabId);
    if (targetContent) targetContent.classList.add('active');
}

// Fetch all data from backend REST services
async function fetchData() {
    try {
        const [incidentsRes, sheltersRes, victimsRes, batchesRes, donationsRes, invoicesRes, respondersRes, assignmentsRes, zonesRes] = await Promise.all([
            fetch(INCIDENTS_API),
            fetch(SHELTERS_API),
            fetch(VICTIMS_API),
            fetch(BATCHES_API),
            fetch(FINANCE_API + '/donations'),
            fetch(FINANCE_API + '/invoices'),
            fetch(PERSONNEL_API + '/responders'),
            fetch(PERSONNEL_API + '/assignments'),
            fetch(ZONES_API)
        ]);

        incidents = incidentsRes.ok ? await incidentsRes.json() : [];
        shelters = sheltersRes.ok ? await sheltersRes.json() : [];
        victims = victimsRes.ok ? await victimsRes.json() : [];
        batches = batchesRes.ok ? await batchesRes.json() : [];
        donations = donationsRes.ok ? await donationsRes.json() : [];
        invoices = invoicesRes.ok ? await invoicesRes.json() : [];
        responders = respondersRes.ok ? await respondersRes.json() : [];
        assignments = assignmentsRes.ok ? await assignmentsRes.json() : [];
        zones = zonesRes.ok ? await zonesRes.json() : [];

        // Clear existing markers
        markers.forEach(marker => map.removeLayer(marker));
        markers = [];

        renderIncidents();
        renderShelters();
        renderVictims();
        renderBatches();
        renderFinance();
        renderAssignments();
        renderZones();
        plotMapMarkers();
        updateMetrics();

    } catch (error) {
        console.error('Error fetching dashboard data:', error);
    }
}

// Plot pins on the Leaflet map
function plotMapMarkers() {
    // Plot Emergency Shelters
    shelters.forEach(shelter => {
        const shelterHtml = `
            <div class="marker-shelter">
                <i class="fa-solid fa-house-chimney-medical"></i>
            </div>
        `;
        const customIcon = L.divIcon({
            className: 'custom-div-icon',
            html: shelterHtml,
            iconSize: [30, 30],
            iconAnchor: [15, 15]
        });

        const occupancyRate = shelter.capacity > 0 ? ((shelter.occupied / shelter.capacity) * 100).toFixed(0) : 0;

        const marker = L.marker([shelter.latitude, shelter.longitude], { icon: customIcon })
            .bindPopup(`
                <div class="popup-content">
                    <strong style="color:var(--color-success)"><i class="fa-solid fa-house"></i> ${shelter.name}</strong><br/>
                    <small>${shelter.structuralAddress}</small><br/>
                    <span style="font-size:0.8rem">Occupancy: ${shelter.occupied} / ${shelter.capacity} (${occupancyRate}%)</span>
                </div>
            `);
        marker.addTo(map);
        markers.push(marker);
    });

    // Plot Crisis Incidents
    incidents.forEach(incident => {
        const isIncidentActive = (incident.isActive !== undefined ? incident.isActive : incident.active);
        if (!isIncidentActive) return;

        let colorClass = 'info';
        let pulseRing = '';
        let severityLabel = 'LOW';
        if (incident.severityScale >= 9) {
            colorClass = 'critical';
            severityLabel = 'CRITICAL';
            pulseRing = '<div class="pulse-ring red"></div>';
        } else if (incident.severityScale >= 7) {
            colorClass = 'high';
            severityLabel = 'HIGH';
            pulseRing = '<div class="pulse-ring red"></div>';
        } else if (incident.severityScale >= 4) {
            colorClass = 'medium';
            severityLabel = 'MEDIUM';
            pulseRing = '<div class="pulse-ring orange"></div>';
        }

        let iconType = 'fa-triangle-exclamation';
        if (incident.hazardType === 'FLOOD') iconType = 'fa-cloud-showers-heavy';
        else if (incident.hazardType === 'EARTHQUAKE') iconType = 'fa-house-crack';
        else if (incident.hazardType === 'WILDFIRE') iconType = 'fa-fire-flame-curved';
        else if (incident.hazardType === 'HURRICANE') iconType = 'fa-wind';

        const incidentHtml = `
            <div class="marker-incident ${colorClass}-marker">
                ${pulseRing}
                <i class="fa-solid ${iconType}"></i>
            </div>
        `;

        const customIcon = L.divIcon({
            className: 'custom-div-icon',
            html: incidentHtml,
            iconSize: [34, 34],
            iconAnchor: [17, 17]
        });

        const timeString = new Date(incident.reportedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

        const marker = L.marker([incident.latitude, incident.longitude], { icon: customIcon })
            .bindPopup(`
                <div class="popup-content">
                    <strong style="color:var(--color-danger)">${incident.title}</strong><br/>
                    <span class="severity-tag severity-${severityLabel}">Scale: ${incident.severityScale}</span><br/>
                    <p style="margin:5px 0; font-size:0.8rem">${incident.description}</p>
                    <small>Reported at: ${timeString}</small>
                    <div style="margin-top: 8px;">
                        <button class="btn btn-secondary" style="padding:4px 8px; font-size:0.75rem;" onclick="resolveIncident(${incident.id})">
                            <i class="fa-solid fa-check"></i> Mark Resolved
                        </button>
                    </div>
                </div>
            `);
        marker.addTo(map);
        markers.push(marker);
    });
}

// Render incident log in sidebar feed
function renderIncidents() {
    const feed = document.getElementById('incidents-feed');
    feed.innerHTML = '';

    const sorted = [...incidents].sort((a, b) => new Date(b.reportedAt) - new Date(a.reportedAt));

    if (sorted.length === 0) {
        feed.innerHTML = '<div class="loading-state">No incidents logged. System normal.</div>';
        return;
    }

    sorted.forEach(incident => {
        const itemCard = document.createElement('div');
        itemCard.className = 'item-card';
        itemCard.onclick = () => focusMap(incident.latitude, incident.longitude);

        let iconClass = 'text-secondary';
        let severityLabel = 'LOW';
        if (incident.severityScale >= 9) {
            iconClass = 'text-danger';
            severityLabel = 'CRITICAL';
        } else if (incident.severityScale >= 7) {
            iconClass = 'text-danger';
            severityLabel = 'HIGH';
        } else if (incident.severityScale >= 4) {
            iconClass = 'text-warning';
            severityLabel = 'MEDIUM';
        }

        let iconType = 'fa-triangle-exclamation';
        if (incident.hazardType === 'FLOOD') iconType = 'fa-cloud-showers-heavy';
        else if (incident.hazardType === 'EARTHQUAKE') iconType = 'fa-house-crack';
        else if (incident.hazardType === 'WILDFIRE') iconType = 'fa-fire';
        else if (incident.hazardType === 'HURRICANE') iconType = 'fa-wind';

        const reportedTime = new Date(incident.reportedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

        itemCard.innerHTML = `
            <div class="item-header">
                <div class="item-title">
                    <i class="fa-solid ${iconType} ${iconClass}"></i> ${incident.title}
                </div>
                <span class="severity-tag severity-${severityLabel}">Scale: ${incident.severityScale}</span>
            </div>
            <div class="item-desc">${incident.description}</div>
            <div class="item-footer">
                <span class="item-meta"><i class="fa-regular fa-clock"></i> Reported: ${reportedTime}</span>
                <span class="item-meta" style="text-transform: uppercase;">
                    <i class="fa-solid fa-circle-dot" style="font-size:0.5rem; color:${(incident.isActive !== undefined ? incident.isActive : incident.active) ? 'var(--color-danger)' : 'var(--color-success)'}"></i> ${(incident.isActive !== undefined ? incident.isActive : incident.active) ? 'ACTIVE' : 'RESOLVED'}
                </span>
            </div>
        `;
        feed.appendChild(itemCard);
    });
}

// Render shelter cards
function renderShelters() {
    const feed = document.getElementById('shelters-feed');
    feed.innerHTML = '';

    if (shelters.length === 0) {
        feed.innerHTML = '<div class="loading-state">No shelters available.</div>';
        return;
    }

    shelters.forEach(shelter => {
        const occupancyRate = shelter.capacity > 0 ? ((shelter.occupied / shelter.capacity) * 100).toFixed(0) : 0;
        let colorClass = '';
        if (occupancyRate >= 90) colorClass = 'danger';
        else if (occupancyRate >= 70) colorClass = 'warning';

        const card = document.createElement('div');
        card.className = 'shelter-card';
        card.onclick = () => focusMap(shelter.latitude, shelter.longitude);

        card.innerHTML = `
            <div class="shelter-name">
                <i class="fa-solid fa-house-chimney-user text-success"></i> ${shelter.name}
            </div>
            <div class="shelter-occupancy-wrap">
                <div class="occupancy-text">
                    <span>Occupancy Rate</span>
                    <strong>${shelter.occupied} / ${shelter.capacity} beds (${occupancyRate}%)</strong>
                </div>
                <div class="occupancy-bar-bg">
                    <div class="occupancy-bar-fill ${colorClass}" style="width: ${occupancyRate}%"></div>
                </div>
            </div>
            <div class="shelter-meta-info">
                <span><i class="fa-solid fa-location-dot"></i> ${shelter.structuralAddress}</span>
                <span><i class="fa-solid fa-phone"></i> ${shelter.contactInfo}</span>
            </div>
        `;
        feed.appendChild(card);
    });
}

// Render Evacuees Victims Directory Table
function renderVictims() {
    const body = document.getElementById('victims-table-body');
    body.innerHTML = '';

    if (victims.length === 0) {
        body.innerHTML = '<tr><td colspan="7" class="loading-state">No registered evacuees in directory.</td></tr>';
        return;
    }

    victims.forEach(v => {
        const shelterName = v.shelter ? v.shelter.name : 'Unassigned';
        const roomNo = v.facilityRoom ? 'Room ' + v.facilityRoom.roomNo : 'Unassigned';
        const needs = v.specializedNeedsLog || 'None';
        
        let triageTag = `<span class="severity-tag severity-LOW">Tier 3 (Stable)</span>`;
        if (v.triageStatus && v.triageStatus.includes("Tier 2")) triageTag = `<span class="severity-tag severity-MEDIUM">Tier 2 (Urgent)</span>`;
        else if (v.triageStatus && v.triageStatus.includes("Tier 1")) triageTag = `<span class="severity-tag severity-CRITICAL">Tier 1 (Critical)</span>`;

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>${v.name}</strong></td>
            <td>Age: ${v.age} | ${v.gender}</td>
            <td>${triageTag}</td>
            <td>${shelterName}</td>
            <td><span class="badge success-badge">${roomNo}</span></td>
            <td><small>${needs}</small></td>
            <td>
                <button class="btn btn-secondary btn-sm" onclick="openMedModal(${v.id}, '${v.name}', '${needs.replace(/'/g, "\\'")}')">
                    <i class="fa-solid fa-notes-medical"></i> Medical File
                </button>
            </td>
        `;
        body.appendChild(tr);
    });
}

// Render Relief Cargo Batches
function renderBatches() {
    const body = document.getElementById('batches-table-body');
    body.innerHTML = '';

    if (batches.length === 0) {
        body.innerHTML = '<tr><td colspan="6" class="loading-state">No cargo batches pulls active.</td></tr>';
        return;
    }

    batches.forEach(b => {
        const totalValuation = b.items ? b.items.reduce((sum, item) => sum + (item.unitCostValuation * item.currentStockLevel), 0) : 0;
        const itemCount = b.items ? b.items.length : 0;

        let statusClass = 'warning';
        if (b.deliveryStatus === 'APPROVED' || b.deliveryStatus === 'SHIPPED') statusClass = 'success-badge';
        else if (b.deliveryStatus === 'REJECTED') statusClass = 'danger';

        // Action Buttons based on status
        let actionButtons = '';
        if (b.deliveryStatus === 'PENDING') {
            actionButtons = `
                <button class="btn btn-primary btn-sm" style="display:inline-block; margin-right:4px;" onclick="updateBatchStatus(${b.id}, 'APPROVED')">
                    <i class="fa-solid fa-check"></i> Approve
                </button>
                <button class="btn btn-danger btn-sm" style="display:inline-block; margin-right:4px;" onclick="updateBatchStatus(${b.id}, 'REJECTED')">
                    <i class="fa-solid fa-xmark"></i> Reject
                </button>
            `;
        }
        
        // Manifest modifier is always open
        actionButtons += `
            <button class="btn btn-secondary btn-sm" style="display:inline-block; margin-right:4px;" onclick="openBatchItemsModal(${b.id}, '${b.cargoBatchId}', '${b.deliveryStatus}')">
                <i class="fa-solid fa-boxes-packing"></i> Manifest (${itemCount})
            </button>
        `;

        if (b.deliveryStatus !== 'PENDING' && b.deliveryStatus !== 'REJECTED') {
            const associatedInvoice = invoices.find(inv => inv.reliefBatch && inv.reliefBatch.id === b.id);
            if (associatedInvoice) {
                actionButtons += `
                    <button class="btn btn-sm" style="display:inline-block; margin-right:4px; background-color:var(--color-success); border-color:var(--color-success); color: white;" onclick="downloadInvoice(${associatedInvoice.id}, '${associatedInvoice.invoiceId}')">
                        <i class="fa-solid fa-file-arrow-down"></i> Invoice
                    </button>
                `;
            } else {
                actionButtons += `
                    <button class="btn btn-primary btn-sm" style="display:inline-block; margin-right:4px; background-color:var(--color-warning); border-color:var(--color-warning);" onclick="triggerInvoiceGeneration(${b.id}, '${b.cargoBatchId}')">
                        <i class="fa-solid fa-file-invoice-dollar"></i> Generate Invoice
                    </button>
                `;
            }
        }

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>${b.cargoBatchId}</strong></td>
            <td>${b.logisticsSourceNode}</td>
            <td><span class="badge ${statusClass}">${b.deliveryStatus}</span></td>
            <td>${itemCount} supply category logs</td>
            <td><strong>$${totalValuation.toFixed(2)}</strong></td>
            <td>${actionButtons}</td>
        `;
        body.appendChild(tr);
    });
}

// Render Financial Ledger & Donations
function renderFinance() {
    // Calculate totals for stats panel
    let totalAvailable = 0;
    donations.forEach(don => {
        totalAvailable += don.clearFundsAmount;
    });

    let totalSpent = 0;
    invoices.forEach(inv => {
        if (inv.approvalSignature === 'CLEARED') {
            totalSpent += inv.aggregatedOperationalCost;
        }
    });

    const totalReceived = totalAvailable + totalSpent;

    const totalDonationsElem = document.getElementById('fin-total-donations');
    const totalSpentElem = document.getElementById('fin-total-spent');
    const netBalanceElem = document.getElementById('fin-net-balance');

    if (totalDonationsElem) totalDonationsElem.innerText = '$' + totalReceived.toFixed(2);
    if (totalSpentElem) totalSpentElem.innerText = '$' + totalSpent.toFixed(2);
    if (netBalanceElem) netBalanceElem.innerText = '$' + totalAvailable.toFixed(2);

    // 1. Render Donations table
    const donBody = document.getElementById('donations-table-body');
    donBody.innerHTML = '';
    
    if (donations.length === 0) {
        donBody.innerHTML = '<tr><td colspan="4" class="loading-state">No donations registered yet.</td></tr>';
    } else {
        donations.forEach(d => {
            const donor = d.anonymousDonorFlag ? 'Anonymous' : 'Public Donor';
            const date = new Date(d.timestamp).toLocaleString();
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>${d.transactionalPaymentId}</strong></td>
                <td><strong style="color:var(--color-success)">$${d.clearFundsAmount.toFixed(2)}</strong></td>
                <td>${d.routingChannelApproach} (${donor})</td>
                <td><small>${date}</small></td>
            `;
            donBody.appendChild(tr);
        });
    }

    // 2. Render Invoices Table
    const invBody = document.getElementById('invoices-table-body');
    invBody.innerHTML = '';

    if (invoices.length === 0) {
        invBody.innerHTML = '<tr><td colspan="5" class="loading-state">No invoices logged.</td></tr>';
    } else {
        invoices.forEach(inv => {
            let statusClass = 'warning';
            let actionHtml = '';
            
            if (inv.approvalSignature === 'CLEARED') {
                statusClass = 'success-badge';
                actionHtml = `<span style="color:var(--color-success); font-size:0.75rem;"><i class="fa-solid fa-circle-check"></i> Settle Complete</span>`;
            } else {
                actionHtml = `
                    <button class="btn btn-primary btn-sm" onclick="openSettleModal(${inv.id}, '${inv.invoiceId}', ${inv.aggregatedOperationalCost})">
                        <i class="fa-solid fa-key"></i> Settle Handshake
                    </button>
                `;
            }

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>${inv.invoiceId}</strong></td>
                <td>${inv.associatedTargetZoneId}</td>
                <td><strong>$${inv.aggregatedOperationalCost.toFixed(2)}</strong></td>
                <td><span class="badge ${statusClass}">${inv.approvalSignature === 'CLEARED' ? 'ACCEPTED' : inv.approvalSignature}</span></td>
                <td>${actionHtml}</td>
            `;
            invBody.appendChild(tr);
        });
    }
}

// Calculate and update metrics
function updateMetrics() {
    const activeCrises = incidents.filter(i => i.status !== 'RESOLVED').length;
    document.getElementById('header-active-count').innerText = activeCrises;
    document.getElementById('incident-count-badge').innerText = activeCrises;
    document.getElementById('shelter-count-badge').innerText = shelters.length;

    let totalBeds = 0;
    let occupiedBeds = 0;
    shelters.forEach(s => {
        totalBeds += s.capacity;
        occupiedBeds += s.occupied;
    });

    const availableBeds = totalBeds - occupiedBeds;
    document.getElementById('header-shelter-beds').innerText = availableBeds + ' BEDS';
    document.getElementById('metric-evacuated').innerText = occupiedBeds;

    const occupancyRate = totalBeds > 0 ? ((occupiedBeds / totalBeds) * 100).toFixed(0) : 0;
    document.getElementById('metric-occupancy-rate').innerText = occupancyRate + '%';
}

// Map center focus helper
function focusMap(lat, lng) {
    map.setView([lat, lng], 14, { animate: true });
}

// Modal Toggle Helpers
function openReportModal(lat = null, lng = null) {
    const modal = document.getElementById('report-modal');
    modal.classList.add('active');
    const defaultLat = lat !== null ? lat : map.getCenter().lat;
    const defaultLng = lng !== null ? lng : map.getCenter().lng;
    document.getElementById('form-lat').value = defaultLat.toFixed(6);
    document.getElementById('form-lng').value = defaultLng.toFixed(6);
    setTempReportMarker(defaultLat, defaultLng);
}

function closeReportModal() {
    document.getElementById('report-modal').classList.remove('active');
    if (tempReportMarker) {
        map.removeLayer(tempReportMarker);
        tempReportMarker = null;
    }
}

function openVictimModal() {
    document.getElementById('victim-modal').classList.add('active');
    const select = document.getElementById('vic-room');
    select.innerHTML = '<option value="">-- Choose Vacant Room --</option>';
    
    let hasVacantRoom = false;
    shelters.forEach(s => {
        if (s.rooms && s.rooms.length > 0) {
            s.rooms.forEach(r => {
                const vacancy = r.currentBedVacancyCount;
                if (vacancy > 0) {
                    hasVacantRoom = true;
                    select.innerHTML += `<option value="${r.id}">${s.name} - Room ${r.roomNo} (${vacancy} beds free)</option>`;
                }
            });
        }
    });

    if (!hasVacantRoom) {
        select.innerHTML = '<option value="">NO VACANT ROOMS AVAILABLE IN ANY SHELTER</option>';
    }
}
function closeVictimModal() {
    document.getElementById('victim-modal').classList.remove('active');
    document.getElementById('victim-form').reset();
}

function openBatchModal() {
    document.getElementById('batch-modal').classList.add('active');
}
function closeBatchModal() {
    document.getElementById('batch-modal').classList.remove('active');
    document.getElementById('batch-form').reset();
}

function openDonationModal() {
    document.getElementById('donation-modal').classList.add('active');
}
function closeDonationModal() {
    document.getElementById('donation-modal').classList.remove('active');
    document.getElementById('donation-form').reset();
}

// Settle Invoice Modal Logic
function openSettleModal(invoiceId, code, cost) {
    document.getElementById('settle-invoice-modal').classList.add('active');
    document.getElementById('settle-invoice-id').value = invoiceId;
    document.getElementById('settle-invoice-code').innerText = code;
    document.getElementById('settle-invoice-cost').innerText = 'Operational Cost: $' + cost.toFixed(2);

    // Populate dropdown with all available donations
    const select = document.getElementById('settle-donation-select');
    select.innerHTML = '<option value="">-- Choose Payment --</option>';

    // Filter to donations that are reasonably large enough
    donations.forEach(don => {
        select.innerHTML += `<option value="${don.id}">${don.transactionalPaymentId} ($${don.clearFundsAmount.toFixed(2)}) via ${don.routingChannelApproach}</option>`;
    });
}
function closeSettleModal() {
    document.getElementById('settle-invoice-modal').classList.remove('active');
}

// Medical chart modal logic
async function openMedModal(victimId, name, needs) {
    document.getElementById('med-chart-modal').classList.add('active');
    document.getElementById('med-victim-id').value = victimId;
    document.getElementById('med-patient-name').innerText = name;
    document.getElementById('med-patient-needs').innerText = 'Special Needs: ' + (needs || 'None');
    
    // Fetch live record chart entries
    const logBox = document.getElementById('med-history-log');
    logBox.innerText = 'Loading medical chart...';

    try {
        const res = await fetch(`${VICTIMS_API}/${victimId}/medical-record`);
        if (!res.ok) throw new Error();
        const record = await res.json();
        
        document.getElementById('med-patient-name').innerHTML = `${name} <span style="font-size:0.8rem; color:var(--text-secondary);">(Blood Type: ${record.bloodType || 'Unknown'})</span>`;
        logBox.innerText = record.historyLog && record.historyLog.trim() !== '[]' ? record.historyLog : 'No chart clinical entries filed yet.';

    } catch (err) {
        logBox.innerText = 'Failed to load medical history file.';
    }
}
function closeMedModal() {
    document.getElementById('med-chart-modal').classList.remove('active');
    document.getElementById('med-entry-form').reset();
}

// Relief Batch Manifest modal
function openBatchItemsModal(batchId, code, status) {
    document.getElementById('batch-items-modal').classList.add('active');
    document.getElementById('manifest-batch-id').value = batchId;
    document.getElementById('manifest-batch-code').innerText = code;
    document.getElementById('manifest-batch-status').innerText = 'Status: ' + status;

    // Lock manifest addition form if the status is not PENDING
    const addForm = document.getElementById('manifest-add-form');
    if (addForm) {
        if (status !== 'PENDING') {
            addForm.style.display = 'none';
        } else {
            addForm.style.display = 'block';
        }
    }

    // Load items in popup table
    loadManifestTable(batchId);
}

function loadManifestTable(batchId) {
    const batch = batches.find(b => b.id === batchId);
    const body = document.getElementById('manifest-items-body');
    body.innerHTML = '';

    if (!batch || !batch.items || batch.items.length === 0) {
        body.innerHTML = '<tr><td colspan="5" class="loading-state">Manifest empty. No items bundled.</td></tr>';
        return;
    }

    const isEditable = (batch.deliveryStatus === 'PENDING');

    batch.items.forEach(item => {
        const tr = document.createElement('tr');
        
        let actionColumn = '';
        if (isEditable) {
            actionColumn = `
                <button class="btn btn-danger btn-sm" style="padding:2px 6px; font-size:0.7rem;" onclick="deleteManifestItem(${batchId}, ${item.id})">
                    <i class="fa-solid fa-trash"></i>
                </button>
            `;
        } else {
            actionColumn = `<span style="font-size:0.8rem; color:var(--text-muted); font-style:italic;">Locked</span>`;
        }

        tr.innerHTML = `
            <td><strong>${item.stockKeepingUnitSKU}</strong></td>
            <td>${item.itemCategoryLabel}</td>
            <td>$${item.unitCostValuation.toFixed(2)}</td>
            <td>${item.currentStockLevel}</td>
            <td>${actionColumn}</td>
        `;
        body.appendChild(tr);
    });
}
function closeBatchItemsModal() {
    document.getElementById('batch-items-modal').classList.remove('remove');
    document.getElementById('batch-items-modal').classList.remove('active');
    document.getElementById('manifest-add-form').reset();
}

// ----- FORM SUBMISSIONS -----

// Add new Incident
async function submitIncidentForm(e) {
    e.preventDefault();
    const title = document.getElementById('form-title').value;
    const type = document.getElementById('form-type').value;
    const severity = document.getElementById('form-severity').value;
    const latitude = parseFloat(document.getElementById('form-lat').value);
    const longitude = parseFloat(document.getElementById('form-lng').value);
    const description = document.getElementById('form-desc').value;

    try {
        const res = await fetch(INCIDENTS_API, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ title, hazardType: type, severityScale: parseInt(severity), latitude, longitude, description })
        });
        if (!res.ok) throw new Error();
        closeReportModal();
        await fetchData();
    } catch (err) {
        alert('Error filing incident report.');
    }
}

// Add new Evacuee (Victim) with manual shelter-room allocation
async function submitVictimForm(e) {
    e.preventDefault();
    const name = document.getElementById('vic-name').value;
    const age = parseInt(document.getElementById('vic-age').value);
    const gender = document.getElementById('vic-gender').value;
    const phoneNumber = document.getElementById('vic-phone').value;
    const triageStatus = document.getElementById('vic-triage').value;
    const specialNeeds = document.getElementById('vic-needs').value;
    const medicalAlertFlag = document.getElementById('vic-alert').checked;
    const roomId = document.getElementById('vic-room').value;

    if (!roomId) {
        alert('Please select an available shelter facility room.');
        return;
    }

    try {
        const res = await fetch(VICTIMS_API, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                name,
                age,
                gender,
                phoneNumber,
                triageStatus,
                specializedNeedsLog: specialNeeds,
                medicalAlertFlag,
                facilityRoom: { id: parseInt(roomId) }
            })
        });

        if (!res.ok) {
            const errText = await res.text();
            throw new Error(errText);
        }

        const allocatedVictim = await res.json();
        closeVictimModal();
        await fetchData();

        alert(`Intake Successful!\n${name} has been allocated to ${allocatedVictim.shelter.name} (Room ${allocatedVictim.facilityRoom.roomNo}).`);

    } catch (err) {
        alert('Intake Failed: ' + err.message);
    }
}

// Log new Medical record entry
async function submitMedEntry(e) {
    e.preventDefault();
    const id = document.getElementById('med-victim-id').value;
    const type = document.getElementById('med-type').value;
    const details = document.getElementById('med-details').value;

    try {
        const res = await fetch(`${VICTIMS_API}/${id}/medical-record/entry?type=${type}&details=${details}`, {
            method: 'POST'
        });
        if (!res.ok) throw new Error();
        
        // Reset entry form and refresh medical charts
        document.getElementById('med-details').value = '';
        const updatedRecord = await res.json();
        document.getElementById('med-history-log').innerText = updatedRecord.historyLog;
        await fetchData();
    } catch (err) {
        alert('Failed to append medical entry.');
    }
}

// Request new Batch
async function submitBatchForm(e) {
    e.preventDefault();
    const batchCode = document.getElementById('bat-code').value;
    const sourceNode = document.getElementById('bat-node').value;

    try {
        const res = await fetch(BATCHES_API, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ cargoBatchId: batchCode, logisticsSourceNode: sourceNode })
        });
        if (!res.ok) throw new Error();
        closeBatchModal();
        await fetchData();
    } catch (err) {
        alert('Failed to request cargo batch.');
    }
}

// Add item to batch manifest
async function submitManifestItem(e) {
    e.preventDefault();
    const id = document.getElementById('manifest-batch-id').value;
    const sku = document.getElementById('item-sku').value;
    const category = document.getElementById('item-cat').value;
    const unitCost = parseFloat(document.getElementById('item-cost').value);
    const stockLevel = parseInt(document.getElementById('item-qty').value);

    try {
        const res = await fetch(`${BATCHES_API}/${id}/items`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ stockKeepingUnitSKU: sku, itemCategoryLabel: category, unitCostValuation: unitCost, currentStockLevel: stockLevel })
        });
        if (!res.ok) throw new Error();
        
        document.getElementById('manifest-add-form').reset();
        await fetchData();
        loadManifestTable(parseInt(id));
    } catch (err) {
        alert('Failed to bundle supply item.');
    }
}

// Delete item from batch manifest
async function deleteManifestItem(batchId, itemId) {
    if (!confirm('Are you sure you want to remove this supply item from the manifest?')) return;
    try {
        const res = await fetch(`${BATCHES_API}/${batchId}/items/${itemId}`, {
            method: 'DELETE'
        });
        if (!res.ok) throw new Error();
        await fetchData();
        loadManifestTable(batchId);
    } catch (err) {
        alert('Failed to delete item.');
    }
}

// Settle Invoice with Donation Handshake
async function submitSettleForm(e) {
    e.preventDefault();
    const id = document.getElementById('settle-invoice-id').value;
    const donId = document.getElementById('settle-donation-select').value;

    if (!donId) {
        alert('Please select a valid funding transaction.');
        return;
    }

    try {
        const res = await fetch(`${FINANCE_API}/invoices/${id}/settle?donationId=${donId}`, {
            method: 'POST'
        });

        if (!res.ok) {
            const errText = await res.text();
            throw new Error(errText);
        }

        closeSettleModal();
        await fetchData();
        alert('Invoice successfully cleared and settled via payment handshake!');
    } catch (err) {
        alert('Settlement failed: ' + err.message);
    }
}

// Log Donation
async function submitDonationForm(e) {
    e.preventDefault();
    const amount = parseFloat(document.getElementById('don-amount').value);
    const channel = document.getElementById('don-channel').value;
    const anonymous = document.getElementById('don-anonymous').checked;

    try {
        const res = await fetch(FINANCE_API + '/donations', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ clearFundsAmount: amount, routingChannelApproach: channel, anonymousDonorFlag: anonymous })
        });
        if (!res.ok) throw new Error();
        closeDonationModal();
        await fetchData();
    } catch (err) {
        alert('Failed to record donation payment.');
    }
}

// Change Batch Status (Approve/Reject)
async function updateBatchStatus(id, status) {
    try {
        const res = await fetch(`${BATCHES_API}/${id}/status?status=${status}`, {
            method: 'PUT'
        });
        if (!res.ok) throw new Error();
        await fetchData();
    } catch (err) {
        alert('Failed to update cargo status.');
    }
}

// Generate Invoice for Relief Cargo
async function triggerInvoiceGeneration(batchId, code) {
    const zone = prompt(`Generate Allocation Invoice for Batch ${code}\nPlease enter Affected Target Zone geocode (e.g. Z-NORTH):`, "Z-NORTH");
    if (!zone) return;

    try {
        const res = await fetch(`${BATCHES_API}/${batchId}/invoice?zoneId=${zone}`, {
            method: 'POST'
        });
        if (!res.ok) {
            const errText = await res.text();
            throw new Error(errText || 'Failed to generate allocation invoice.');
        }
        await fetchData();
        alert('Allocation invoice generated and logged in Financial tab!');
    } catch (err) {
        alert(err.message || 'Failed to generate allocation invoice.');
    }
}

// Download Invoice formatted text helper
function downloadInvoice(invoiceId, invoiceCode) {
    const inv = invoices.find(i => i.id == invoiceId);
    if (!inv) {
        alert('Invoice details not found.');
        return;
    }

    const batchCode = inv.reliefBatch ? inv.reliefBatch.cargoBatchId : 'N/A';
    const sourceNode = inv.reliefBatch ? inv.reliefBatch.logisticsSourceNode : 'N/A';
    
    let itemsText = '';
    if (inv.reliefBatch && inv.reliefBatch.items) {
        inv.reliefBatch.items.forEach(item => {
            itemsText += `- SKU: ${item.stockKeepingUnitSKU} | Category: ${item.itemCategoryLabel} | Qty: ${item.currentStockLevel} | Cost: $${item.unitCostValuation.toFixed(2)}\n`;
        });
    } else {
        itemsText = 'No items logged.\n';
    }

    const invoiceContent = `=====================================================
DISASTER RELIEF MANAGEMENT SYSTEM - INVOICE
=====================================================
Invoice Reference : ${inv.invoiceId}
Cargo Batch Code  : ${batchCode}
Source Node       : ${sourceNode}
Target Zone       : ${inv.associatedTargetZoneId}
Approval State    : ${inv.approvalSignature === 'CLEARED' ? 'ACCEPTED' : inv.approvalSignature}
Settled Payment   : ${inv.donationPayment ? 'PAID / SETTLED via Payment ID: ' + inv.donationPayment.transactionalPaymentId + ' (' + inv.donationPayment.routingChannelApproach + ')' : 'UNPAID / PENDING HANDSHAKE'}
-----------------------------------------------------
ITEMS BUNDLED IN CARGO MANIFEST:
${itemsText}
-----------------------------------------------------
AGGREGATED OPERATIONAL COST: $${inv.aggregatedOperationalCost.toFixed(2)}
=====================================================
Generated on local system: ${new Date().toLocaleString()}
`;

    const blob = new Blob([invoiceContent], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `Invoice-${invoiceCode}.txt`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}

// Resolve an incident
async function resolveIncident(id) {
    try {
        const res = await fetch(`${INCIDENTS_API}/${id}/status?status=RESOLVED`, {
            method: 'PUT'
        });
        if (!res.ok) throw new Error();
        map.closePopup();
        await fetchData();
    } catch (err) {
        alert('Failed to update incident: ' + err.message);
    }
}

// SOS Alert Trigger
async function triggerSOS() {
    const center = map.getCenter();
    const lat = center.lat + (Math.random() - 0.5) * 0.05;
    const lng = center.lng + (Math.random() - 0.5) * 0.05;

    const promptDesc = prompt("Please enter description for SOS emergency request:", "CRITICAL SOS ALERT: Emergency rescue assistance required immediately.");
    if (promptDesc === null) return;

    try {
        const res = await fetch(INCIDENTS_API, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                title: "SOS EMERGENCY REQUEST",
                hazardType: "OTHER",
                severityScale: 10,
                latitude: lat,
                longitude: lng,
                description: promptDesc
            })
        });
        if (!res.ok) throw new Error();
        await fetchData();
        map.setView([lat, lng], 13);
        alert("SOS Alert Broadcasted.");
    } catch (err) {
        alert("Failed to submit SOS alert.");
    }
}

// Filter feed based on search input
function filterIncidents() {
    const searchVal = document.getElementById('incident-search').value.toLowerCase();
    const cards = document.querySelectorAll('#incidents-feed .item-card');
    cards.forEach(card => {
        const text = card.textContent.toLowerCase();
        if (text.includes(searchVal)) {
            card.style.display = 'block';
        } else {
            card.style.display = 'none';
        }
    });
}

// ----- NEW TAB RENDERERS & MODALS -----

// Render Responder Assignments (Jobs)
function renderAssignments() {
    const body = document.getElementById('deployments-table-body');
    body.innerHTML = '';

    if (assignments.length === 0) {
        body.innerHTML = '<tr><td colspan="7" class="loading-state">No active responder deployments logged.</td></tr>';
        return;
    }

    assignments.forEach(asg => {
        const isComp = asg.completed;
        const statusClass = isComp ? 'success-badge' : 'warning';
        const statusText = isComp ? 'Standing Down (Completed)' : 'ACTIVE DEPLOYMENT';
        
        let actionButtons = '';
        if (!isComp) {
            actionButtons = `
                <button class="btn btn-primary btn-sm" style="display:inline-block; margin-right:4px;" onclick="toggleAssignmentStatus(${asg.id})">
                    <i class="fa-solid fa-circle-check"></i> Complete
                </button>
            `;
        }
        
        const notesLog = asg.fieldIncidentNotes || '[]';
        
        actionButtons += `
            <button class="btn btn-secondary btn-sm" style="display:inline-block; margin-right:4px;" onclick="openNotesModal(${asg.id}, '${asg.assignmentId}', '${notesLog.replace(/'/g, "\\'").replace(/\n/g, "\\n")}')">
                <i class="fa-solid fa-clipboard-list"></i> Shift Notes
            </button>
        `;

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>${asg.assignmentId}</strong></td>
            <td><strong>${asg.fieldResponder.name}</strong> (${asg.fieldResponder.empId})</td>
            <td><span class="badge success-badge">${asg.fieldResponder.tacticalSpecialty}</span></td>
            <td>${asg.disasterIncident.title} (Scale ${asg.disasterIncident.severityScale})</td>
            <td>${asg.operationalShift}</td>
            <td><span class="badge ${statusClass}">${statusText}</span></td>
            <td>${actionButtons}</td>
        `;
        body.appendChild(tr);
    });
}

// Render Affected Zones Hub
function renderZones() {
    // 1. Render Glow Metrics Grid (aggregates statistics)
    const glowContainer = document.getElementById('zones-glow-container');
    glowContainer.innerHTML = '';

    let totalCasualties = 0;
    let totalDamage = 0;
    let totalLives = 0;
    let totalEvacuations = 0;
    let maxRisk = 0;

    zones.forEach(z => {
        totalCasualties += z.casualtyCount;
        totalDamage += z.housesDamaged;
        totalLives += z.livesLost;
        totalEvacuations += z.evacuationRequestCount;
        if (z.infrastructureRiskScale > maxRisk) maxRisk = z.infrastructureRiskScale;
    });

    glowContainer.innerHTML = `
        <div class="glow-card casualties">
            <div class="val" id="glow-casualties-count">${totalCasualties}</div>
            <div class="lbl">Total Casualties</div>
        </div>
        <div class="glow-card damage">
            <div class="val" id="glow-damage-count">${totalDamage}</div>
            <div class="lbl">Houses Damaged</div>
        </div>
        <div class="glow-card lives">
            <div class="val" id="glow-lives-count">${totalLives}</div>
            <div class="lbl">Lives Lost</div>
        </div>
        <div class="glow-card evacuations">
            <div class="val" id="glow-evac-count">${totalEvacuations}</div>
            <div class="lbl">Evac Requests</div>
        </div>
        <div class="glow-card risk">
            <div class="val" id="glow-risk-scale">${maxRisk}/10</div>
            <div class="lbl">Max Risk Scale</div>
        </div>
    `;

    // 2. Render Zones Table
    const body = document.getElementById('zones-table-body');
    body.innerHTML = '';

    if (zones.length === 0) {
        body.innerHTML = '<tr><td colspan="10" class="loading-state">No affected zones logged in system.</td></tr>';
        return;
    }

    zones.forEach(z => {
        const incidentsCount = z.registeredIncidents ? z.registeredIncidents.length : 0;
        const evacTag = z.evacuationRequired 
            ? '<span class="badge danger"><i class="fa-solid fa-circle-exclamation"></i> EVACUATE</span>' 
            : '<span class="badge success-badge"><i class="fa-solid fa-circle-check"></i> Monitor</span>';

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>${z.zoneId}</strong></td>
            <td>${z.quadrantGeocode}</td>
            <td>${evacTag}</td>
            <td><strong style="color:var(--color-danger)">${z.casualtyCount}</strong></td>
            <td><strong style="color:var(--color-warning)">${z.housesDamaged}</strong></td>
            <td><strong style="color:#fff">${z.livesLost}</strong></td>
            <td><strong style="color:var(--color-info)">${z.evacuationRequestCount}</strong></td>
            <td><span class="badge success-badge">${z.infrastructureRiskScale}/10</span></td>
            <td>${incidentsCount} tracking</td>
            <td>
                <button class="btn btn-primary btn-sm" onclick="openZoneMetricsModal(${z.id}, '${z.zoneId}', '${z.quadrantGeocode}', ${z.casualtyCount}, ${z.housesDamaged}, ${z.livesLost}, ${z.evacuationRequestCount}, ${z.infrastructureRiskScale})">
                    <i class="fa-solid fa-file-pen"></i> Update Metrics
                </button>
            </td>
        `;
        body.appendChild(tr);
    });
}

// ----- DEPLOY RESPONDER MODAL LOGIC -----

function openDeployModal() {
    document.getElementById('deploy-modal').classList.add('active');
    
    // Populate dropdown with available standby responders
    const respSelect = document.getElementById('dep-responder');
    respSelect.innerHTML = '<option value="">-- Choose Responder --</option>';
    responders.forEach(r => {
        if (r.operationalStatus === 'STANDBY') {
            respSelect.innerHTML += `<option value="${r.id}">${r.name} (${r.tacticalSpecialty} - L${r.certificationLevel})</option>`;
        }
    });

    // Populate dropdown with active incidents
    const incSelect = document.getElementById('dep-incident');
    incSelect.innerHTML = '<option value="">-- Choose Incident --</option>';
    incidents.forEach(inc => {
        const isIncidentActive = (inc.isActive !== undefined ? inc.isActive : inc.active);
        if (isIncidentActive) {
            incSelect.innerHTML += `<option value="${inc.id}">${inc.title} (${inc.hazardType} - Scale ${inc.severityScale})</option>`;
        }
    });
}

function closeDeployModal() {
    document.getElementById('deploy-modal').classList.remove('active');
    document.getElementById('deploy-form').reset();
}

async function submitDeploymentForm(e) {
    e.preventDefault();
    const responderId = parseInt(document.getElementById('dep-responder').value);
    const incidentId = parseInt(document.getElementById('dep-incident').value);
    const shift = document.getElementById('dep-shift').value;

    if (!responderId || !incidentId) {
        alert('Please select a responder and an active incident.');
        return;
    }

    try {
        const res = await fetch(PERSONNEL_API + '/assignments', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                operationalShift: shift,
                fieldResponder: { id: responderId },
                disasterIncident: { id: incidentId }
            })
        });

        if (!res.ok) {
            const errText = await res.text();
            throw new Error(errText);
        }

        closeDeployModal();
        await fetchData();
        alert('Field responder deployed successfully!');
    } catch (err) {
        alert('Deployment failed: ' + err.message);
    }
}

// ----- SHIFT NOTES MODAL LOGIC -----

function openNotesModal(assignmentId, jobCode, notes) {
    document.getElementById('notes-modal').classList.add('active');
    document.getElementById('notes-assignment-id').value = assignmentId;
    document.getElementById('notes-job-code').innerText = jobCode;

    const assignment = assignments.find(a => a.id === assignmentId);
    if (assignment && assignment.fieldResponder) {
        document.getElementById('notes-responder-name').innerText = `Responder: ${assignment.fieldResponder.name} (${assignment.fieldResponder.tacticalSpecialty})`;
    }

    const logBox = document.getElementById('notes-log-box');
    logBox.innerText = notes && notes.trim() !== '[]' ? notes : 'No shift telemetry entries logged yet.';
}

function closeNotesModal() {
    document.getElementById('notes-modal').classList.remove('active');
    document.getElementById('notes-entry-form').reset();
}

async function submitNotesEntry(e) {
    e.preventDefault();
    const assignmentId = document.getElementById('notes-assignment-id').value;
    const noteText = document.getElementById('notes-details').value;

    try {
        const res = await fetch(`${PERSONNEL_API}/assignments/${assignmentId}/notes?notes=${encodeURIComponent(noteText)}`, {
            method: 'POST'
        });
        if (!res.ok) throw new Error();

        const updated = await res.json();
        
        // Refresh notes box with updated logs
        document.getElementById('notes-log-box').innerText = updated.fieldIncidentNotes;
        document.getElementById('notes-details').value = '';
        
        await fetchData();
    } catch (err) {
        alert('Failed to append shift note telemetry.');
    }
}

async function toggleAssignmentStatus(id) {
    if (!confirm('Are you sure this responder deployment is completed? Operational status will return to STANDBY.')) return;
    try {
        const res = await fetch(`${PERSONNEL_API}/assignments/${id}/complete`, {
            method: 'PUT'
        });
        if (!res.ok) throw new Error();
        await fetchData();
        alert('Assignment completed. Responder returned to standby.');
    } catch (err) {
        alert('Failed to update assignment status.');
    }
}

// ----- ZONE METRICS MODAL LOGIC -----

function openZoneMetricsModal(id, zoneId, geocode, casualties, damage, lives, evacuations, risk) {
    document.getElementById('zone-metrics-modal').classList.add('active');
    document.getElementById('metric-zone-id').value = id;
    document.getElementById('metric-zone-name').innerText = zoneId;
    document.getElementById('metric-zone-coords').innerText = `Quadrant Geocode: ${geocode}`;

    document.getElementById('met-casualties').value = casualties;
    document.getElementById('met-damage').value = damage;
    document.getElementById('met-lives').value = lives;
    document.getElementById('met-evacuations').value = evacuations;
    document.getElementById('met-risk').value = risk;
}

function closeZoneMetricsModal() {
    document.getElementById('zone-metrics-modal').classList.remove('active');
    document.getElementById('zone-metrics-form').reset();
}

async function submitZoneMetricsForm(e) {
    e.preventDefault();
    const id = document.getElementById('metric-zone-id').value;
    const casualties = parseInt(document.getElementById('met-casualties').value);
    const damage = parseInt(document.getElementById('met-damage').value);
    const lives = parseInt(document.getElementById('met-lives').value);
    const evacuations = parseInt(document.getElementById('met-evacuations').value);
    const risk = parseInt(document.getElementById('met-risk').value);

    try {
        const res = await fetch(`${ZONES_API}/${id}/metrics?casualties=${casualties}&damage=${damage}&lives=${lives}&evacuations=${evacuations}&risk=${risk}`, {
            method: 'PUT'
        });
        if (!res.ok) throw new Error();

        closeZoneMetricsModal();
        await fetchData();
        alert('Affected zone metrics updated successfully!');
    } catch (err) {
        alert('Failed to update affected zone metrics.');
    }
}
