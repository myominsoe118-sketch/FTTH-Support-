"""
FTTH OmniSupport - Python FastAPI Backend & Web Application
Unified Customer Support Desk for Fiber-To-The-Home (FTTH) ISPs in Myanmar
Supported Real Channels: Telegram Bot API, Viber Partners API, Facebook Messenger Graph API
"""

import os
import time
import json
import sqlite3
from typing import Optional, List
from datetime import datetime

from fastapi import FastAPI, Request, HTTPException, Form, Depends
from fastapi.responses import HTMLResponse, JSONResponse
from fastapi.staticfiles import StaticFiles
from fastapi.templating import Jinja2Templates
from pydantic import BaseModel
import httpx

app = FastAPI(title="FTTH OmniSupport Web App", version="1.0.0")

# Setup templates
TEMPLATES_DIR = os.path.join(os.path.dirname(__file__), "templates")
templates = Jinja2Templates(directory=TEMPLATES_DIR)

DB_PATH = os.path.join(os.path.dirname(__file__), "ftth_support.db")

# In-memory config for bot credentials (or stored in DB/env)
CONFIG = {
    "telegram_bot_token": os.getenv("TELEGRAM_BOT_TOKEN", ""),
    "viber_auth_token": os.getenv("VIBER_AUTH_TOKEN", ""),
    "viber_sender_name": os.getenv("VIBER_SENDER_NAME", "FTTH Support"),
    "messenger_page_token": os.getenv("MESSENGER_PAGE_TOKEN", ""),
    "messenger_verify_token": os.getenv("MESSENGER_VERIFY_TOKEN", "ftth_verify_secret_123"),
}

def get_db():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn

def init_db():
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS tickets (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        ticket_no TEXT UNIQUE,
        customer_name TEXT,
        customer_phone TEXT,
        customer_address TEXT,
        township TEXT,
        service_plan TEXT,
        channel TEXT, -- TELEGRAM, VIBER, MESSENGER, PHONE
        channel_sender_id TEXT, -- Telegram ChatID or Viber Sender ID or Messenger PSID
        priority TEXT, -- URGENT, NORMAL
        ticket_type TEXT, -- NEW_CUSTOMER, LOS_RED_LIGHT, SLOW_SPEED, ROUTER_CONFIG, RELOCATION, BILLING
        status TEXT, -- OPEN, IN_PROGRESS, SCHEDULED, RESOLVED, CLOSED
        created_at INTEGER,
        updated_at INTEGER,
        appointment_date INTEGER,
        appointment_time_slot TEXT,
        assigned_technician TEXT,
        optical_power_dbm REAL,
        pon_splitter_port TEXT,
        latest_message TEXT,
        unread_count INTEGER DEFAULT 0
    )
    """)

    cursor.execute("""
    CREATE TABLE IF NOT EXISTS messages (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        ticket_id INTEGER,
        sender TEXT, -- CUSTOMER, AGENT, SYSTEM
        sender_name TEXT,
        channel TEXT,
        text TEXT,
        timestamp INTEGER,
        FOREIGN KEY (ticket_id) REFERENCES tickets (id)
    )
    """)

    cursor.execute("SELECT COUNT(*) FROM tickets")
    count = cursor.fetchone()[0]
    if count == 0:
        now = int(time.time() * 1000)
        day_ms = 86400 * 1000

        # Sample 1: Overdue (>48h) fiber cut complain
        cursor.execute("""
        INSERT INTO tickets (ticket_no, customer_name, customer_phone, customer_address, township, service_plan, channel, channel_sender_id, priority, ticket_type, status, created_at, updated_at, optical_power_dbm, pon_splitter_port, latest_message, unread_count)
        VALUES ('FTTH-1082', 'ဦးမင်းသူ (U Min Thu)', '09450123456', 'အမှတ်(၂၄)၊ အင်းစိန်လမ်းမကြီး', 'လှိုင် (Hlaing)', 'FTTH 100 Mbps Pro', 'VIBER', 'viber_user_1', 'URGENT', 'LOS_RED_LIGHT', 'OPEN', ?, ?, -28.9, 'FAT-HLN-02 / P3', 'Router မှာ မီးနီပြတ်တောက်နေတာ ၂ ရက်ကျော်ပါပြီ ပြင်ပေးပါ', 2)
        """, (now - 3 * day_ms, now - 3 * day_ms))
        t1_id = cursor.lastrowid
        cursor.execute("""
        INSERT INTO messages (ticket_id, sender, sender_name, channel, text, timestamp)
        VALUES (?, 'CUSTOMER', 'ဦးမင်းသူ', 'VIBER', 'Router မှာ မီးနီပြတ်တောက်နေတာ ၂ ရက်ကျော်ပါပြီ ပြင်ပေးပါ', ?)
        """, (t1_id, now - 3 * day_ms))

        # Sample 2: New installation with appointment
        cursor.execute("""
        INSERT INTO tickets (ticket_no, customer_name, customer_phone, customer_address, township, service_plan, channel, channel_sender_id, priority, ticket_type, status, created_at, updated_at, appointment_date, appointment_time_slot, assigned_technician, optical_power_dbm, pon_splitter_port, latest_message)
        VALUES ('FTTH-3041', 'ဒေါ်သင်းသင်းခိုင် (Daw Thin Thin)', '09790112233', 'တိုက် ၂၃၊ သစ္စာလမ်း', 'ရန်ကင်း (Yankin)', 'FTTH 60 Mbps Home', 'TELEGRAM', 'tg_user_1', 'NORMAL', 'NEW_CUSTOMER', 'SCHEDULED', ?, ?, ?, '09:00 AM - 12:00 PM (နံနက်ပိုင်း)', 'ကိုအောင်မြင့် (FTTH Field Tech)', -21.4, 'FAT-YKN-01 / P4', 'Fiber အသစ်တပ်ဆင်လိုပါသည်')
        """, (now - 6 * 3600 * 1000, now - 6 * 3600 * 1000, now + day_ms))
        t2_id = cursor.lastrowid
        cursor.execute("""
        INSERT INTO messages (ticket_id, sender, sender_name, channel, text, timestamp)
        VALUES (?, 'CUSTOMER', 'ဒေါ်သင်းသင်းခိုင်', 'TELEGRAM', 'Fiber အသစ်တပ်ဆင်လိုပါသည်', ?)
        """, (t2_id, now - 6 * 3600 * 1000))

    conn.commit()
    conn.close()

init_db()

# ==========================================
# REAL OUTBOUND SENDERS (TELEGRAM / VIBER / MESSENGER)
# ==========================================

async def send_real_telegram_message(chat_id: str, text: str):
    token = CONFIG.get("telegram_bot_token")
    if not token or not chat_id:
        return False
    url = f"https://api.telegram.org/bot{token}/sendMessage"
    async with httpx.AsyncClient(timeout=10.0) as client:
        try:
            resp = await client.post(url, json={"chat_id": chat_id, "text": text})
            return resp.status_code == 200
        except Exception as e:
            print(f"Telegram send error: {e}")
            return False

async def send_real_viber_message(receiver_id: str, text: str):
    token = CONFIG.get("viber_auth_token")
    if not token or not receiver_id:
        return False
    url = "https://chatapi.viber.com/pa/send_message"
    headers = {"X-Viber-Auth-Token": token}
    payload = {
        "receiver": receiver_id,
        "min_api_version": 1,
        "sender": {"name": CONFIG.get("viber_sender_name", "FTTH Support")},
        "type": "text",
        "text": text
    }
    async with httpx.AsyncClient(timeout=10.0) as client:
        try:
            resp = await client.post(url, json=payload, headers=headers)
            return resp.status_code == 200
        except Exception as e:
            print(f"Viber send error: {e}")
            return False

async def send_real_messenger_message(recipient_psid: str, text: str):
    token = CONFIG.get("messenger_page_token")
    if not token or not recipient_psid:
        return False
    url = f"https://graph.facebook.com/v19.0/me/messages?access_token={token}"
    payload = {
        "recipient": {"id": recipient_psid},
        "message": {"text": text}
    }
    async with httpx.AsyncClient(timeout=10.0) as client:
        try:
            resp = await client.post(url, json=payload)
            return resp.status_code == 200
        except Exception as e:
            print(f"Messenger send error: {e}")
            return False

# ==========================================
# WEBHOOK ENDPOINTS (INCOMING MESSAGES FROM REAL APPS)
# ==========================================

@app.post("/webhook/telegram")
async def telegram_webhook(request: Request):
    """
    Real Telegram Bot Webhook endpoint
    Configure with: https://api.telegram.org/bot<TOKEN>/setWebhook?url=https://<your-domain>/webhook/telegram
    """
    try:
        data = await request.json()
        message = data.get("message") or data.get("channel_post")
        if not message:
            return {"ok": True}

        text = message.get("text", "").strip()
        chat = message.get("chat", {})
        from_user = message.get("from", {})
        chat_id = str(chat.get("id") or from_user.get("id"))
        sender_name = f"{from_user.get('first_name', '')} {from_user.get('last_name', '')}".strip() or "Telegram User"

        now = int(time.time() * 1000)

        conn = get_db()
        cursor = conn.cursor()
        cursor.execute("SELECT * FROM tickets WHERE channel_sender_id = ? AND status != 'CLOSED'", (chat_id,))
        ticket = cursor.fetchone()

        if ticket:
            ticket_id = ticket["id"]
            cursor.execute("""
            INSERT INTO messages (ticket_id, sender, sender_name, channel, text, timestamp)
            VALUES (?, 'CUSTOMER', ?, 'TELEGRAM', ?, ?)
            """, (ticket_id, sender_name, text, now))
            cursor.execute("""
            UPDATE tickets SET latest_message = ?, updated_at = ?, unread_count = unread_count + 1 WHERE id = ?
            """, (text, now, ticket_id))
        else:
            is_complain = any(k in text.lower() for k in ["မရ", "ကျ", "slow", "los", "မီးနီ", "error", "ပျက်"])
            ticket_type = "LOS_RED_LIGHT" if is_complain else "NEW_CUSTOMER"
            priority = "URGENT" if is_complain else "NORMAL"
            ticket_no = f"TG-{int(time.time()) % 10000}"

            cursor.execute("""
            INSERT INTO tickets (ticket_no, customer_name, customer_phone, customer_address, township, service_plan, channel, channel_sender_id, priority, ticket_type, status, created_at, updated_at, latest_message, unread_count)
            VALUES (?, ?, ?, 'Telegram Direct Contact', 'လှိုင် (Hlaing)', 'FTTH 50 Mbps', 'TELEGRAM', ?, ?, ?, 'OPEN', ?, ?, ?, 1)
            """, (ticket_no, sender_name, chat_id, chat_id, priority, ticket_type, now, now, text))
            ticket_id = cursor.lastrowid
            cursor.execute("""
            INSERT INTO messages (ticket_id, sender, sender_name, channel, text, timestamp)
            VALUES (?, 'CUSTOMER', ?, 'TELEGRAM', ?, ?)
            """, (ticket_id, sender_name, text, now))

        conn.commit()
        conn.close()
        return {"ok": True}
    except Exception as e:
        print(f"Telegram webhook error: {e}")
        return {"ok": False, "error": str(e)}

@app.post("/webhook/viber")
async def viber_webhook(request: Request):
    """
    Real Viber Bot Webhook endpoint
    Configure with: curl -H "X-Viber-Auth-Token: <TOKEN>" https://chatapi.viber.com/pa/set_webhook -d '{"url": "https://<your-domain>/webhook/viber"}'
    """
    try:
        data = await request.json()
        event = data.get("event")
        if event == "webhook":
            return {"status": 0, "message": "ok"}
        if event == "message":
            sender = data.get("sender", {})
            sender_id = sender.get("id")
            sender_name = sender.get("name", "Viber Customer")
            message = data.get("message", {})
            text = message.get("text", "").strip()

            now = int(time.time() * 1000)
            conn = get_db()
            cursor = conn.cursor()
            cursor.execute("SELECT * FROM tickets WHERE channel_sender_id = ? AND status != 'CLOSED'", (sender_id,))
            ticket = cursor.fetchone()

            if ticket:
                ticket_id = ticket["id"]
                cursor.execute("""
                INSERT INTO messages (ticket_id, sender, sender_name, channel, text, timestamp)
                VALUES (?, 'CUSTOMER', ?, 'VIBER', ?, ?)
                """, (ticket_id, sender_name, text, now))
                cursor.execute("""
                UPDATE tickets SET latest_message = ?, updated_at = ?, unread_count = unread_count + 1 WHERE id = ?
                """, (text, now, ticket_id))
            else:
                is_complain = any(k in text.lower() for k in ["မရ", "ကျ", "slow", "los", "မီးနီ", "error", "ပျက်"])
                ticket_type = "LOS_RED_LIGHT" if is_complain else "NEW_CUSTOMER"
                priority = "URGENT" if is_complain else "NORMAL"
                ticket_no = f"VB-{int(time.time()) % 10000}"

                cursor.execute("""
                INSERT INTO tickets (ticket_no, customer_name, customer_phone, customer_address, township, service_plan, channel, channel_sender_id, priority, ticket_type, status, created_at, updated_at, latest_message, unread_count)
                VALUES (?, ?, ?, 'Viber Direct Contact', 'ကမာရွတ် (Kamayut)', 'FTTH 50 Mbps', 'VIBER', ?, ?, ?, 'OPEN', ?, ?, ?, 1)
                """, (ticket_no, sender_name, sender_id, sender_id, priority, ticket_type, now, now, text))
                ticket_id = cursor.lastrowid
                cursor.execute("""
                INSERT INTO messages (ticket_id, sender, sender_name, channel, text, timestamp)
                VALUES (?, 'CUSTOMER', ?, 'VIBER', ?, ?)
                """, (ticket_id, sender_name, text, now))

            conn.commit()
            conn.close()
        return {"status": 0}
    except Exception as e:
        print(f"Viber webhook error: {e}")
        return {"status": 1, "error": str(e)}

@app.get("/webhook/messenger")
async def messenger_verify(request: Request):
    """
    Facebook Messenger Webhook Verification (hub.challenge)
    """
    params = request.query_params
    mode = params.get("hub.mode")
    token = params.get("hub.verify_token")
    challenge = params.get("hub.challenge")
    if mode == "subscribe" and token == CONFIG.get("messenger_verify_token"):
        return HTMLResponse(content=challenge, status_code=200)
    raise HTTPException(status_code=403, detail="Verification failed")

@app.post("/webhook/messenger")
async def messenger_webhook(request: Request):
    """
    Real Facebook Messenger message events receiver
    """
    try:
        data = await request.json()
        if data.get("object") == "page":
            for entry in data.get("entry", []):
                for messaging in entry.get("messaging", []):
                    sender_id = messaging.get("sender", {}).get("id")
                    message = messaging.get("message", {})
                    text = message.get("text", "").strip()

                    if sender_id and text:
                        now = int(time.time() * 1000)
                        conn = get_db()
                        cursor = conn.cursor()
                        cursor.execute("SELECT * FROM tickets WHERE channel_sender_id = ? AND status != 'CLOSED'", (sender_id,))
                        ticket = cursor.fetchone()

                        if ticket:
                            ticket_id = ticket["id"]
                            cursor.execute("""
                            INSERT INTO messages (ticket_id, sender, sender_name, channel, text, timestamp)
                            VALUES (?, 'CUSTOMER', 'Messenger Customer', 'MESSENGER', ?, ?)
                            """, (ticket_id, text, now))
                            cursor.execute("""
                            UPDATE tickets SET latest_message = ?, updated_at = ?, unread_count = unread_count + 1 WHERE id = ?
                            """, (text, now, ticket_id))
                        else:
                            is_complain = any(k in text.lower() for k in ["မရ", "ကျ", "slow", "los", "မီးနီ", "error"])
                            ticket_type = "LOS_RED_LIGHT" if is_complain else "NEW_CUSTOMER"
                            priority = "URGENT" if is_complain else "NORMAL"
                            ticket_no = f"FB-{int(time.time()) % 10000}"

                            cursor.execute("""
                            INSERT INTO tickets (ticket_no, customer_name, customer_phone, customer_address, township, service_plan, channel, channel_sender_id, priority, ticket_type, status, created_at, updated_at, latest_message, unread_count)
                            VALUES (?, 'Messenger User', ?, 'Facebook Page Message', 'စမ်းချောင်း (Sanchaung)', 'FTTH 60 Mbps', 'MESSENGER', ?, ?, ?, 'OPEN', ?, ?, ?, 1)
                            """, (ticket_no, sender_id, sender_id, priority, ticket_type, now, now, text))
                            ticket_id = cursor.lastrowid
                            cursor.execute("""
                            INSERT INTO messages (ticket_id, sender, sender_name, channel, text, timestamp)
                            VALUES (?, 'CUSTOMER', 'Messenger User', 'MESSENGER', ?, ?)
                            """, (ticket_id, text, now))

                        conn.commit()
                        conn.close()
            return {"status": "ok"}
    except Exception as e:
        print(f"Messenger webhook error: {e}")
    return {"status": "ok"}

# ==========================================
# REST API FOR WEB DASHBOARD
# ==========================================

@app.get("/api/tickets")
def get_tickets(channel: Optional[str] = None, priority: Optional[str] = None, status: Optional[str] = None, search: Optional[str] = None):
    conn = get_db()
    cursor = conn.cursor()
    query = "SELECT * FROM tickets WHERE 1=1"
    params = []
    if channel and channel != "ALL":
        query += " AND channel = ?"
        params.append(channel)
    if priority and priority != "ALL":
        query += " AND priority = ?"
        params.append(priority)
    if status and status != "ALL":
        query += " AND status = ?"
        params.append(status)
    if search:
        query += " AND (customer_name LIKE ? OR customer_phone LIKE ? OR ticket_no LIKE ? OR latest_message LIKE ?)"
        term = f"%{search}%"
        params.extend([term, term, term, term])
    query += " ORDER BY updated_at DESC"
    cursor.execute(query, params)
    tickets = [dict(row) for row in cursor.fetchall()]
    conn.close()
    return tickets

@app.get("/api/tickets/{ticket_id}/messages")
def get_messages(ticket_id: int):
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM messages WHERE ticket_id = ? ORDER BY timestamp ASC", (ticket_id,))
    msgs = [dict(row) for row in cursor.fetchall()]
    conn.close()
    return msgs

class ReplyPayload(BaseModel):
    text: str
    agent_name: Optional[str] = "Support Desk"

@app.post("/api/tickets/{ticket_id}/reply")
async def send_reply(ticket_id: int, payload: ReplyPayload):
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM tickets WHERE id = ?", (ticket_id,))
    ticket = cursor.fetchone()
    if not ticket:
        conn.close()
        raise HTTPException(status_code=404, detail="Ticket not found")

    now = int(time.time() * 1000)
    cursor.execute("""
    INSERT INTO messages (ticket_id, sender, sender_name, channel, text, timestamp)
    VALUES (?, 'AGENT', ?, ?, ?, ?)
    """, (ticket_id, payload.agent_name, ticket["channel"], payload.text, now))

    cursor.execute("""
    UPDATE tickets SET latest_message = ?, updated_at = ?, unread_count = 0, status = CASE WHEN status = 'OPEN' THEN 'IN_PROGRESS' ELSE status END WHERE id = ?
    """, (f"Desk: {payload.text}", now, ticket_id))
    conn.commit()
    conn.close()

    # REAL OUTBOUND SENDING TO REAL APPS
    ch = ticket["channel"]
    sender_id = ticket["channel_sender_id"] or ticket["customer_phone"]
    if ch == "TELEGRAM":
        await send_real_telegram_message(sender_id, payload.text)
    elif ch == "VIBER":
        await send_real_viber_message(sender_id, payload.text)
    elif ch == "MESSENGER":
        await send_real_messenger_message(sender_id, payload.text)

    return {"status": "sent", "channel": ch}

class AppointmentPayload(BaseModel):
    time_slot: str
    technician: str
    auto_notify_channel: bool = True

@app.post("/api/tickets/{ticket_id}/appointment")
async def book_appointment(ticket_id: int, payload: AppointmentPayload):
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM tickets WHERE id = ?", (ticket_id,))
    ticket = cursor.fetchone()
    if not ticket:
        conn.close()
        raise HTTPException(status_code=404, detail="Ticket not found")

    now = int(time.time() * 1000)
    is_complain = ticket["ticket_type"] != "NEW_CUSTOMER"
    purpose = f"Complain ချို့ယွင်းချက် ပြင်ဆင်ရေး ({ticket['ticket_type']})" if is_complain else "FTTH Fiber အသစ် တပ်ဆင်ရေး"

    record_text = (
        f"📅 [{ticket['channel']} On-site Record]:\n"
        f"• အမျိုးအစား: {purpose}\n"
        f"• ရက်ချိန်းအချိန်: {payload.time_slot}\n"
        f"• တာဝန်ကျ နည်းပညာရှင်: {payload.technician}\n"
        f"• ဖောက်သည်: {ticket['customer_name']} ({ticket['customer_phone']})\n"
        f"• လိပ်စာ: {ticket['customer_address']}, {ticket['township']}\n"
        f"✅ {ticket['channel']} ချန်နယ်သို့ အကြောင်းကြားလွှာ ပေးပို့မှတ်တမ်းတင်ပြီးပါပြီ။"
    )

    cursor.execute("""
    INSERT INTO messages (ticket_id, sender, sender_name, channel, text, timestamp)
    VALUES (?, 'SYSTEM', 'Appointment Desk', ?, ?, ?)
    """, (ticket_id, ticket["channel"], record_text, now))

    cursor.execute("""
    UPDATE tickets SET appointment_time_slot = ?, assigned_technician = ?, status = 'SCHEDULED', latest_message = ?, updated_at = ? WHERE id = ?
    """, (payload.time_slot, payload.technician, f"[ရက်ချိန်း]: {payload.time_slot} ({payload.technician})", now, ticket_id))

    conn.commit()
    conn.close()

    # If auto-notify is enabled, push the appointment confirmation to customer's real app!
    if payload.auto_notify_channel:
        ch = ticket["channel"]
        sender_id = ticket["channel_sender_id"] or ticket["customer_phone"]
        cust_msg = f"မင်္ဂလာပါရှင်၊ သင်၏ FTTH ဝန်ဆောင်မှုအတွက် On-site ရက်ချိန်း သတ်မှတ်ပြီးပါပြီ။\nအချိန်: {payload.time_slot}\nနည်းပညာရှင်: {payload.technician}"
        if ch == "TELEGRAM":
            await send_real_telegram_message(sender_id, cust_msg)
        elif ch == "VIBER":
            await send_real_viber_message(sender_id, cust_msg)
        elif ch == "MESSENGER":
            await send_real_messenger_message(sender_id, cust_msg)

    return {"status": "scheduled", "record": record_text}

@app.post("/api/config")
def update_config(data: dict):
    for k, v in data.items():
        if k in CONFIG and v is not None:
            CONFIG[k] = v.strip()
    return {"status": "updated", "config": {k: ("***" if v else "") for k, v in CONFIG.items()}}

# ==========================================
# WEB DASHBOARD (HTML UI)
# ==========================================

@app.get("/", response_class=HTMLResponse)
def index_page(request: Request):
    return templates.TemplateResponse("index.html", {"request": request, "config": CONFIG})

if __name__ == "__main__":
    import uvicorn
    print("🚀 Starting FTTH OmniSupport Web App on http://localhost:8000")
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
