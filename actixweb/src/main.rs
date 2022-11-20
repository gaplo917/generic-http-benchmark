use std::time::Duration;

use actix_web::{
    body::BoxBody, get, http::header::ContentType, web, App, HttpResponse, HttpServer, Responder,
};
use serde::Serialize;

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

impl Responder for DummyResponseBox {
    type Body = BoxBody;

    fn respond_to(self, _req: &actix_web::HttpRequest) -> actix_web::HttpResponse<Self::Body> {
        let body = serde_json::to_string(&self.0).unwrap();

        HttpResponse::Ok()
            .content_type(ContentType::json())
            .body(body)
    }
}

async fn non_blocking_io(io_delay: u64) -> DummyResponse {
    actix_web::rt::time::sleep(Duration::from_millis(io_delay)).await;
    DummyResponse::default()
}

async fn dependent_non_blocking_io(io_delay: u64, resp: DummyResponse) -> Vec<DummyResponse> {
    actix_web::rt::time::sleep(Duration::from_millis(io_delay)).await;
    vec![resp, DummyResponse::default()]
}

#[get("/actixweb-nio/{io_delay}")]
async fn handler(path: web::Path<u64>) -> DummyResponseBox {
    let io_delay = path.into_inner();
    let resp = non_blocking_io(io_delay).await;
    let resp = dependent_non_blocking_io(io_delay, resp).await;
    DummyResponseBox(resp)
}

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    HttpServer::new(|| App::new().service(handler))
        .bind(("0.0.0.0", 8080))?
        .run()
        .await
}
