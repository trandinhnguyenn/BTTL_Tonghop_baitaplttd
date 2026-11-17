"""REST API thực hành. Python 3.9+, chỉ dùng thư viện chuẩn, lưu JSON trên đĩa."""
import argparse
import json
import math
import re
import threading
import uuid
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import urlparse

DEFAULT_FILE = Path(__file__).with_name('students.json')
SAMPLE = [
    {'id': '1', 'full_name': 'Trần Đình Nguyên', 'student_code': '2415053122225',
     'email': 'nguyen.demo@example.com', 'gpa': 3.2},
    {'id': '2', 'full_name': 'Nguyễn Minh Anh', 'student_code': '2415053000002',
     'email': 'minhanh.demo@example.com', 'gpa': 3.6},
    {'id': '3', 'full_name': 'Lê Quốc Bảo', 'student_code': '2415053000003',
     'email': 'quocbao.demo@example.com', 'gpa': 2.8}
]


def validate(data):
    if not isinstance(data, dict):
        raise ValueError('JSON phải là một object.')
    cleaned = {}
    for key in ('full_name', 'student_code', 'email'):
        value = data.get(key)
        if not isinstance(value, str) or not value.strip():
            raise ValueError(f'Thiếu hoặc sai trường {key}.')
        cleaned[key] = value.strip()
    if not re.fullmatch(r'[A-Za-z0-9_-]+', cleaned['student_code']):
        raise ValueError('MSSV không hợp lệ.')
    if not re.fullmatch(r'[^\s@]+@[^\s@]+\.[^\s@]+', cleaned['email']):
        raise ValueError('Email không hợp lệ.')
    gpa = data.get('gpa')
    if isinstance(gpa, bool) or not isinstance(gpa, (int, float)) or not math.isfinite(gpa) or not 0 <= gpa <= 4:
        raise ValueError('GPA phải từ 0 đến 4.')
    cleaned['gpa'] = float(gpa)
    return cleaned


class Store:
    def __init__(self, path):
        self.path = Path(path)
        self.lock = threading.Lock()
        if not self.path.exists():
            self.write(SAMPLE)
        # Không âm thầm ghi đè nếu file JSON đang bị lỗi.
        self.read()

    def read(self):
        rows = json.loads(self.path.read_text(encoding='utf-8'))
        if not isinstance(rows, list):
            raise ValueError('students.json phải chứa danh sách.')
        for row in rows:
            validate(row)
        return rows

    def write(self, rows):
        self.path.parent.mkdir(parents=True, exist_ok=True)
        temporary = self.path.with_suffix('.tmp')
        temporary.write_text(json.dumps(rows, ensure_ascii=False, indent=2, allow_nan=False), encoding='utf-8')
        temporary.replace(self.path)


def make_server(host='127.0.0.1', port=8000, data_file=DEFAULT_FILE):
    store = Store(data_file)

    class Handler(BaseHTTPRequestHandler):
        def reply(self, status, body):
            raw = json.dumps(body, ensure_ascii=False, allow_nan=False).encode('utf-8')
            self.send_response(status)
            self.send_header('Content-Type', 'application/json; charset=utf-8')
            self.send_header('Content-Length', str(len(raw)))
            self.end_headers()
            self.wfile.write(raw)

        def do_GET(self):
            route = urlparse(self.path).path.rstrip('/')
            if route == '/api/v1/students':
                try:
                    with store.lock:
                        rows = store.read()
                    self.reply(200, rows)
                except (OSError, ValueError):
                    self.reply(500, {'error': 'Không đọc được dữ liệu máy chủ.'})
            elif route in ('', '/health'):
                self.reply(200, {'status': 'ok', 'author': 'Trần Đình Nguyên', 'student_code': '2415053122225'})
            else:
                self.reply(404, {'error': 'Không tìm thấy endpoint.'})

        def do_POST(self):
            if urlparse(self.path).path.rstrip('/') != '/api/v1/students':
                self.reply(404, {'error': 'Không tìm thấy endpoint.'})
                return
            try:
                length = int(self.headers.get('Content-Length', '0'))
                if length <= 0 or length > 65536:
                    raise ValueError('Kích thước body không hợp lệ.')
                incoming = validate(json.loads(self.rfile.read(length).decode('utf-8')))
            except (ValueError, UnicodeDecodeError) as error:
                self.reply(400, {'error': str(error)})
                return
            try:
                with store.lock:
                    rows = store.read()
                    old = next((row for row in rows if row['student_code'] == incoming['student_code']), None)
                    if old:
                        incoming['id'] = old['id']
                        rows[rows.index(old)] = incoming
                        status = 200
                    else:
                        incoming['id'] = str(uuid.uuid4())
                        rows.append(incoming)
                        status = 201
                    store.write(rows)
                self.reply(status, incoming)
            except (OSError, ValueError):
                self.reply(500, {'error': 'Không lưu được dữ liệu máy chủ.'})

    return ThreadingHTTPServer((host, port), Handler)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='API mẫu Room + Retrofit, Trần Đình Nguyên')
    parser.add_argument('--host', default='127.0.0.1')
    parser.add_argument('--port', type=int, default=8000)
    parser.add_argument('--data', type=Path, default=DEFAULT_FILE)
    args = parser.parse_args()
    httpd = make_server(args.host, args.port, args.data)
    print(f'API: http://{args.host}:{args.port}/api/v1/students', flush=True)
    print('Dừng server bằng Ctrl+C. Chỉ dùng cho bài thực hành trong mạng nội bộ.', flush=True)
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        pass
    finally:
        httpd.server_close()
