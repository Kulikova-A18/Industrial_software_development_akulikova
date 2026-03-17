SELECT l.loan_id, l.client_id, l.principal,
       COALESCE(SUM(lp.amount), 0) AS paid_amount
FROM loans l
LEFT JOIN loan_payments lp ON l.loan_id = lp.loan_id AND lp.status = 'success'
GROUP BY l.loan_id, l.client_id, l.principal
HAVING COALESCE(SUM(lp.amount), 0) < l.principal * 0.5;