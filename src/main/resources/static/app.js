const placements = ["slot-home", "slot-sidebar"];
const segments = [["saas", "ecommerce"], ["finance"], ["developer", "cloud"], ["founder"]];
const events = document.querySelector("#events");
let lastWinningBid = null;

async function fetchJson(url, options) {
  const response = await fetch(url, options);
  if (!response.ok) throw new Error(`HTTP ${response.status}`);
  return response.json();
}

async function loadCampaigns() {
  const campaigns = await fetchJson("/api/campaigns");
  document.querySelector("#campaigns").innerHTML = campaigns.map(campaign => `
    <div class="campaign">
      <div>
        <div class="campaign-name">${campaign.name}</div>
        <div class="campaign-meta">${campaign.country} / ${campaign.placementId} / ${campaign.targetSegments.join(", ")}</div>
      </div>
      <div class="bid">$${Number(campaign.bidPrice).toFixed(2)}</div>
    </div>
  `).join("");
}

async function loadMetrics() {
  const metrics = await fetchJson("/api/metrics");
  const matchRate = metrics.totalBidRequests === 0 ? 0 : metrics.matchedBidRequests / metrics.totalBidRequests;
  document.querySelector("#totalBids").textContent = metrics.totalBidRequests;
  document.querySelector("#matchRate").textContent = `${(matchRate * 100).toFixed(1)}%`;
  document.querySelector("#matchRateBar").style.width = `${Math.min(100, matchRate * 100)}%`;
  document.querySelector("#avgLatency").textContent = `${metrics.averageLatencyMs.toFixed(1)} ms`;
  document.querySelector("#p95Latency").textContent = `${metrics.p95LatencyMs} ms`;
  document.querySelector("#p99Latency").textContent = `${metrics.p99LatencyMs} ms`;
  document.querySelector("#currentQps").textContent = Number(metrics.currentQps).toFixed(2);
  document.querySelector("#recentBids").textContent = `${metrics.recentBidRequests} bids / 60s`;
  document.querySelector("#ctr").textContent = `${(metrics.ctr * 100).toFixed(1)}%`;
  document.querySelector("#clickCount").textContent = `${metrics.clicks} clicks`;
  document.querySelector("#previewLatency").textContent = `p95 ${metrics.p95LatencyMs}ms`;
  renderTopCampaigns(metrics.topCampaigns);
}

async function loadHealth() {
  const health = document.querySelector("#healthStatus");
  try {
    const response = await fetchJson("/actuator/health");
    health.className = `health-pill ${response.status === "UP" ? "up" : "down"}`;
    health.innerHTML = `<span class="status-dot"></span>Health ${response.status}`;
  } catch (error) {
    health.className = "health-pill down";
    health.innerHTML = `<span class="status-dot"></span>Health DOWN`;
  }
}

async function loadDemoConfig() {
  const resetButton = document.querySelector("#resetDemo");
  try {
    const config = await fetchJson("/api/demo/config");
    resetButton.hidden = !config.resetEnabled;
  } catch (error) {
    resetButton.hidden = true;
  }
}

async function runBidBurst() {
  const button = document.querySelector("#simulate");
  button.disabled = true;
  button.textContent = "Running burst...";
  try {
    const burstId = Date.now();
    const requests = Array.from({ length: 20 }, (_, index) => ({
      requestId: `demo-${burstId}-${index}`,
      userId: `demo-user-${burstId}-${index % 6}`,
      placementId: placements[index % placements.length],
      device: index % 2 === 0 ? "mobile" : "desktop",
      country: index % 3 === 0 ? "SG" : "HK",
      userSegments: segments[index % segments.length]
    }));
    const responses = await Promise.all(requests.map(request =>
      fetchJson("/api/bid", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(request)
      }).then(response => ({ request, response }))
    ));
    responses.forEach(({ request, response }) => {
      prependEvent(response, request);
      if (response.matched) {
        lastWinningBid = { request, response };
        renderWinningCreative(response);
      }
    });
    await loadMetrics();
  } finally {
    button.disabled = false;
    button.textContent = "Run bid burst";
  }
}

function prependEvent(response, request, eventType = null) {
  const type = eventType || (response.matched ? "WIN" : "NO BID");
  const node = document.createElement("div");
  node.className = `event ${type === "WIN" ? "win" : type === "CLICK" ? "click" : "no-bid"}`;
  node.innerHTML = `
    <span><b>${type}</b> ${request.country} / ${request.placementId}</span>
    <strong>${response.matched ? `Campaign ${response.campaignId} at $${Number(response.bidPrice).toFixed(2)}` : response.reason}</strong>
  `;
  events.prepend(node);
  while (events.children.length > 12) events.removeChild(events.lastChild);
}

function renderWinningCreative(response) {
  const preview = document.querySelector("#creativePreview");
  const empty = document.querySelector("#creativeEmpty");
  preview.hidden = false;
  empty.hidden = true;
  document.querySelector("#winnerStatus").textContent = `campaign ${response.campaignId} won`;
  document.querySelector("#winnerCampaign").textContent = `Campaign ${response.campaignId}`;
  document.querySelector("#winnerCreative").textContent = `Creative ${response.creativeId}`;
  document.querySelector("#winnerPrice").textContent = `$${Number(response.bidPrice).toFixed(2)}`;
  document.querySelector("#previewCampaign").textContent = `Campaign ${response.campaignId}`;
  document.querySelector("#previewBidPrice").textContent = `$${Number(response.bidPrice).toFixed(2)}`;
  document.querySelector("#previewPlacement").textContent = lastWinningBid?.request.placementId || "--";
  document.querySelector("#previewCountry").textContent = lastWinningBid?.request.country || "--";
  document.querySelector("#previewSegment").textContent = (lastWinningBid?.request.userSegments || []).join(", ");
  const landing = document.querySelector("#winnerLanding");
  landing.href = response.landingUrl;
  landing.textContent = new URL(response.landingUrl).hostname;
  document.querySelector("#clickWinner").disabled = false;
}

function renderTopCampaigns(campaigns = []) {
  const list = document.querySelector("#topCampaigns");
  if (!campaigns.length) {
    list.innerHTML = `<div class="empty-row">No impressions yet</div>`;
    return;
  }
  list.innerHTML = campaigns.map((campaign, index) => `
    <div class="leader-row">
      <span class="rank">${index + 1}</span>
      <div>
        <strong>Campaign ${campaign.campaignId}</strong>
        <span>${campaign.impressions} impressions / ${campaign.clicks} clicks</span>
      </div>
      <em>${(campaign.ctr * 100).toFixed(1)}%</em>
    </div>
  `).join("");
}

function clearDemoView() {
  lastWinningBid = null;
  events.innerHTML = "";
  document.querySelector("#totalBids").textContent = "0";
  document.querySelector("#matchRate").textContent = "0.0%";
  document.querySelector("#matchRateBar").style.width = "0%";
  document.querySelector("#avgLatency").textContent = "0.0 ms";
  document.querySelector("#p95Latency").textContent = "0 ms";
  document.querySelector("#p99Latency").textContent = "0 ms";
  document.querySelector("#currentQps").textContent = "0.00";
  document.querySelector("#recentBids").textContent = "0 bids / 60s";
  document.querySelector("#ctr").textContent = "0.0%";
  document.querySelector("#clickCount").textContent = "0 clicks";
  document.querySelector("#previewLatency").textContent = "p95 0ms";
  renderTopCampaigns([]);

  document.querySelector("#creativePreview").hidden = true;
  document.querySelector("#creativeEmpty").hidden = false;
  document.querySelector("#winnerStatus").textContent = "waiting for bid";
  document.querySelector("#winnerCampaign").textContent = "No campaign selected";
  document.querySelector("#winnerCreative").textContent = "--";
  document.querySelector("#winnerPrice").textContent = "$0.00";
  const landing = document.querySelector("#winnerLanding");
  landing.href = "#";
  landing.textContent = "--";
  document.querySelector("#clickWinner").disabled = true;
}

async function simulateClick() {
  if (!lastWinningBid) return;
  document.querySelector("#clickWinner").disabled = true;
  try {
    const { request, response } = lastWinningBid;
    await fetchJson("/api/events/click", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        requestId: `${request.requestId}-click`,
        userId: request.userId,
        campaignId: response.campaignId
      })
    });
    prependEvent({ matched: true, campaignId: response.campaignId, bidPrice: response.bidPrice }, {
      ...request,
      country: "CLICK"
    }, "CLICK");
    await loadMetrics();
  } finally {
    document.querySelector("#clickWinner").disabled = false;
  }
}

async function resetDemo() {
  const button = document.querySelector("#resetDemo");
  button.disabled = true;
  button.textContent = "Resetting...";
  clearDemoView();
  try {
    await fetchJson("/api/demo/reset", { method: "POST" });
  } catch (error) {
    await loadMetrics();
    await loadHealth();
    throw error;
  } finally {
    button.disabled = false;
    button.textContent = "Reset demo";
  }
}

document.querySelector("#simulate").addEventListener("click", runBidBurst);
document.querySelector("#resetDemo").addEventListener("click", resetDemo);
document.querySelector("#clickWinner").addEventListener("click", simulateClick);
loadCampaigns();
loadMetrics();
loadHealth();
loadDemoConfig();
setInterval(loadMetrics, 3000);
setInterval(loadHealth, 10000);
