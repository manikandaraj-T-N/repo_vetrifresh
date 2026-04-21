create database vetrifresh;
use vetrifresh;
show tables;

select * from users;
select * from products;
select * from categories;
select * from cart_items;
select * from wishlists;
select * from blogs;

TRUNCATE TABLE categories;
TRUNCATE TABLE wishlists;


INSERT INTO categories (name, slug, description, image_url, is_active) VALUES
('Fresh Fruit',      'fresh-fruit',      'Fresh seasonal fruits',        '/images/bag.jpg',        true),
('Fresh Vegetables', 'fresh-vegetables', 'Farm fresh vegetables',        '/images/vegetables.jpg', true),
('Meat & Fish',      'meat-fish',        'Fresh meat and seafood',       '/images/meat.jpg',       true),
('Snacks',           'snacks',           'Chips, biscuits and snacks',   '/images/snacks.jpg',     true),
('Beverages',        'beverages',        'Drinks and beverages',         '/images/beverages.jpg',  true),
('Beauty & Health',  'beauty-health',    'Beauty and health products',   '/images/beauty.jpg',     true),
('Bread & Bakery',   'bread-bakery',     'Fresh bread and baked goods',  '/images/bread.jpg',      true),
('Baking Needs',     'baking-needs',     'Flour, sugar, baking items',   '/images/baking.jpg',     true),
('Cooking',          'cooking',          'Cooking essentials',           '/images/cooking.jpg',    true),
('Diabetic Food',    'diabetic-food',    'Sugar-free diabetic products', '/images/Diabetic.jpg',   true),
('Dish Detergents',  'dish-detergents',  'Dishwash and cleaning items',  '/images/dish.jpg',       true),
('Oil',              'oil',              'Cooking oils',                 '/images/oil.jpg',        true);



SET FOREIGN_KEY_CHECKS = 0;
truncate users;


ALTER TABLE users 
MODIFY COLUMN role ENUM('CUSTOMER', 'ADMIN', 'USER') NOT NULL DEFAULT 'USER';

UPDATE users SET role='ADMIN' WHERE email='admin@vetrifresh.com';

UPDATE users SET role = 'USER' WHERE role = 'CUSTOMER';

ALTER TABLE users 
MODIFY COLUMN role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER';

SELECT email, role FROM users;


SELECT id, name, price, original_price FROM products;
-- Check if original_price is actually saved
SELECT id, name, price, original_price FROM products WHERE original_price IS NOT NULL;

$2a$12$jFNxXMH.WHiLLLMPFXb6V.Rjij60XiftG1J4sp6vGBFWpZ9CTjIla
