# FTTH OmniSupport - Python FastAPI Web Application

FTTH (Fiber-To-The-Home) ISP များအတွက် Omnichannel Customer Support Web Desk & Real Bot Webhook Server ဖြစ်ပါသည်။
Viber, Telegram, Facebook Messenger စသော တကယ့် Bot API များနှင့် ချိတ်ဆက်ပြီး တစ်နေရာတည်းတွင် မက်ဆေ့ခ်ျများ လက်ခံခြင်း၊ အရေးကြီး (Urgent) / ရက်ကြာ (Overdue >48h) ခွဲခြားစစ်ဆေးခြင်း၊ ရက်ချိန်း (Appointments) မှတ်တမ်းတင်ခြင်းတို့ကို Web Browser ပေါ်မှ တိုက်ရိုက် အသုံးပြုနိုင်ပါသည်။

---

## ၁။ အသုံးပြုနိုင်သော စွမ်းဆောင်ရည်များ (Features)

1. **Omnichannel Messaging (တကယ့် App များနှင့် ချိတ်ဆက်ခြင်း):**
   - **Telegram:** Telegram Bot API ဖြင့် စာအပို့အယူ တိုက်ရိုက်လုပ်ဆောင်နိုင်ခြင်း။
   - **Viber:** Viber Partners Bot API (`X-Viber-Auth-Token`) ဖြင့် Customer ထံ တိုက်ရိုက်ပြန်ပို့နိုင်ခြင်း။
   - **Facebook Messenger:** Meta Graph API (`Page Access Token`) ဖြင့် Reply ပို့နိုင်ခြင်း။
2. **Webhooks အပြည့်အစုံ ပါဝင်ခြင်း (24/7 Real-Time Receiving):**
   - Telegram Webhook: `/webhook/telegram`
   - Viber Webhook: `/webhook/viber`
   - Messenger Webhook: `/webhook/messenger` (GET for hub.challenge, POST for messages)
3. **Daily Operations & SLA Aging Tracker:**
   - 🚨 အရေးကြီး (Urgent): Fiber ပြတ်တောက်မှု (LOS Red Light), Speed Drop
   - ⏳ ရက်ကြာမှု (>48h Overdue): SLA ရက်ကျော်လွန်နေသော Complain များကို အနီရောင်ဖြင့် ဦးစားပေးဖော်ပြခြင်း
4. **On-site ရက်ချိန်းများ စီမံခန့်ခွဲခြင်း (Appointments):**
   - Complain ပြင်ဆင်ရေးနှင့် ဖောက်သည်အသစ် တပ်ဆင်ရေးအတွက် Technician၊ ရက်စွဲနှင့် အချိန်သတ်မှတ်နိုင်ခြင်း။
   - ရက်ချိန်းသတ်မှတ်ချိန်တွင် သက်ဆိုင်ရာ Channel (Viber/Telegram/Messenger) ထဲသို့ အလိုအလျောက် Record သွင်းပေးပြီး ဖောက်သည်ထံ Confirmation စာပို့ပေးခြင်း။

---

## ၂။ Run ပြုလုပ်ပုံ အဆင့်ဆင့် (How to Run)

### လိုအပ်ချက်များ:
- Python 3.8 သို့မဟုတ် ၎င်းထက်မြင့်သော version

```bash
# ၁။ Folder ထဲသို့ သွားပါ
cd backend_server

# ၂။ လိုအပ်သော Libraries များကို install ပြုလုပ်ပါ
pip install -r requirements.txt

# ၃။ FastAPI Server ကို စတင်ပါ
python main.py
```

Console တွင် အောက်ပါအတိုင်း ပေါ်လာပါမည်-
```text
🚀 Starting FTTH OmniSupport Web App on http://localhost:8000
```

Browser တွင် `http://localhost:8000` ကို ဖွင့်၍ အသုံးပြုနိုင်ပါသည်။

---

## ၃။ တကယ့် Bot များနှင့် Webhook ချိတ်ဆက်ပုံ (Connecting Real Bots)

Local စက်တွင် စမ်းသပ်လိုပါက `ngrok` သို့မဟုတ် Cloudflare Tunnel ဖြင့် Public HTTPS URL ရယူနိုင်ပါသည်-
```bash
ngrok http 8000
```
(ရရှိလာသော URL ဥပမာ - `https://abc-123.ngrok-free.app`)

### က။ Telegram Bot ချိတ်ဆက်နည်း
၁။ Telegram တွင် `@BotFather` ထံသွား၍ `/newbot` ဖြင့် Bot အသစ်ဆောက်ပါ။ Token ရယူပါ။
၂။ Webhook Set လုပ်ရန် Browser တွင် အောက်ပါ Link ကို ခေါ်ပါ-
```text
https://api.telegram.org/bot<YOUR_BOT_TOKEN>/setWebhook?url=https://<YOUR_DOMAIN>/webhook/telegram
```

### ခ။ Viber Bot ချိတ်ဆက်နည်း
၁။ `https://partners.viber.com` တွင် အကောင့်ဖွင့်ပြီး Bot အသစ် create ပြုလုပ်ပါ။ `X-Viber-Auth-Token` ရယူပါ။
၂။ Webhook Set လုပ်ရန်:
```bash
curl -X POST "https://chatapi.viber.com/pa/set_webhook" \
     -H "X-Viber-Auth-Token: <YOUR_VIBER_TOKEN>" \
     -H "Content-Type: application/json" \
     -d '{"url": "https://<YOUR_DOMAIN>/webhook/viber"}'
```

### ဂ။ Facebook Messenger ချိတ်ဆက်နည်း
၁။ `https://developers.facebook.com` တွင် App ဆောက်ပြီး Messenger Product ထည့်ပါ။
၂။ Facebook Page နှင့် ချိတ်ဆက်ပြီး `Page Access Token` ရယူပါ။
၃။ Webhook URL တွင် `https://<YOUR_DOMAIN>/webhook/messenger` နှင့် Verify Token တွင် `ftth_verify_secret_123` ဟု ထည့်သွင်း Verify ပြုလုပ်ပါ။
