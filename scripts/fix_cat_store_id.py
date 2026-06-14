import psycopg2

conn = psycopg2.connect(
    host='82.157.130.254',
    port=5432,
    database='postgres',
    user='admin_user',
    password='123456789'
)
cur = conn.cursor()

# 1. 加 store_id 字段（如果不存在）
cur.execute("""
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'cat_profiles' AND column_name = 'store_id'
""")
if not cur.fetchone():
    cur.execute("ALTER TABLE cat_profiles ADD COLUMN store_id INT")
    print("Added store_id column")
else:
    print("store_id column already exists")

# 2. 给现有猫分配 store_id：前面都给1，最后一只给2
cur.execute("SELECT cat_id, name FROM cat_profiles ORDER BY cat_id")
cats = cur.fetchall()
print(f"Found {len(cats)} cats")

for i, (cat_id, name) in enumerate(cats):
    store_id = 1 if i < len(cats) - 1 else 2
    cur.execute(
        "UPDATE cat_profiles SET store_id = %s WHERE cat_id = %s",
        (store_id, cat_id)
    )
    print(f"  cat_id={cat_id}, name={name} -> store_id={store_id}")

conn.commit()

# 3. 验证
cur.execute("SELECT cat_id, name, store_id FROM cat_profiles ORDER BY cat_id")
print("\nFinal result:")
for row in cur.fetchall():
    print(f"  {row}")

conn.close()
print("Done.")
