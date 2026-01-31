-- Insert / upsert Authorities (H2)
MERGE INTO authorities (authority_id, authority) KEY (authority_id) VALUES
(1, 'USER_READ'),
(2, 'USER_WRITE'),
(3, 'USER_DELETE'),
(4, 'PRODUCT_READ'),
(5, 'PRODUCT_WRITE'),
(6, 'PRODUCT_DELETE'),
(7, 'ORDER_READ'),
(8, 'ORDER_WRITE'),
(9, 'ORDER_DELETE'),
(10, 'ADMIN_DASHBOARD_ACCESS'),
(11, 'SYSTEM_CONFIGURATION');


-- Insert / upsert Roles (H2)
-- Keep "name" quoted in case your schema created it as a quoted identifier.
MERGE INTO roles (role_id, name) KEY (role_id) VALUES
(1, 'ROLE_USER'),
(2, 'ROLE_ADMIN'),
(3, 'ROLE_MANAGER');


-- Insert / upsert Role Authorities (H2)
MERGE INTO role_authorities (role_id, authority_id) KEY (role_id, authority_id) VALUES
(1, 1),
(1, 4),
(1, 7),
(2, 1),
(2, 2),
(2, 3),
(2, 4),
(2, 5),
(2, 6),
(2, 7),
(2, 8),
(2, 9),
(2, 10),
(2, 11),
(3, 1),
(3, 4),
(3, 7),
(3, 10),
(3, 11);