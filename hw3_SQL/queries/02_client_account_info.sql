SELECT c.full_name, a.account_type, a.currency, a.status
FROM clients c
JOIN accounts a ON c.client_id = a.client_id;