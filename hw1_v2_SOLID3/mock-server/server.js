const express = require('express');
const app = express();
const port = process.env.SERVER_PORT || 8082;

app.use(express.json());

app.use((req, res, next) => {
    console.log(`${new Date().toISOString()} - ${req.method} ${req.path}`);
    next();
});

app.get('/health', (req, res) => {
    res.json({ status: 'UP', timestamp: new Date().toISOString() });
});

app.get('/actuator/health', (req, res) => {
    res.json({ status: 'UP', components: { ping: { status: 'UP' } } });
});

app.post('/api/missions', (req, res) => {
    console.log('Mission request received:', JSON.stringify(req.body, null, 2));
    res.status(202).json({ 
        status: 'ACCEPTED', 
        message: 'Mission accepted for execution',
        missionId: `mission-${Date.now()}`
    });
});

app.use((req, res) => {
    console.log(`Unknown route: ${req.method} ${req.path}`);
    res.status(404).json({ error: 'Not found' });
});

app.listen(port, () => {
    console.log(`Mock space server running on port ${port}`);
    console.log(`Health check: http://localhost:${port}/health`);
    console.log(`API endpoint: http://localhost:${port}/api/missions`);
});
