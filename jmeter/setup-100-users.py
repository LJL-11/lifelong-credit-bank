"""
一次性脚本：创建测试用户 + 并发登录获取Token + 重置秒杀库存
运行: python jmeter/setup-100-users.py
"""
import hashlib, os, random, requests, pymysql
from concurrent.futures import ThreadPoolExecutor, as_completed

DB_CONFIG = {
    "host": "localhost", "port": 3306, "user": "root", "password": "123456",
    "database": "credit_bank", "charset": "utf8mb4"
}
API_BASE = "http://localhost:8080"
PASSWORD = "123456"
USER_COUNT = 1000
FLASH_SALE_ID = 3
FLASH_STOCK = 100
CREDITS = 99999
BATCH = 200  # 分批插入大小

def make_password(raw_password):
    salt_bytes = os.urandom(16)
    salt_hex = salt_bytes.hex()
    h = hashlib.sha256()
    h.update(salt_hex.encode('utf-8'))
    h.update(raw_password.encode('utf-8'))
    return salt_hex + ":" + h.hexdigest()

# ==================== 1. 数据库操作 ====================
conn = pymysql.connect(**DB_CONFIG)
cur = conn.cursor()

# 清理旧数据
cur.execute("DELETE FROM credit_account WHERE learner_id IN (SELECT id FROM learner WHERE username LIKE 'test%')")
cur.execute("DELETE FROM flash_sale_record WHERE flash_sale_id = %s", (FLASH_SALE_ID,))
cur.execute("DELETE FROM credit_order WHERE remark LIKE '%%秒杀%%' AND learner_id IN (SELECT id FROM learner WHERE username LIKE 'test%')")
cur.execute("DELETE FROM learner WHERE username LIKE 'test%'")
conn.commit()
print("旧数据已清理")

# 分批插入学员
sql = """INSERT INTO learner (username, real_name, phone, email, education_level, status, password, role, institution_id, created_at, updated_at, deleted)
         VALUES (%s, %s, %s, %s, '本科', 'ACTIVE', %s, 'STUDENT', %s, NOW(), NOW(), 0)"""
total = 0
for start in range(0, USER_COUNT, BATCH):
    batch = [
        (f"test{i:04d}", f"测试{i:04d}", f"139{random.randint(10000000, 99999999)}",
         f"test{i:04d}@test.com", make_password(PASSWORD), (i % 3) + 1)
        for i in range(start + 1, min(start + BATCH, USER_COUNT) + 1)
    ]
    cur.executemany(sql, batch)
    conn.commit()
    total += cur.rowcount
    print(f"  学员 {total}/{USER_COUNT}")

# 获取学员ID
cur.execute("SELECT id, username FROM learner WHERE username LIKE 'test%' ORDER BY id")
learner_rows = cur.fetchall()
print(f"学员总数: {len(learner_rows)}")

# 分批创建积分账户
sql2 = """INSERT INTO credit_account (learner_id, available_credits, total_credits, frozen_credits, account_status, created_at, updated_at, deleted)
          VALUES (%s, %s, %s, 0, 'ACTIVE', NOW(), NOW(), 0)
          ON DUPLICATE KEY UPDATE available_credits=%s, total_credits=%s, updated_at=NOW()"""
for i in range(0, len(learner_rows), BATCH):
    chunk = learner_rows[i:i+BATCH]
    cur.executemany(sql2, [(lid, CREDITS, CREDITS, CREDITS, CREDITS) for lid, _ in chunk])
    conn.commit()
print(f"积分账户: {len(learner_rows)}")

# 重置秒杀
cur.execute("UPDATE flash_sale SET stock=%s, status='ACTIVE', end_time=DATE_ADD(NOW(), INTERVAL 24 HOUR) WHERE id=%s",
            (FLASH_STOCK, FLASH_SALE_ID))
conn.commit()
print(f"flash_sale {FLASH_SALE_ID} 库存={FLASH_STOCK}")
conn.close()

# ==================== 2. 并发登录 ====================
def login_one(i):
    username = f"test{i:04d}"  # 4位！和数据库一致
    try:
        resp = requests.post(f"{API_BASE}/api/auth/login",
            json={"username": username, "password": PASSWORD}, timeout=15)
        data = resp.json()
        if data.get("code") == 200:
            return (i, data["data"]["token"])
    except:
        pass
    return (i, None)

print(f"\n并发登录 {USER_COUNT} 用户（10线程）...")
tokens_dict = {}
with ThreadPoolExecutor(max_workers=10) as pool:
    futures = {pool.submit(login_one, i): i for i in range(1, USER_COUNT + 1)}
    done = 0
    for f in as_completed(futures):
        i, token = f.result()
        if token:
            tokens_dict[i] = token
        done += 1
        if done % 200 == 0:
            print(f"  {done}/{USER_COUNT} (成功{len(tokens_dict)})")

# 按序号写入
script_dir = os.path.dirname(os.path.abspath(__file__))
token_file = os.path.join(script_dir, "tokens.txt")
with open(token_file, "w") as f:
    for i in sorted(tokens_dict.keys()):
        f.write(tokens_dict[i] + "\n")

print(f"Token: {len(tokens_dict)} 个 → {token_file}")

# ==================== 3. Redis ====================
try:
    import redis
    r = redis.Redis(host='192.168.116.130', port=6379, db=4, socket_connect_timeout=3)
    r.set(f'flash:stock:{FLASH_SALE_ID}', FLASH_STOCK)
    r.delete(f'flash:order:{FLASH_SALE_ID}')
    print(f"Redis: flash:stock:{FLASH_SALE_ID}={FLASH_STOCK}")
except Exception as e:
    print(f"Redis失败（手动执行）: {e}")
    print(f"  SET flash:stock:{FLASH_SALE_ID} {FLASH_STOCK}")
    print(f"  DEL flash:order:{FLASH_SALE_ID}")
