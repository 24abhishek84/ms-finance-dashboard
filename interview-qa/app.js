const LEVELS = ["basic", "medium", "senior"];
const $nav = document.getElementById("nav");
const $main = document.getElementById("main");
const $search = document.getElementById("search");

const esc = (s) => s.replace(/[&<>"]/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" }[c]));
const inline = (s) => esc(s).replace(/`([^`]+)`/g, "<code>$1</code>");
const cap = (s) => s[0].toUpperCase() + s.slice(1);

// route: #/topic/level/index
function parse() {
  const [, topic, level, idx] = location.hash.split("/");
  return { topic, level, idx: idx === undefined ? undefined : Number(idx) };
}

function renderNav() {
  const { topic, level, idx } = parse();
  $nav.innerHTML = Object.entries(QA).map(([tk, t]) => {
    const open = tk === topic;
    const levels = LEVELS.map((l) =>
      `<a class="level-link ${open && l === level ? "active" : ""}" href="#/${tk}/${l}">${cap(l)}</a>`).join("");
    let qs = "";
    if (open && level) {
      qs = t.levels[level].map((x, i) =>
        `<a class="qlink ${i === idx ? "active" : ""}" href="#/${tk}/${level}/${i}">${i + 1}. ${esc(x.q)}</a>`).join("");
    }
    return `<div class="topic"><a href="#/${tk}">${t.title}</a>${levels}${qs}</div>`;
  }).join("");
}

function renderHome() {
  $main.innerHTML = `<h1>Interview Q&amp;A</h1>
    <p class="hint">Pick a topic and level. Questions progress from basic → medium → senior.</p>
    <div class="cards">${Object.entries(QA).map(([tk, t]) => `
      <div class="card"><h3>${t.title}</h3>
      ${LEVELS.map((l) => `<p><a href="#/${tk}/${l}"><span class="badge ${l}">${l}</span></a> ${t.levels[l].length} questions</p>`).join("")}
      </div>`).join("")}</div>`;
}

function renderList(tk, level) {
  const t = QA[tk];
  const levels = level ? [level] : LEVELS;
  $main.innerHTML = `<h1>${t.title}${level ? ` – ${cap(level)}` : ""}</h1>` +
    levels.map((l) => `<h3><span class="badge ${l}">${l}</span></h3><div class="list">` +
      t.levels[l].map((x, i) => `<a href="#/${tk}/${l}/${i}">${i + 1}. ${esc(x.q)}</a>`).join("") + `</div>`).join("");
}

function renderQuestion(tk, level, i) {
  const list = QA[tk].levels[level];
  const x = list[i];
  const prev = i > 0 ? `<a href="#/${tk}/${level}/${i - 1}">← Previous</a>` : "<span></span>";
  const next = i < list.length - 1 ? `<a href="#/${tk}/${level}/${i + 1}">Next →</a>` : "<span></span>";
  $main.innerHTML = `<p><a href="#/${tk}/${level}">← ${QA[tk].title} / ${cap(level)}</a></p>
    <div class="qa"><span class="badge ${level}">${level}</span>
    <h2>${i + 1}. ${esc(x.q)}</h2>
    <p>${inline(x.a)}</p>
    ${x.code ? `<pre><code>${esc(x.code)}</code></pre>` : ""}</div>
    <div class="pager">${prev}${next}</div>`;
}

function renderSearch(term) {
  const hits = [];
  for (const [tk, t] of Object.entries(QA))
    for (const l of LEVELS)
      t.levels[l].forEach((x, i) => {
        if ((x.q + " " + x.a).toLowerCase().includes(term)) hits.push({ tk, l, i, x, t });
      });
  $main.innerHTML = `<h1>Search: “${esc(term)}”</h1><div class="list">` +
    (hits.map((h) => `<a href="#/${h.tk}/${h.l}/${h.i}"><span class="badge ${h.l}">${h.l}</span> ${h.t.title}: ${esc(h.x.q)}</a>`).join("") || "<p>No results.</p>") + "</div>";
}

function render() {
  const { topic, level, idx } = parse();
  renderNav();
  window.scrollTo(0, 0);
  if ($search.value.trim()) return renderSearch($search.value.trim().toLowerCase());
  if (!QA[topic]) return renderHome();
  if (!LEVELS.includes(level)) return renderList(topic);
  if (idx === undefined || !QA[topic].levels[level][idx]) return renderList(topic, level);
  renderQuestion(topic, level, idx);
}

$search.addEventListener("input", render);
window.addEventListener("hashchange", () => { $search.value = ""; render(); });
render();
