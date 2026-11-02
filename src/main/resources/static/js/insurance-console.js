const state = {
    products: [],
    selectedProduct: null,
    order: null,
    policy: null,
    step: 1
};

const elements = {
    productList: document.querySelector('#product-list'),
    continueButton: document.querySelector('#continue-to-form'),
    purchaseForm: document.querySelector('#purchase-form'),
    orderReceipt: document.querySelector('#order-receipt'),
    policyReceipt: document.querySelector('#policy-receipt'),
    payButton: document.querySelector('#pay-button'),
    paymentConfirmation: document.querySelector('#payment-confirmation'),
    summary: document.querySelector('#summary-content'),
    alert: document.querySelector('#alert'),
    statusText: document.querySelector('#status-text'),
    statusLamp: document.querySelector('#status-lamp'),
    statusTime: document.querySelector('#status-time')
};

const relationshipLabels = {
    SELF: '本人',
    SPOUSE: '配偶',
    PARENT: '父母',
    CHILD: '子女'
};

document.addEventListener('DOMContentLoaded', initialize);

async function initialize() {
    bindEvents();
    updateClock();
    setInterval(updateClock, 1000);
    await loadProducts();
}

function bindEvents() {
    document.querySelectorAll('[data-go-step]').forEach(button => {
        button.addEventListener('click', () => goToStep(Number(button.dataset.goStep)));
    });

    elements.continueButton.addEventListener('click', () => goToStep(2));
    elements.purchaseForm.addEventListener('submit', createOrder);
    elements.paymentConfirmation.addEventListener('change', () => {
        elements.payButton.disabled = !elements.paymentConfirmation.checked;
    });
    elements.payButton.addEventListener('click', payOrder);
    document.querySelector('#new-purchase').addEventListener('click', resetPurchase);
    document.querySelector('#order-nav').addEventListener('click', () => {
        goToStep(state.order ? 3 : 1);
    });
}

async function loadProducts() {
    try {
        const response = await fetch('/api/products');
        if (!response.ok) throw new Error('无法读取保险产品');
        state.products = await response.json();
        renderProducts();
        setStatus('产品服务已连接，请选择保障方案', 'ok');
    } catch (error) {
        elements.productList.innerHTML = '<div class="loading-row">产品加载失败，请确认 Spring Boot 服务正在运行。</div>';
        showError(error.message);
        setStatus('产品服务连接失败', 'error');
    }
}

function renderProducts() {
    elements.productList.innerHTML = state.products.map(product => `
        <label class="product-row" data-product-id="${product.id}">
            <input type="radio" name="product" value="${product.id}" aria-label="选择${escapeHtml(product.name)}">
            <span>
                <strong class="product-name">${escapeHtml(product.name)}</strong>
                <span class="product-code">${escapeHtml(product.code)}</span>
                <p class="product-description">${escapeHtml(product.description)}</p>
            </span>
            <span class="product-price">
                <strong>¥ ${formatMoney(product.premium)}</strong>
                <small>保额 ¥${formatMoney(product.coverageAmount)} / ${product.coverageDays} 天</small>
            </span>
        </label>
    `).join('');

    elements.productList.querySelectorAll('input[name="product"]').forEach(input => {
        input.addEventListener('change', () => selectProduct(Number(input.value)));
    });
}

function selectProduct(id) {
    state.selectedProduct = state.products.find(product => product.id === id);
    elements.productList.querySelectorAll('.product-row').forEach(row => {
        row.classList.toggle('is-selected', Number(row.dataset.productId) === id);
    });
    elements.continueButton.disabled = false;
    renderSummary();
    setStatus(`已选择：${state.selectedProduct.name}`, 'ok');
}

function goToStep(step) {
    hideError();
    if (step > 1 && !state.selectedProduct) {
        showError('请先选择一款保险产品。');
        step = 1;
    }
    if (step > 2 && !state.order) {
        showError('请先填写投保资料并生成订单。');
        step = 2;
    }
    if (step > 3 && !state.policy) {
        showError('请先完成模拟支付。');
        step = 3;
    }

    state.step = step;
    document.querySelectorAll('[data-step-panel]').forEach(panel => {
        panel.classList.toggle('is-visible', Number(panel.dataset.stepPanel) === step);
    });
    document.querySelectorAll('[data-step-item]').forEach(item => {
        const itemStep = Number(item.dataset.stepItem);
        item.classList.toggle('is-active', itemStep === step);
        item.classList.toggle('is-complete', itemStep < step);
    });
    document.querySelector(`[data-step-panel="${step}"] h2`)?.focus?.();
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

async function createOrder(event) {
    event.preventDefault();
    hideError();

    if (!elements.purchaseForm.reportValidity()) return;

    const form = new FormData(elements.purchaseForm);
    const request = {
        productId: state.selectedProduct.id,
        applicant: {
            name: form.get('applicantName').trim(),
            idCard: form.get('applicantIdCard').trim(),
            phone: form.get('applicantPhone').trim()
        },
        insuredPerson: {
            name: form.get('insuredName').trim(),
            idCard: form.get('insuredIdCard').trim(),
            relationship: form.get('relationship')
        }
    };

    setBusy(true, '正在生成投保单…');
    try {
        state.order = await requestJson('/api/orders', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(request)
        });
        renderOrder();
        renderSummary();
        setStatus(`投保单 ${state.order.orderNo} 已生成`, 'ok');
        goToStep(3);
    } catch (error) {
        showError(error.message);
        setStatus('投保单生成失败', 'error');
    } finally {
        setBusy(false);
    }
}

function renderOrder() {
    elements.orderReceipt.innerHTML = dataRows([
        ['订单号', state.order.orderNo],
        ['保险产品', state.order.productName],
        ['投保人', state.order.applicantName],
        ['被保险人', `${state.order.insuredName}（${relationshipLabels[state.order.relationship] || state.order.relationship}）`],
        ['保障金额', `¥ ${formatMoney(state.order.coverageAmount)}`],
        ['保障期限', `${state.order.coverageDays} 天`],
        ['应付保费', `<span class="money-value">¥ ${formatMoney(state.order.premium)}</span>`, true],
        ['订单状态', translateStatus(state.order.status)]
    ]);
}

async function payOrder() {
    if (!state.order || !elements.paymentConfirmation.checked) return;
    hideError();
    setBusy(true, '正在处理模拟支付…');

    try {
        state.policy = await requestJson(`/api/orders/${encodeURIComponent(state.order.orderNo)}/pay`, {
            method: 'POST'
        });
        state.order.status = 'PAID';
        renderPolicy();
        renderSummary();
        setStatus(`支付完成，保单 ${state.policy.policyNo} 已生效`, 'ok');
        goToStep(4);
    } catch (error) {
        showError(error.message);
        setStatus('模拟支付失败', 'error');
    } finally {
        setBusy(false);
    }
}

function renderPolicy() {
    elements.policyReceipt.innerHTML = dataRows([
        ['保单号', state.policy.policyNo],
        ['关联订单', state.policy.orderNo],
        ['保险产品', state.policy.productName],
        ['被保险人', state.policy.insuredName],
        ['保障金额', `<span class="money-value">¥ ${formatMoney(state.policy.coverageAmount)}</span>`, true],
        ['保障生效日', state.policy.effectiveDate],
        ['保障到期日', state.policy.expirationDate],
        ['保单状态', '已生效']
    ]);
}

function renderSummary() {
    if (!state.selectedProduct) return;

    elements.summary.className = 'summary-product';
    elements.summary.innerHTML = `
        <h3>${escapeHtml(state.selectedProduct.name)}</h3>
        <dl>
            <div><dt>产品代码</dt><dd>${escapeHtml(state.selectedProduct.code)}</dd></div>
            <div><dt>保障金额</dt><dd>¥ ${formatMoney(state.selectedProduct.coverageAmount)}</dd></div>
            <div><dt>保障期限</dt><dd>${state.selectedProduct.coverageDays} 天</dd></div>
            <div><dt>应付保费</dt><dd>¥ ${formatMoney(state.selectedProduct.premium)}</dd></div>
            ${state.order ? `<div><dt>订单状态</dt><dd>${translateStatus(state.order.status)}</dd></div>` : ''}
            ${state.policy ? `<div><dt>保单号</dt><dd>${escapeHtml(state.policy.policyNo)}</dd></div>` : ''}
        </dl>
    `;
}

function resetPurchase() {
    state.selectedProduct = null;
    state.order = null;
    state.policy = null;
    elements.purchaseForm.reset();
    elements.paymentConfirmation.checked = false;
    elements.payButton.disabled = true;
    elements.continueButton.disabled = true;
    elements.productList.querySelectorAll('input').forEach(input => input.checked = false);
    elements.productList.querySelectorAll('.product-row').forEach(row => row.classList.remove('is-selected'));
    elements.summary.className = 'summary-empty';
    elements.summary.innerHTML = '<span>尚未选择产品</span><small>请从产品列表中选择一项</small>';
    setStatus('已开始新的购买流程，请选择保障方案', 'ok');
    goToStep(1);
}

async function requestJson(url, options = {}) {
    const response = await fetch(url, options);
    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
        throw new Error(data.message || `请求失败（HTTP ${response.status}）`);
    }
    return data;
}

function dataRows(rows) {
    return `<dl>${rows.map(([label, value, trustedHtml = false]) => `
        <div class="data-row">
            <dt>${escapeHtml(label)}</dt>
            <dd>${trustedHtml ? value : escapeHtml(value)}</dd>
        </div>
    `).join('')}</dl>`;
}

function setStatus(message, type = '') {
    elements.statusText.textContent = message;
    elements.statusLamp.className = `status-lamp${type ? ` is-${type}` : ''}`;
}

function setBusy(busy, message = '') {
    document.querySelectorAll('.button-primary').forEach(button => {
        if (button !== elements.continueButton) button.disabled = busy;
    });
    if (busy && message) setStatus(message);
    if (!busy) elements.payButton.disabled = !elements.paymentConfirmation.checked;
}

function showError(message) {
    elements.alert.textContent = message;
    elements.alert.hidden = false;
    elements.alert.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

function hideError() {
    elements.alert.hidden = true;
    elements.alert.textContent = '';
}

function updateClock() {
    elements.statusTime.textContent = new Intl.DateTimeFormat('zh-CN', {
        hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false
    }).format(new Date());
}

function translateStatus(status) {
    return ({ PENDING_PAYMENT: '待支付', PAID: '已支付', CANCELLED: '已取消' })[status] || status;
}

function formatMoney(value) {
    return Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function escapeHtml(value) {
    return String(value)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}
