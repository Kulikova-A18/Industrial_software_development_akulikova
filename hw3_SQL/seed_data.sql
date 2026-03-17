-- seed_data.sql

-- Generation of 50 clients
INSERT INTO clients (full_name, phone, email, created_at)
SELECT
    'Client ' || g.id,
    '+1555' || LPAD((100000 + g.id * 7)::TEXT, 7, '0'),
    'client' || g.id || '@example.com',
    '2020-01-01'::DATE + (random() * 2000)::INT
FROM generate_series(1, 50) AS g(id);

-- Bill generation (1-3 per client)
INSERT INTO accounts (client_id, account_type, currency, opened_at, status)
SELECT
    c.client_id,
    (ARRAY['debit', 'savings', 'credit'])[1 + floor(random() * 3)::INT],
    (ARRAY['USD', 'EUR', 'RUB'])[1 + floor(random() * 3)::INT],
    c.created_at + (random() * 1000)::INT,
    (ARRAY['active', 'blocked', 'closed'])[1 + floor(random() * 3)::INT]
FROM clients c
JOIN generate_series(1, 1 + floor(random() * 2)::INT) AS s(n) ON true;

-- Card generation (0-2 to an active account)
INSERT INTO cards (account_id, card_type, issued_at, expires_at, status)
SELECT
    a.account_id,
    (ARRAY['debit', 'credit', 'virtual'])[1 + floor(random() * 3)::INT],
    a.opened_at,
    a.opened_at + INTERVAL '4 years',
    CASE WHEN random() < 0.1 THEN 'expired' ELSE 'active' END
FROM accounts a
JOIN generate_series(1, floor(random() * 2)::INT) AS s(n) ON true
WHERE a.status = 'active';

-- Transaction generation (up to 20 per account)
INSERT INTO transactions (account_id, txn_type, amount, txn_date, description)
SELECT
    a.account_id,
    (ARRAY['deposit', 'withdrawal', 'transfer_in', 'transfer_out', 'fee'])[1 + floor(random() * 5)::INT],
    (random() * 10000 + 10)::NUMERIC(14,2),
    a.opened_at + (random() * 1000)::INT + (random() * 86400)::INT * '1 second'::INTERVAL,
    'Auto-generated transaction'
FROM accounts a
JOIN generate_series(1, floor(random() * 20)::INT) AS s(n) ON true;

-- Credit generation (for 40% of clients, 1-2 loans)
INSERT INTO loans (client_id, principal, interest_rate, start_date, end_date, status)
SELECT
    c.client_id,
    (1000 + random() * 50000)::NUMERIC(14,2),
    (3.0 + random() * 12.0)::NUMERIC(5,2),
    '2024-01-01'::DATE + (random() * 700)::INT,
    '2024-01-01'::DATE + (random() * 700)::INT + 365,
    (ARRAY['active', 'overdue', 'closed'])[1 + floor(random() * 3)::INT]
FROM clients c
JOIN generate_series(1, 1 + floor(random())::INT) AS s(n) ON true
WHERE random() < 0.4;

-- Generation of loan payments
INSERT INTO loan_payments (loan_id, amount, payment_date, status)
SELECT
    l.loan_id,
    (l.principal * (0.05 + random() * 0.2))::NUMERIC(14,2),
    l.start_date + (random() * 365)::INT,
    (ARRAY['success', 'failed'])[1 + floor(random() * 2)::INT]
FROM loans l
JOIN generate_series(1, 1 + floor(random() * 3)::INT) AS s(n) ON true
WHERE l.status != 'closed';