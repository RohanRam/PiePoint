// ─── PiePoint AI Backend ─────────────────────────────────────────────────────
// Express server proxying Groq API for pizza building and chat.
// The Android app NEVER holds the Groq API key — all AI calls go through here.
// ─────────────────────────────────────────────────────────────────────────────

import 'dotenv/config';
import express from 'express';
import cors from 'cors';
import rateLimit from 'express-rate-limit';
import Groq from 'groq-sdk';
import { createRequire } from 'module';

const require = createRequire(import.meta.url);
const menu = require('./menu.json');

// ─── Config ──────────────────────────────────────────────────────────────────

const PORT = process.env.PORT || 3000;
const MODEL = process.env.GROQ_MODEL || 'llama-3.3-70b-versatile';
const MAX_PROMPT_LENGTH = 500;
const MAX_CHAT_MESSAGES = 50;
const MAX_MESSAGE_LENGTH = 1000;

const apiKey = process.env.GROQ_API_KEY;
if (!apiKey) {
  console.warn('⚠️  GROQ_API_KEY is not set.');
  console.warn('    Copy .env.example to .env and add your Groq API key from https://console.groq.com/');
}

const groq = new Groq({
  apiKey: apiKey || 'missing-key',
});

// ─── Express Setup ───────────────────────────────────────────────────────────

const app = express();
app.use(cors());
app.use(express.json({ limit: '16kb' }));

const apiLimiter = rateLimit({
  windowMs: 60 * 1000,    // 1 minute window
  max: 30,                // 30 requests per minute
  standardHeaders: true,
  legacyHeaders: false,
  message: { error: 'Too many requests. Please try again in a minute.' },
});

// ─── Menu Helpers ────────────────────────────────────────────────────────────

/**
 * Fuzzy-find an item in a menu category by ID or name.
 * Tries exact ID, exact name, & / and normalization, then substring.
 */
function findMenuItem(key, items) {
  if (!key || typeof key !== 'string') return null;
  const lower = key.toLowerCase().trim();

  // 1. Match by id
  const byId = items.find((item) => item.id.toLowerCase() === lower);
  if (byId) return byId;

  // 2. Exact case-insensitive name match
  const exact = items.find((item) => item.name.toLowerCase() === lower);
  if (exact) return exact;

  // 3. Normalized match (& ↔ and)
  const normalized = lower.replace(/&/g, 'and').replace(/\s+/g, ' ');
  const norm = items.find(
    (item) => item.name.toLowerCase().replace(/&/g, 'and').replace(/\s+/g, ' ') === normalized
  );
  if (norm) return norm;

  // 4. Substring match (either direction)
  const partial = items.find(
    (item) =>
      item.name.toLowerCase().includes(lower) ||
      lower.includes(item.name.toLowerCase())
  );
  return partial || null;
}

function findSize(sizeStr) {
  if (!sizeStr || typeof sizeStr !== 'string') return null;
  const lower = sizeStr.toLowerCase().trim();
  return menu.sizes.find(
    (s) => s.id.toLowerCase() === lower || s.label.toLowerCase() === lower
  );
}

/**
 * Build the menu summary string injected into the model's system prompt.
 */
function buildMenuSummary() {
  const crusts = menu.crusts.map((c) => `  - id: "${c.id}", name: "${c.name}" (₹${c.price})`).join('\n');
  const sauces = menu.sauces.map((s) => `  - id: "${s.id}", name: "${s.name}" (₹${s.price})`).join('\n');
  const cheeses = menu.cheeses.map((ch) => `  - id: "${ch.id}", name: "${ch.name}" (₹${ch.price})`).join('\n');
  const toppings = menu.toppings.map((t) => `  - id: "${t.id}", name: "${t.name}" ${t.emoji} (₹${t.price})`).join('\n');
  const sizes = menu.sizes
    .map((s) => `  - id: "${s.id}" (${s.inches}", +₹${s.priceModifier})`)
    .join('\n');
  const pizzas = menu.pizzas
    .map((p) => `  - "${p.name}" — ${p.description} (₹${p.basePrice})`)
    .join('\n');

  return [
    'CRUSTS:', crusts,
    '\nSAUCES:', sauces,
    '\nCHEESES:', cheeses,
    '\nTOPPINGS:', toppings,
    '\nSIZES:', sizes,
    '\nSIGNATURE PIZZAS (pre-built):', pizzas,
  ].join('\n');
}

const menuSummary = buildMenuSummary();

/**
 * Remove markdown code fences that models sometimes wrap JSON in.
 */
function stripCodeFences(text) {
  let s = text.trim();
  s = s.replace(/^```(?:json)?\s*\n?/i, '');
  s = s.replace(/\n?\s*```\s*$/i, '');
  return s.trim();
}

/**
 * Calls Groq chat.completions with response_format json_object and retries once if JSON fails to parse.
 */
async function completeWithJsonRetry(messages, maxTokens = 512, temperature = 0.5) {
  let attempts = 0;
  while (attempts < 2) {
    attempts++;
    const completion = await groq.chat.completions.create({
      model: MODEL,
      messages,
      response_format: { type: 'json_object' },
      max_tokens: maxTokens,
      temperature,
    });

    const content = completion.choices?.[0]?.message?.content;
    if (!content) {
      if (attempts < 2) {
        console.warn(`[groq] Empty response on attempt ${attempts}, retrying...`);
        continue;
      }
      throw new Error('EMPTY_RESPONSE');
    }

    try {
      const cleaned = stripCodeFences(content);
      const parsed = JSON.parse(cleaned);
      return parsed;
    } catch (parseError) {
      console.warn(`[groq] JSON parse error (attempt ${attempts}):`, parseError.message);
      if (attempts < 2) {
        console.log('[groq] Retrying request with JSON parse retry...');
        continue;
      }
      throw new Error('INVALID_JSON');
    }
  }
}

/**
 * Validate a raw AI-generated pizza object against menu.json
 * and compute the real price server-side.
 */
function validateAndPricePizza(raw) {
  const warnings = [];

  // ── Crust ──
  let crust = findMenuItem(raw.crust || raw.crustId, menu.crusts);
  if (!crust) {
    warnings.push(`Unknown crust "${raw.crust}", defaulted to Classic.`);
    crust = menu.crusts[0];
  }

  // ── Sauce ──
  let sauce = findMenuItem(raw.sauce || raw.sauceId, menu.sauces);
  if (!sauce) {
    warnings.push(`Unknown sauce "${raw.sauce}", defaulted to Classic Tomato.`);
    sauce = menu.sauces[0];
  }

  // ── Cheese ──
  let cheese = findMenuItem(raw.cheese || raw.cheeseId, menu.cheeses);
  if (!cheese) {
    warnings.push(`Unknown cheese "${raw.cheese}", defaulted to Mozzarella.`);
    cheese = menu.cheeses[0];
  }

  // ── Toppings ──
  const validToppings = [];
  const rawToppings = Array.isArray(raw.toppings) ? raw.toppings : [];
  for (const tItem of rawToppings.slice(0, 10)) {
    const tKey = typeof tItem === 'string' ? tItem : (tItem?.id || tItem?.name);
    const topping = findMenuItem(tKey, menu.toppings);
    if (topping) {
      validToppings.push(topping);
    } else {
      warnings.push(`Unknown topping "${tKey}", skipped.`);
    }
  }

  // ── Size ──
  let size = findSize(raw.size);
  if (!size) {
    warnings.push(`Unknown size "${raw.size}", defaulted to MEDIUM.`);
    size = menu.sizes.find((s) => s.id === 'MEDIUM');
  }

  // ── Price (computed server-side, never from model) ──
  const price =
    menu.customBasePrice +
    size.priceModifier +
    crust.price +
    sauce.price +
    cheese.price +
    validToppings.reduce((sum, t) => sum + t.price, 0);

  const pizza = {
    name: typeof raw.name === 'string' && raw.name.trim() ? raw.name.trim() : 'AI Custom Pizza',
    crust: { id: crust.id, name: crust.name, price: crust.price },
    sauce: { id: sauce.id, name: sauce.name, price: sauce.price },
    cheese: { id: cheese.id, name: cheese.name, price: cheese.price },
    toppings: validToppings.map((t) => ({
      id: t.id,
      name: t.name,
      emoji: t.emoji,
      price: t.price,
    })),
    size: {
      id: size.id,
      label: size.label,
      inches: size.inches,
      priceModifier: size.priceModifier,
    },
    price,
  };

  return { pizza, warnings: warnings.length > 0 ? warnings : undefined };
}

/**
 * Validate structured pizza { crustId, sauceId, cheeseId, toppings: [{id, qty}], size }
 * Returns priced pizza or null if invalid.
 */
function validateAndPriceStructuredPizza(raw) {
  if (!raw || typeof raw !== 'object') return null;

  const crust = findMenuItem(raw.crustId || raw.crust, menu.crusts);
  const sauce = findMenuItem(raw.sauceId || raw.sauce, menu.sauces);
  const cheese = findMenuItem(raw.cheeseId || raw.cheese, menu.cheeses);
  const size = findSize(raw.size);

  if (!crust || !sauce || !cheese || !size) {
    return null; // Required core components missing or invalid
  }

  const validToppings = [];
  const rawToppings = Array.isArray(raw.toppings) ? raw.toppings : [];
  for (const item of rawToppings.slice(0, 10)) {
    const toppingKey = typeof item === 'string' ? item : (item?.id || item?.name);
    const qty = typeof item === 'object' && typeof item?.qty === 'number'
      ? Math.max(1, Math.min(3, item.qty))
      : 1;
    const topping = findMenuItem(toppingKey, menu.toppings);
    if (topping) {
      validToppings.push({ topping, qty });
    }
  }

  const toppingsPrice = validToppings.reduce((sum, item) => sum + (item.topping.price * item.qty), 0);
  const price =
    menu.customBasePrice +
    size.priceModifier +
    crust.price +
    sauce.price +
    cheese.price +
    toppingsPrice;

  return {
    name: typeof raw.name === 'string' && raw.name.trim() ? raw.name.trim() : 'AI Custom Pizza',
    crust: { id: crust.id, name: crust.name, price: crust.price },
    sauce: { id: sauce.id, name: sauce.name, price: sauce.price },
    cheese: { id: cheese.id, name: cheese.name, price: cheese.price },
    toppings: validToppings.map((item) => ({
      id: item.topping.id,
      name: item.topping.name,
      emoji: item.topping.emoji,
      price: item.topping.price * item.qty,
    })),
    size: {
      id: size.id,
      label: size.label,
      inches: size.inches,
      priceModifier: size.priceModifier,
    },
    price,
  };
}

// ─── System Prompts ──────────────────────────────────────────────────────────

const CREATE_PIZZA_SYSTEM = `You are the PiePoint pizza builder AI. Given a user's description, build a custom pizza using ONLY items from our menu.

${menuSummary}

Custom pizza base price: ₹${menu.customBasePrice}
Formula: base + size modifier + crust + sauce + cheese + Σ toppings

Respond with ONLY a JSON object:
{
  "name": "A creative, appetizing name",
  "crust": "exact crust name or id from menu",
  "sauce": "exact sauce name or id from menu",
  "cheese": "exact cheese name or id from menu",
  "toppings": ["exact topping name or id", "..."],
  "size": "SMALL" | "MEDIUM" | "LARGE"
}

Rules:
- Use ONLY items listed above. Never invent items.
- Pick 2–5 toppings that match the description.
- Default to MEDIUM if size is unspecified.
- If a budget is given, pick items that keep the total under it.
- Vegetarian = no Pepperoni (t2), no Bacon (t9).
- Spicy = include Jalapeño (t10) and/or Spicy Arrabbiata (s2) sauce.
- Give the pizza a fun, creative name.`;

const CHAT_SYSTEM = `You are Pie, the cheerful and knowledgeable pizza concierge for PiePoint pizza restaurant.
You help customers explore our menu, recommend pizzas, answer dietary/ingredient questions, and build custom pizzas.

${menuSummary}

All prices are in Indian Rupees (₹).
Custom pizza base price is ₹${menu.customBasePrice}.

You must ALWAYS respond with a JSON object in this exact schema:
{
  "reply": "Your friendly, concise conversational answer in clean markdown with emojis.",
  "pizza": {
    "name": "Appetizing name for the pizza",
    "crustId": "c1",
    "sauceId": "s1",
    "cheeseId": "ch1",
    "toppings": [
      { "id": "t10", "qty": 1 }
    ],
    "size": "MEDIUM"
  } | null
}

Rules:
- If the customer wants you to build, create, customize, or recommend a specific custom pizza for them, generate the "pizza" object using the exact IDs above.
- If the customer is only asking questions, greeting, or chatting without asking for a pizza to be built, set "pizza": null.
- Use ONLY the valid IDs listed in the menu above.
- Vegetarian means NO Pepperoni (t2), NO Bacon (t9).
- Keep responses friendly, helpful, and focused on PiePoint.`;

// ─── Routes ──────────────────────────────────────────────────────────────────

// Health check
app.get('/health', (_req, res) => {
  res.json({
    status: 'ok',
    provider: 'groq',
    model: MODEL,
    menu: {
      crusts: menu.crusts.length,
      sauces: menu.sauces.length,
      cheeses: menu.cheeses.length,
      toppings: menu.toppings.length,
      sizes: menu.sizes.length,
      pizzas: menu.pizzas.length,
    },
  });
});

// Menu endpoint
app.get('/menu', (_req, res) => {
  res.json(menu);
});

// ─── POST /create-pizza ──────────────────────────────────────────────────────

app.post('/create-pizza', apiLimiter, async (req, res) => {
  try {
    const { prompt } = req.body;

    // ── Input validation ──
    if (!prompt || typeof prompt !== 'string') {
      return res.status(400).json({ error: '"prompt" (string) is required in the request body.' });
    }
    if (prompt.trim().length === 0) {
      return res.status(400).json({ error: '"prompt" cannot be empty.' });
    }
    if (prompt.length > MAX_PROMPT_LENGTH) {
      return res
        .status(400)
        .json({ error: `Prompt too long. Maximum ${MAX_PROMPT_LENGTH} characters.` });
    }

    console.log(`[create-pizza] prompt: "${prompt.substring(0, 80)}…"`);

    // Call Groq chat.completions with json_object format and retry once if JSON parse fails
    const parsed = await completeWithJsonRetry(
      [
        { role: 'system', content: CREATE_PIZZA_SYSTEM },
        { role: 'user', content: prompt.trim() },
      ],
      512,
      0.5
    );

    // ── Validate & price ──
    const { pizza, warnings } = validateAndPricePizza(parsed);

    console.log(`[create-pizza] → ${pizza.name} (₹${pizza.price})`);
    return res.json({ pizza, warnings });
  } catch (err) {
    console.error('[create-pizza] Error:', err.message || err);

    if (err?.status === 429 || err?.message?.includes('429') || err?.error?.code === 'rate_limit_exceeded') {
      return res.status(429).json({ error: 'Groq API rate limit reached. Please wait a moment and try again.' });
    }
    if (err?.status === 401 || err?.message?.includes('401')) {
      return res.status(500).json({ error: 'Invalid Groq API key. Check GROQ_API_KEY in backend/.env.' });
    }
    if (err?.message === 'INVALID_JSON') {
      return res.status(502).json({ error: 'AI returned invalid JSON after retry. Please try again.' });
    }
    if (err?.message === 'EMPTY_RESPONSE') {
      return res.status(502).json({ error: 'AI returned an empty response. Please try again.' });
    }

    return res.status(500).json({ error: 'Internal server error.' });
  }
});

// ─── POST /chat ──────────────────────────────────────────────────────────────

app.post('/chat', apiLimiter, async (req, res) => {
  try {
    const { messages } = req.body;

    if (!Array.isArray(messages) || messages.length === 0) {
      return res.status(400).json({ error: '"messages" must be a non-empty array.' });
    }
    if (messages.length > MAX_CHAT_MESSAGES) {
      return res.status(400).json({ error: `Too many messages. Maximum ${MAX_CHAT_MESSAGES}.` });
    }

    const groqMessages = [
      { role: 'system', content: CHAT_SYSTEM },
    ];

    for (const msg of messages) {
      if (!msg.content || typeof msg.content !== 'string' || !msg.content.trim()) continue;
      const role = (msg.role || '').toLowerCase() === 'user' ? 'user' : 'assistant';
      groqMessages.push({
        role,
        content: msg.content.slice(0, MAX_MESSAGE_LENGTH),
      });
    }

    if (groqMessages.length <= 1) {
      return res.status(400).json({ error: 'No valid message content provided.' });
    }

    console.log(`[chat] processing ${groqMessages.length - 1} message(s)...`);

    const parsed = await completeWithJsonRetry(groqMessages, 1024, 0.6);

    let reply = typeof parsed.reply === 'string' && parsed.reply.trim()
      ? parsed.reply.trim()
      : "I'm here to help you build your favorite pizza! 🍕";

    let suggestedPizza = null;

    if (parsed.pizza) {
      const validated = validateAndPriceStructuredPizza(parsed.pizza);
      if (validated) {
        suggestedPizza = validated;
      } else {
        // Pizza was invalid, drop it and mention in reply
        reply += "\n\n*(Note: Some ingredients in the requested pizza weren't available in our current menu, so I couldn't build that exact pizza. Feel free to ask what ingredients we have!)*";
      }
    }

    console.log(`[chat] → reply: "${reply.slice(0, 60)}…", pizza: ${suggestedPizza ? suggestedPizza.name : 'null'}`);
    return res.json({
      reply,
      suggestedPizza,
      pizza: suggestedPizza, // Backwards compatibility for Android client
    });
  } catch (err) {
    console.error('[chat] Error:', err.message || err);

    if (err?.status === 429 || err?.message?.includes('429') || err?.error?.code === 'rate_limit_exceeded') {
      return res.status(429).json({ error: 'Groq API rate limit reached. Please wait a moment and try again.' });
    }
    if (err?.status === 401 || err?.message?.includes('401')) {
      return res.status(500).json({ error: 'Invalid Groq API key. Check GROQ_API_KEY in backend/.env.' });
    }
    if (err?.message === 'INVALID_JSON') {
      return res.status(502).json({ error: 'AI returned invalid JSON after retry. Please try again.' });
    }
    if (err?.message === 'EMPTY_RESPONSE') {
      return res.status(502).json({ error: 'AI returned an empty response. Please try again.' });
    }

    return res.status(500).json({ error: 'Internal server error.' });
  }
});

// ─── Global error handler ────────────────────────────────────────────────────

app.use((err, _req, res, _next) => {
  if (err instanceof SyntaxError && err.status === 400 && 'body' in err) {
    return res.status(400).json({ error: 'Malformed JSON payload.' });
  }
  console.error('Unhandled error:', err);
  res.status(500).json({ error: 'Internal server error.' });
});

// ─── Start ───────────────────────────────────────────────────────────────────

app.listen(PORT, () => {
  console.log('');
  console.log('🍕 PiePoint AI Backend (Groq)');
  console.log(`   http://localhost:${PORT}`);
  console.log('');
  console.log('   POST /create-pizza  — Direct natural-language pizza builder');
  console.log('   POST /chat          — Conversational chat and recommendations');
  console.log('   GET  /menu          — View the full menu');
  console.log('   GET  /health        — Health check');
  console.log('');
  console.log(`   Model: ${MODEL}`);
  console.log('');
});
