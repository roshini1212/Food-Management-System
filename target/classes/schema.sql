PRAGMA foreign_keys = ON;
-- statement-break
CREATE TABLE IF NOT EXISTS restaurant_accounts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    restaurant_name TEXT NOT NULL,
    owner_name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);
-- statement-break
CREATE TABLE IF NOT EXISTS restaurant_suppliers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    restaurant_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    contact_person TEXT NOT NULL,
    email TEXT,
    phone TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (restaurant_id) REFERENCES restaurant_accounts(id) ON DELETE CASCADE
);
-- statement-break
CREATE UNIQUE INDEX IF NOT EXISTS idx_restaurant_suppliers_name
ON restaurant_suppliers(restaurant_id, name);
-- statement-break
CREATE TABLE IF NOT EXISTS restaurant_food_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    restaurant_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    category TEXT NOT NULL,
    supplier_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity >= 0),
    reorder_level INTEGER NOT NULL CHECK (reorder_level >= 0),
    unit TEXT NOT NULL,
    expiry_date TEXT NOT NULL,
    batch_code TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (restaurant_id) REFERENCES restaurant_accounts(id) ON DELETE CASCADE,
    FOREIGN KEY (supplier_id) REFERENCES restaurant_suppliers(id) ON DELETE RESTRICT
);
-- statement-break
CREATE UNIQUE INDEX IF NOT EXISTS idx_restaurant_food_items_batch
ON restaurant_food_items(restaurant_id, batch_code);
-- statement-break
CREATE TABLE IF NOT EXISTS restaurant_alert_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    restaurant_id INTEGER NOT NULL,
    food_item_id INTEGER NOT NULL,
    alert_type TEXT NOT NULL,
    message TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'OPEN',
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (restaurant_id) REFERENCES restaurant_accounts(id) ON DELETE CASCADE,
    FOREIGN KEY (food_item_id) REFERENCES restaurant_food_items(id) ON DELETE CASCADE
);
-- statement-break
CREATE TRIGGER IF NOT EXISTS trg_restaurant_food_expiry_on_insert
AFTER INSERT ON restaurant_food_items
WHEN date(NEW.expiry_date) <= date('now', '+7 day')
BEGIN
    INSERT INTO restaurant_alert_logs(restaurant_id, food_item_id, alert_type, message)
    VALUES (
        NEW.restaurant_id,
        NEW.id,
        'EXPIRY',
        'Food item ' || NEW.name || ' is expiring on ' || NEW.expiry_date
    );
END;
-- statement-break
CREATE TRIGGER IF NOT EXISTS trg_restaurant_food_expiry_on_update
AFTER UPDATE OF expiry_date ON restaurant_food_items
WHEN date(NEW.expiry_date) <= date('now', '+7 day')
BEGIN
    INSERT INTO restaurant_alert_logs(restaurant_id, food_item_id, alert_type, message)
    VALUES (
        NEW.restaurant_id,
        NEW.id,
        'EXPIRY',
        'Food item ' || NEW.name || ' is expiring on ' || NEW.expiry_date
    );
END;
-- statement-break
CREATE TRIGGER IF NOT EXISTS trg_restaurant_low_stock_on_update
AFTER UPDATE OF quantity ON restaurant_food_items
WHEN NEW.quantity <= NEW.reorder_level
BEGIN
    INSERT INTO restaurant_alert_logs(restaurant_id, food_item_id, alert_type, message)
    VALUES (
        NEW.restaurant_id,
        NEW.id,
        'LOW_STOCK',
        'Food item ' || NEW.name || ' reached reorder threshold at quantity ' || NEW.quantity
    );
END;
