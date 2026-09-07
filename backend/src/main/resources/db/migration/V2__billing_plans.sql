INSERT INTO billing_plans (code, name, price_monthly, features)
SELECT 'STARTER', 'Starter', 99000, 'Up to 50 assets|Email alerts|Community support'
WHERE NOT EXISTS (SELECT 1 FROM billing_plans WHERE code = 'STARTER');

INSERT INTO billing_plans (code, name, price_monthly, features)
SELECT 'GROWTH', 'Growth', 249000, 'Up to 500 assets|Investment optimizer|What-if analysis|Priority support'
WHERE NOT EXISTS (SELECT 1 FROM billing_plans WHERE code = 'GROWTH');

INSERT INTO billing_plans (code, name, price_monthly, features)
SELECT 'ENTERPRISE', 'Enterprise', 799000, 'Unlimited assets|SSO|Dedicated CSM|Custom reports'
WHERE NOT EXISTS (SELECT 1 FROM billing_plans WHERE code = 'ENTERPRISE');
