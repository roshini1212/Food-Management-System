const state = {
    foods: [],
    suppliers: [],
    alerts: [],
    summary: null,
    selectedCategory: "ALL",
    searchTerm: "",
    prepPlan: [],
    planView: "prep",
};

const STORAGE_KEY = "fmsRestaurantAccount";
const account = readAccount();

if (!account) {
    window.location.replace("/login.html");
}

const summaryFields = {
    totalItems: document.getElementById("totalItems"),
    criticalItems: document.getElementById("criticalItems"),
    expiringSoon: document.getElementById("expiringSoon"),
    lowStock: document.getElementById("lowStock"),
    openAlerts: document.getElementById("openAlerts"),
};

const productGrid = document.getElementById("productGrid");
const categoryNav = document.getElementById("categoryNav");
const supplierList = document.getElementById("supplierList");
const supplierSelect = document.getElementById("supplierSelect");
const storyCard = document.getElementById("storyCard");
const visibleCount = document.getElementById("visibleCount");
const catalogTitle = document.getElementById("catalogTitle");
const planList = document.getElementById("planList");
const searchInput = document.getElementById("searchInput");
const restaurantNameNode = document.getElementById("restaurantName");
const foodNameInput = document.getElementById("foodNameInput");
const categorySelect = document.getElementById("categorySelect");
const batchCodeInput = document.getElementById("batchCodeInput");

restaurantNameNode.textContent = account ? account.restaurantName : "Restaurant";

document.getElementById("orderDate").textContent = new Date().toLocaleDateString(undefined, {
    weekday: "long",
    month: "long",
    day: "numeric",
    year: "numeric",
});

document.getElementById("refreshButton").addEventListener("click", () => {
    loadDashboard();
});

document.getElementById("logoutButton").addEventListener("click", () => {
    localStorage.removeItem(STORAGE_KEY);
    window.location.replace("/login.html");
});

foodNameInput?.addEventListener("input", updateBatchCodeSuggestion);
categorySelect?.addEventListener("change", updateBatchCodeSuggestion);

searchInput.addEventListener("input", (event) => {
    state.searchTerm = event.target.value.trim().toLowerCase();
    renderCatalog();
});

categoryNav.addEventListener("click", (event) => {
    const button = event.target.closest("[data-category]");
    if (!button) {
        return;
    }

    state.selectedCategory = button.dataset.category;
    renderCategoryNav();
    renderCatalog();
});

document.querySelector(".order-tabs").addEventListener("click", (event) => {
    const tab = event.target.closest("[data-plan-view]");
    if (!tab) {
        return;
    }

    state.planView = tab.dataset.planView;
    renderPlanTabs();
    renderPlanPanel();
});

document.getElementById("supplierForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    const feedback = document.getElementById("supplierFeedback");
    const payload = Object.fromEntries(new FormData(form).entries());

    const success = await submitForm("/api/suppliers", payload, feedback, "Supplier created successfully.");
    if (success) {
        form.reset();
        await loadDashboard();
    }
});

document.getElementById("foodForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    const feedback = document.getElementById("foodFeedback");
    const raw = Object.fromEntries(new FormData(form).entries());

    const payload = {
        ...raw,
        supplierId: Number(raw.supplierId),
        quantity: Number(raw.quantity),
        reorderLevel: Number(raw.reorderLevel),
    };

    const success = await submitForm("/api/foods", payload, feedback, "Food item created successfully.");
    if (success) {
        form.reset();
        if (batchCodeInput) {
            batchCodeInput.dataset.userEdited = "false";
        }
        updateBatchCodeSuggestion();
        await loadDashboard();
    }
});

productGrid.addEventListener("click", (event) => {
    const button = event.target.closest("[data-food-id]");
    if (!button) {
        return;
    }

    togglePrepPlan(Number(button.dataset.foodId));
});

planList.addEventListener("click", (event) => {
    const actionButton = event.target.closest("[data-plan-remove]");
    if (!actionButton) {
        return;
    }

    removeFromPrepPlan(Number(actionButton.dataset.planRemove));
});

async function loadDashboard() {
    try {
        const [summary, foods, suppliers, alerts] = await Promise.all([
            fetchJson("/api/dashboard/summary"),
            fetchJson("/api/foods"),
            fetchJson("/api/suppliers"),
            fetchJson("/api/alerts?status=OPEN"),
        ]);

        state.summary = summary;
        state.foods = foods;
        state.suppliers = suppliers;
        state.alerts = alerts;

        syncPrepPlanWithInventory();
        renderSummary();
        renderCategoryNav();
        renderCatalog();
        renderSuppliers();
        populateSupplierSelect();
        renderPlanTabs();
        renderPlanPanel();
        renderPurpose();
        renderInsight();
    } catch (error) {
        storyCard.textContent = `Dashboard data could not be loaded: ${cleanError(error.message)}`;
    }
}

async function fetchJson(url) {
    const response = await fetch(url, {
        headers: buildRestaurantHeaders(),
    });
    if (!response.ok) {
        throw new Error(`Failed to load ${url}`);
    }
    return response.json();
}

async function submitForm(url, payload, feedbackNode, successMessage) {
    setFeedback(feedbackNode, "Saving...", "");

    try {
        const response = await fetch(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                ...buildRestaurantHeaders(),
            },
            body: JSON.stringify(payload),
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || "Request failed");
        }

        setFeedback(feedbackNode, successMessage, "feedback-success");
        return true;
    } catch (error) {
        setFeedback(feedbackNode, cleanError(error.message), "feedback-error");
        return false;
    }
}

function renderSummary() {
    summaryFields.totalItems.textContent = state.summary.totalItems;
    summaryFields.criticalItems.textContent = getCriticalFoods().length;
    summaryFields.expiringSoon.textContent = state.summary.expiringSoon;
    summaryFields.lowStock.textContent = state.summary.lowStock;
    summaryFields.openAlerts.textContent = state.summary.openAlerts;

    document.getElementById("criticalItemsSummary").textContent = String(getCriticalFoods().length);
    document.getElementById("reorderValue").textContent = String(getReorderFoods().length);
    document.getElementById("totalValue").textContent = String(state.summary.openAlerts);
    document.getElementById("prepItemsValue").textContent = String(state.prepPlan.length);
}

function renderCategoryNav() {
    const categories = ["ALL", ...new Set(state.foods.map((food) => food.category).filter(Boolean))];
    categoryNav.innerHTML = categories.map((category) => {
        const label = category === "ALL" ? "All Items" : escapeHtml(category);
        const activeClass = category === state.selectedCategory ? " active" : "";
        return `<button class="category-pill${activeClass}" data-category="${escapeHtml(category)}">${label}</button>`;
    }).join("");
}

function renderCatalog() {
    const foods = getFilteredFoods();
    visibleCount.textContent = `${foods.length} item${foods.length === 1 ? "" : "s"}`;
    catalogTitle.textContent = state.selectedCategory === "ALL" ? "All Food Items" : `${state.selectedCategory} Items`;

    if (!foods.length) {
        productGrid.innerHTML = `<article class="empty-card">No food items match this filter right now.</article>`;
        return;
    }

    productGrid.innerHTML = foods.map((food) => {
        const status = getFoodStatus(food);
        const selected = state.prepPlan.includes(food.id) ? " selected" : "";
        return `
            <button class="product-card${selected}" data-food-id="${food.id}">
                <div class="product-visual" style="${getVisualGradient(food)}">
                    <span class="price-tag">Use in ${daysUntil(food.expiryDate)}d</span>
                    <span class="product-status">${status.label}</span>
                </div>
                <div class="product-info">
                    <div class="product-name">${escapeHtml(food.name)}</div>
                    <div class="product-meta">${escapeHtml(food.category)} • ${escapeHtml(food.supplierName)}</div>
                    <div class="product-footer">
                        <span>${food.quantity} ${escapeHtml(food.unit)} in stock</span>
                        <span>${daysUntil(food.expiryDate)} day${daysUntil(food.expiryDate) === 1 ? "" : "s"} left</span>
                    </div>
                    <div class="product-action">${selected ? "Selected for today's prep" : "Click to add to today's prep plan"}</div>
                </div>
            </button>
        `;
    }).join("");
}

function renderSuppliers() {
    supplierList.innerHTML = `
        <article class="supplier-item">
            <div class="supplier-name">1. Review risk labels</div>
            <div class="supplier-meta">Critical and Watch labels show which ingredients need attention first.</div>
        </article>
        <article class="supplier-item">
            <div class="supplier-name">2. Build the prep plan</div>
            <div class="supplier-meta">Click food cards to mark the items the kitchen should prioritize today.</div>
        </article>
        <article class="supplier-item">
            <div class="supplier-name">3. Reorder before stockouts</div>
            <div class="supplier-meta">Switch to Reorder to see what is low and which supplier should be contacted.</div>
        </article>
    `;
}

function renderPurpose() {
    const text = state.summary.openAlerts > 0
        ? `This website is for a restaurant manager or kitchen lead. It shows which ingredients are close to expiry, which products are low in stock, and which suppliers may need to be called. Right now it is tracking ${state.summary.totalItems} items and ${state.summary.openAlerts} active alert${state.summary.openAlerts === 1 ? "" : "s"}.`
        : "This website is for a restaurant manager or kitchen lead. It helps decide what to use first, what to reorder, and which supplier to contact.";
    document.getElementById("purposeText").textContent = text;
}

function renderInsight() {
    const highestRiskItem = [...state.foods].sort((a, b) => daysUntil(a.expiryDate) - daysUntil(b.expiryDate))[0];
    if (!highestRiskItem) {
        storyCard.textContent = "Add inventory data to unlock daily kitchen decisions.";
        return;
    }

    storyCard.textContent = `${highestRiskItem.name} is the nearest-expiry ingredient with ${daysUntil(highestRiskItem.expiryDate)} day(s) remaining. The meaningful use of this website is daily restaurant decision-making: use expiring ingredients first, reorder low-stock items early, and keep supplier contacts and alerts visible in one place.`;
}

function renderPlanTabs() {
    document.querySelectorAll("[data-plan-view]").forEach((node) => {
        node.classList.toggle("active", node.dataset.planView === state.planView);
    });
}

function renderPlanPanel() {
    document.getElementById("prepItemsValue").textContent = String(state.prepPlan.length);

    if (state.planView === "prep") {
        renderPrepPlan();
        return;
    }
    if (state.planView === "reorder") {
        renderReorderPlan();
        return;
    }
    renderSupplierPlan();
}

function renderPrepPlan() {
    if (!state.prepPlan.length) {
        planList.innerHTML = `<article class="cart-empty">Click an inventory card to add it to today’s prep plan.</article>`;
        return;
    }

    planList.innerHTML = state.prepPlan.map((foodId) => {
        const food = state.foods.find((item) => item.id === foodId);
        if (!food) {
            return "";
        }

        return `
            <article class="cart-item">
                <div class="cart-top">
                    <div>
                        <div class="cart-name">${escapeHtml(food.name)}</div>
                        <div class="cart-meta">${escapeHtml(food.category)} • ${escapeHtml(food.supplierName)}</div>
                        <div class="plan-label">Use within ${daysUntil(food.expiryDate)} day(s)</div>
                    </div>
                    <button class="qty-button" data-plan-remove="${food.id}" aria-label="Remove item">x</button>
                </div>
            </article>
        `;
    }).join("");
}

function renderReorderPlan() {
    const items = getReorderFoods();
    if (!items.length) {
        planList.innerHTML = `<article class="cart-empty">No items currently need reorder.</article>`;
        return;
    }

    planList.innerHTML = items.map((food) => `
        <article class="cart-item">
            <div class="cart-top">
                <div>
                    <div class="cart-name">${escapeHtml(food.name)}</div>
                    <div class="cart-meta">${food.quantity} ${escapeHtml(food.unit)} left • reorder level ${food.reorderLevel}</div>
                    <div class="plan-label">Supplier: ${escapeHtml(food.supplierName)}</div>
                </div>
            </div>
        </article>
    `).join("");
}

function renderSupplierPlan() {
    if (!state.suppliers.length) {
        planList.innerHTML = `<article class="cart-empty">No suppliers available.</article>`;
        return;
    }

    planList.innerHTML = state.suppliers.map((supplier) => `
        <article class="cart-item">
            <div class="cart-top">
                <div>
                    <div class="cart-name">${escapeHtml(supplier.name)}</div>
                    <div class="cart-meta">${escapeHtml(supplier.contactPerson)}</div>
                    <div class="plan-label">${escapeHtml(supplier.phone)}</div>
                </div>
            </div>
        </article>
    `).join("");
}

function togglePrepPlan(foodId) {
    if (state.prepPlan.includes(foodId)) {
        state.prepPlan = state.prepPlan.filter((id) => id !== foodId);
    } else {
        state.prepPlan.push(foodId);
    }

    renderCatalog();
    renderPlanPanel();
    renderSummary();
    renderInsight();
}

function removeFromPrepPlan(foodId) {
    state.prepPlan = state.prepPlan.filter((id) => id !== foodId);
    renderCatalog();
    renderPlanPanel();
    renderSummary();
    renderInsight();
}

function populateSupplierSelect() {
    const currentValue = supplierSelect.value;
    supplierSelect.innerHTML = `<option value="">Select supplier</option>` + state.suppliers
        .map((supplier) => `<option value="${supplier.id}">${escapeHtml(supplier.name)}</option>`)
        .join("");
    supplierSelect.value = currentValue;
}

function getFilteredFoods() {
    return state.foods.filter((food) => {
        const matchesCategory = state.selectedCategory === "ALL" || food.category === state.selectedCategory;
        const haystack = `${food.name} ${food.category} ${food.supplierName} ${food.batchCode}`.toLowerCase();
        const matchesSearch = !state.searchTerm || haystack.includes(state.searchTerm);
        return matchesCategory && matchesSearch;
    });
}

function syncPrepPlanWithInventory() {
    state.prepPlan = state.prepPlan.filter((id) => state.foods.some((food) => food.id === id));
}

function getFoodStatus(food) {
    const days = daysUntil(food.expiryDate);
    if (days <= 2) {
        return { label: "Critical" };
    }
    if (food.quantity <= food.reorderLevel) {
        return { label: "Low Stock" };
    }
    if (days <= 7) {
        return { label: "Watch" };
    }
    return { label: "Fresh" };
}

function getCriticalFoods() {
    return state.foods.filter((food) => daysUntil(food.expiryDate) <= 2);
}

function getReorderFoods() {
    return state.foods.filter((food) => food.quantity <= food.reorderLevel);
}

function getVisualGradient(food) {
    const gradients = [
        "background: linear-gradient(135deg, #ff9d72, #ff5e86);",
        "background: linear-gradient(135deg, #ffa26d, #ff7c56);",
        "background: linear-gradient(135deg, #7f94ff, #5468ff);",
        "background: linear-gradient(135deg, #3fc8a7, #4d9dff);",
        "background: linear-gradient(135deg, #f8b24b, #ff6f91);",
    ];
    return gradients[food.id % gradients.length];
}

function daysUntil(dateString) {
    const today = new Date();
    const target = new Date(`${dateString}T00:00:00`);
    const start = new Date(today.getFullYear(), today.getMonth(), today.getDate());
    return Math.round((target - start) / 86400000);
}

function cleanError(message) {
    return message.replace(/^Error:\s*/, "");
}

function setFeedback(node, text, className) {
    node.textContent = text;
    node.className = `form-feedback ${className}`.trim();
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}

function readAccount() {
    try {
        const raw = localStorage.getItem(STORAGE_KEY);
        return raw ? JSON.parse(raw) : null;
    } catch (error) {
        localStorage.removeItem(STORAGE_KEY);
        return null;
    }
}

function buildRestaurantHeaders() {
    return account ? { "X-Restaurant-Id": String(account.id) } : {};
}

function updateBatchCodeSuggestion() {
    if (!batchCodeInput || batchCodeInput.dataset.userEdited === "true") {
        return;
    }

    const item = (foodNameInput?.value || "").trim().toUpperCase().replace(/[^A-Z0-9]+/g, "").slice(0, 3) || "ITM";
    const category = (categorySelect?.value || "").trim().toUpperCase().replace(/[^A-Z0-9]+/g, "").slice(0, 2) || "CT";
    batchCodeInput.value = `${category}-${item}-${new Date().getFullYear().toString().slice(-2)}`;
}

batchCodeInput?.addEventListener("input", () => {
    batchCodeInput.dataset.userEdited = batchCodeInput.value.trim() ? "true" : "false";
});

updateBatchCodeSuggestion();

loadDashboard();
