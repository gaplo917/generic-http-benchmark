use std::{net::SocketAddr, time::Duration};

use axum::{extract::Path, routing::get, Json, Router};
use serde::Serialize;

#[derive(Serialize)]
struct DummyResponse {
    name: Option<String>,
    is_active: Option<bool>,
    is_virtual: Option<bool>,
    thread_group_active_count: Option<i32>,
    thread_group_count: Option<i32>,
}

impl Default for DummyResponse {
    fn default() -> Self {
        Self {
            name: None,
            is_active: None,
            is_virtual: None,
            thread_group_active_count: None,
            thread_group_count: None,
        }
    }
}

async fn non_blocking_io(io_delay: u64) -> DummyResponse {
    tokio::time::sleep(Duration::from_millis(io_delay)).await;
    DummyResponse::default()
}

async fn dependent_non_blocking_io(io_delay: u64, resp: DummyResponse) -> Vec<DummyResponse> {
    tokio::time::sleep(Duration::from_millis(io_delay)).await;
    vec![resp, DummyResponse::default()]
}

async fn handler(Path(io_delay): Path<u64>) -> Json<Vec<DummyResponse>> {
    let resp = non_blocking_io(io_delay).await;
    let resp = dependent_non_blocking_io(io_delay, resp).await;
    Json(resp)
}

#[tokio::main]
async fn main() {
    let app = Router::new().route("/axum-nio/:io_delay", get(handler));

    let addr = SocketAddr::from(([0, 0, 0, 0], 8080));

    axum::Server::bind(&addr)
        .serve(app.into_make_service())
        .await
        .unwrap();
}
