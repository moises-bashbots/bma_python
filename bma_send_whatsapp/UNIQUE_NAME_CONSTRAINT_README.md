# Unique Name Constraint - contato_whatsapp Table

**Date**: 2026-01-21  
**Status**: ✅ Implemented and Active

---

## 📋 Overview

The `contato_whatsapp` table now has a **UNIQUE constraint** on the `name` column to prevent duplicate contact names in the database.

---

## 🎯 What Changed

### **Database Changes**

1. **Cleaned up existing duplicates**
   - Deleted 10 duplicate records (kept oldest record for each name)
   - Updated 7 records with NULL/empty names to `"Unknown - {phone}"`

2. **Added UNIQUE constraint**
   - Index name: `idx_unique_name`
   - Column: `name`
   - Type: UNIQUE BTREE index

3. **Updated column requirements**
   - `name` column is now effectively required (no NULL/empty values allowed)
   - Empty names are automatically converted to `"Unknown - {phone}"`

### **Code Changes**

1. **Updated `get_chats_sync.py`**
   - `map_zapi_to_db()`: Ensures names are never NULL or empty
   - `sync_chat_to_db()`: Handles duplicate name conflicts gracefully
   - If a duplicate name is detected, appends phone number: `"{name} ({phone})"`

2. **Updated `database/bma_models.py`**
   - `ContatoWhatsapp.name`: Added `unique=True` and `nullable=False`

---

## 🔧 How It Works

### **During Sync (get_chats_sync.py)**

When syncing chats from Z-API:

1. **Empty name handling**:
   ```python
   if not name:
       name = f"Unknown - {phone}"
   ```

2. **Duplicate name handling**:
   - If a name already exists for a different phone number
   - The system automatically appends the phone: `"{name} ({phone})"`
   - Example: `"CCB Friall"` → `"CCB Friall (120363334038898156-group)"`

3. **Update existing contacts**:
   - Contacts are matched by `phone` (primary unique key)
   - Name can be updated if the phone already exists

---

## 📊 Migration Results

### **Before Migration**
- Total contacts: 455
- Duplicate names: 11 (including 7 empty names)

### **After Migration**
- Total contacts: 445 (10 duplicates removed)
- Duplicate names: 0
- Empty names: 0 (converted to "Unknown - {phone}")

### **Records Deleted**
The following duplicate records were removed (kept oldest):
- ALPA X BMA (1 duplicate)
- BMA x Banco Inter (1 duplicate)
- BRASSUCO X BMA (1 duplicate)
- CCB Friall (1 duplicate)
- Frigovale X BMA (1 duplicate)
- Kuky X BMA (1 duplicate)
- Pasquini x BMA (1 duplicate)
- Pietrobon X BMA (1 duplicate)
- Proquitec X BMA (1 duplicate)
- TREVO x BMA (1 duplicate)

---

## ✅ Verification

### **Check UNIQUE constraint exists**
```sql
SHOW INDEX FROM contato_whatsapp WHERE Key_name = 'idx_unique_name';
```

### **Verify no duplicates**
```sql
SELECT name, COUNT(*) as count 
FROM contato_whatsapp 
GROUP BY name 
HAVING count > 1;
```
Should return no rows.

### **Check for NULL/empty names**
```sql
SELECT * FROM contato_whatsapp 
WHERE name IS NULL OR name = '';
```
Should return no rows.

---

## 🚀 Usage

### **Running the Sync**

The sync script now handles duplicates automatically:

```bash
cd /home/robot/Deploy/bma_send_whatsapp
./get_chats_sync.pyz
```

Or from source:
```bash
cd /home/robot/Dev/bma_python
python bma_send_whatsapp/get_chats_sync.py
```

### **What Happens During Sync**

1. **New contact with unique name**: Inserted normally
2. **Existing contact (same phone)**: Updated with new data
3. **New contact with duplicate name**: Name modified to `"{name} ({phone})"`
4. **Contact with empty name**: Name set to `"Unknown - {phone}"`

---

## 📁 Related Files

### **Migration**
- `database/migrations/add_unique_constraint_contato_whatsapp_name.sql`

### **Code**
- `bma_send_whatsapp/get_chats_sync.py` (updated)
- `database/bma_models.py` (updated)

### **Documentation**
- This file: `bma_send_whatsapp/UNIQUE_NAME_CONSTRAINT_README.md`

---

## 🔍 Testing

### **Test 1: Verify constraint prevents duplicates**
```sql
-- This should fail with duplicate key error
INSERT INTO contato_whatsapp (phone, name) 
VALUES ('test-phone-123', 'CCB Friall');
```

### **Test 2: Verify updates work**
```sql
-- This should work (updating existing phone)
UPDATE contato_whatsapp 
SET messagesUnread = 5 
WHERE phone = '120363420074766333-group';
```

### **Test 3: Run sync script**
```bash
python bma_send_whatsapp/get_chats_sync.py
```
Should complete without errors and handle any duplicate names gracefully.

---

## 🎉 Benefits

✅ **Data Integrity**: No duplicate contact names in database  
✅ **Automatic Handling**: Duplicates are handled gracefully during sync  
✅ **No Manual Intervention**: System automatically resolves conflicts  
✅ **Backward Compatible**: Existing code continues to work  
✅ **Clear Audit Trail**: Modified names show the phone number  

---

## 📝 Notes

- The `phone` column remains the primary unique identifier
- The `name` column is now also unique to prevent confusion
- Empty/NULL names are not allowed and are auto-converted
- Duplicate names from Z-API are automatically disambiguated
- The oldest record is kept when cleaning up historical duplicates

---

**✓ Constraint is active and working!**

