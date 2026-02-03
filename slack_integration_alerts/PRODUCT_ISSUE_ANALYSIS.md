# Product Issue Analysis - "SEM PRODUTO" Root Cause

## 🔍 Investigation Summary

**Date:** 2026-02-03  
**Issue:** Dashboard showing "SEM PRODUTO" for 21 proposals  
**Status:** ✅ ROOT CAUSE IDENTIFIED

---

## 🎯 Root Cause Found

The issue is **NOT** that `id_produto` is NULL. The issue is a **BROKEN JOIN CHAIN** due to **REFERENTIAL INTEGRITY VIOLATION** in the product lookup tables.

### The Problem:

```
APRTitulos.id_produto → ProdutoCedente.Id → ProdutoCedente.IdProdutoAtributo → ProdutoAtributo.Id → Produto.Id
     ✅ 2866                ✅ 2866                ✅ 6 (EXISTS!)                  ❌ NOT FOUND!        ❌ NULL
```

### Specific Examples from SQL Server:

| Proposal | Cedente | id_produto | ProdCed.Id | ProdCed.IdProdAttr | ProdAttr.Id | Produto.Id | Product Name |
|----------|---------|------------|------------|-------------------|-------------|------------|--------------|
| 27 | FRIALTO | 2866 | 2866 | **6** | **NOT FOUND** ❌ | NULL | ❌ BROKEN CHAIN |
| 23 | FRIGOVALE | 202 | 202 | **6** | **NOT FOUND** ❌ | NULL | ❌ BROKEN CHAIN |
| 22 | FRIJOA | 204 | 204 | **6** | **NOT FOUND** ❌ | NULL | ❌ BROKEN CHAIN |

### The Real Issue:

**`ProdutoCedente.IdProdutoAtributo` references non-existent records in `ProdutoAtributo` table!**

- `ProdutoCedente` has 2,367 records with `IdProdutoAtributo` values like: 6, 5, 12, 16, 13, 11, 8, 17, 2, etc.
- `ProdutoAtributo` table **ONLY HAS 2 RECORDS**: Id=3 and Id=4
- **None of the referenced values (6, 5, 12, etc.) exist in ProdutoAtributo!** ❌

---

## 📊 Technical Details

### Database Schema Chain:

1. **APRTitulos** table has `id_produto` column
   - Points to `ProdutoCedente.Id`
   
2. **ProdutoCedente** table has `IdProdutoAtributo` column
   - **THIS IS NULL** for the affected products ❌
   - Should point to `ProdutoAtributo.Id`
   
3. **ProdutoAtributo** table has `IdProduto` column
   - Points to `Produto.Id`
   
4. **Produto** table has `Descritivo` column
   - Contains the product name

### The Break Point:

```sql
-- This join SUCCEEDS
APRTitulos.id_produto = ProdutoCedente.Id  ✅

-- This join FAILS because ProdutoCedente.IdProdutoAtributo references non-existent records
ProdutoCedente.IdProdutoAtributo = ProdutoAtributo.Id  ❌
-- Example: ProdutoCedente.IdProdutoAtributo = 6, but ProdutoAtributo.Id = 6 does NOT EXIST!
```

### Database State:

**ProdutoAtributo table (ONLY 2 records!):**
| Id | IdProduto | Product Name |
|----|-----------|--------------|
| 3  | 6         | CONVENCIONAL PRÉ-IMPR. BMA FIDC |
| 4  | 6         | CONVENCIONAL PRÉ-IMPR. BMA FIDC |

**ProdutoCedente table (2,367 records) references:**
- IdProdutoAtributo = 6 ❌ (NOT FOUND - doesn't exist in ProdutoAtributo)
- IdProdutoAtributo = 5 ❌ (NOT FOUND)
- IdProdutoAtributo = 12 ❌ (NOT FOUND)
- IdProdutoAtributo = 16 ❌ (NOT FOUND)
- IdProdutoAtributo = 13 ❌ (NOT FOUND)
- ... and many more non-existent values

---

## 🔧 Why the Dashboard Shows "SEM PRODUTO"

The monitoring code uses this query:

```python
query = mssql_session.query(
    APRCapa.DATA,
    APRCapa.NUMERO.label('PROPOSTA'),
    APRCapa.CEDENTE,
    sqlfunc.coalesce(Produto.Descritivo, 'SEM PRODUTO').label('PRODUTO'),  # ← Returns 'SEM PRODUTO'
    ...
).outerjoin(
    ProdutoCedente,
    APRTitulos.id_produto == ProdutoCedente.Id  # ✅ This works
).outerjoin(
    ProdutoAtributo,
    ProdutoCedente.IdProdutoAtributo == ProdutoAtributo.Id  # ❌ This fails (NULL)
).outerjoin(
    Produto,
    ProdutoAtributo.IdProduto == Produto.Id  # ❌ Never reached
)
```

Since `Produto.Descritivo` is NULL (join chain broken), `COALESCE` returns `'SEM PRODUTO'`.

---

## ✅ Dashboard is Working Correctly!

The dashboard is **accurately reporting** the data issue:
- ✅ Correctly identifies that product names cannot be resolved
- ✅ Uses "SEM PRODUTO" as a fallback for NULL product names
- ✅ Still tracks all other proposal data correctly

---

## ⚠️ The Real Problem

### Referential Integrity Violation in Source Database:

The `ProdutoCedente` table has **INVALID FOREIGN KEY REFERENCES**:
- `Id` is populated (2866, 202, 204, etc.) ✅
- `IdProdutoAtributo` is populated (6, 5, 12, 16, 13, etc.) ✅
- **BUT** these `IdProdutoAtributo` values **DON'T EXIST** in the `ProdutoAtributo` table! ❌

**Database Integrity Issue:**
- `ProdutoAtributo` table has ONLY 2 records (Id=3, Id=4)
- `ProdutoCedente` references non-existent Ids (6, 5, 12, 16, 13, 11, 8, 17, 2, etc.)
- Foreign key constraint is either **missing** or **not enforced**

This breaks the lookup chain to get the product name.

### Affected Products:

Based on MariaDB tracking, **21 proposals** today are affected:

| id_produto | Proposals Affected | Total Titles | Total Value (R$) |
|------------|-------------------|--------------|------------------|
| 2866 | 1 (FRIALTO) | 10 | 1,641,922.68 |
| 202 | 1 (FRIGOVALE) | 2 | 30,380.03 |
| 204 | 1 (FRIJOA) | 192 | 1,572,933.25 |
| ... | 18 more | 1,006 | 2,340,523.00 |
| **TOTAL** | **21** | **1,210** | **5,585,758.96** |

---

## 💡 Recommendations

### Option 1: Fix Source Data (Recommended) ✅

Fix the `ProdutoCedente` table in SQL Server:

```sql
-- Find broken records
SELECT Id, IdProdutoAtributo
FROM ProdutoCedente
WHERE Id IN (2866, 202, 204, ...)
  AND IdProdutoAtributo IS NULL;

-- Update with correct IdProdutoAtributo values
-- (Need to determine correct mapping from business rules)
UPDATE ProdutoCedente
SET IdProdutoAtributo = <correct_value>
WHERE Id = 2866;
```

### Option 2: Use Alternative Product Lookup

If `ProdutoCedente` is unreliable, check if there's an alternative way to get product names:
- Direct `id_produto` to `Produto` mapping?
- Product name stored elsewhere?
- Business rules to infer product type?

### Option 3: Accept as Business Reality

If these products legitimately don't have attributes:
- Keep "SEM PRODUTO" as a valid category
- Document which `id_produto` values are expected to have NULL attributes
- Use for reporting and analytics

---

## 📋 Action Items

1. **Investigate ProdutoCedente table**
   - Why is `IdProdutoAtributo` NULL for these records?
   - Is this expected or a data quality issue?
   
2. **Check with business users**
   - Are these products supposed to have names?
   - Is "SEM PRODUTO" acceptable for these cases?
   
3. **Database maintenance**
   - If this is a bug, fix the `ProdutoCedente` records
   - Add constraints to prevent NULL `IdProdutoAtributo` in future
   
4. **Documentation**
   - Document which products are expected to have NULL attributes
   - Update business rules if "SEM PRODUTO" is valid

---

## ✅ Conclusion

**The dashboard is working perfectly!** It's accurately showing that these proposals have products with broken lookup chains in the source database.

**This is NOT a monitoring system bug** - it's a **data integrity issue** in the SQL Server database where `ProdutoCedente.IdProdutoAtributo` is NULL.

**Next Steps:**
1. ✅ Confirm with database admin why `IdProdutoAtributo` is NULL
2. ✅ Fix the source data if this is a bug
3. ✅ Or accept "SEM PRODUTO" as valid if this is expected behavior

