import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";

const errorRate = new Rate('errors');
const checkOrderDuration = new Trend('check_order_duration');
const createSatelliteDuration = new Trend('create_satellite_duration');
const getSatellitesDuration = new Trend('get_satellites_duration');
const successfulChecks = new Counter('successful_checks');

export const options = {
    stages: [
        { duration: '10s', target: 10 },
        { duration: '20s', target: 50 },
        { duration: '20s', target: 100 },
        { duration: '30s', target: 100 },
        { duration: '10s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<2000'],
        errors: ['rate<0.1'],
    },
};

const BASE_URL = 'http://localhost:8080/api/satellites';

function generateSatelliteData() {
    const names = ['Starlink', 'OneWeb', 'GPS', 'GLONASS', 'Galileo', 'BeiDou', 'Telstar', 'Intelsat'];
    const name = `${names[Math.floor(Math.random() * names.length)]}-${Math.floor(Math.random() * 10000)}`;

    return {
        name: name,
        altitude: 200 + Math.random() * 35800,
        inclination: Math.random() * 180,
        speed: 3 + Math.random() * 5,
        status: 'ACTIVE'
    };
}

export default function () {
    group('Бизнес-сценарий: проверка и управление спутниками', function () {

        group('Получение списка спутников', function () {
            const getAllResponse = http.get(BASE_URL, {
                tags: { name: 'GetAllSatellites' },
            });

            check(getAllResponse, {
                'status is 200': (r) => r.status === 200,
                'response has data': (r) => r.json().length > 0,
            });

            errorRate.add(getAllResponse.status !== 200);
            getSatellitesDuration.add(getAllResponse.timings.duration);
        });

        sleep(1);

        group('Проверка порядка спутников', function () {
            const sortParams = ['altitude', 'speed', 'inclination', 'name'];
            const randomSort = sortParams[Math.floor(Math.random() * sortParams.length)];

            const checkOrderResponse = http.get(`${BASE_URL}/check-order?sortBy=${randomSort}`, {
                tags: { name: 'CheckOrder' },
            });

            check(checkOrderResponse, {
                'check-order status is 200': (r) => r.status === 200,
                'has ordered field': (r) => r.json().hasOwnProperty('ordered'),
                'has orderType field': (r) => r.json().hasOwnProperty('orderType'),
            });

            errorRate.add(checkOrderResponse.status !== 200);
            checkOrderDuration.add(checkOrderResponse.timings.duration);

            if (checkOrderResponse.status === 200) {
                successfulChecks.add(1);
            }
        });

        sleep(1);

        group('Создание нового спутника', function () {
            const satelliteData = generateSatelliteData();

            const createResponse = http.post(BASE_URL, JSON.stringify(satelliteData), {
                headers: { 'Content-Type': 'application/json' },
                tags: { name: 'CreateSatellite' },
            });

            check(createResponse, {
                'create status is 201': (r) => r.status === 201,
                'created satellite has id': (r) => r.json().hasOwnProperty('id'),
                'name matches': (r) => r.json().name === satelliteData.name,
            });

            errorRate.add(createResponse.status !== 201);
            createSatelliteDuration.add(createResponse.timings.duration);
        });

        sleep(2);

        group('Получение спутника по ID', function () {
            const satelliteId = Math.floor(Math.random() * 5) + 1;

            const getByIdResponse = http.get(`${BASE_URL}/${satelliteId}`, {
                tags: { name: 'GetSatelliteById' },
            });

            check(getByIdResponse, {
                'get-by-id status is 200 or 404': (r) => r.status === 200 || r.status === 404,
            });
        });

        sleep(1);

        group('Обновление спутника', function () {
            const satelliteId = Math.floor(Math.random() * 5) + 1;
            const updateData = {
                name: `Updated-Satellite-${Math.floor(Math.random() * 1000)}`,
                altitude: 400 + Math.random() * 1000,
                inclination: Math.random() * 90,
                speed: 5 + Math.random() * 3,
                status: 'ACTIVE'
            };

            const updateResponse = http.put(`${BASE_URL}/${satelliteId}`,
                JSON.stringify(updateData), {
                headers: { 'Content-Type': 'application/json' },
                tags: { name: 'UpdateSatellite' },
            });

            check(updateResponse, {
                'update status is 200 or 404': (r) => r.status === 200 || r.status === 404,
            });
        });

        sleep(1);
    });
}

export function handleSummary(data) {
    return {
        "load-tests/reports/summary.html": htmlReport(data),
        "load-tests/reports/summary.json": JSON.stringify(data, null, 2),
    };
}