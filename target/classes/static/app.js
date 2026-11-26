const API = "";
const state = { page: "dashboard", companies: [], runs: [], jobs: [], summary: {} };
const $ = (s) => document.querySelector(s);
const esc = (v) => String(v ?? "").replace(/[&<>"']/g, c => ({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[c]));
async function request(url, options={}) {
  const r = await fetch(API + url, {headers: {"Content-Type":"application/json", ...(options.headers||{})}, ...options});
  const text = await r.text();
  let data = {}; try { data = text ? JSON.parse(text) : {}; } catch { data = {message:text}; }
  if (!r.ok) throw new Error(data.message || data.error || `Request failed (${r.status})`);
  return data;
}
function toast(message, error=false) {
  const el=$("#toast"); el.textContent=message; el.className="toast"+(error?" error":"");
  setTimeout(()=>el.classList.add("hidden"),4200);
}
function setPage(page) {
  state.page=page;
  document.querySelectorAll(".nav-item").forEach(b=>b.classList.toggle("active",b.dataset.page===page));
  const labels={"dashboard":"Dashboard","companies":"Companies","run-center":"Run Center","jobs":"Job Results","history":"Run History","configuration":"Configuration","settings":"Settings"};
  $("#page-title").textContent=labels[page]; $("#crumb").textContent=labels[page];
  render();
}
document.querySelectorAll(".nav-item").forEach(b=>b.addEventListener("click",()=>setPage(b.dataset.page)));
$("#top-run").addEventListener("click",()=>setPage("run-center"));

async function loadAll() {
  try {
    const [summary,companies,runs,jobs]=await Promise.all([
      request("/api/dashboard/summary"),request("/api/companies"),request("/api/runs"),request("/api/jobs?size=100")
    ]);
    state.summary=summary; state.companies=companies; state.runs=runs; state.jobs=jobs.content||[];
    render();
  } catch(e) { toast("Backend connection failed: "+e.message,true); }
}
function stat(label,value,foot,icon) {
  return `<div class="card"><div class="stat-top"><span>${label}</span><span class="stat-icon">${icon}</span></div><div class="stat-value">${value??0}</div><div class="stat-foot">${foot}</div></div>`;
}
function companyTable(items) {
  if(!items.length) return `<div class="empty"><b>No companies configured yet</b>Add a company or import your Excel configuration to get started.</div>`;
  return `<table><thead><tr><th>Company</th><th>Career website</th><th>Type</th><th>Status</th><th>Added</th></tr></thead><tbody>${items.map(c=>`<tr>
    <td><div class="company-cell"><span class="company-logo">${esc((c.name||"?").slice(0,2).toUpperCase())}</span>${esc(c.name)}</div><div class="small muted" style="margin:4px 0 0 36px">${esc(c.companyKey)}</div></td>
    <td><a href="${esc(c.careerUrl)}" target="_blank" rel="noreferrer">${esc(c.careerUrl)}</a></td>
    <td>${esc(c.interactionType)}</td><td><span class="pill ${c.enabled?"green":"amber"}">${c.enabled?"Enabled":"Disabled"}</span></td>
    <td>${c.createdAt?new Date(c.createdAt).toLocaleDateString():"—"}</td></tr>`).join("")}</tbody></table>`;
}
function runTable(items) {
  if(!items.length) return `<div class="empty"><b>No search runs yet</b>Launch a search from Run Center. This starter records requests; the crawler worker is not wired yet.</div>`;
  return `<table><thead><tr><th>Run ID</th><th>Company</th><th>Keyword</th><th>Location</th><th>Posted days</th><th>Status</th><th>Created</th></tr></thead><tbody>${items.map(r=>`<tr><td>#${r.id}</td><td>${esc(r.companyKey)}</td><td>${esc(r.keyword||"—")}</td><td>${esc(r.location||"—")}</td><td>${r.postedWithinDays??"—"}</td><td><span class="pill amber">${esc(r.status)}</span></td><td>${r.createdAt?new Date(r.createdAt).toLocaleString():"—"}</td></tr>`).join("")}</tbody></table>`;
}
function jobTable(items) {
  if(!items.length) return `<div class="empty"><b>No jobs stored yet</b>Job results will appear here after a crawler worker has been implemented and run.</div>`;
  return `<table><thead><tr><th>Job title</th><th>External ID</th><th>Location</th><th>Posted date</th><th>First seen</th><th>Source</th></tr></thead><tbody>${items.map(j=>`<tr><td><b>${esc(j.title)}</b></td><td>${esc(j.externalJobId||"—")}</td><td>${esc(j.location||"—")}</td><td>${esc(j.postedDate||"—")}</td><td>${j.firstSeenAt?new Date(j.firstSeenAt).toLocaleDateString():"—"}</td><td>${j.jobUrl?`<a href="${esc(j.jobUrl)}" target="_blank" rel="noreferrer">Open job ↗</a>`:"—"}</td></tr>`).join("")}</tbody></table>`;
}
function dashboard() {
 const s=state.summary;
 return `<div class="grid stats">
  ${stat("Total companies",s.totalCompanies,"Companies in your workspace","▤")}
  ${stat("Enabled websites",s.enabledCompanies,"Ready for configured searches","◎")}
  ${stat("Jobs collected",s.totalJobs,"Persisted job postings","⌕")}
  ${stat("Search runs",s.totalRuns,"Recorded execution requests","◷")}
 </div>
 <div class="section-row"><h2>Workspace overview</h2><button class="btn btn-primary" onclick="setPage('run-center')">＋ Start a search</button></div>
 <div class="grid two-col">
  <div class="panel"><div class="panel-head"><h3>Configured companies</h3><button class="btn btn-sm" onclick="setPage('companies')">View all</button></div>${companyTable(state.companies.slice(0,5))}</div>
  <div class="panel"><div class="panel-head"><h3>Latest search activity</h3><button class="btn btn-sm" onclick="setPage('history')">Run history</button></div>${runTable(state.runs.slice(-5).reverse())}</div>
 </div>
 <div class="section-row"><h2>Getting started</h2><span class="pill blue">Starter workspace</span></div>
 <div class="grid two-col"><div class="card"><h3 style="margin:0 0 8px;font-size:13px">1. Add career websites</h3><p class="muted small">Register company career URLs or upload your Excel workbook to validate its company list.</p><button class="btn btn-sm" onclick="setPage('companies')">Manage companies →</button></div>
 <div class="card"><h3 style="margin:0 0 8px;font-size:13px">2. Configure a search</h3><p class="muted small">Create a search request with keyword, location and posted-day criteria.</p><button class="btn btn-sm" onclick="setPage('run-center')">Open Run Center →</button></div></div>`;
}
function companies() {
 return `<div class="section-row" style="margin-top:0"><div><h2>Company directory</h2><div class="muted small">Manage career websites registered in JobScout.</div></div><button class="btn btn-primary" onclick="document.getElementById('company-form').scrollIntoView({behavior:'smooth'})">＋ Add company</button></div>
 <div class="panel">${companyTable(state.companies)}</div>
 <div class="section-row"><h2>Add a company</h2></div>
 <div class="card"><form id="company-form"><div class="form-grid">
 <div class="field"><label>COMPANY KEY</label><input name="companyKey" placeholder="AUTODESK" required></div>
 <div class="field"><label>COMPANY NAME</label><input name="name" placeholder="Autodesk" required></div>
 <div class="field full"><label>CAREER WEBSITE URL</label><input name="careerUrl" type="url" placeholder="https://jobs.autodesk.com/careers" required></div>
 <div class="field"><label>INTERACTION TYPE</label><select name="interactionType"><option>PLAYWRIGHT</option><option>API</option></select></div>
 <div class="field"><label>STATUS</label><select name="enabled"><option value="true">Enabled</option><option value="false">Disabled</option></select></div>
 </div><div class="form-actions"><button class="btn btn-primary">Save company</button></div></form></div>`;
}
function runCenter() {
 const opts=state.companies.map(c=>`<option value="${esc(c.companyKey)}">${esc(c.name)} (${esc(c.companyKey)})</option>`).join("");
 return `<div class="grid two-col">
 <div class="card"><h2 style="font-size:15px;margin:0 0 6px">Create a search run</h2><p class="muted small">Choose a company and search criteria. The current starter records the run request; browser crawling will be added in the next milestone.</p>
 <form id="run-form"><div class="form-grid">
 <div class="field full"><label>COMPANY</label><select name="companyKey" required>${opts||'<option value="">Add a company first</option>'}</select></div>
 <div class="field"><label>JOB TITLE / KEYWORD</label><input name="keyword" value="Java" placeholder="Java Backend Developer"></div>
 <div class="field"><label>LOCATION</label><input name="location" value="India" placeholder="India"></div>
 <div class="field"><label>POSTED WITHIN (DAYS)</label><input name="postedWithinDays" type="number" min="0" value="7"></div>
 </div><div class="notice" style="margin-top:15px">Sample POC criteria: Autodesk · Java · India · last 7 days. Actual website search and scraping are not yet connected to this button.</div>
 <div class="form-actions"><button class="btn btn-primary" ${state.companies.length?"":"disabled"}>Create run request</button></div></form></div>
 <div class="card"><h3 style="font-size:13px;margin:0 0 12px">Execution flow</h3>
 ${["Load company configuration","Open career website","Fill keyword and location","Apply posted-date filter","Crawl result pages","Extract and store jobs"].map((x,i)=>`<div style="display:flex;gap:10px;align-items:center;padding:11px 0;border-bottom:1px solid #edf0f4"><span class="company-logo">${i+1}</span><span class="small">${x}</span><span class="pill" style="margin-left:auto">Planned</span></div>`).join("")}
 </div></div>`;
}
function jobs() {
 return `<div class="section-row" style="margin-top:0"><div><h2>Job results explorer</h2><div class="muted small">Search and review normalized job postings collected from career websites.</div></div><button class="btn" onclick="loadAll()">↻ Refresh</button></div>
 <div class="toolbar"><input class="search-input" id="job-filter" placeholder="Filter by title, ID or location…"><select id="company-filter"><option value="">All companies</option>${state.companies.map(c=>`<option>${esc(c.name)}</option>`).join("")}</select><span class="pill blue">${state.jobs.length} records loaded</span></div>
 <div class="panel" id="job-table">${jobTable(state.jobs)}</div>`;
}
function history() {
 return `<div class="section-row" style="margin-top:0"><div><h2>Run history</h2><div class="muted small">All submitted search execution requests.</div></div><button class="btn" onclick="loadAll()">↻ Refresh</button></div><div class="panel">${runTable([...state.runs].reverse())}</div>`;
}
function configuration() {
 return `<div class="grid two-col">
 <div class="card"><h2 style="font-size:15px;margin:0 0 6px">Import Excel configuration</h2><p class="muted small">Upload a workbook with a <b>Companies</b> sheet. This initial version validates required headers and previews rows; it does not yet import every configuration sheet into the database.</p>
 <form id="excel-form"><div class="field"><label>EXCEL WORKBOOK (.XLSX)</label><input type="file" name="file" accept=".xlsx,.xls" required></div><div class="form-actions"><button class="btn btn-primary">Upload and validate</button></div></form><div id="excel-result" class="small muted" style="margin-top:12px"></div></div>
 <div class="card"><h3 style="font-size:13px;margin:0 0 12px">Expected Companies sheet columns</h3><div class="notice">company_id · company_name · career_url · enabled · interaction_type</div><p class="small muted">SearchFields, ExtractionFields and Pagination sheets are part of the target design and will be added to the full configuration importer.</p></div>
 </div>`;
}
function settings() {
 return `<div class="grid two-col">
 <div class="card"><h2 style="font-size:15px;margin:0 0 14px">Application settings</h2><div class="field"><label>BACKEND API</label><input value="Same origin · /api" readonly></div><div class="field" style="margin-top:12px"><label>DATABASE</label><input value="MySQL · configured in application.yml" readonly></div><div class="field" style="margin-top:12px"><label>SERVER PORT</label><input value="8080" readonly></div></div>
 <div class="card"><h3 style="font-size:13px;margin:0 0 10px">Integration status</h3><p class="small">Backend API <span class="pill green">Connected</span></p><p class="small">MySQL <span class="pill blue">Configured</span></p><p class="small">Google Drive <span class="pill amber">Not connected</span></p><p class="small">Crawler worker <span class="pill amber">Not implemented</span></p><p class="small">Playwright <span class="pill blue">Dependency included</span></p></div>
 </div><div class="notice" style="margin-top:15px">Change DB_URL, DB_USERNAME and DB_PASSWORD environment variables or edit application.yml for your local MySQL instance. Do not commit production credentials.</div>`;
}
function render() {
 const content={dashboard,companies,"run-center":runCenter,jobs,history,configuration,settings}[state.page]();
 $("#page-content").innerHTML=content;
 bindForms();
}
function bindForms() {
 const cf=$("#company-form");
 if(cf) cf.addEventListener("submit",async e=>{
  e.preventDefault(); const f=new FormData(cf); const body=Object.fromEntries(f.entries()); body.enabled=body.enabled==="true";
  try{await request("/api/companies",{method:"POST",body:JSON.stringify(body)});toast("Company saved.");await loadAll();setPage("companies");}catch(err){toast(err.message,true);}
 });
 const rf=$("#run-form");
 if(rf) rf.addEventListener("submit",async e=>{
  e.preventDefault();const f=new FormData(rf);const body=Object.fromEntries(f.entries());body.postedWithinDays=Number(body.postedWithinDays);
  try{await request("/api/runs",{method:"POST",body:JSON.stringify(body)});toast("Run request created. Crawling is not connected in this starter.");await loadAll();setPage("history");}catch(err){toast(err.message,true);}
 });
 const ef=$("#excel-form");
 if(ef) ef.addEventListener("submit",async e=>{
  e.preventDefault();const file=new FormData(ef).get("file");const fd=new FormData();fd.append("file",file);
  try{const r=await fetch("/api/configurations/upload",{method:"POST",body:fd});const d=await r.json();if(!r.ok)throw new Error(d.message||"Upload failed");$("#excel-result").textContent=`Validated ${d.parsedCompanyRows} company rows from ${d.fileName}.`;toast("Excel parsed successfully.");}catch(err){toast(err.message,true);}
 });
 const jf=$("#job-filter");
 if(jf) jf.addEventListener("input",()=>filterJobs());
 const jcf=$("#company-filter");
 if(jcf) jcf.addEventListener("change",()=>filterJobs());
}
function filterJobs(){
 const q=($("#job-filter")?.value||"").toLowerCase(), c=($("#company-filter")?.value||"").toLowerCase();
 const filtered=state.jobs.filter(j=>(`${j.title||""} ${j.externalJobId||""} ${j.location||""}`.toLowerCase().includes(q)) && (!c || (j.company?.name||"").toLowerCase()===c));
 $("#job-table").innerHTML=jobTable(filtered);
}
loadAll();
