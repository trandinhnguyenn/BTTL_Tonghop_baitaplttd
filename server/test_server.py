"""Chạy: python -m unittest discover -s server -v (từ thư mục gốc dự án)."""
import importlib.util
import json
from pathlib import Path
import tempfile
import threading
import unittest
from urllib.request import Request, urlopen
from urllib.error import HTTPError

spec = importlib.util.spec_from_file_location('student_server', Path(__file__).with_name('server.py'))
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


class ApiTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.server = module.make_server(port=0, data_file=Path(self.tmp.name) / 'students.json')
        self.worker = threading.Thread(target=self.server.serve_forever, daemon=True)
        self.worker.start()
        self.url = f'http://127.0.0.1:{self.server.server_port}/api/v1/students'

    def tearDown(self):
        self.server.shutdown()
        self.server.server_close()
        self.worker.join()
        self.tmp.cleanup()

    def request(self, body=None):
        req = Request(self.url, data=None if body is None else json.dumps(body).encode(),
                      headers={'Content-Type': 'application/json'})
        with urlopen(req, timeout=3) as response:
            return response.status, json.load(response)

    def test_seed_and_post_upsert(self):
        status, rows = self.request()
        self.assertEqual(status, 200)
        self.assertEqual(rows[0]['student_code'], '2415053122225')
        student = {'full_name': 'Sinh viên kiểm thử', 'student_code': 'TEST01', 'email': 'test@example.com', 'gpa': 3.0}
        status, created = self.request(student)
        self.assertEqual(status, 201)
        student['gpa'] = 3.9
        status, updated = self.request(student)
        self.assertEqual(status, 200)
        self.assertEqual(updated['id'], created['id'])
        self.assertEqual(len(self.request()[1]), 4)
        self.assertEqual(updated['gpa'], 3.9)
        # Store mới đọc lại được dữ liệu trên đĩa.
        persisted = module.Store(Path(self.tmp.name) / 'students.json').read()
        self.assertEqual(persisted[-1]['gpa'], 3.9)

    def test_bad_input_does_not_mutate_store(self):
        with self.assertRaises(HTTPError) as error:
            self.request({'full_name': 'A', 'student_code': 'X', 'email': 'bad', 'gpa': 8})
        self.assertEqual(error.exception.code, 400)
        self.assertEqual(len(self.request()[1]), 3)


if __name__ == '__main__':
    unittest.main()
