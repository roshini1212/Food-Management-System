#!/usr/bin/env python3
import json
import sqlite3
from pathlib import Path


BASE_DIR = Path(__file__).resolve().parents[1]
DB_PATH = BASE_DIR / "food-management.db"
OUTPUT_PATH = BASE_DIR / "etl" / "output" / "alert_report.json"


def fetch_report(connection: sqlite3.Connection) -> dict:
    open_alerts = connection.execute(
        """
        SELECT id, food_item_id, alert_type, message, status, created_at
        FROM alert_logs
        WHERE status = 'OPEN'
        ORDER BY created_at DESC
        """
    ).fetchall()

    expiring_items = connection.execute(
        """
        SELECT name, quantity, unit, expiry_date
        FROM food_items
        WHERE date(expiry_date) <= date('now', '+7 day')
        ORDER BY expiry_date ASC
        """
    ).fetchall()

    return {
        "open_alert_count": len(open_alerts),
        "alerts": [
            {
                "id": row[0],
                "food_item_id": row[1],
                "alert_type": row[2],
                "message": row[3],
                "status": row[4],
                "created_at": row[5],
            }
            for row in open_alerts
        ],
        "expiring_items": [
            {
                "name": row[0],
                "quantity": row[1],
                "unit": row[2],
                "expiry_date": row[3],
            }
            for row in expiring_items
        ],
    }


def main() -> None:
    connection = sqlite3.connect(DB_PATH)
    try:
        report = fetch_report(connection)
    finally:
        connection.close()

    OUTPUT_PATH.write_text(json.dumps(report, indent=2), encoding="utf-8")
    print(f"Alert report written to {OUTPUT_PATH}")
    print(json.dumps(report, indent=2))


if __name__ == "__main__":
    main()
