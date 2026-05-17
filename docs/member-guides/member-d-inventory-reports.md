# Member D Guide - Inventory and Reports Module

## Your Claimed Area

Consumable inventory, consumable type creation, stock transaction workflow, and management reports.

## Files You Should Understand

- `src/main/java/edu/ucd/comp2013j/lab/dao/InventoryDao.java`
- `src/main/java/edu/ucd/comp2013j/lab/service/InventoryService.java`
- `src/main/java/edu/ucd/comp2013j/lab/dao/ReportDao.java`
- `src/main/resources/web/index.html`
- `src/main/resources/web/app.js`
- `src/main/java/edu/ucd/comp2013j/lab/model/Consumable.java`
- `src/main/java/edu/ucd/comp2013j/lab/model/ReportRow.java`

## What You Need To Understand

Inventory is handled using two tables:

- `consumables`: current stock quantity for each lab item.
- `stock_transactions`: history of stock increases and decreases.

When stock changes, `InventoryService.changeStock`:

1. starts a transaction;
2. locks the consumable row with `FOR UPDATE`;
3. calculates the new quantity;
4. rejects the update if the result would be negative;
5. updates `consumables.quantity`;
6. inserts a row into `stock_transactions`;
7. commits.

Reports are read-only. `ReportDao.labUsage()` reads from `v_lab_usage_report`. `ReportDao.equipmentStatusSummary()` groups equipment by status.

Adding a consumable type is different from changing stock. Adding creates a new row in `consumables`; changing stock updates an existing row and writes a `stock_transactions` record.

## Manual Things You Should Do

- Login as `tech`.
- Login as `admin` and add a new consumable item type.
- Change a stock item by `-1` and check the new quantity.
- Try `-999` and confirm the system rejects it.
- Open Reports tab and explain both tables.
- Read the SQL view `v_lab_usage_report`.

## Possible Viva Questions

Q: Why not only keep current stock quantity?

A: Because we need history. The transaction table records each change, who made it, and the reason.

Q: How do you prevent negative stock?

A: The service locks the row, reads current quantity, calculates the new value, and rejects it before update if it is below zero. The table also has a check constraint.

Q: What report queries did you implement?

A: One report counts reservations by lab. Another groups equipment by status and counts high-risk equipment and open tickets.

Q: What is the purpose of `reorder_level`?

A: It lets the UI mark low stock items when quantity is less than or equal to the reorder level.

Q: Why does a new consumable need a reorder level?

A: The system uses it to decide whether the item should be shown as low stock.

Q: What could be improved?

A: The system could automatically create purchase requests when stock is low.
