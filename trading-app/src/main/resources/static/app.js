import { createApp, ref, reactive, onMounted } from 'https://unpkg.com/vue@3/dist/vue.esm-browser.js';

const API = '/api/v1';

createApp({
    setup() {
        let seq = 1;
        const order = reactive({ symbol: 'AAPL', side: 'BUY', quantity: 10, price: 190.5 });
        const quoteSymbol = ref('AAPL');
        const quote = ref(null);
        const orders = ref([]);
        const lastMessage = ref('');
        const lastOk = ref(true);
        const report = reactive({
            logs: [], timings: {}, audits: [], exceptions: [],
            cacheHits: 0, cacheMisses: 0, retryAttempts: {}
        });

        async function post(body) {
            const res = await fetch(`${API}/orders`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body)
            });
            const data = await res.json();
            return { ok: res.ok, status: res.status, data };
        }

        async function placeOrder() {
            const body = {
                clientOrderId: 'UI-' + (seq++),
                symbol: order.symbol, side: order.side,
                quantity: order.quantity, price: order.price
            };
            const { ok, status, data } = await post(body);
            lastOk.value = ok;
            lastMessage.value = ok
                ? `下單成功：${data.orderId}（${data.status}）`
                : `${status} ${data.errorCode}：${data.message}`;
            await refreshAll();
        }

        async function triggerRisk() {
            const { ok, status, data } = await post({
                clientOrderId: 'UI-RISK-' + (seq++),
                symbol: 'AAPL', side: 'BUY', quantity: 5000, price: 190.5
            });
            lastOk.value = false;
            lastMessage.value = `${status} ${data.errorCode}（ruleCode=${data.ruleCode}）：${data.message}`;
            await refreshAll();
        }

        async function getQuote() {
            const res = await fetch(`${API}/pricing/${quoteSymbol.value}`);
            quote.value = await res.json();
            await loadReport();
        }

        async function loadReport() {
            const res = await fetch(`${API}/aspects/report`);
            const data = await res.json();
            report.logs = data.logs || [];
            report.timings = data.timings || {};
            report.audits = data.audits || [];
            report.exceptions = data.exceptions || [];
            report.cacheHits = data.cacheHits || 0;
            report.cacheMisses = data.cacheMisses || 0;
            report.retryAttempts = data.retryAttempts || {};
        }

        async function loadOrders() {
            const res = await fetch(`${API}/orders`);
            orders.value = await res.json();
        }

        async function refreshAll() {
            await Promise.all([loadReport(), loadOrders()]);
        }

        onMounted(refreshAll);

        return { order, quoteSymbol, quote, orders, lastMessage, lastOk, report,
            placeOrder, triggerRisk, getQuote, loadReport };
    }
}).mount('#app');
