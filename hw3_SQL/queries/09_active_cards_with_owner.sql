SELECT c.full_name, cr.account_id, cr.expires_at
FROM cards cr
JOIN accounts a ON cr.account_id = a.account_id
JOIN clients c ON a.client_id = c.client_id
WHERE cr.status = 'active';