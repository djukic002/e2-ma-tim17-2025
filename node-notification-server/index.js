import express from 'express';
import bodyParser from 'body-parser';
import admin from 'firebase-admin';
import serviceAccount from './service-account.json' with { type: 'json' };

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

const app = express();
app.use(bodyParser.json());

app.post('/send-invites', async (req, res) => {
  const { tokens, title, body, data } = req.body;

  if (!tokens || tokens.length === 0) {
    return res.status(400).json({ success: false, error: 'No tokens provided' });
  }

  try {
    // include notification (for tray) and custom data (for actions)
    const message = {
      tokens,
      data: {
        title: title || '',
        body: body || '',
        action_accept: 'ACCEPT',
        action_decline: 'DECLINE',
        ...(data || {}),
      },
    };

    // ✅ firebase-admin v13+ uses sendEachForMulticast
    const response = await admin.messaging().sendEachForMulticast(message);

    console.log('FCM response:', response);
    res.json({ success: true, response });
  } catch (err) {
    console.error('Error sending FCM:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

app.listen(5007, () =>
  console.log('Notification server running on port 5007')
);
