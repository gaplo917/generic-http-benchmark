use std::{convert::Infallible, time::Duration};

use serde::Serialize;
use warp::{http::HeaderValue, hyper::header::CONTENT_TYPE, reply::Response, Filter};

#[derive(Serialize)]
struct DummyResponse {
    name: Option<String>,
    is_active: Option<bool>,
    is_virtual: Option<bool>,
    thread_group_active_count: Option<i32>,
    thread_group_count: Option<i32>,
}

struct DummyResponseBox(Vec<DummyResponse>);

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

impl warp::Reply for DummyResponseBox {
    fn into_response(self) -> Response {
        let json = serde_json::to_string(&self.0).unwrap();
        let mut res = Response::new(json.into());
        res.headers_mut()
            .insert(CONTENT_TYPE, HeaderValue::from_static("application/json"));
        res
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

async fn handler(io_delay: u64) -> Result<DummyResponseBox, Infallible> {
    let resp = non_blocking_io(io_delay).await;
    let resp = dependent_non_blocking_io(io_delay, resp).await;
    Ok(DummyResponseBox(resp))
}

#[tokio::main]
async fn main() {
    let server = warp::get()
        .and(warp::path!("warp-nio" / u64))
        .and_then(handler);

    warp::serve(server).run(([0, 0, 0, 0], 8080)).await;
}
