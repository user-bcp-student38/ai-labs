import time
from pathlib import Path

import jwt
from fastapi.testclient import TestClient

from app.main import AUTH_JWT_SECRET, TOKEN_ISSUER, create_app


def token(username: str = "student", role: str = "STUDENT", expires_in: int = 3600, secret: str = AUTH_JWT_SECRET) -> str:
    payload = {"sub": username, "role": role, "iss": TOKEN_ISSUER, "exp": int(time.time()) + expires_in}
    return jwt.encode(payload, secret, algorithm="HS256")


def headers(username: str = "student", role: str = "STUDENT") -> dict[str, str]:
    return {"Authorization": f"Bearer {token(username, role)}"}


def test_lists_seeded_tasks(tmp_path: Path) -> None:
    app = create_app(str(tmp_path / "workshop.db"), str(tmp_path / "uploads"))
    with TestClient(app) as client:
        response = client.get("/api/tasks", headers=headers())
    assert response.status_code == 200
    assert len(response.json()) == 2


def test_creates_task_and_summary(tmp_path: Path) -> None:
    app = create_app(str(tmp_path / "workshop.db"), str(tmp_path / "uploads"))
    with TestClient(app) as client:
        created = client.post(
            "/api/tasks",
            headers=headers(),
            json={"title": "Add tests", "description": "Improve coverage", "status": "TODO", "priority": "HIGH"},
        )
        summary = client.get("/api/summary", headers=headers())
    assert created.status_code == 201
    assert created.json()["owner"] == "student"
    assert summary.json()["total"] == 3


def test_uploads_requirement(tmp_path: Path) -> None:
    upload_dir = tmp_path / "uploads"
    app = create_app(str(tmp_path / "workshop.db"), str(upload_dir))
    with TestClient(app) as client:
        response = client.post(
            "/api/requirements",
            headers=headers(),
            files={"file": ("requirements.txt", b"Login must be accessible", "text/plain")},
        )
    assert response.status_code == 201
    assert (upload_dir / "requirements.txt").exists()


def test_rejects_invalid_token(tmp_path: Path) -> None:
    app = create_app(str(tmp_path / "workshop.db"), str(tmp_path / "uploads"))
    with TestClient(app) as client:
        response = client.get("/api/tasks", headers={"Authorization": "Bearer invalid"})
    assert response.status_code == 401


def test_rejects_expired_token(tmp_path: Path) -> None:
    app = create_app(str(tmp_path / "workshop.db"), str(tmp_path / "uploads"))
    expired = {"Authorization": f"Bearer {token(expires_in=-10)}"}
    with TestClient(app) as client:
        response = client.get("/api/tasks", headers=expired)
    assert response.status_code == 401


def test_rejects_token_with_wrong_signature(tmp_path: Path) -> None:
    app = create_app(str(tmp_path / "workshop.db"), str(tmp_path / "uploads"))
    tampered = {"Authorization": f"Bearer {token(secret='not-the-shared-secret')}"}
    with TestClient(app) as client:
        response = client.get("/api/tasks", headers=tampered)
    assert response.status_code == 401
