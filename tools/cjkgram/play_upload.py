#!/usr/bin/env python3
"""Upload a built APK to an existing CJKGram Play package's internal track."""
import argparse
from google.oauth2 import service_account
from googleapiclient.discovery import build
from googleapiclient.http import MediaFileUpload

parser = argparse.ArgumentParser()
parser.add_argument('--credentials', required=True)
parser.add_argument('--apk', required=True)
parser.add_argument('--package', default='space.hjs.cjkgram')
parser.add_argument('--track', default='internal')
args = parser.parse_args()
creds = service_account.Credentials.from_service_account_file(
    args.credentials, scopes=['https://www.googleapis.com/auth/androidpublisher'])
service = build('androidpublisher', 'v3', credentials=creds, cache_discovery=False)
edit = service.edits().insert(packageName=args.package, body={}).execute()
edit_id = edit['id']
try:
    uploaded = service.edits().apks().upload(
        packageName=args.package, editId=edit_id,
        media_body=MediaFileUpload(args.apk, mimetype='application/vnd.android.package-archive', resumable=True),
    ).execute()
    version_code = str(uploaded['versionCode'])
    service.edits().tracks().update(
        packageName=args.package, editId=edit_id, track=args.track,
        body={'track': args.track, 'releases': [{'versionCodes': [version_code], 'status': 'completed', 'name': 'CJKGram 0.1.0 preview'}]},
    ).execute()
    result = service.edits().commit(packageName=args.package, editId=edit_id).execute()
    print({'package': args.package, 'track': args.track, 'versionCode': version_code, 'edit': result.get('id')})
except Exception:
    try: service.edits().delete(packageName=args.package, editId=edit_id).execute()
    except Exception: pass
    raise
