#!/usr/bin/env python3
import csv
import sqlite3
from pathlib import Path


BASE_DIR = Path(__file__).resolve().parents[1]
DB_PATH = BASE_DIR / "food-management.db"
SCHEMA_PATH = BASE_DIR / "sql" / "schema.sql"
SUPPLIERS_CSV = BASE_DIR / "etl" / "input" / "suppliers.csv"
FOOD_ITEMS_CSV = BASE_DIR / "etl" / "input" / "food_items.csv"


def apply_schema(connection: sqlite3.Connection) -> None:
    raw_sql = SCHEMA_PATH.read_text(encoding="utf-8")
    statements = [part.strip() for part in raw_sql.split("-- statement-break") if part.strip()]
    for statement in statements:
        connection.execute(statement)
    connection.commit()


def load_suppliers(connection: sqlite3.Connection) -> None:
    with SUPPLIERS_CSV.open(newline="", encoding="utf-8") as csv_file:
        reader = csv.DictReader(csv_file)
        for row in reader:
            connection.execute(
                """
                INSERT INTO suppliers(name, contact_person, email, phone)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(name) DO UPDATE SET
                    contact_person = excluded.contact_person,
                    email = excluded.email,
                    phone = excluded.phone
                """,
                (row["name"], row["contact_person"], row["email"], row["phone"]),
            )
    connection.commit()


def load_food_items(connection: sqlite3.Connection) -> None:
    with FOOD_ITEMS_CSV.open(newline="", encoding="utf-8") as csv_file:
        reader = csv.DictReader(csv_file)
        for row in reader:
            supplier_id = connection.execute(
                "SELECT id FROM suppliers WHERE name = ?",
                (row["supplier_name"],),
            ).fetchone()

            if supplier_id is None:
                raise ValueError(f"Supplier not found for row: {row}")

            payload = (
                row["name"],
                row["category"],
                supplier_id[0],
                int(row["quantity"]),
                int(row["reorder_level"]),
                row["unit"],
                row["expiry_date"],
                row["batch_code"],
            )

            existing_item = connection.execute(
                "SELECT id FROM food_items WHERE batch_code = ?",
                (row["batch_code"],),
            ).fetchone()

            if existing_item:
                connection.execute(
                    """
                    UPDATE food_items
                    SET name = ?, category = ?, supplier_id = ?, quantity = ?, reorder_level = ?, unit = ?, expiry_date = ?
                    WHERE batch_code = ?
                    """,
                    payload[:-1] + (row["batch_code"],),
                )
            else:
                connection.execute(
                    """
                    INSERT INTO food_items(name, category, supplier_id, quantity, reorder_level, unit, expiry_date, batch_code)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    payload,
                )
    connection.commit()


def main() -> None:
    connection = sqlite3.connect(DB_PATH)
    try:
        apply_schema(connection)
        load_suppliers(connection)
        load_food_items(connection)
        print(f"ETL completed successfully. Database updated at {DB_PATH}")
    finally:
        connection.close()


if __name__ == "__main__":
    main()
