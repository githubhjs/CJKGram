"""Inject build credentials in CI only; never print or commit generated credentials."""
import base64
import os
import re
from pathlib import Path

root = Path(__file__).resolve().parents[2]
app_id = os.environ['TELEGRAM_API_ID']
app_hash = os.environ['TELEGRAM_API_HASH']
assert app_id.isdigit() and int(app_id) > 0
assert re.fullmatch(r'[0-9a-fA-F]{32}', app_hash)
p = root / 'TMessagesProj/src/main/java/org/telegram/messenger/BuildVars.java'
s = re.sub(r'public static int APP_ID = .*?;', f'public static int APP_ID = {app_id};', p.read_text())
s = re.sub(r'public static String APP_HASH = .*?;', f'public static String APP_HASH = "{app_hash}";', s)
p.write_text(s)
key = root / 'TMessagesProj/config/release.keystore'
key.write_bytes(base64.b64decode(os.environ['SIGNING_KEY_BASE64'], validate=True))
p = root / 'gradle.properties'
s = p.read_text()
for property_name, env_name in [('RELEASE_STORE_PASSWORD', 'KEY_STORE_PASSWORD'),
                                ('RELEASE_KEY_PASSWORD', 'KEY_PASSWORD'),
                                ('RELEASE_KEY_ALIAS', 'KEY_ALIAS')]:
    value = os.environ[env_name]
    assert re.fullmatch(r'[A-Za-z0-9_-]+', value)
    s = re.sub(rf'^{property_name}=.*$', f'{property_name}={value}', s, flags=re.M)
p.write_text(s)
